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
package org.apache.chemistry.opencmis.inmemory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.TypeManagerCreatable;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.InMemoryAcl;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.MongoMap;
import org.apache.chemistry.opencmis.inmemory.types.BSONConverter;
import org.apache.chemistry.opencmis.inmemory.types.ChemistryTypeObject;
import org.apache.chemistry.opencmis.inmemory.types.DocumentTypeCreationHelper;
import org.apache.chemistry.opencmis.inmemory.types.InMemoryDocumentTypeDefinition;
import org.apache.chemistry.opencmis.inmemory.types.InMemoryFolderTypeDefinition;
import org.apache.chemistry.opencmis.inmemory.types.InMemoryItemTypeDefinition;
import org.apache.chemistry.opencmis.inmemory.types.InMemoryPolicyTypeDefinition;
import org.apache.chemistry.opencmis.inmemory.types.InMemoryRelationshipTypeDefinition;
import org.apache.chemistry.opencmis.inmemory.types.InMemorySecondaryTypeDefinition;
import org.apache.chemistry.opencmis.inmemory.types.TypeConverter;
import org.apache.chemistry.opencmis.inmemory.types.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * Class that manages a type system for a repository types can be added, the
 * inheritance can be managed and type can be retrieved for a given type id.
 * 
 * @author Jens
 * 
 */
public class TypeManagerImpl implements TypeManagerCreatable {

	private static final Logger LOG = LoggerFactory.getLogger(TypeManagerImpl.class.getName());
	/**
	 * map from repository id to a types map
	 */
	// private final Map<String, TypeDefinitionContainer> fTypesMap = new
	// HashMap<String, TypeDefinitionContainer>();

	private static Map<String, TypeDefinition> typeCache = new HashMap<String, TypeDefinition>();

	private DBCollection types;

