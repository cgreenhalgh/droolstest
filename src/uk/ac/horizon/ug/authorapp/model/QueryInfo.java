/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

import java.util.LinkedList;
import java.util.List;

/** Specification of client query for publish to client and filtering client input.
 * 
 * @author cmg
 *
 */
public class QueryInfo {
	/** main type */
	protected String typeName;
	/** sub type name */
	protected List<String> subtypeNames;
	/** constraints */
	protected List<QueryConstraintInfo> constraints;
	/**
	 * 
	 */
	public QueryInfo() {
		super();
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
	 * @return the subtypeNames
	 */
	public List<String> getSubtypeNames() {
		if (subtypeNames==null)
			subtypeNames = new LinkedList<String>();
		return subtypeNames;
	}
	/**
	 * @param subtypeNames the subtypeNames to set
	 */
	public void setSubtypeNames(List<String> subtypeNames) {
		this.subtypeNames = subtypeNames;
	}
	/**
	 * @return the constraints
	 */
	public List<QueryConstraintInfo> getConstraints() {
		if (constraints==null)
			constraints = new LinkedList<QueryConstraintInfo>();
		return constraints;
	}
	/**
	 * @param constraints the constraints to set
	 */
	public void setConstraints(List<QueryConstraintInfo> constraints) {
		this.constraints = constraints;
	}
	
}
