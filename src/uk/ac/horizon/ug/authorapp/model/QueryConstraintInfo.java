/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

/** Client Query Constraint - see QueryInfo
 * 
 * @author cmg
 *
 */
public class QueryConstraintInfo {
	/** field name */
	protected String fieldName;
	/** constraint kind */
	protected QueryConstraintType constraintType;
	/** constraint value / parameter (depends on kind) */
	protected String parameter;
	/**
	 * 
	 */
	public QueryConstraintInfo() {
		super();
	}
	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}
	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	/**
	 * @return the constraintType
	 */
	public QueryConstraintType getConstraintType() {
		return constraintType;
	}
	/**
	 * @param constraintType the constraintType to set
	 */
	public void setConstraintType(QueryConstraintType constraintType) {
		this.constraintType = constraintType;
	}
	/**
	 * @return the parameter
	 */
	public String getParameter() {
		return parameter;
	}
	/**
	 * @param parameter the parameter to set
	 */
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	
}
