/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

/**
 * @author cmg
 *
 */
public enum ClientSubscriptionScopeType {
	CONVERSATION_PUBLICATION, // client-specific cache of facts published in this conversation
	CLIENT_PUBLICATION, // client-specific cache of facts published (all converations within this session)
	SESSION // whole session
}
