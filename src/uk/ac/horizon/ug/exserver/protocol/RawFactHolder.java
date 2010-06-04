/**
 * 
 */
package uk.ac.horizon.ug.exserver.protocol;

import java.util.Map;

import org.drools.runtime.rule.FactHandle;

import org.drools.definition.type.FactType;


/**
 * @author cmg
 *
 */
public class RawFactHolder {
	/** fact handle */
	protected String handle;
	/** object */
	protected Object fact;
	/** operation */
	protected Operation operation = Operation.add;
	/** cons 
	 */
	public RawFactHolder() {
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
	public String toString() {
		return operation+" "+handle+" "+fact;
	}
}
