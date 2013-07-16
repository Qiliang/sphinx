/*
x * Licensed to the Apache Software Foundation (ASF) under one
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.bson.BSONObject;
import org.bson.util.StringRangeSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class InMemoryAcl extends BasicDBObject implements Cloneable {

	private static final long serialVersionUID = -6858916824378131437L;
	// private DbList<InMemoryAce> aces;
	// private int id;
	@SuppressWarnings("serial")
	private static InMemoryAcl DEFAULT_ACL = new InMemoryAcl(new ArrayList<InMemoryAce>() {
		{
			add(InMemoryAce.getDefaultAce());
		}
	});

	private static class AceComparator<T extends InMemoryAce> implements Comparator<T> {

		public int compare(T o1, T o2) {
			if (null == o1 || null == o2) {
				if (o1 == o2)
					return 0;
				else if (o1 == null)
					return 1;
				else
					return -1;
			}
			int res = o1.getPrincipalId().compareTo(o2.getPrincipalId());
			return res;
		}

	};

	private static Comparator<? super InMemoryAce> COMP = new AceComparator<InMemoryAce>();

	public static InMemoryAcl createFromCommonsAcl(Acl commonsAcl) {
		InMemoryAcl acl = new InMemoryAcl();
		for (Ace cace : commonsAcl.getAces()) {
			if (acl.hasPrincipal(cace.getPrincipalId())) {
				Permission perm = acl.getPermission(cace.getPrincipalId());
				Permission newPerm = Permission.fromCmisString(cace.getPermissions().get(0));
				if (perm.ordinal() > newPerm.ordinal())
					acl.setPermission(cace.getPrincipalId(), newPerm);
			} else {
				acl.addAce(new InMemoryAce(cace));
			}

		}
		return acl;
	}

	public static InMemoryAcl getDefaultAcl() {
		return DEFAULT_ACL;
	}

	public InMemoryAcl() {
		put("aces", new ArrayList<InMemoryAce>());
	}

	public InMemoryAcl(final List<InMemoryAce> arg) {
		List<InMemoryAce> aces = new ArrayList<InMemoryAce>();
		aces.addAll(arg);
		// DbList<InMemoryAce> aces = (DbList<InMemoryAce>) get("aces");
		// Collections.sort(this.aces, COMP);
		for (int i = 0; i < aces.size(); i++) {
			InMemoryAce ace = aces.get(i);
			if (ace == null)
				throw new IllegalArgumentException("Cannot create ACLs with a null principal id or permission.");
		}
		for (int i = 0; i < aces.size() - 1; i++) {
			if (aces.get(i).equals(aces.get(i + 1)))
				throw new IllegalArgumentException("Cannot create ACLs with same principal id in more than one ACE.");
		}

		put("aces", aces);
	}

	// private void initAces() {
	// aces = new DbList<InMemoryAce>();
	// BasicDBList list = (BasicDBList) get("aces");
	// if (list == null)
	// return;
	// for (Object o : list) {
	// BasicDBObject dbObject = (BasicDBObject) o;
	// aces.add(new InMemoryAce(dbObject.getString("principalId"),
	// Permission.valueOf(dbObject.getString("permission"))));
	// }
	// }

	public void setId(int id) {
		put("_id", id);
	}

	public int getId() {
		if (containsField("_id"))
			return getInt("_id");
		return -1;
	}

	public final List<InMemoryAce> getAces() {
		List<InMemoryAce> aces = new ArrayList<InMemoryAce>();
		Object result = get("aces");
		if (result instanceof ArrayList) {
			ArrayList dbList = (ArrayList) result;
			for (Object o : dbList) {
				BasicDBObject dbObject = (BasicDBObject) o;
				aces.add(new InMemoryAce(dbObject.getString("principalId"), Permission.fromValue(dbObject.getString("permission"))));
			}
			return aces;
		}

		return aces;
	}

	public boolean addAce(InMemoryAce ace) {
		if (ace == null)
			return false;

		List<InMemoryAce> aces = getAces();
		for (InMemoryAce ace2 : aces) {
			if (ace2.getPermission().equals(ace.getPrincipalId()))
				return false;
		}
		aces.add(ace);
		Collections.sort(aces, COMP);
		put("aces", aces);
		return true;
	}

	public boolean removeAce(InMemoryAce ace) {
		List<InMemoryAce> aces = getAces();
		boolean success = aces.remove(ace);
		put("aces", aces);
		return success;
	}

	public void mergeAcl(InMemoryAcl acl2) {
		if (acl2 == null)
			return;
		for (InMemoryAce ace : acl2.getAces()) {
			InMemoryAce existingAce = getAce(ace.getPrincipalId());
			if (existingAce == null)
				addAce(ace);
			else if (existingAce.getPermission().ordinal() < ace.getPermission().ordinal())
				existingAce.setPermission(ace.getPermission());
		}
		// Collections.sort(this.aces, COMP);
	}

	public Permission getPermission(String principalId) {
		InMemoryAce ace = getAce(principalId);
		return ace == null ? Permission.NONE : ace.getPermission();
	}

	private InMemoryAce getAce(String principalId) {
		if (null == principalId)
			return null;

		List<InMemoryAce> aces = getAces();
		for (InMemoryAce ace2 : aces) {
			if (ace2.getPermission().equals(principalId))
				return ace2;
		}

		return null;
	}

	public boolean hasPermission(String principalId, Permission permission) {
		if (null == permission)
			return false;

		List<InMemoryAce> aces = getAces();

		if (null == principalId)
			for (InMemoryAce ace : aces)
				if (ace.getPrincipalId().equals(InMemoryAce.getAnonymousUser()))
					return ace.hasPermission(permission);

		for (InMemoryAce ace : aces) {
			if (ace.getPrincipalId().equals(principalId) || ace.getPrincipalId().equals(InMemoryAce.getAnyoneUser()) || ace.getPrincipalId().equals(InMemoryAce.getAnonymousUser()))
				return ace.hasPermission(permission);
		}
		return false;
	}

	public void setPermission(String principalId, Permission permission) {
		List<InMemoryAce> aces = getAces();
		for (InMemoryAce ace : aces) {
			if (ace.getPrincipalId().equals(principalId))
				ace.setPermission(permission);
		}
		throw new IllegalArgumentException("Unknown principalId in setPermission: " + principalId);
	}

	public int size() {
		List<InMemoryAce> aces = getAces();
		return aces.size();
	}

	@Override
	public int hashCode() {
		List<InMemoryAce> aces = getAces();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aces == null) ? 0 : aces.hashCode());
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
		InMemoryAcl other = (InMemoryAcl) obj;
		List<InMemoryAce> aces = getAces();
		if (aces == null) {
			if (other.get("aces") != null)
				return false;
		} else if (!aces.equals(other.get("aces")))
			return false;
		return true;
	}

	@Override
	public String toString() {
		List<InMemoryAce> aces = getAces();
		return "InMemoryAcl [acl=" + aces + "]";
	}

	private boolean hasPrincipal(String principalId) {
		List<InMemoryAce> aces = getAces();
		for (InMemoryAce ace : aces) {
			if (ace.getPrincipalId().equals(principalId))
				return true;
		}
		return false;
	}

	public Acl toCommonsAcl() {
		List<InMemoryAce> aces = getAces();
		List<Ace> commonsAcl = new ArrayList<Ace>();
		for (InMemoryAce memAce : aces)
			commonsAcl.add(memAce.toCommonsAce());

		return new AccessControlListImpl(commonsAcl);
	}

	public InMemoryAcl clone() {
		List<InMemoryAce> aces = getAces();
		InMemoryAcl newAcl = new InMemoryAcl(aces);
		return newAcl;
	}

}
