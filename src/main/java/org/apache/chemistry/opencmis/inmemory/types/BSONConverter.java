package org.apache.chemistry.opencmis.inmemory.types;

import java.util.List;
import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.PropertyBooleanDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDateTimeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDecimalDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyHtmlDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIdDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIntegerDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyStringDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyUriDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class BSONConverter {

	private static List toChoiceList(BasicDBList dbList) {
		if (dbList == null)
			return null;
		List<Choice> choices = null;
		return choices;
	}

	private static PropertyDefinition<?> toPropertyDefinition(ChemistryTypeObject dbObject) {
		PropertyType propertyType = dbObject.getPropertyType("propertyType");
		switch (propertyType) {
		case BOOLEAN:
			return toPropertyBooleanDefinition(dbObject);
		case DATETIME:
			return toPropertyDateTimeDefinition(dbObject);
		case DECIMAL:
			return toPropertyDecimalDefinition(dbObject);
		case HTML:
			return toPropertyHtmlDefinition(dbObject);
		case ID:
			return toPropertyIdDefinition(dbObject);
		case INTEGER:
			return toPropertyIntegerDefinition(dbObject);
		case STRING:
			return toPropertyStringDefinition(dbObject);
		case URI:
			return toPropertyUriDefinition(dbObject);

		}
		return null;

	}

	private static void addBasicProperties(AbstractPropertyDefinition propertyDefinition, ChemistryTypeObject dbObject) {

		propertyDefinition.setId(dbObject.getString("id"));
		propertyDefinition.setLocalName(dbObject.getString("localName"));
		propertyDefinition.setLocalNamespace(dbObject.getString("localNamespace"));
		propertyDefinition.setQueryName(dbObject.getString("queryName"));
		propertyDefinition.setDisplayName(dbObject.getString("displayName"));
		propertyDefinition.setDescription(dbObject.getString("description"));
		propertyDefinition.setPropertyType(dbObject.getPropertyType("propertyType"));
		propertyDefinition.setCardinality(dbObject.getCardinality("cardinality"));
		propertyDefinition.setChoices(toChoiceList((BasicDBList) dbObject.get("choiceList")));
		propertyDefinition.setDefaultValue((List) dbObject.get("defaultValue"));
		propertyDefinition.setUpdatability(dbObject.getUpdatability("updatability"));
		propertyDefinition.setIsInherited(dbObject.getBoolean2("isInherited"));
		propertyDefinition.setIsQueryable(dbObject.getBoolean2("isQueryable"));
		propertyDefinition.setIsOrderable(dbObject.getBoolean2("isOrderable"));
		propertyDefinition.setIsRequired(dbObject.getBoolean2("isRequired"));
		propertyDefinition.setIsOpenChoice(dbObject.getBoolean2("isOpenChoice"));
	}

	private static PropertyBooleanDefinition toPropertyBooleanDefinition(ChemistryTypeObject dbObject) {
		PropertyBooleanDefinitionImpl propertyBooleanDefinition = new PropertyBooleanDefinitionImpl();
		addBasicProperties(propertyBooleanDefinition, dbObject);

		return propertyBooleanDefinition;
	}

	private static PropertyDateTimeDefinition toPropertyDateTimeDefinition(ChemistryTypeObject dbObject) {
		PropertyDateTimeDefinitionImpl propertyDateTimeDefinition = new PropertyDateTimeDefinitionImpl();
		addBasicProperties(propertyDateTimeDefinition, dbObject);
		propertyDateTimeDefinition.setDateTimeResolution(dbObject.getDateTimeResolution("dateTimeResolution"));
		return propertyDateTimeDefinition;
	}

	private static PropertyDecimalDefinition toPropertyDecimalDefinition(ChemistryTypeObject dbObject) {
		PropertyDecimalDefinitionImpl propertyDecimalDefinition = new PropertyDecimalDefinitionImpl();
		addBasicProperties(propertyDecimalDefinition, dbObject);
		propertyDecimalDefinition.setMinValue(dbObject.getBigDecimal("minValue"));
		propertyDecimalDefinition.setMinValue(dbObject.getBigDecimal("maxValue"));
		propertyDecimalDefinition.setPrecision(dbObject.getDecimalPrecision("precision"));
		return propertyDecimalDefinition;
	}

	private static PropertyHtmlDefinition toPropertyHtmlDefinition(ChemistryTypeObject dbObject) {
		PropertyHtmlDefinitionImpl propertyHtmlDefinition = new PropertyHtmlDefinitionImpl();
		addBasicProperties(propertyHtmlDefinition, dbObject);

		return propertyHtmlDefinition;
	}

	private static PropertyIdDefinition toPropertyIdDefinition(ChemistryTypeObject dbObject) {
		PropertyIdDefinitionImpl propertyIdDefinition = new PropertyIdDefinitionImpl();
		addBasicProperties(propertyIdDefinition, dbObject);

		return propertyIdDefinition;
	}

	private static PropertyIntegerDefinition toPropertyIntegerDefinition(ChemistryTypeObject dbObject) {
		PropertyIntegerDefinitionImpl propertyIntegerDefinition = new PropertyIntegerDefinitionImpl();
		addBasicProperties(propertyIntegerDefinition, dbObject);
		propertyIntegerDefinition.setMinValue(dbObject.getBigInteger("minValue"));
		propertyIntegerDefinition.setMinValue(dbObject.getBigInteger("maxValue"));
		return propertyIntegerDefinition;
	}

	private static PropertyStringDefinition toPropertyStringDefinition(ChemistryTypeObject dbObject) {
		PropertyStringDefinitionImpl propertyStringDefinition = new PropertyStringDefinitionImpl();
		addBasicProperties(propertyStringDefinition, dbObject);
		propertyStringDefinition.setMaxLength(dbObject.getBigInteger("maxLength"));
		return propertyStringDefinition;
	}

	private static PropertyUriDefinition toPropertyUriDefinition(ChemistryTypeObject dbObject) {
		PropertyUriDefinitionImpl propertyUriDefinition = new PropertyUriDefinitionImpl();
		addBasicProperties(propertyUriDefinition, dbObject);

		return propertyUriDefinition;
	}

	public static AbstractTypeDefinition toTypeDefinition(ChemistryTypeObject dbObject, boolean includePropertyDefinitions) {
		BaseTypeId baseTypeId = dbObject.getBaseTypeId("baseId");
		if (baseTypeId == BaseTypeId.CMIS_FOLDER) {
			return toFolderTypeDefinition(dbObject, includePropertyDefinitions);
		} else if (baseTypeId == BaseTypeId.CMIS_DOCUMENT) {
			return toDocumentTypeDefinition(dbObject, includePropertyDefinitions);
		} else if (baseTypeId == BaseTypeId.CMIS_RELATIONSHIP) {
			return toRelationshipTypeDefinition(dbObject, includePropertyDefinitions);
		} else if (baseTypeId == BaseTypeId.CMIS_POLICY) {
			return toPolicyTypeDefinition(dbObject, includePropertyDefinitions);
		}else if (baseTypeId == BaseTypeId.CMIS_ITEM) {
			return toPolicyTypeDefinition(dbObject, includePropertyDefinitions);
		}else if (baseTypeId == BaseTypeId.CMIS_SECONDARY) {
			return toPolicyTypeDefinition(dbObject, includePropertyDefinitions);
		} else {
			return null;
		}
	}

	private static void addBasicPropertyDefinitions(AbstractTypeDefinition typeDefinition, ChemistryTypeObject dbObject, boolean includePropertyDefinitions) {

		typeDefinition.setId(dbObject.getString("_id"));
		typeDefinition.setLocalName(dbObject.getString("localName"));
		typeDefinition.setLocalNamespace(dbObject.getString("localNamespace"));
		typeDefinition.setQueryName(dbObject.getString("queryName"));
		typeDefinition.setDisplayName(dbObject.getString("displayName"));
		typeDefinition.setDescription(dbObject.getString("description"));
		typeDefinition.setBaseTypeId(dbObject.getBaseTypeId("baseId"));
		typeDefinition.setParentTypeId(dbObject.getString("parentId"));
		typeDefinition.setIsCreatable(dbObject.getBoolean2("isCreatable"));
		typeDefinition.setIsFileable(dbObject.getBoolean2("isFileable"));
		typeDefinition.setIsQueryable(dbObject.getBoolean2("isQueryable"));
		typeDefinition.setIsIncludedInSupertypeQuery(dbObject.getBoolean2("isIncludedInSupertypeQuery"));
		typeDefinition.setIsFulltextIndexed(dbObject.getBoolean2("isFulltextIndexed"));
		typeDefinition.setIsControllableAcl(dbObject.getBoolean2("isControllableACL"));
		typeDefinition.setIsControllablePolicy(dbObject.getBoolean2("isControllablePolicy"));

		if (includePropertyDefinitions) {
			ChemistryTypeObject obj = ChemistryTypeObject.wrap((DBObject)dbObject.get("propertyDefinitions"));
			for (String name : obj.keySet()) 
				typeDefinition.addPropertyDefinition(toPropertyDefinition(ChemistryTypeObject.wrap((DBObject)obj.get(name))));

		}
	}

	public static FolderTypeDefinitionImpl toFolderTypeDefinition(ChemistryTypeObject dbObject, boolean includePropertyDefinitions) {
		FolderTypeDefinitionImpl folderTypeDefinition = new FolderTypeDefinitionImpl();
		addBasicPropertyDefinitions(folderTypeDefinition, dbObject, includePropertyDefinitions);
		return folderTypeDefinition;

	}

	public static DocumentTypeDefinitionImpl toDocumentTypeDefinition(ChemistryTypeObject dbObject, boolean includePropertyDefinitions) {
		DocumentTypeDefinitionImpl documentTypeDefinition = new DocumentTypeDefinitionImpl();
		addBasicPropertyDefinitions(documentTypeDefinition, dbObject, includePropertyDefinitions);
		documentTypeDefinition.setContentStreamAllowed(dbObject.getContentStreamAllowed("contentStreamAllowed"));
		documentTypeDefinition.setIsVersionable(dbObject.getBoolean("isVersionable"));
		return documentTypeDefinition;
	}

	public static RelationshipTypeDefinitionImpl toRelationshipTypeDefinition(ChemistryTypeObject dbObject, boolean includePropertyDefinitions) {
		RelationshipTypeDefinitionImpl relationshipTypeDefinition = new RelationshipTypeDefinitionImpl();
		addBasicPropertyDefinitions(relationshipTypeDefinition, dbObject, includePropertyDefinitions);
		relationshipTypeDefinition.setAllowedSourceTypes((List) dbObject.get("allowedSourceTypes"));
		relationshipTypeDefinition.setAllowedTargetTypes((List) dbObject.get("allowedTargetTypes"));
		return relationshipTypeDefinition;
	}

	public static PolicyTypeDefinitionImpl toPolicyTypeDefinition(ChemistryTypeObject dbObject, boolean includePropertyDefinitions) {
		PolicyTypeDefinitionImpl policyTypeDefinition = new PolicyTypeDefinitionImpl();
		addBasicPropertyDefinitions(policyTypeDefinition, dbObject, includePropertyDefinitions);
		return policyTypeDefinition;
	}
	
	public static ItemTypeDefinitionImpl toItemTypeDefinition(ChemistryTypeObject dbObject, boolean includePropertyDefinitions) {
		ItemTypeDefinitionImpl itemTypeDefinition = new ItemTypeDefinitionImpl();
		addBasicPropertyDefinitions(itemTypeDefinition, dbObject, includePropertyDefinitions);
		return itemTypeDefinition;
	}
	
	//SECONDARY
	public static SecondaryTypeDefinitionImpl toSecondaryTypeDefinition(ChemistryTypeObject dbObject, boolean includePropertyDefinitions) {
		SecondaryTypeDefinitionImpl secondaryTypeDefinition = new SecondaryTypeDefinitionImpl();
		addBasicPropertyDefinitions(secondaryTypeDefinition, dbObject, includePropertyDefinitions);
		return secondaryTypeDefinition;
	}
}
