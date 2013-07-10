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

import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;

import com.mongodb.BasicDBObject;

public class InMemoryAce extends BasicDBObject {

	// private final String principalId;
	// private Permission permission;
	private static InMemoryAce DEFAULT_ACE = new InMemoryAce(InMemoryAce.getAnyoneUser(), Permission.ALL);

	public static final String getAnyoneUser() {
		return "anyone";
	}

	public static final String getAnonymousUser() {
		return "anonymous";
	}

	public static final InMemoryAce getDefaultAce() {
		return DEFAULT_ACE;
	}

	public InMemoryAce(Ace commonsAce) {
		if (null == commonsAce || null == commonsAce.getPrincipalId() || null == commonsAce.getPermissions())
			throw new IllegalArgumentException("Cannot create InMemoryAce with null value");
		List<String> perms = commonsAce.getPermissions();
		if (perms.size() != 1)
			throw new IllegalArgumentException("InMemory only supports ACEs with a single permission.");
		String perm = perms.get(0);
		put("principalId", commonsAce.getPrincipalId());
		put("permission", Permission.fromCmisString(perm).value());
		// this.principalId = commonsAce.getPrincipalId();
		// this.permission = Permission.fromCmisString(perm);
	}

	public InMemoryAce(String prinicpalId, Permission permission) {
		if (null == prinicpalId || null == permission)
			throw new IllegalArgumentException("Cannot create InMemoryAce with null value");

		put("principalId", prinicpalId);
		put("permission", permission.value());
	}

	public String getPrincipalId() {
		return getString("principalId");
	}

	public Permission getPermission() {
		return Permission.fromValue(getString("permission"));
	}

	public void setPermission(Permission newPermission) {
		put("permission", newPermission.value());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getPermission() == null) ? 0 : getPermission().hashCode());
		result = prime * result + ((getPrincipalId() == null) ? 0 : getPrincipalId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InMemoryAce other = (InMemoryAce) obj;
		if (getPermission() != other.getPermission())
			return false;
		if (getPrincipalId() == null) {
			if (other.getPrincipalId() != null)
				return false;
		} else if (!getPrincipalId().equals(other.getPrincipalId()))
			return false;
		return true;
	}

	public boolean hasPermission(Permission permission2) {
		return this.getPermission().compareTo(permission2) >= 0;
	}

	@Override
	public String toString() {
		return "InMemoryAce [principalId=" + getPrincipalId() + ", permission=" + getPermission() + "]";
	}

	public Ace toCommonsAce() {
		return new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(getPrincipalId()), Collections.singletonList(getPermission().toCmisString()));
	}

}
