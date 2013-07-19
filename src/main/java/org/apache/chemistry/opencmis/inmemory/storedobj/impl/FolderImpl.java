package org.apache.chemistry.opencmis.inmemory.storedobj.impl;

/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RenditionDataImpl;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.inmemory.FilterParser;
import org.apache.chemistry.opencmis.inmemory.NameValidator;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Document;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Filing;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;

public class FolderImpl extends AbstractSingleFilingImpl implements Folder {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSingleFilingImpl.class.getName());

	FolderImpl(ObjectStore objStore) {
		super(objStore);
	}

	public FolderImpl(ObjectStore objStore, String name, Folder parent) {
		super(objStore);
		init(name, parent);
	}

	public void addChildFolder(Folder folder) {
		try {
			fObjStore.lock();
			boolean hasChild;
			String name = folder.getName();
			hasChild = hasChild(name);
			if (hasChild) {
				throw new CmisNameConstraintViolationException("Cannot create folder " + name + ". Name already exists in parent folder");
			}
			folder.setParentId(this.getId());
		} finally {
			fObjStore.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opencmis.client.provider.spi.inmemory.IFolder#addChildDocument(org
	 * .opencmis.client.provider .spi.inmemory.storedobj.impl.DocumentImpl)
	 */
	public void addChildDocument(Document doc) {
		addChildObject(doc);
	}

	public void addChildDocument(VersionedDocument doc) {
		addChildObject(doc);
	}

	public void addChildItem(StoredObject item) {
		addChildObject(item);
	}

	private void addChildObject(StoredObject so) {
		try {
			fObjStore.lock();
			String name = so.getName();

			boolean hasChild;
			hasChild = hasChild(name);
			if (hasChild) {
				throw new CmisNameConstraintViolationException("Cannot create object: " + name + ". Name already exists in parent folder");
			}

			if (so instanceof SingleFiling) {
				((SingleFiling) so).setParentId(this.getId());
			} else if (so instanceof MultiFiling) {
				((MultiFiling) so).addParent(this.getId());
			} else {
				throw new CmisInvalidArgumentException("Cannot create document, object is not fileable.");
			}

		} finally {
			fObjStore.unlock();
		}
	}

	public ChildrenResult getChildren(int maxItems, int skipCount, String user) {
		List<ObjectId> aclIds = fObjStore.getAllAclsForUser(user, Permission.READ);
		BasicDBObject where = new BasicDBObject();
		where.put("parentIds", new BasicDBObject("$in", new String[] { this.getId() }));
		where.put("aclId", new BasicDBObject("$in", aclIds));
		List<StoredObject> result = fObjStore.find(where, new BasicDBObject("cmis:lastModificationDate", -1), maxItems, skipCount);
		return new ChildrenResult(result, fObjStore.count(where));
	}

	public ChildrenResult getFolderChildren(int maxItems, int skipCount, String user) {
		List<ObjectId> aclIds = fObjStore.getAllAclsForUser(user, Permission.READ);
		BasicDBObject where = new BasicDBObject();
		where.put("parentIds", new BasicDBObject("$in", new String[] { this.getId() }));
		where.put("cmis:baseTypeId", BaseTypeId.CMIS_FOLDER.value());
		where.put("aclId", new BasicDBObject("$in", aclIds));
		List<StoredObject> result = fObjStore.find(where, new BasicDBObject("cmis:lastModificationDate", -1), maxItems, skipCount);
		return new ChildrenResult(result, fObjStore.count(where));
	}

	public boolean hasChild(String name) {
		BasicDBObject where = new BasicDBObject();
		where.put("parentIds", new BasicDBObject("$in", new String[] { this.getId() }));
		where.put("name", name);
		return fObjStore.has(where);
	}

	@Override
	public void fillProperties(Map<String, PropertyData<?>> properties, BindingsObjectFactory objFactory, List<String> requestedIds) {

		super.fillProperties(properties, objFactory, requestedIds);

		// add folder specific properties

		if (FilterParser.isContainedInFilter(PropertyIds.PARENT_ID, requestedIds)) {
			String parentId = getParentId();
			properties.put(PropertyIds.PARENT_ID, objFactory.createPropertyIdData(PropertyIds.PARENT_ID, parentId));
		}

		if (FilterParser.isContainedInFilter(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, requestedIds)) {
			String allowedChildObjects = "*"; // TODO: not yet supported
			properties.put(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, objFactory.createPropertyIdData(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, allowedChildObjects));
		}

		if (FilterParser.isContainedInFilter(PropertyIds.PATH, requestedIds)) {
			String path = getPath();
			properties.put(PropertyIds.PATH, objFactory.createPropertyStringData(PropertyIds.PATH, path));
		}
	}

	// Helper functions
	private void init(String name, Folder parent) {
		if (!NameValidator.isValidName(name)) {
			throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
		}
		setName(name);
		if (parent != null) {
			setParentId(parent.getId());
		}
	}

	private static void sortFolderList(List<? extends StoredObject> list) {
		// TODO evaluate orderBy, for now sort by path segment
		class FolderComparator implements Comparator<StoredObject> {

			public int compare(StoredObject f1, StoredObject f2) {
				String segment1 = f1.getName();
				String segment2 = f2.getName();

				return segment1.compareTo(segment2);
			}
		}

		Collections.sort(list, new FolderComparator());
	}

	public void moveChildDocument(StoredObject so, Folder oldParent, Folder newParent) {
		try {
			fObjStore.lock();
			if (newParent.hasChild(so.getName())) {
				throw new IllegalArgumentException("Cannot move object, this name already exists in target.");
			}
			if (!(so instanceof Filing)) {
				throw new IllegalArgumentException("Cannot move object, object does not have a path.");
			}

			if (so instanceof SingleFiling) {
				SingleFiling pathObj = (SingleFiling) so;
				pathObj.setParentId(newParent.getId());
			} else if (so instanceof MultiFiling) {
				MultiFiling pathObj = (MultiFiling) so;
				pathObj.addParent(newParent.getId());
				pathObj.removeParent(oldParent.getId());
			}
		} finally {
			fObjStore.unlock();
		}
	}

	public List<String> getAllowedChildObjectTypeIds() {
		// TODO implement this.
		return null;
	}

	public List<RenditionData> getRenditions(String renditionFilter, long maxItems, long skipCount) {
		if (null == renditionFilter)
			return null;
		String tokenizer = "[\\s;]";
		String[] formats = renditionFilter.split(tokenizer);
		boolean isImageRendition = testRenditionFilterForImage(formats);

		if (isImageRendition) {
			List<RenditionData> renditions = new ArrayList<RenditionData>(1);
			RenditionDataImpl rendition = new RenditionDataImpl();
			rendition.setBigHeight(BigInteger.valueOf(ICON_SIZE));
			rendition.setBigWidth(BigInteger.valueOf(ICON_SIZE));
			rendition.setKind("cmis:thumbnail");
			rendition.setMimeType(RENDITION_MIME_TYPE_PNG);
			rendition.setRenditionDocumentId(getId());
			rendition.setStreamId(getId() + RENDITION_SUFFIX);
			rendition.setBigLength(BigInteger.valueOf(-1L));
			rendition.setTitle(getName());
			rendition.setRenditionDocumentId(getId());
			renditions.add(rendition);
			return renditions;
		} else {
			return null;
		}
	}

	public ContentStream getRenditionContent(String streamId, long offset, long length) {
		try {
			return getIconFromResourceDir("/folder.png");
		} catch (IOException e) {
			LOG.error("Failed to generate rendition: ", e);
			throw new CmisRuntimeException("Failed to generate rendition: " + e);
		}
	}

	public boolean hasRendition(String user) {
		return true;
	}

}
