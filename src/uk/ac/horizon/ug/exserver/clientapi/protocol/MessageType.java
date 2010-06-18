/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi.protocol;

/**
 * @author cmg
 *
 */
public enum MessageType {
	NEW_CONV(false, true), // new conversation 
	FACT_EX(false, true), // fact already exists (matching a subscription)
	FACT_ADD(false, true), // fact added (matching a subscription)
	FACT_UPD(false, true), // fact updated (matching a subscription)
	FACT_DEL(false, true), // fact deleted (matching a subscription)
	POLL_RESP(false, true), // response to poll (e.g. no. messages still unsent)
	POLL(true, false), // poll request 
	ACK(true, true), // acknowledge message
	
	ADD_FACT(true, false), // request to add fact
	UPD_FACT(true, false), // request to update fact
	DEL_FACT(true, false), // request to delete fact
	ERROR(false, true), // error response, e.g. to add/update/delete request
	SUBS_EN(true, false), // enable a subscription
	SUBS_DIS(true, false) // disable a subscription
	;
	MessageType(boolean toServer, boolean toClient) {
		this.toServer = toServer;
		this.toClient = toClient;
	}
	private boolean toServer;
	public boolean toServer() { return toServer; }
	private boolean toClient;
	public boolean toClient() { return toClient; }
}
