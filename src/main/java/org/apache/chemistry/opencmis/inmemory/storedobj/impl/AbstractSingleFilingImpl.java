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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.inmemory.NameValidator;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Document;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;

import com.mongodb.BasicDBList;

/**
 * AbstractPathImpl is the common superclass of all objects hold in the
 * repository that have a single parent, these are: Folders.
 * 
 * @author Jens
 */
public abstract class AbstractSingleFilingImpl extends StoredObjectImpl implements SingleFiling {

	// protected FolderImpl fParent;

	protected AbstractSingleFilingImpl(ObjectStore objStore) {
		super(objStore);
	}

	public String getPath() {
		StringBuffer path = new StringBuffer(getName());
		if (null == getParentId()) {
			path.replace(0, path.length(), PATH_SEPARATOR);
		} else {
			// root folder-->
			Folder f = fObjStore.getFolderById(getParentId());
			while (f.getParentId() != null) {
				path.insert(0, PATH_SEPARATOR);
				path.insert(0, f.getName());
				f = fObjStore.getFolderById(f.getParentId());
			}
			path.insert(0, PATH_SEPARATOR);
		}
		// if (LOG.isDebugEnabled())
		// LOG.debug("getPath() returns: " + path.toString());
		return path.toString();
	}

	public String getParentId() {
		BasicDBList list = (BasicDBList) get("parentIds");
		return (String) list.get(0);
	}

	public boolean hasParent() {
		return null != getParentId();
	}

	public List<Folder> getParents() {
		if (null == getParentId()) {
			return Collections.emptyList();
		} else {
			Folder folder = getParentFolder();
			return Collections.singletonList(folder);
		}
	}

	public List<Folder> getParents(String user) {
		return getParents();
	}

	public void setParentId(String parentId) {
		List<String> parentIds = new ArrayList<String>();
		parentIds.add(parentId);
		put("parentIds", parentIds);
	}

	public Folder getParentFolder() {
		String parentId = this.getParentId();
		if (parentId == null)
			return null;
		return fObjStore.getFolderById(parentId);
	}

	@Override
	public void rename(String newName) {
		if (!NameValidator.isValidId(newName)) {
			throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
		}
		try {
			fObjStore.lock();
			if (getParentId() == null) {
				throw new CmisInvalidArgumentException("Root folder cannot be renamed.");
			}
			Folder folder = getParentFolder();
			if (folder.hasChild(newName)) {
				throw new CmisNameConstraintViolationException("Cannot rename object to " + newName + ". This path already exists.");
			}

			setName(newName);
		} finally {
			fObjStore.unlock();
		}
	}

	public void move(Folder oldParent, Folder newParent) {
		FolderImpl folder = (FolderImpl) getParentFolder();
		try {
			fObjStore.lock();

			if (this instanceof Document || this instanceof VersionedDocument) {
				folder.moveChildDocument(this, oldParent, newParent);
			} else {// it must be a folder
				if (getParentId() == null) {
					throw new IllegalArgumentException("Root folder cannot be moved.");
				}
				if (newParent == null) {
					throw new IllegalArgumentException("null is not a valid move target.");
				}
				if (newParent.hasChild(getName())) {
					throw new IllegalArgumentException("Cannot move folder, this name already exists in target.");
				}

				setParentId(newParent.getId());
			}
		} finally {
			fObjStore.unlock();
		}
	}

}
