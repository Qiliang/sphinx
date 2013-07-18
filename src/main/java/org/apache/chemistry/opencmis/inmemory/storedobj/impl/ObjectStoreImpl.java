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
import java.util.UUID;
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
import org.apache.chemistry.opencmis.inmemory.ConfigConstants;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Document;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.bson.types.ObjectId;

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
			MongoClient mongoClient = new MongoClient(ConfigConstants.DB_HOST);
			DB db = mongoClient.getDB(fRepositoryId);
			fs = new GridFS(db);
			objects = new MongoMap(db.getCollection("objects"), this);
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

	public void lock() {
		// fLock.lock();
	}

	public void unlock() {
		// fLock.unlock();
	}

	public Folder getRootFolder() {
		return fRootFolder;
	}

	private String[] getPathSegments(String path) {
		String[] pathSegments = path.split(Folder.PATH_SEPARATOR);
		if (pathSegments.length == 0)
			return pathSegments;
		return (String[]) org.apache.commons.lang.ArrayUtils.remove(pathSegments, 0);
	}

	private StoredObject getObjectByName(String name, String parentId, String user) {
		List<ObjectId> aclIds = this.getAllAclsForUser(user, Permission.READ);
		BasicDBObject where = new BasicDBObject();
		where.put("parentIds", new BasicDBObject("$in", new String[] { parentId }));
		where.put("name", name);
		where.put("aclId", new BasicDBObject("$in", aclIds));
		DBObject dbObject = objects.dbCollection.findOne(where);
		return objects.convert(dbObject);
	}

	public StoredObject getObjectByPath(String path, String user) {
		String[] pathSegments = getPathSegments(path);
		StoredObject object = getRootFolder();
		for (String segment : pathSegments) {
			object = getObjectByName(segment, object.getId(), user);
			if (object == null)
				return null;
		}

		return object;
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

	private String getBasicTypeId(StoredObject so) {
		if (so instanceof RelationshipImpl) {
			return BaseTypeId.CMIS_RELATIONSHIP.value();
		} else if (so instanceof PolicyImpl) {
			return BaseTypeId.CMIS_POLICY.value();
		} else if (so instanceof DocumentVersionImpl) {
			return "DocumentVersion";
		} else if (so instanceof DocumentImpl) {
			return BaseTypeId.CMIS_DOCUMENT.value();
		} else if (so instanceof ItemImpl) {
			return BaseTypeId.CMIS_ITEM.value();
		} else if (so instanceof VersionedDocumentImpl) {
			return "VersionedDocument";
		} else if (so instanceof FolderImpl) {
			return BaseTypeId.CMIS_FOLDER.value();
		} else {
			return null;
		}
	}

	public String storeObject(StoredObject so) {
		String id = so.getId();
		// check if update or create
		if (null == id) {
			id = getNextId().toString();
			so.put("_id", id);
		}

		so.setBaseTypeId(getBasicTypeId(so));

		objects.save(so);

		return id;
	}

	StoredObject getObject(String id) {
		return this.getObjectById(id);
	}

	void removeObject(String id) {
		objects.remove(id);
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
		rootFolder.setAclId(addAcl(InMemoryAcl.getDefaultAcl()));
		rootFolder.persist();
		fRootFolder = rootFolder;
	}

	public Document createDocument(String name, Map<String, PropertyData<?>> propMap, String user, Folder folder, List<String> policies, Acl addACEs, Acl removeACEs) {
		DocumentImpl doc = new DocumentImpl(this);
		doc.createSystemBasePropertiesWhenCreated(propMap, user);
		doc.setCustomProperties(propMap);
		doc.setName(name);
		if (null != folder) {
			((FolderImpl) folder).addChildDocument(doc); // add document to
															// folder and
		}
		ObjectId aclId = getAclId(((FolderImpl) folder), addACEs, removeACEs);
		doc.setAclId(aclId);
		if (null != policies)
			doc.setAppliedPolicies(policies);
		return doc;
	}

	public StoredObject createItem(String name, Map<String, PropertyData<?>> propMap, String user, Folder folder, List<String> policies, Acl addACEs, Acl removeACEs) {
		StoredObjectImpl item = new ItemImpl(this);
		item.createSystemBasePropertiesWhenCreated(propMap, user);
		item.setCustomProperties(propMap);
		item.setName(name);
		if (null != folder) {
			((FolderImpl) folder).addChildItem(item); // add document to folder
														// and
		}
		if (null != policies)
			item.setAppliedPolicies(policies);
		ObjectId aclId = getAclId(((FolderImpl) folder), addACEs, removeACEs);
		item.setAclId(aclId);
		return item;
	}

	public DocumentVersion createVersionedDocument(String name, Map<String, PropertyData<?>> propMap, String user, Folder folder, List<String> policies, Acl addACEs, Acl removeACEs, ContentStream contentStream, VersioningState versioningState) {
		VersionedDocumentImpl doc = new VersionedDocumentImpl(this);
		doc.createSystemBasePropertiesWhenCreated(propMap, user);
		doc.setCustomProperties(propMap);
		doc.setName(name);
		DocumentVersion version = doc.addVersion(contentStream, versioningState, user);
		if (null != folder) {
			((FolderImpl) folder).addChildDocument(doc); // add document to
															// folder and set
		}
		version.createSystemBasePropertiesWhenCreated(propMap, user);
		version.setCustomProperties(propMap);
		ObjectId aclId = getAclId(((FolderImpl) folder), addACEs, removeACEs);
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
		if (null != parent) {
			((FolderImpl) parent).addChildFolder(folder); // add document to
															// folder and set
		}

		ObjectId aclId = getAclId(((FolderImpl) parent), addACEs, removeACEs);
		folder.setAclId(aclId);
		if (null != policies)
			folder.setAppliedPolicies(policies);

		return folder;
	}

	public Folder createFolder(String name) {
		Folder folder = new FolderImpl(this, name, null);
		return folder;
	}

	public StoredObject createPolicy(String name, String policyText, Map<String, PropertyData<?>> propMap, String user) {
		PolicyImpl policy = new PolicyImpl(this);
		policy.createSystemBasePropertiesWhenCreated(propMap, user);
		policy.setCustomProperties(propMap);
		policy.setName(name);
		policy.setPolicyText(policyText);
		policy.persist();
		return policy;
	}

	public List<StoredObject> getCheckedOutDocuments(String orderBy, String user, IncludeRelationships includeRelationships) {
		List<StoredObject> res = new ArrayList<StoredObject>();
		// 没有实现
		// for (StoredObject so : objects.values()) {
		// if (so instanceof VersionedDocument) {
		// VersionedDocument verDoc = (VersionedDocument) so;
		// if (verDoc.isCheckedOut() && hasReadAccess(user, verDoc)) {
		// res.add(verDoc.getPwc());
		// }
		// }
		// }

		return res;
	}

	public StoredObject createRelationship(String name, StoredObject sourceObject, StoredObject targetObject, Map<String, PropertyData<?>> propMap, String user, Acl addACEs, Acl removeACEs) {

		RelationshipImpl rel = new RelationshipImpl(this);
		rel.createSystemBasePropertiesWhenCreated(propMap, user);
		rel.setCustomProperties(propMap);
		rel.setName(name);
		if (null != sourceObject)
			rel.setSource(sourceObject.getId());
		if (null != targetObject)
			rel.setTarget(targetObject.getId());
		ObjectId aclId = getAclId(null, addACEs, removeACEs);
		rel.setAclId(aclId);
		rel.persist();
		return rel;
	}

	public List<StoredObject> getRelationships(String objectId, List<String> typeIds, RelationshipDirection direction) {

		List<StoredObject> res = new ArrayList<StoredObject>();
		// 没有实现
		// if (typeIds != null && typeIds.size() > 0) {
		// for (String typeId : typeIds) {
		// for (StoredObject so : objects.values()) {
		// if (so instanceof Relationship && so.getTypeId().equals(typeId)) {
		// Relationship ro = (Relationship) so;
		// if (ro.getSourceObjectId().equals(objectId) &&
		// (RelationshipDirection.EITHER == direction ||
		// RelationshipDirection.SOURCE == direction)) {
		// res.add(so);
		// } else if (ro.getTargetObjectId().equals(objectId) &&
		// (RelationshipDirection.EITHER == direction ||
		// RelationshipDirection.TARGET == direction)) {
		// res.add(so);
		// }
		// }
		// }
		// }
		// } else
		// res = getAllRelationships(objectId, direction);
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

	private List<String> getPrincipalIds(String principalId) {
		List<String> principalIds = new ArrayList<String>();
		if (principalId == null)
			return principalIds;
		DBCursor dbCursor = objects.dbCollection.find(new BasicDBObject("$or",
				new DBObject[] {
						new BasicDBObject("system:users", new BasicDBObject("$in", new String[] { principalId })),
						new BasicDBObject("system:groups", new BasicDBObject("$in", new String[] { principalId }))
				}
				));
		try {
			for (DBObject dbObject : dbCursor) {
				String parentPrincipalId = dbObject.get("cmis:name").toString();
				principalIds.add(parentPrincipalId);
				principalIds.addAll(getPrincipalIds(parentPrincipalId));

			}
		} finally {
			dbCursor.close();
		}

		return principalIds;
	}

	public List<ObjectId> getAllAclsForUser(String principalId, Permission permission) {
		List<String> others = getPrincipalIds(principalId);
		DBCursor cursor = acls.find();
		try {
			List<ObjectId> acls = new ArrayList<ObjectId>();
			for (DBObject dbObject : cursor.toArray()) {
				InMemoryAcl acl = new InMemoryAcl();
				acl.putAll(dbObject);
				if (acl.hasPermission(principalId, permission))
					acls.add(acl.getId());
				for (String otherId : others) {
					if (acl.hasPermission(otherId, permission))
						acls.add(acl.getId());
				}
			}
			return acls;
		} finally {
			cursor.close();
		}

	}

	public Acl getAcl(ObjectId aclId) {
		InMemoryAcl acl = getInMemoryAcl(aclId);
		return acl == null ? InMemoryAcl.getDefaultAcl().toCommonsAcl() : acl.toCommonsAcl();
	}

	public ObjectId getAclId(StoredObjectImpl so, Acl addACEs, Acl removeACEs) {
		InMemoryAcl newAcl;
		boolean removeDefaultAcl = false;
		ObjectId aclId = InMemoryAcl.getDefaultAcl().getId();

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
			return InMemoryAcl.getDefaultAcl().getId();

		if (null != removeACEs)
			for (Ace ace : removeACEs.getAces()) {
				InMemoryAce inMemAce = new InMemoryAce(ace);
				if (inMemAce.equals(InMemoryAce.getDefaultAce()))
					removeDefaultAcl = true;
			}

		if (so != null && null == aclId && !removeDefaultAcl)
			return InMemoryAcl.getDefaultAcl().getId();
		// not be removed we do nothing

		// add ACEs
		if (null != addACEs)
			for (Ace ace : addACEs.getAces()) {
				InMemoryAce inMemAce = new InMemoryAce(ace);
				if (inMemAce.equals(InMemoryAce.getDefaultAce()))
					return InMemoryAcl.getDefaultAcl().getId();
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
			return InMemoryAcl.getDefaultAcl().getId();
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
		List<ObjectId> aclIds = getAllAclsForUser(principalId, permission);
		return aclIds.contains(((StoredObjectImpl) so).getAclId());
	}

	private InMemoryAcl getInMemoryAcl(ObjectId aclId) {
		DBObject dbObject = acls.findOne(new BasicDBObject("_id", aclId));
		if (dbObject == null)
			return null;
		InMemoryAcl acl = new InMemoryAcl();
		acl.putAll(dbObject);
		return acl;
	}

	private ObjectId setAcl(StoredObjectImpl so, Acl acl) {
		ObjectId aclId;
		if (null == acl || acl.getAces().isEmpty())
			aclId = null;
		else {
			aclId = getAclId(null, acl, null);
		}
		so.setAclId(aclId);
		this.objects.updateAclId(so.getId(), aclId);
		return aclId;
	}

	/**
	 * check if an Acl is already known
	 * 
	 * @param acl
	 *            acl to be checked
	 * @return 0 if Acl is not known, id of Acl otherwise
	 */
	private ObjectId hasAcl(InMemoryAcl acl) {

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
		return null;
	}

	private ObjectId addAcl(InMemoryAcl acl) {
		ObjectId aclId = InMemoryAcl.defaultId;

		if (null == acl)
			return null;

		aclId = hasAcl(acl);
		if (aclId == null) {
			aclId=ObjectId.get();
			if (acl.equals(InMemoryAcl.getDefaultAcl())) {
				aclId = InMemoryAcl.defaultId;
			}
			acl.setId(aclId);
			acls.insert(acl);
		}
		return aclId;
	}

	private Acl applyAcl(StoredObject so, Acl acl) {
		ObjectId aclId = setAcl((StoredObjectImpl) so, acl);
		return getAcl(aclId);
	}

	private Acl applyAcl(StoredObject so, Acl addAces, Acl removeAces) {
		ObjectId aclId = getAclId((StoredObjectImpl) so, addAces, removeAces);
		((StoredObjectImpl) so).setAclId(aclId);
		this.objects.updateAclId(so.getId(), aclId);
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
		// 没有实现
		// for (StoredObject so : objects.values()) {
		// if (so instanceof Relationship) {
		// Relationship ro = (Relationship) so;
		// if (ro.getSourceObjectId().equals(objectId) &&
		// (RelationshipDirection.EITHER == direction ||
		// RelationshipDirection.SOURCE == direction)) {
		// res.add(so);
		// } else if (ro.getTargetObjectId().equals(objectId) &&
		// (RelationshipDirection.EITHER == direction ||
		// RelationshipDirection.TARGET == direction)) {
		// res.add(so);
		// }
		// }
		// }
		return res;
	}

	public boolean isTypeInUse(String typeId) {
		// iterate over all the objects and check for each if the type matches
		return this.has(new BasicDBObject("cmis:objectTypeId", typeId));
	}

	public String storeContentStream(ContentStream cs) {
		String id = UUID.randomUUID().toString();
		GridFSInputFile file = fs.createFile(cs.getStream());
		file.setId(id);
		// file.setChunkSize(cs.getLength());
		file.setContentType(cs.getMimeType());
		file.setFilename(cs.getFileName());
		file.save();
		return id;
	}

	public ContentStream getContentStream(String contentStreamId) {

		GridFSDBFile file = fs.findOne(new BasicDBObject("_id", contentStreamId));
		ContentStreamDataImpl cs = new ContentStreamDataImpl(file.getLength());
		try {
			cs.setContent(file.getInputStream());
			cs.setFileName(file.getFilename());
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(file.getUploadDate());
			cs.setLastModified(calendar);
			cs.setMimeType(file.getContentType());
		} catch (IOException e) {
			throw new RuntimeException("Failed to get content from InputStream", e);
		}

		return cs;
	}

	public List<StoredObject> find(DBObject where, DBObject orderBy, int maxItems, int skipCount) {

		if (maxItems < 0)
			maxItems = Integer.MAX_VALUE;
		if (skipCount < 0) {
			skipCount = 0;
		}
		if (orderBy == null)
			orderBy = new BasicDBObject("_id", 1);
		DBCursor dbCursor = objects.dbCollection.find(where).sort(orderBy).skip(skipCount).limit(maxItems);
		return objects.from(dbCursor);
	}

	public boolean has(DBObject where) {
		return null != objects.dbCollection.findOne(where);
	}

	public int count(DBObject where) {
		return objects.dbCollection.find(where).count();
	}

}
