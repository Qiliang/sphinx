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

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.inmemory.NameValidator;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;

import com.mongodb.BasicDBList;

/**
 * AbstractMultiPathImpl is the common superclass of all objects hold in the
 * repository that have multiple parent folders, these are: Folders
 * 
 * @author Jens
 */
public abstract class AbstractMultiFilingImpl extends StoredObjectImpl implements MultiFiling {

	// protected List<Folder> fParents = new ArrayList<Folder>(1);

	AbstractMultiFilingImpl(ObjectStore objStore) {
		super(objStore);
	}

	public void addParent(String parentId) {
		try {
			fObjStore.lock();
			addParentIntern(parentId);
		} finally {
			fObjStore.unlock();
		}
	}

	private void addParentIntern(String parentId) {

		Folder folder = fObjStore.getFolderById(parentId);

		if (folder.hasChild(getName())) {
			throw new IllegalArgumentException("Cannot assign new parent folder, this name already exists in target folder.");
		}

		BasicDBList list = (BasicDBList) get("parentIds");
		if (list == null)
			list = new BasicDBList();

		list.add(parentId);
		put("parentIds", list);
	}

	public void removeParent(String parentId) {
		try {
			fObjStore.lock();
			removeParentIntern(parentId);
		} finally {
			fObjStore.unlock();
		}
	}

	private void removeParentIntern(String parentId) {

		BasicDBList list = (BasicDBList) get("parentIds");
		if (list == null)
			list = new BasicDBList();

		list.remove(parentId);
		put("parentIds", list);
	}

	public String[] getParentIds() {
		BasicDBList list = (BasicDBList) get("parentIds");

		if (null == list)
			return new String[] {};
		else {
			return list.toArray(new String[] {});
		}

	}

	private List<Folder> getParents() {
		List<Folder> folders = new ArrayList<Folder>();
		BasicDBList list = (BasicDBList) get("parentIds");
		for (Object o : list) {
			String parentId = o.toString();
			folders.add(fObjStore.getFolderById(parentId));
		}
		return folders;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.opencmis.inmemory.storedobj.api.MultiParentPath#getParents()
	 */
	public List<Folder> getParents(String user) {
		if (null == get("parentIds"))
			return Collections.emptyList();
		else if (null == user)
			return Collections.unmodifiableList(getParents());
		else {
			List<Folder> visibleParents = new ArrayList<Folder>();
			for (Folder folder : getParents())
				if (fObjStore.hasReadAccess(user, folder))
					visibleParents.add(folder);
			return visibleParents;
		}
	}

	public boolean hasParent() {
		BasicDBList list = (BasicDBList) get("parentIds");
		return null != list && !list.isEmpty();
	}

	public String getPathSegment() {
		return getName();
	}

	public void move(Folder oldParent, Folder newParent) {
		try {
			fObjStore.lock();
			addParentIntern(newParent.getId());
			removeParentIntern(oldParent.getId());
		} finally {
			fObjStore.unlock();
		}
	}

	@Override
	public void rename(String newName) {
		try {
			if (!NameValidator.isValidId(newName)) {
				throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
			}
			fObjStore.lock();
			for (Folder folder : getParents()) {
				if (folder == null) {
					throw new CmisInvalidArgumentException("Root folder cannot be renamed.");
				}
				if (folder.hasChild(newName)) {
					throw new CmisNameConstraintViolationException("Cannot rename object to " + newName + ". This path already exists in parent " + folder.getPath() + ".");
				}
			}
			setName(newName);
		} finally {
			fObjStore.unlock();
		}
	}

}
