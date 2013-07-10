package org.apache.chemistry.opencmis.inmemory.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.DecimalPrecision;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ChemistryTypeObject extends BasicDBObject {

	private static final long serialVersionUID = 558064620148968264L;

	public static ChemistryTypeObject wrap(DBObject dbObject) {
		return new ChemistryTypeObject(dbObject.toMap());
	}

	public ChemistryTypeObject() {
		super();
	}

	public ChemistryTypeObject(int size) {
		super(size);
	}

	public ChemistryTypeObject(Map m) {
		super(m);
	}

	public ChemistryTypeObject(String key, Object value) {
		super(key, value);
	}

	public BigInteger getBigInteger(String key) {
		Object foo = get(key);
		if (foo == null)
			return null;
		return new BigInteger(foo.toString());
	}

	public BigDecimal getBigDecimal(String key) {
		Object foo = get(key);
		if (foo == null)
			return null;
		return new BigDecimal(foo.toString());
	}

	public Boolean getBoolean2(String key) {
		Object foo = get(key);
		if (foo == null)
			return null;
		return super.getBoolean(key);
	}

	public BaseTypeId getBaseTypeId(String key) {
		String val = super.getString(key);
		if(val==null)
			return null;
		return BaseTypeId.fromValue(val);
	}
	
	public DateTimeResolution getDateTimeResolution(String key) {
		String val = super.getString(key);
		if(val==null)
			return null;
		return DateTimeResolution.fromValue(val);
	}
	
	public DecimalPrecision getDecimalPrecision(String key) {
		String val = super.getString(key);
		if(val==null)
			return null;
		return DecimalPrecision.fromValue(new BigInteger(val));
	}
	
	public PropertyType getPropertyType(String key) {
		String val = super.getString(key);
		if(val==null)
			return null;
		return PropertyType.fromValue(val);
	}
	
	public Cardinality getCardinality(String key) {
		String val = super.getString(key);
		if(val==null)
			return null;
		return Cardinality.fromValue(val);
	}
	
	public Updatability getUpdatability(String key) {
		String val = super.getString(key);
		if(val==null)
			return null;
		return Updatability.fromValue(val);
	}
	
	public ContentStreamAllowed getContentStreamAllowed(String key) {
		String val = super.getString(key);
		if(val==null)
			return null;
		return ContentStreamAllowed.fromValue(val);
	}

	@Override
	public Object put(String key, Object val) {
		if (val == null)
			return super.put(key, val);
		else if (val instanceof BaseTypeId) {
			BaseTypeId baseTypeId = (BaseTypeId) val;
			return super.put(key, baseTypeId.value());
		} else if (val instanceof DateTimeResolution) {
			DateTimeResolution dateTimeResolution = (DateTimeResolution) val;
			return super.put(key, dateTimeResolution == null ? null : dateTimeResolution.value());
		} else if (val instanceof DecimalPrecision) {
			DecimalPrecision decimalPrecision = (DecimalPrecision) val;
			return super.put(key, decimalPrecision == null ? null : decimalPrecision.value().toString());
		} else if (val instanceof BigDecimal) {
			BigDecimal bigDecimal = (BigDecimal) val;
			return super.put(key, bigDecimal == null ? null : bigDecimal.toString());
		} else if (val instanceof BigInteger) {
			BigInteger bigInteger = (BigInteger) val;
			return super.put(key, bigInteger == null ? null : bigInteger.toString());
		} else if (val instanceof PropertyType) {
			PropertyType propertyType = (PropertyType) val;
			return super.put(key, propertyType == null ? null : propertyType.value());
		} else if (val instanceof Cardinality) {
			Cardinality cardinality = (Cardinality) val;
			return super.put(key, cardinality == null ? null : cardinality.value());
		} else if (val instanceof Updatability) {
			Updatability updatability = (Updatability) val;
			return super.put(key, updatability == null ? null : updatability.value());
		}else if (val instanceof ContentStreamAllowed) {
			ContentStreamAllowed contentStreamAllowed = (ContentStreamAllowed) val;
			return super.put(key, contentStreamAllowed == null ? null : contentStreamAllowed.value());
		}

		return super.put(key, val);
	}

}
