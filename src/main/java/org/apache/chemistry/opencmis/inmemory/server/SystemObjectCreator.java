package org.apache.chemistry.opencmis.inmemory.server;

import java.util.HashSet;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.server.CmisService;

public class SystemObjectCreator {

	private final CmisService cmisService;
	private final String repositoryId;

	public SystemObjectCreator(String repositoryId, CmisService     cmisService) {
		this.cmisService=cmisService;
		this.repositoryId = repositoryId;
	}

	public void createObjects(){
		createUsers();
		createGroups();
	}
	
	private void createUsers() {
		createUser("xql","123");
		createUser("wangl","123");
		createUser("lihua","123");
	}
	
	private void createGroups() {
		createGroup("g1");
		createGroup("g2");
		createGroup("g3");
	}
	

	private void createUser(String username,String password) {
		Set<PropertyData<?>> properties=new HashSet<PropertyData<?>>();
		properties.add(new PropertyStringImpl("cmis:objectTypeId","system:user"));
		properties.add(new PropertyStringImpl("cmis:name",username));
		properties.add(new PropertyStringImpl("system:password",password));
		cmisService.createItem(repositoryId, new PropertiesImpl(properties), null, null, null, null, null);
	}
	
	private void createGroup(String groupname) {
		Set<PropertyData<?>> properties=new HashSet<PropertyData<?>>();
		properties.add(new PropertyStringImpl("cmis:objectTypeId","system:group"));
		properties.add(new PropertyStringImpl("cmis:name",groupname));
		cmisService.createItem(repositoryId, new PropertiesImpl(properties), null, null, null, null, null);
	}
	
	
}
