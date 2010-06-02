package uk.ac.horizon.ug.exserver.protocol;

import java.util.Arrays;

/** ruleset error object */
public class RulesetError {
	protected String errorType;
	protected int errorLines[];
	protected String message;
	protected String longMessage;
	/**
	 * @param errorLines
	 * @param message
	 */
	public RulesetError(String errorType, int[] errorLines, String message, String longMessage) {
		super();
		this.errorType = errorType;
		this.errorLines = errorLines;
		this.message = message;
		this.longMessage = longMessage;
	}
	/**
	 * @return the errorType
	 */
	public String getErrorType() {
		return errorType;
	}
	/**
	 * @param errorType the errorType to set
	 */
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}
	/**
	 * 
	 */
	public RulesetError() {
		super();
	}
	/**
	 * @return the errorLines
	 */
	public int[] getErrorLines() {
		return errorLines;
	}
	/**
	 * @param errorLines the errorLines to set
	 */
	public void setErrorLines(int[] errorLines) {
		this.errorLines = errorLines;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * @return the longMessage
	 */
	public String getLongMessage() {
		return longMessage;
	}
	/**
	 * @param longMessage the longMessage to set
	 */
	public void setLongMessage(String longMessage) {
		this.longMessage = longMessage;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RulesetError [errorLines=" + Arrays.toString(errorLines)
				+ ", errorType=" + errorType + ", message=" + message + "]";
	}
	              
}