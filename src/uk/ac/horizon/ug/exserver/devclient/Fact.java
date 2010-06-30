/**
 * 
 */
package uk.ac.horizon.ug.exserver.devclient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cmg
 *
 */
public class Fact {
	/** package */
	protected String namespace;
	/** name */
	protected String typeName;
	/** type-wide metadata */
	protected Map<String,Object> fieldValues;
	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}
	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}
	/**
	 * @param typeName the typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	/**
	 * @return the fieldValues
	 */
	public Map<String, Object> getFieldValues() {
		if (fieldValues==null)
			fieldValues = new HashMap<String,Object>();
		return fieldValues;		
	}
	/**
	 * @param fieldValues the fieldValues to set
	 */
	public void setFieldValues(Map<String, Object> fieldValues) {
		this.fieldValues = fieldValues;
	}
	/** field access helper */
	public boolean hasField(String name) {
		return fieldValues!=null && fieldValues.containsKey(name);
	}
	/** field access helper */
	public Object getObject(String fieldName) {
		return getFieldValues().get(fieldName);
	}
	/** field access helper */
	public Double getDouble(String fieldName) {
		Object value = getObject(fieldName);
		if (value ==null)
			return null;
		if (value instanceof Number) 
			return ((Number)value).doubleValue();
		return Double.parseDouble(value.toString());
	}
	/** field access helper */
	public String getString(String fieldName) {
		Object value = getObject(fieldName);
		if (value ==null)
			return null;
		return value.toString();
	}	
}
