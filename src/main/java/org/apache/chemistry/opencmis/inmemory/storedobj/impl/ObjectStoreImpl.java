/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.chemistry.opencmis.inmemory.storedobj.impl;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Document;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Relationship;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * The object store is the central core of the in-memory repository. It is based
 * on huge HashMap map mapping ids to objects in memory. To allow access from
 * multiple threads a Java concurrent HashMap is used that allows parallel
 * access methods.
 * <p>
 * Certain methods in the in-memory repository must guarantee constraints. For
 * example a folder enforces that each child has a unique name. Therefore
 * certain operations must occur in an atomic manner. In the example it must be
 * guaranteed that no write access occurs to the map between acquiring the
 * iterator to find the children and finishing the add operation when no name
 * conflicts can occur. For this purpose this class has methods to lock an
 * unlock the state of the repository. It is very important that the caller
 * acquiring the lock enforces an unlock under all circumstances. Typical code
 * is:
 * <p>
 * 
 * <pre>
 * ObjectStoreImpl os = ... ;
 * try {
 *     os.lock();
 * } finally {
 *     os.unlock();
 * }
 * </pre>
 * 
 * The locking is very coarse-grained. Productive implementations would probably
 * implement finer grained locks on a folder or document rather than the
 * complete repository.
 */
public class ObjectStoreImpl implements ObjectStore {

	/**
	 * user id for administrator always having all rights
	 */
	public static final String ADMIN_PRINCIPAL_ID = "Admin";

	/**
	 * Simple id generator that uses just an integer
	 */
	private static int NEXT_UNUSED_ID = 100;

	/**
	 * a concurrent HashMap as core element to hold all objects in the
	 * repository
	 */
	// private final Map<String, StoredObject> fStoredObjectMap = new
	// ConcurrentHashMap<String, StoredObject>();

	/**
	 * a concurrent HashMap to hold all Acls in the repository
	 */
	private int nextUnusedAclId = 0;

	// private final List<InMemoryAcl> fAcls = new ArrayList<InMemoryAcl>();

	private final Lock fLock = new ReentrantLock();

	GridFS fs;
	MongoMap objects;
	DBCollection acls;
	final String fRepositoryId;
	FolderImpl fRootFolder = null;

