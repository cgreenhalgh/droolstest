/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi.protocol;

/**
 * @author cmg
 *
 */
public enum MessageStatusType {
	OK, // not an error
	INVALID_REQUEST, // request not well-formed
	NOT_PERMITTED, // no permission, probably not going to change
	INTERNAL_ERROR, // e.g. exception
	NOT_FOUND, // probably not going to change
	TOO_EARLY, // e.g. before start of session
	TOO_LATE, // e.g. after close of session
	SERVER_BUSY, // overload - hopefully temporary
	REDIRECT_SERVER, // to a fail-over server?!
	REDIRECT_LOBBY // go back to the lobby and restart...
}
