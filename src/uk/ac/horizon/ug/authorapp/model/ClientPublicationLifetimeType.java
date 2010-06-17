/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

/** e.g. of facts published by client.
 * 
 * @author cmg
 *
 */
public enum ClientPublicationLifetimeType {
	PROHIBITED, // not allowed to exist :-)
	CONVERSATION, // this conversation of this client
	CLIENT, // all converations of this client
	SESSION, // this session
	UNLIMITED // all sessions...
}
