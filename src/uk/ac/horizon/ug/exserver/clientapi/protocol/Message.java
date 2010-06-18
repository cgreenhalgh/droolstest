/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi.protocol;

/**
 * @author cmg
 *
 */
public class Message {
	/** sequence number */
	protected int seqNo;
	/** type */
	protected MessageType type;
	/** time */
	protected Long time;
	/** subscription index - for FACT_ADD/UPD/DEL & SUBS_EN/DIS */
	protected Integer subsIx;
	/** old value - for FACT_UPD/DEL & UPD/DEL_FACT */
	protected String oldVal;
	/** new value - for FACT_ADD/UPD & ADD/UPD_FACT */
	protected String newVal;
	/** ack seq - for ACK, ERROR and POLL_RESP */
	protected Integer ackSeq;
	/** messages to follow - for POLL/POLL_RESP */
	protected Integer toFollow;
	/** error code - for ERROR / ACK */
	protected MessageStatusType status;
	/** error message - for ERROR */
	protected String errorMsg;
	/** cons */
	public Message() {		
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
	public Long getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(Long time) {
		this.time = time;
	}
	/**
	 * @return the subsIx
	 */
	public Integer getSubsIx() {
		return subsIx;
	}
	/**
	 * @param subsIx the subsIx to set
	 */
	public void setSubsIx(Integer subsIx) {
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
	 * @return the ackSeq
	 */
	public Integer getAckSeq() {
		return ackSeq;
	}
	/**
	 * @param ackSeq the ackSeq to set
	 */
	public void setAckSeq(Integer ackSeq) {
		this.ackSeq = ackSeq;
	}
	/**
	 * @return the toFollow
	 */
	public Integer getToFollow() {
		return toFollow;
	}
	/**
	 * @param toFollow the toFollow to set
	 */
	public void setToFollow(Integer toFollow) {
		this.toFollow = toFollow;
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
	/**
	 * @return the errorMsg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}
	/**
	 * @param errorMsg the errorMsg to set
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
}
