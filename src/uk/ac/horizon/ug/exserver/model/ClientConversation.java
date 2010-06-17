/**
 * 
 */
package uk.ac.horizon.ug.exserver.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

/** DB state about a sequence of interaction (betwee restarts) with a registered client.
 * Initialised (nominally) by Lobby server.
 * 
 * @author cmg
 *
 */
@Entity
public class ClientConversation implements Serializable {
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
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// just conversationId like Drools @key
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((conversationId == null) ? 0 : conversationId.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// just conversationId like Drools @key
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientConversation other = (ClientConversation) obj;
		if (conversationId == null) {
			if (other.conversationId != null)
				return false;
		} else if (!conversationId.equals(other.conversationId))
			return false;
		return true;
	}
	
}
