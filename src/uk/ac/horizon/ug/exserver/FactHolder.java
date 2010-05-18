/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.Map;

import org.drools.runtime.rule.FactHandle;

import org.drools.definition.type.FactType;

/**
 * @author cmg
 *
 */
public class FactHolder {
    public static enum Operation { add, update, delete };
	/** fact handle */
	protected String handle;
	/** object */
	protected Object fact;
	/** fact (class) name */
	protected String name;
	/** fact fields */
	protected Map<String,Object> fields;
	/** operation */
	protected Operation operation = Operation.add;
	/** fact type ? */
	transient protected FactType factType;
	/** cons 
	 */
	public FactHolder() {
	}
	/**
	 * @return the handle
	 */
	public String getHandle() {
		return handle;
	}
	/**
	 * @param handle the handle to set
	 */
	public void setHandle(String handle) {
		this.handle = handle;
	}
	/**
	 * @return the fact
	 */
	public Object getFact() {
		return fact;
	}
	/**
	 * @param fact the fact to set
	 */
	public void setFact(Object fact) {
		this.fact = fact;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the fields
	 */
	public Map<String, Object> getFields() {
		return fields;
	}
	/**
	 * @param fields the fields to set
	 */
	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}
	/**
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}
	/**
	 * @param operation the operation to set
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	/**
	 * @return the factType
	 */
	public FactType getFactType() {
		return factType;
	}
	/**
	 * @param factType the factType to set
	 */
	public void setFactType(FactType factType) {
		this.factType = factType;
	}
	public String toString() {
		return operation+" "+handle+" "+name+":"+fields;
	}
}
