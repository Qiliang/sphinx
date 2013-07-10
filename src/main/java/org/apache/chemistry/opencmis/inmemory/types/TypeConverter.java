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
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class TypeConverter {

	public static DBObject toDBObject(TypeDefinition typeDefinition) {
		ChemistryTypeObject dbObject = new ChemistryTypeObject();
		dbObject.append("_id", typeDefinition.getId())
				.append("localName", typeDefinition.getLocalName())
				.append("localNamespace", typeDefinition.getLocalNamespace())
				.append("queryName", typeDefinition.getQueryName())
				.append("displayName", typeDefinition.getDisplayName())
				.append("description", typeDefinition.getDescription())
				.append("baseId", typeDefinition.getBaseTypeId())
				.append("parentId", typeDefinition.getParentTypeId())
				.append("isCreatable", typeDefinition.isCreatable())
				.append("isFileable", typeDefinition.isFileable())
				.append("isQueryable", typeDefinition.isQueryable())
				.append("isIncludedInSupertypeQuery", typeDefinition.isIncludedInSupertypeQuery())
				.append("isFulltextIndexed", typeDefinition.isFulltextIndexed())
				.append("isControllableACL", typeDefinition.isControllableAcl())
				.append("isControllablePolicy", typeDefinition.isControllablePolicy());

		ChemistryTypeObject propertyDefinitions = new ChemistryTypeObject();

		for (String name : typeDefinition.getPropertyDefinitions().keySet()) {
			PropertyDefinition<?> propertyDefinition = typeDefinition.getPropertyDefinitions().get(name);
			propertyDefinitions.append(name, toDBObject(propertyDefinition));
		}

		dbObject.append("propertyDefinitions", propertyDefinitions);

		return dbObject;
	}

	private static ChemistryTypeObject toBSON(PropertyBooleanDefinition propertyBooleanDefinition) {
		ChemistryTypeObject ChemistryDBObject = newBasicProperties(propertyBooleanDefinition);
		return ChemistryDBObject;
	}

	private static ChemistryTypeObject toBSON(PropertyDateTimeDefinition propertyDateTimeDefinition) {
		ChemistryTypeObject ChemistryDBObject = newBasicProperties(propertyDateTimeDefinition);
		ChemistryDBObject.append("dateTimeResolution", propertyDateTimeDefinition.getDateTimeResolution());
		return ChemistryDBObject;
	}

	private static ChemistryTypeObject toBSON(PropertyDecimalDefinition propertyDecimalDefinition) {
		ChemistryTypeObject ChemistryDBObject = newBasicProperties(propertyDecimalDefinition);
		ChemistryDBObject.append("minValue", propertyDecimalDefinition.getMinValue());
		ChemistryDBObject.append("maxValue", propertyDecimalDefinition.getMaxValue());
		ChemistryDBObject.append("precision", propertyDecimalDefinition.getPrecision());
		return ChemistryDBObject;
	}

	private static ChemistryTypeObject toBSON(PropertyHtmlDefinition propertyHtmlDefinition) {
		ChemistryTypeObject ChemistryDBObject = newBasicProperties(propertyHtmlDefinition);
		return ChemistryDBObject;
	}

	private static ChemistryTypeObject toBSON(PropertyIdDefinition propertyIdDefinition) {
		ChemistryTypeObject ChemistryDBObject = newBasicProperties(propertyIdDefinition);
		return ChemistryDBObject;
	}

	private static ChemistryTypeObject toBSON(PropertyIntegerDefinition propertyIntegerDefinition) {
		ChemistryTypeObject ChemistryDBObject = newBasicProperties(propertyIntegerDefinition);
		ChemistryDBObject.append("minValue", propertyIntegerDefinition.getMinValue());
		ChemistryDBObject.append("maxValue", propertyIntegerDefinition.getMaxValue());
		return ChemistryDBObject;
	}

	private static ChemistryTypeObject toBSON(PropertyStringDefinition propertyStringDefinition) {
		ChemistryTypeObject ChemistryDBObject = newBasicProperties(propertyStringDefinition);
		ChemistryDBObject.append("maxLength", propertyStringDefinition.getMaxLength());
		return ChemistryDBObject;
	}

	private static ChemistryTypeObject toBSON(PropertyUriDefinition propertyUriDefinition) {
		ChemistryTypeObject ChemistryDBObject = newBasicProperties(propertyUriDefinition);
		return ChemistryDBObject;
	}

	private static ChemistryTypeObject newBasicProperties(PropertyDefinition propertyDefinition) {

		ChemistryTypeObject dbObject = new ChemistryTypeObject();
		dbObject.append("id", propertyDefinition.getId())
				.append("localName", propertyDefinition.getLocalName())
				.append("localNamespace", propertyDefinition.getLocalNamespace())
				.append("queryName", propertyDefinition.getQueryName())
				.append("displayName", propertyDefinition.getDisplayName())
				.append("description", propertyDefinition.getDescription())
				.append("propertyType", propertyDefinition.getPropertyType())
				.append("cardinality", propertyDefinition.getCardinality())
				.append("choiceList", toDBObject(propertyDefinition.getChoices()))
				.append("defaultValue", propertyDefinition.getDefaultValue())
				.append("updatability", propertyDefinition.getUpdatability())
				.append("isInherited", propertyDefinition.isInherited())
				.append("isQueryable", propertyDefinition.isQueryable())
				.append("isOrderable", propertyDefinition.isOrderable())
				.append("isRequired", propertyDefinition.isRequired())
				.append("isOpenChoice", propertyDefinition.isOpenChoice());

		return dbObject;
	}

	public static <T> DBObject toDBObject(PropertyDefinition<T> propertyDefinition) {

		switch (propertyDefinition.getPropertyType()) {
		case BOOLEAN:
			return toBSON((PropertyBooleanDefinition) propertyDefinition);
		case DATETIME:
			return toBSON((PropertyDateTimeDefinition) propertyDefinition);
		case DECIMAL:
			return toBSON((PropertyDecimalDefinition) propertyDefinition);
		case HTML:
			return toBSON((PropertyHtmlDefinition) propertyDefinition);
		case ID:
			return toBSON((PropertyIdDefinition) propertyDefinition);
		case INTEGER:
			return toBSON((PropertyIntegerDefinition) propertyDefinition);
		case STRING:
			return toBSON((PropertyStringDefinition) propertyDefinition);
		case URI:
			return toBSON((PropertyUriDefinition) propertyDefinition);
		}

		return null;
	}

	private static <T> BasicDBList toDBObject(List<Choice<T>> choiceList) {
		if (choiceList == null)
			return null;
		BasicDBList dbList = new BasicDBList();
		for (Choice<?> choice : choiceList) {
			ChemistryTypeObject dbObject = new ChemistryTypeObject();
			dbObject.append("displayName", choice.getDisplayName())
					.append("value", choice.getValue())
					.append("choice", toDBObject(choice.getChoice()));

		}

		return dbList;
	}

}
