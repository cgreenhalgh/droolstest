/**
 * 
 */
package uk.ac.horizon.ug.exserver.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import uk.ac.horizon.ug.authorapp.model.ClientSubscriptionLifetimeType;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageStatusType;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageType;

/**
 * @author cmg
 *
 */
@Entity
public class MessageToClient {
	/** internal key */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	/** cons */
	public MessageToClient() {
	}
	/**
	 * @param id
	 */
	public MessageToClient(int id) {
		super();
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/** client id */
	protected String clientId;
	/** conversation id */
	protected String conversationId;
	/** session id */
	protected String sessionId;
	/** sequence number */
	protected int seqNo;
	/** type */
	protected MessageType type;
	/** time */
	protected long time;
	/** subscription index - for FACT_ADD/UPD/DEL & SUBS_EN/DIS */
	protected int subsIx;
	/** old value - for FACT_UPD/DEL & UPD/DEL_FACT */
	protected String oldVal;
	/** new value - for FACT_ADD/UPD & ADD/UPD_FACT */
	protected String newVal;
	/** handle - FACT_UPD/DEL, UPD/DEL_FACT (option vs oldVal), ADD/UPD_FACT ACK */
	protected String handle;

	/** lifetime */
	protected ClientSubscriptionLifetimeType lifetime;
	/** sent to client? (time, 0=no) */
	protected long sentToClient;
	/** acked by client? (time, 0=no) */
	protected long ackedByClient;
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
	 * @return the seqNo
	 */
	public int getSeqNo() {
		return seqNo;
	}
	/**
	 * @param seqNo the seqNo to set
	 */
	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}
	/**
	 * @return the type
	 */
	public MessageType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(MessageType type) {
		this.type = type;
	}
	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}
	/**
	 * @return the subsIx
	 */
	public int getSubsIx() {
		return subsIx;
	}
	/**
	 * @param subsIx the subsIx to set
	 */
	public void setSubsIx(int subsIx) {
		this.subsIx = subsIx;
	}
	/**
	 * @return the oldVal
	 */
	public String getOldVal() {
		return oldVal;
	}
	/**
	 * @param oldVal the oldVal to set
	 */
	public void setOldVal(String oldVal) {
		this.oldVal = oldVal;
	}
	/**
	 * @return the newVal
	 */
	public String getNewVal() {
		return newVal;
	}
	/**
	 * @param newVal the newVal to set
	 */
	public void setNewVal(String newVal) {
		this.newVal = newVal;
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
	 * @return the lifetime
	 */
	public ClientSubscriptionLifetimeType getLifetime() {
		return lifetime;
	}
	/**
	 * @param lifetime the lifetime to set
	 */
	public void setLifetime(ClientSubscriptionLifetimeType lifetime) {
		this.lifetime = lifetime;
	}
	/**
	 * @return the sentToClient
	 */
	public long getSentToClient() {
		return sentToClient;
	}
	/**
	 * @param sentToClient the sentToClient to set
	 */
	public void setSentToClient(long sentToClient) {
		this.sentToClient = sentToClient;
	}
	/**
	 * @return the ackedByClient
	 */
	public long getAckedByClient() {
		return ackedByClient;
	}
	/**
	 * @param ackedByClient the ackedByClient to set
	 */
	public void setAckedByClient(long ackedByClient) {
		this.ackedByClient = ackedByClient;
	}
}