	public ObjectStoreImpl(String repositoryId) {
		fRepositoryId = repositoryId;

		try {
			MongoClient mongoClient = new MongoClient("localhost");
			DB db = mongoClient.getDB(fRepositoryId);
			fs = new GridFS(db);
			objects = new MongoMap(db.getCollection("objects"), this);
			objects.clear();
			acls = db.getCollection("acls");
			acls.setObjectClass(InMemoryAcl.class);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		createRootFolder();
	}

	private static synchronized Integer getNextId() {
		return NEXT_UNUSED_ID++;
	}

	private synchronized Integer getNextAclId() {
		return nextUnusedAclId++;
	}

	public void lock() {
		fLock.lock();
	}

	public void unlock() {
		fLock.unlock();
	}

	public Folder getRootFolder() {
		return fRootFolder;
	}

	public StoredObject getObjectByPath(String path, String user) {
		for (StoredObject so : objects.values()) {
			if (so instanceof SingleFiling) {
				String soPath = ((SingleFiling) so).getPath();
				if (soPath.equals(path)) {
					return so;
				}
			} else if (so instanceof MultiFiling) {
				MultiFiling mfo = (MultiFiling) so;
				List<Folder> parents = mfo.getParents(user);
				for (Folder parent : parents) {
					String parentPath = parent.getPath();
					String mfPath = parentPath.equals(Folder.PATH_SEPARATOR) ? parentPath + mfo.getPathSegment() : parentPath + Folder.PATH_SEPARATOR + mfo.getPathSegment();
					if (mfPath.equals(path)) {
						return so;
					}
				}
			}
		}
		return null;
	}

	public StoredObject getObjectById(String objectId) {
		return objects.get(objectId);
	}

	public FolderImpl getFolderById(String objectId) {
		StoredObject so = objects.get(objectId);
		if (so instanceof FolderImpl) {
			return (FolderImpl) so;
		}

		return null;
	}

	public void deleteObject(String objectId, Boolean allVersions, String user) {
		StoredObject obj = getObjectById(objectId);

		if (null == obj) {
			throw new RuntimeException("Cannot delete object with id  " + objectId + ". Object does not exist.");
		}

		if (obj instanceof FolderImpl) {
			deleteFolder(objectId, user);
		} else if (obj instanceof DocumentVersion) {
			DocumentVersion vers = (DocumentVersion) obj;
			VersionedDocument parentDoc = vers.getParentDocument();
			boolean otherVersionsExists;
			if (allVersions != null && allVersions) {
				otherVersionsExists = false;
				List<DocumentVersion> allVers = parentDoc.getAllVersions();
				for (DocumentVersion ver : allVers) {
					objects.remove(ver.getId());
				}
			} else {
				objects.remove(objectId);
				otherVersionsExists = parentDoc.deleteVersion(vers);
			}

			if (!otherVersionsExists) {
				objects.remove(parentDoc.getId());
			}
		} else {
			objects.remove(objectId);
		}
	}

	public void removeVersion(DocumentVersion vers) {
		DBObject found = objects.findOne(vers.getId());
		if (null == found) {
			throw new CmisInvalidArgumentException("Cannot delete object with id  " + vers.getId() + ". Object does not exist.");
		}

		objects.remove(vers.getId());
	}

	public String storeObject(StoredObject so) {
		String id = so.getId();
		// check if update or create
		if (null == id) {
			id = getNextId().toString();
			so.put("_id", id);
		}

		so.put("className", so.getClass().getName());
		objects.save(so);

		return id;
	}

	StoredObject getObject(String id) {
		return this.getObjectById(id);
	}

	void removeObject(String id) {
		objects.remove(id);
	}

	public Set<String> getIds() {
		Set<String> entries = objects.keySet();
		return entries;
	}

	/**
	 * Clear repository and remove all data.
	 */
	public void clear() {
		lock();
		objects.clear();
		storeObject(fRootFolder);
		unlock();
	}

	public long getObjectCount() {
		return objects.size();
	}

	// /////////////////////////////////////////
	// private helper methods

	private void createRootFolder() {
		FolderImpl rootFolder = new FolderImpl(this);
		rootFolder.setName("RootFolder");
		rootFolder.setParentId(null);
		rootFolder.setTypeId(BaseTypeId.CMIS_FOLDER.value());
		rootFolder.setCreatedBy("Admin");
		rootFolder.setModifiedBy("Admin");
		rootFolder.setModifiedAtNow();
		rootFolder.setRepositoryId(fRepositoryId);
		rootFolder.setAclId(addAcl(InMemoryAcl.getDefaultAcl()));
		rootFolder.persist();
		fRootFolder = rootFolder;
	}

	public Document createDocument(String name, Map<String, PropertyData<?>> propMap, String user, Folder folder, List<String> policies, Acl addACEs, Acl removeACEs) {
		DocumentImpl doc = new DocumentImpl(this);
		doc.createSystemBasePropertiesWhenCreated(propMap, user);
		doc.setCustomProperties(propMap);
		doc.setRepositoryId(fRepositoryId);
		doc.setName(name);
		if (null != folder) {
			((FolderImpl) folder).addChildDocument(doc); // add document to
															// folder and
		}
		int aclId = getAclId(((FolderImpl) folder), addACEs, removeACEs);
		doc.setAclId(aclId);
		if (null != policies)
			doc.setAppliedPolicies(policies);
		return doc;
	}

	public StoredObject createItem(String name, Map<String, PropertyData<?>> propMap, String user, Folder folder, List<String> policies, Acl addACEs, Acl removeACEs) {
		StoredObjectImpl item = new ItemImpl(this);
		item.createSystemBasePropertiesWhenCreated(propMap, user);
		item.setCustomProperties(propMap);
		item.setRepositoryId(fRepositoryId);
		item.setName(name);
		if (null != folder) {
			((FolderImpl) folder).addChildItem(item); // add document to folder
														// and
		}
		if (null != policies)
			item.setAppliedPolicies(policies);
		int aclId = getAclId(((FolderImpl) folder), addACEs, removeACEs);
		item.setAclId(aclId);
		return item;
	}

	public DocumentVersion createVersionedDocument(String name, Map<String, PropertyData<?>> propMap, String user, Folder folder, List<String> policies, Acl addACEs, Acl removeACEs, ContentStream contentStream, VersioningState versioningState) {
		VersionedDocumentImpl doc = new VersionedDocumentImpl(this);
		doc.createSystemBasePropertiesWhenCreated(propMap, user);
		doc.setCustomProperties(propMap);
		doc.setRepositoryId(fRepositoryId);
		doc.setName(name);
		DocumentVersion version = doc.addVersion(contentStream, versioningState, user);
		if (null != folder) {
			((FolderImpl) folder).addChildDocument(doc); // add document to
															// folder and set
		}
		version.createSystemBasePropertiesWhenCreated(propMap, user);
		version.setCustomProperties(propMap);
		int aclId = getAclId(((FolderImpl) folder), addACEs, removeACEs);
		doc.setAclId(aclId);
		if (null != policies)
			doc.setAppliedPolicies(policies);
		doc.persist();
		return version;
	}

	public Folder createFolder(String name, Map<String, PropertyData<?>> propMap, String user, Folder parent, List<String> policies, Acl addACEs, Acl removeACEs) {

		FolderImpl folder = new FolderImpl(this, name, null);
		if (null != propMap) {
			folder.createSystemBasePropertiesWhenCreated(propMap, user);
			folder.setCustomProperties(propMap);
		}
		folder.setRepositoryId(fRepositoryId);
		if (null != parent) {
			((FolderImpl) parent).addChildFolder(folder); // add document to
															// folder and set
		}

		int aclId = getAclId(((FolderImpl) parent), addACEs, removeACEs);
		folder.setAclId(aclId);
		if (null != policies)
			folder.setAppliedPolicies(policies);

		return folder;
	}

	public Folder createFolder(String name) {
		Folder folder = new FolderImpl(this, name, null);
		folder.setRepositoryId(fRepositoryId);
		return folder;
	}

	public StoredObject createPolicy(String name, String policyText, Map<String, PropertyData<?>> propMap, String user) {
		PolicyImpl policy = new PolicyImpl(this);
		policy.createSystemBasePropertiesWhenCreated(propMap, user);
		policy.setCustomProperties(propMap);
		policy.setRepositoryId(fRepositoryId);
		policy.setName(name);
		policy.setPolicyText(policyText);
		policy.persist();
		return policy;
	}

	public List<StoredObject> getCheckedOutDocuments(String orderBy, String user, IncludeRelationships includeRelationships) {
		List<StoredObject> res = new ArrayList<StoredObject>();

		for (StoredObject so : objects.values()) {
			if (so instanceof VersionedDocument) {
				VersionedDocument verDoc = (VersionedDocument) so;
				if (verDoc.isCheckedOut() && hasReadAccess(user, verDoc)) {
					res.add(verDoc.getPwc());
				}
			}
		}

		return res;
	}

	public StoredObject createRelationship(String name, StoredObject sourceObject, StoredObject targetObject, Map<String, PropertyData<?>> propMap, String user, Acl addACEs, Acl removeACEs) {

		RelationshipImpl rel = new RelationshipImpl(this);
		rel.createSystemBasePropertiesWhenCreated(propMap, user);
		rel.setCustomProperties(propMap);
		rel.setRepositoryId(fRepositoryId);
		rel.setName(name);
		if (null != sourceObject)
			rel.setSource(sourceObject.getId());
		if (null != targetObject)
			rel.setTarget(targetObject.getId());
		int aclId = getAclId(null, addACEs, removeACEs);
		rel.setAclId(aclId);
		rel.persist();
		return rel;
	}

	public List<StoredObject> getRelationships(String objectId, List<String> typeIds, RelationshipDirection direction) {

		List<StoredObject> res = new ArrayList<StoredObject>();

		if (typeIds != null && typeIds.size() > 0) {
			for (String typeId : typeIds) {
				for (StoredObject so : objects.values()) {
					if (so instanceof Relationship && so.getTypeId().equals(typeId)) {
						Relationship ro = (Relationship) so;
						if (ro.getSourceObjectId().equals(objectId) && (RelationshipDirection.EITHER == direction || RelationshipDirection.SOURCE == direction)) {
							res.add(so);
						} else if (ro.getTargetObjectId().equals(objectId) && (RelationshipDirection.EITHER == direction || RelationshipDirection.TARGET == direction)) {
							res.add(so);
						}
					}
				}
			}
		} else
			res = getAllRelationships(objectId, direction);
		return res;
	}

	public Acl applyAcl(StoredObject so, Acl addAces, Acl removeAces, AclPropagation aclPropagation, String principalId) {
		if (aclPropagation == AclPropagation.OBJECTONLY || !(so instanceof Folder)) {
			return applyAcl(so, addAces, removeAces);
		} else {
			return applyAclRecursive(((Folder) so), addAces, removeAces, principalId);
		}
	}

	public Acl applyAcl(StoredObject so, Acl acl, AclPropagation aclPropagation, String principalId) {
		if (aclPropagation == AclPropagation.OBJECTONLY || !(so instanceof Folder)) {
			return applyAcl(so, acl);
		} else {
			return applyAclRecursive(((Folder) so), acl, principalId);
		}
	}

	public List<Integer> getAllAclsForUser(String principalId, Permission permission) {
		DBCursor cursor = acls.find();
		try {
			List<Integer> acls = new ArrayList<Integer>();
			for (DBObject dbObject : cursor.toArray()) {
				InMemoryAcl acl = new InMemoryAcl();
				acl.putAll(dbObject);
				if (acl.hasPermission(principalId, permission))
					acls.add(acl.getId());
			}
			return acls;
		} finally {
			cursor.close();
		}

	}

	public Acl getAcl(int aclId) {
		InMemoryAcl acl = getInMemoryAcl(aclId);
		return acl == null ? InMemoryAcl.getDefaultAcl().toCommonsAcl() : acl.toCommonsAcl();
	}

	public int getAclId(StoredObjectImpl so, Acl addACEs, Acl removeACEs) {
		InMemoryAcl newAcl;
		boolean removeDefaultAcl = false;
		int aclId = 0;

		if (so == null) {
			newAcl = new InMemoryAcl();
		} else {
			aclId = so.getAclId();
			newAcl = getInMemoryAcl(aclId);
			if (null == newAcl)
				newAcl = new InMemoryAcl();
			else
				// copy list so that we can safely change it without effecting
				// the original
				newAcl = new InMemoryAcl(newAcl.getAces());
		}

		if (newAcl.size() == 0 && addACEs == null && removeACEs == null)
			return 0;

		if (null != removeACEs)
			for (Ace ace : removeACEs.getAces()) {
				InMemoryAce inMemAce = new InMemoryAce(ace);
				if (inMemAce.equals(InMemoryAce.getDefaultAce()))
					removeDefaultAcl = true;
			}

		if (so != null && 0 == aclId && !removeDefaultAcl)
			return 0; // if object grants full access to everyone and it will
						// not be removed we do nothing

		// add ACEs
		if (null != addACEs)
			for (Ace ace : addACEs.getAces()) {
				InMemoryAce inMemAce = new InMemoryAce(ace);
				if (inMemAce.equals(InMemoryAce.getDefaultAce()))
					return 0; // if everyone has full access there is no need to
								// add additional ACLs.
				newAcl.addAce(inMemAce);
			}

		// remove ACEs
		if (null != removeACEs)
			for (Ace ace : removeACEs.getAces()) {
				InMemoryAce inMemAce = new InMemoryAce(ace);
				newAcl.removeAce(inMemAce);
			}

		if (newAcl.size() > 0)
			return addAcl(newAcl);
		else
			return 0;
	}

	private void deleteFolder(String folderId, String user) {
		StoredObject folder = objects.get(folderId);
		if (folder == null) {
			throw new CmisInvalidArgumentException("Unknown object with id:  " + folderId);
		}

		if (!(folder instanceof FolderImpl)) {
			throw new CmisInvalidArgumentException("Cannot delete folder with id:  " + folderId + ". Object exists but is not a folder.");
		}

		// check if children exist
		List<? extends StoredObject> children = ((Folder) folder).getChildren(-1, -1, user).getChildren();
		if (children != null && !children.isEmpty()) {
			throw new CmisConstraintException("Cannot delete folder with id:  " + folderId + ". Folder is not empty.");
		}

		objects.remove(folderId);
	}

	public boolean hasReadAccess(String principalId, StoredObject so) {
		return hasAccess(principalId, so, Permission.READ);
	}

	public boolean hasWriteAccess(String principalId, StoredObject so) {
		return hasAccess(principalId, so, Permission.WRITE);
	}

	public boolean hasAllAccess(String principalId, StoredObject so) {
		return hasAccess(principalId, so, Permission.ALL);
	}

	public void checkReadAccess(String principalId, StoredObject so) {
		checkAccess(principalId, so, Permission.READ);
	}

	public void checkWriteAccess(String principalId, StoredObject so) {
		checkAccess(principalId, so, Permission.WRITE);
	}

	public void checkAllAccess(String principalId, StoredObject so) {
		checkAccess(principalId, so, Permission.ALL);
	}

	private void checkAccess(String principalId, StoredObject so, Permission permission) {
		if (!hasAccess(principalId, so, permission))
			throw new CmisPermissionDeniedException("Object with id " + so.getId() + " and name " + so.getName() + " does not grant " + permission.toString() + " access to principal " + principalId);
	}

	private boolean hasAccess(String principalId, StoredObject so, Permission permission) {
		if (null != principalId && principalId.equals(ADMIN_PRINCIPAL_ID))
			return true;
		List<Integer> aclIds = getAllAclsForUser(principalId, permission);
		return aclIds.contains(((StoredObjectImpl) so).getAclId());
	}

	private InMemoryAcl getInMemoryAcl(int aclId) {
		DBObject dbObject = acls.findOne(new BasicDBObject("_id", aclId));
		if (dbObject == null)
			return null;
		InMemoryAcl acl = new InMemoryAcl();
		acl.putAll(dbObject);
		return acl;
	}

	private int setAcl(StoredObjectImpl so, Acl acl) {
		int aclId;
		if (null == acl || acl.getAces().isEmpty())
			aclId = 0;
		else {
			aclId = getAclId(null, acl, null);
		}
		so.setAclId(aclId);
		return aclId;
	}

	/**
	 * check if an Acl is already known
	 * 
	 * @param acl
	 *            acl to be checked
	 * @return 0 if Acl is not known, id of Acl otherwise
	 */
	private int hasAcl(InMemoryAcl acl) {

		DBCursor cursor = acls.find();
		try {
			for (DBObject dbObject : cursor) {
				InMemoryAcl acl2 = new InMemoryAcl();
				acl2.putAll(dbObject);
				if (acl2.equals(acl))
					return acl2.getId();
			}
		} finally {
			cursor.close();
		}
		return -1;
	}

	private int addAcl(InMemoryAcl acl) {
		int aclId = -1;

		if (null == acl)
			return 0;

		lock();
		try {
			aclId = hasAcl(acl);
			if (aclId < 0) {
				aclId = getNextAclId();
				acl.setId(aclId);
				acls.save(acl);
				// acls.add(acl);
			}
		} finally {
			unlock();
		}
		return aclId;
	}

	private Acl applyAcl(StoredObject so, Acl acl) {
		int aclId = setAcl((StoredObjectImpl) so, acl);
		return getAcl(aclId);
	}

	private Acl applyAcl(StoredObject so, Acl addAces, Acl removeAces) {
		int aclId = getAclId((StoredObjectImpl) so, addAces, removeAces);
		((StoredObjectImpl) so).setAclId(aclId);
		return getAcl(aclId);
	}

	private Acl applyAclRecursive(Folder folder, Acl addAces, Acl removeAces, String principalId) {
		List<? extends StoredObject> children = folder.getChildren(-1, -1, ADMIN_PRINCIPAL_ID).getChildren();

		Acl result = applyAcl(folder, addAces, removeAces);

		if (null == children) {
			return result;
		}

		for (StoredObject child : children) {
			if (hasAllAccess(principalId, child)) {
				if (child instanceof Folder) {
					applyAclRecursive((Folder) child, addAces, removeAces, principalId);
				} else {
					applyAcl(child, addAces, removeAces);
				}
			}
		}

		return result;
	}

	private Acl applyAclRecursive(Folder folder, Acl acl, String principalId) {
		List<? extends StoredObject> children = folder.getChildren(-1, -1, ADMIN_PRINCIPAL_ID).getChildren();

		Acl result = applyAcl(folder, acl);

		if (null == children) {
			return result;
		}

		for (StoredObject child : children) {
			if (hasAllAccess(principalId, child)) {
				if (child instanceof Folder) {
					applyAclRecursive((Folder) child, acl, principalId);
				} else {
					applyAcl(child, acl);
				}
			}
		}

		return result;
	}

	private List<StoredObject> getAllRelationships(String objectId, RelationshipDirection direction) {

		List<StoredObject> res = new ArrayList<StoredObject>();

		for (StoredObject so : objects.values()) {
			if (so instanceof Relationship) {
				Relationship ro = (Relationship) so;
				if (ro.getSourceObjectId().equals(objectId) && (RelationshipDirection.EITHER == direction || RelationshipDirection.SOURCE == direction)) {
					res.add(so);
				} else if (ro.getTargetObjectId().equals(objectId) && (RelationshipDirection.EITHER == direction || RelationshipDirection.TARGET == direction)) {
					res.add(so);
				}
			}
		}
		return res;
	}

	public boolean isTypeInUse(String typeId) {
		// iterate over all the objects and check for each if the type matches
		for (String objectId : getIds()) {
			StoredObject so = getObjectById(objectId);
			if (so.getTypeId().equals(typeId))
				return true;
		}
		return false;
	}

	public String storeContentStream(ContentStream cs) {
		String id = UUID.randomUUID().toString();
		GridFSInputFile file = fs.createFile(cs.getStream());
		file.setId(id);
//		file.setChunkSize(cs.getLength());
		file.setContentType(cs.getMimeType());
		file.setFilename(cs.getFileName());
		file.save();
		return id;
	}

	public ContentStream getContentStream(String contentStreamId)  {

		GridFSDBFile file = fs.findOne(new BasicDBObject("_id", contentStreamId));
		ContentStreamDataImpl cs=new ContentStreamDataImpl(file.getLength());
		try {
			cs.setContent(file.getInputStream());
			cs.setFileName(file.getFilename());
			GregorianCalendar calendar=new GregorianCalendar();
			calendar.setTime(file.getUploadDate());
			cs.setLastModified(calendar);
			cs.setMimeType(file.getContentType());
		} catch (IOException e) {
			throw new RuntimeException("Failed to get content from InputStream", e);
		}
		
		return cs;
	}

}