	public TypeManagerImpl(String repositoryId) {
		try {
			MongoClient mongoClient = new MongoClient(ConfigConstants.DB_HOST);
			DB db = mongoClient.getDB(repositoryId);
			types = db.getCollection("types");
			init();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void init() {
		DBCursor dbCursor = types.find();
		try {
			for (DBObject o : dbCursor) {
				TypeDefinition typeDefinition = BSONConverter.toTypeDefinition(ChemistryTypeObject.wrap(o), true);
				typeCache.put(typeDefinition.getId(), typeDefinition);
			}
		} finally {
			dbCursor.close();
		}
	}

	public static TypeDefinition getTypeDefinition(String typeId) {
		if (typeCache.containsKey(typeId))
			return typeCache.get(typeId);

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.chemistry.opencmis.inmemory.TypeManager#getTypeById(java.lang
	 * .String)
	 */
	public TypeDefinitionContainer getTypeById(String typeId) {

		TypeDefinition typeDefinition = getTypeDefinition(typeId);
		if (typeDefinition == null)
			return null;

		return getTypeDefinitionContainer(typeDefinition);
	}

	private TypeDefinitionContainer getTypeDefinitionContainer(TypeDefinition typeDefinition) {

		TypeDefinitionContainerImpl container = new TypeDefinitionContainerImpl(typeDefinition);
		for (TypeDefinition td : typeCache.values()) {
			if (typeDefinition.getId().equals(td.getParentTypeId())) {
				container.getChildren().add(getTypeDefinitionContainer(td));
			}
		}

		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.chemistry.opencmis.inmemory.TypeManager#getTypeByQueryName
	 * (java.lang.String)
	 */
	public TypeDefinition getTypeByQueryName(String typeQueryName) {
		if (typeQueryName == null)
			return null;
		for (TypeDefinition td : typeCache.values()) {
			if (typeQueryName.equals(td.getQueryName())) {
				return td;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.chemistry.opencmis.inmemory.TypeManager#getTypeDefinitionList
	 * ()
	 */
	public Collection<TypeDefinitionContainer> getTypeDefinitionList() {
		Collection<TypeDefinitionContainer> collection = new HashSet<TypeDefinitionContainer>();
		for (TypeDefinition td : typeCache.values()) {
			collection.add(getTypeDefinitionContainer(td));
		}

		return collection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.chemistry.opencmis.inmemory.TypeManager#getRootTypes()
	 */
	public List<TypeDefinitionContainer> getRootTypes() {
		List<TypeDefinitionContainer> rootTypes = new ArrayList<TypeDefinitionContainer>();

		for (TypeDefinition td : typeCache.values()) {
			if (isRootType(td.getId())) {
				rootTypes.add(getTypeDefinitionContainer(td));
			}
		}

		return rootTypes;
	}

	/**
	 * Initialize the type system with the given types. This list must not
	 * contain the CMIS default types. The default type are always contained by
	 * default.
	 * 
	 * @param typesList
	 *            list of types to add to the repository
	 * 
	 */
	public void initTypeSystem(List<TypeDefinition> typesList) {

		createCmisDefaultTypes();

		// merge all types from the list and build the correct hierachy with
		// children
		// and property lists
		if (null != typesList) {
			for (TypeDefinition typeDef : typesList) {
				addTypeDefinition(typeDef);
			}
		}

	}

	/**
	 * Add a type to the type system. Add all properties from inherited types,
	 * add type to children of parent types.
	 * 
	 * @param repositoryId
	 *            repository to which the type is added
	 * @param cmisType
	 *            new type to add
	 */
	public void addTypeDefinition(TypeDefinition cmisType) {

		TypeDefinitionContainerImpl typeContainer = new TypeDefinitionContainerImpl(cmisType);

		// add new type to children of parent types
		TypeDefinitionContainer parentTypeContainer = getTypeById(cmisType.getParentTypeId());
		parentTypeContainer.getChildren().add(typeContainer);

		// recursively add inherited properties
		Map<String, PropertyDefinition<?>> propDefs = typeContainer.getTypeDefinition().getPropertyDefinitions();
		addInheritedProperties(propDefs, parentTypeContainer.getTypeDefinition());

		// add type to type map
		DBObject dbObject = TypeConverter.toDBObject(cmisType);
		types.save(dbObject);
		typeCache.put(cmisType.getId(), cmisType);

		LOG.info("Adding type definition with name " + cmisType.getLocalName() + " and id " + cmisType.getId() + " to repository.");
	}

	public void updateTypeDefinition(TypeDefinition typeDefinition) {
		throw new CmisNotSupportedException("updating a type definition is not supported.");
	}

	/**
	 * Remove a type from a type system
	 * 
	 * @param typeId
	 */
	public void deleteTypeDefinition(String typeId) {
		TypeDefinitionContainer typeDef = getTypeById(typeId);
		types.remove(new BasicDBObject("_id", typeDef.getTypeDefinition().getId()));
		typeCache.remove(typeId);
		for (TypeDefinitionContainer c : typeDef.getChildren()) {
			deleteTypeDefinition(c.getTypeDefinition().getId());
		}
	}

	/**
	 * Remove all types from the type system. After this call only the default
	 * CMIS types are present in the type system. Use this method with care, its
	 * mainly intended for unit tests
	 * 
	 * @param repositoryId
	 */
	public void clearTypeSystem() {
		types.drop();
		typeCache.clear();
		createCmisDefaultTypes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.chemistry.opencmis.inmemory.TypeManager#getPropertyIdForQueryName
	 * (org.apache.chemistry.opencmis.commons.definitions.TypeDefinition,
	 * java.lang.String)
	 */
	public String getPropertyIdForQueryName(TypeDefinition typeDefinition, String propQueryName) {
		for (PropertyDefinition<?> pd : typeDefinition.getPropertyDefinitions().values()) {
			if (pd.getQueryName().equals(propQueryName)) {
				return pd.getId();
			}
		}
		return null;
	}

	private void addInheritedProperties(Map<String, PropertyDefinition<?>> propDefs, TypeDefinition typeDefinition) {

		if (null == typeDefinition) {
			return;
		}

		if (null != typeDefinition.getPropertyDefinitions()) {
			addInheritedPropertyDefinitions(propDefs, typeDefinition.getPropertyDefinitions());
			// propDefs.putAll(typeDefinition.getPropertyDefinitions());
		}

		TypeDefinitionContainer parentTypeContainer = getTypeById(typeDefinition.getParentTypeId());
		TypeDefinition parentType = (null == parentTypeContainer ? null : parentTypeContainer.getTypeDefinition());
		addInheritedProperties(propDefs, parentType);
	}

	private static void addInheritedPropertyDefinitions(Map<String, PropertyDefinition<?>> propDefs, Map<String, PropertyDefinition<?>> superPropDefs) {

		for (Entry<String, PropertyDefinition<?>> superProp : superPropDefs.entrySet()) {
			PropertyDefinition<?> superPropDef = superProp.getValue();
			PropertyDefinition<?> clone = clonePropertyDefinition(superPropDef);
			((AbstractPropertyDefinition<?>) clone).setIsInherited(true);
			propDefs.put(superProp.getKey(), clone);
		}
	}

	private void createCmisDefaultTypes() {
		List<TypeDefinition> typesList = DocumentTypeCreationHelper.createDefaultTypes();
		for (TypeDefinition typeDef : typesList) {

			types.save(TypeConverter.toDBObject(typeDef));
			// TypeDefinitionContainerImpl typeContainer = new
			// TypeDefinitionContainerImpl(typeDef);
			// fTypesMap.put(typeDef.getId(), typeContainer);
		}
	}

	private static boolean isRootType(String objectId) {
		return objectId.equals("cmis:folder")
				|| objectId.equals("cmis:document")
				|| objectId.equals("cmis:relationship")
				|| objectId.equals("cmis:policy")
				|| objectId.equals("cmis:item")
				|| objectId.equals("cmis:secondary");
	}

	private static PropertyDefinition<?> clonePropertyDefinition(PropertyDefinition<?> src) {
		PropertyDefinition<?> clone = TypeUtil.clonePropertyDefinition(src);
		return clone;
	}

}
