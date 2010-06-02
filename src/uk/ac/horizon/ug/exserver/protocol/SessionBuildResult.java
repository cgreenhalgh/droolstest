package uk.ac.horizon.ug.exserver.protocol;

import java.util.Arrays;


/** result class */
public class SessionBuildResult {
	protected String id;
	protected String status;
	protected RulesetErrors errors[];
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the errors
	 */
	public RulesetErrors[] getErrors() {
		return errors;
	}
	/**
	 * @param errors the errors to set
	 */
	public void setErrors(RulesetErrors[] errors) {
		this.errors = errors;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SessionBuildResult [errors=" + Arrays.toString(errors)
				+ ", id=" + id + ", status=" + status + "]";
	}
	
}