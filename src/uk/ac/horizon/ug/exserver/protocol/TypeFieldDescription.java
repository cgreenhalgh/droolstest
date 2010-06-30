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
	/** system field metadata keys */
	public static enum FieldMetaKeys { 
		fk, // foreign key 
		type, // type/range
		key, // drools built-in; key
		id, // our own entity PK
		required, 
		subject, object, value, // relationship / property
		content, from, to // message
		};
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
	/** get type metadata */
	public String getMetaType() {
		return fieldMeta.get(FieldMetaKeys.type.name());
	}
	/** get type metadata match specific key */
	public boolean hasMetaType(String name) {
		String type = getMetaType();
		if (type==null)
			return false;
		String types [] = type.split("[\", |]");
		for (int i=0 ;i<types.length; i++) 
			if (types[i].equals(name))
				return true;
		return false;
	}
	/** get fk metadata */
	public String getFk() {
		return fieldMeta.get(FieldMetaKeys.fk.name());
	}
	/** get required metadata */
	public boolean isRequired() {
		return fieldMeta.containsKey(FieldMetaKeys.required.name());
	}
	/** get subject metadata */
	public boolean isSubject() {
		return fieldMeta.containsKey(FieldMetaKeys.subject.name());
	}
	/** get object metadata */
	public boolean isObject() {
		return fieldMeta.containsKey(FieldMetaKeys.object.name());
	}
	/** get value metadata */
	public boolean isValue() {
		return fieldMeta.containsKey(FieldMetaKeys.value.name());
	}
	/** get content metadata */
	public boolean isContent() {
		return fieldMeta.containsKey(FieldMetaKeys.content.name());
	}
	/** get from metadata */
	public boolean isFrom() {
		return fieldMeta.containsKey(FieldMetaKeys.from.name());
	}
	/** is to metadata */
	public boolean isTo() {
		return fieldMeta.containsKey(FieldMetaKeys.to.name());
	}
	/** get to value metadata */
	public String getTo() {
		return fieldMeta.get(FieldMetaKeys.to.name());
	}
	/** get key metadata */
	public boolean isKey() {
		return fieldMeta.containsKey(FieldMetaKeys.key.name());
	}
	/** get id metadata */
	public boolean isId() {
		return fieldMeta.containsKey(FieldMetaKeys.id.name());
	}
}
