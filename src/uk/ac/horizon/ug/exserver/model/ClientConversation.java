/**
 * 
 */
package uk.ac.horizon.ug.exserver.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/** DB state about a sequence of interaction (betwee restarts) with a registered client.
 * Initialised (nominally) by Lobby server.
 * 
 * @author cmg
 *
 */
@Entity
public class ClientConversation {
	/** current client ID (specific to this sequence of interactions with this client, e.g. after restart) */
	@Id
	protected String conversationId;
	/** persistent client ID (across restarts) */
	protected String clientId;
	/** client type */
	protected String clientType;
	/** session id */
	protected String sessionId;
	/** first registered - java time */
	protected long creationTime;
	/** last client contact time */
	protected long lastContactTime;
	/** conversation status */
	protected ConversationStatus status;
	/**
	 * 
	 */
	public ClientConversation() {
		super();
	}
	/**
	 * @return the conversationId
	 */
	public String getConversationId() {
		return conversationId;
	}
	/**
	 * @param conversationId the conversationId to set
	 */
	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}
	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}
	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	/**
	 * @return the clientType
	 */
	public String getClientType() {
		return clientType;
	}
	/**
	 * @param clientType the clientType to set
	 */
	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}
	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return creationTime;
	}
	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	/**
	 * @return the lastContactTime
	 */
	public long getLastContactTime() {
		return lastContactTime;
	}
	/**
	 * @param lastContactTime the lastContactTime to set
	 */
	public void setLastContactTime(long lastContactTime) {
		this.lastContactTime = lastContactTime;
	}
	/**
	 * @return the status
	 */
	public ConversationStatus getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(ConversationStatus status) {
		this.status = status;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ClientConversation [clientId=" + clientId + ", clientType="
				+ clientType + ", conversationId=" + conversationId
				+ ", creationTime=" + creationTime + ", lastContactTime="
				+ lastContactTime + ", sessionId=" + sessionId + ", status="
				+ status + "]";
	}
	
}
