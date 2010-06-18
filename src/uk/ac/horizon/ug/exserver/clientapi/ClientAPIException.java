/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi;

import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageStatusType;

/**
 * @author cmg
 *
 */
public class ClientAPIException extends Exception {
	/** status */
	protected MessageStatusType status;
	
	/**
	 * 
	 */
	public ClientAPIException(MessageStatusType status) {
		super();
		this.status = status;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClientAPIException(MessageStatusType status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	/**
	 * @param message
	 */
	public ClientAPIException(MessageStatusType status, String message) {
		super(message);
		this.status = status;
	}

	/**
	 * @param cause
	 */
	public ClientAPIException(MessageStatusType status, Throwable cause) {
		super(cause);
		this.status = status;
	}

	/**
	 * @return the status
	 */
	public MessageStatusType getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(MessageStatusType status) {
		this.status = status;
	}

}
