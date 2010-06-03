/**
 * 
 */
package uk.ac.horizon.ug.exserver.protocol;

import java.util.Map;

/**
 * @author cmg
 *
 */
public class TypeFieldDescription {
	/** type-wide metadata */
	protected Map<String,String> fieldMeta;
	/** type name */
	protected String typeName;

	/**
	 * @return the fieldMeta
	 */
	public Map<String, String> getFieldMeta() {
		return fieldMeta;
	}

	/**
	 * @param fieldMeta the fieldMeta to set
	 */
	public void setFieldMeta(Map<String, String> fieldMeta) {
		this.fieldMeta = fieldMeta;
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

}
