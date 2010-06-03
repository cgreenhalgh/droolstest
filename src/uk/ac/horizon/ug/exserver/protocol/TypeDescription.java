/**
 * 
 */
package uk.ac.horizon.ug.exserver.protocol;

import java.util.Map;

import org.drools.lang.descr.TypeFieldDescr;

/** Protocol proxy for drools TypeDefinitionDescr
 * 
 * @author cmg
 *
 */
public class TypeDescription {
	/** type-wide metadata */
	protected Map<String,String> typeMeta;
	/** map of fields */
	protected Map<String,TypeFieldDescription> fields;
	/**
	 * 
	 */
	public TypeDescription() {
		super();
	}
	/**
	 * @param typeMeta
	 * @param fields
	 */
	public TypeDescription(Map<String, String> typeMeta,
			Map<String, TypeFieldDescription> fields) {
		super();
		this.typeMeta = typeMeta;
		this.fields = fields;
	}
	/**
	 * @return the typeMeta
	 */
	public Map<String, String> getTypeMeta() {
		return typeMeta;
	}
	/**
	 * @param typeMeta the typeMeta to set
	 */
	public void setTypeMeta(Map<String, String> typeMeta) {
		this.typeMeta = typeMeta;
	}
	/**
	 * @return the fields
	 */
	public Map<String, TypeFieldDescription> getFields() {
		return fields;
	}
	/**
	 * @param fields the fields to set
	 */
	public void setFields(Map<String, TypeFieldDescription> fields) {
		this.fields = fields;
	}
	
}
