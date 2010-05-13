/**
 * 
 */
package uk.ac.horizon.apptest.model;

import java.util.Date;

/**
 * @author cmg
 *
 */
public class UserContentNotification implements java.io.Serializable {
	/** user id */
	protected String user_id;
	/** content id */
	protected String content_id;
	/** time */
	protected Date timestamp;
	/**
	 * 
	 */
	public UserContentNotification() {
		super();
	}
	/**
	 * @return the user_id
	 */
	public String getUser_id() {
		return user_id;
	}
	/**
	 * @param userId the user_id to set
	 */
	public void setUser_id(String userId) {
		user_id = userId;
	}
	/**
	 * @return the content_id
	 */
	public String getContent_id() {
		return content_id;
	}
	/**
	 * @param contentId the content_id to set
	 */
	public void setContent_id(String contentId) {
		content_id = contentId;
	}
	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
}
