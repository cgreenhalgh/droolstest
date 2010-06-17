/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

/**
 * @author cmg
 *
 */
public enum ClientSubscriptionLifetimeType {
	UNTIL_SENT, UNTIL_ACKNOWLEDGED, // options within conversation
	CONVERSATION, // might request resend within conversation
	CLIENT // kept for all (possible) conversations with client
}
