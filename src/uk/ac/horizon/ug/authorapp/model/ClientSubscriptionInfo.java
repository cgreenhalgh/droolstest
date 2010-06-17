/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

/** Subscription for a client (type)
 * @author cmg
 *
 */
public class ClientSubscriptionInfo {
	/** name/title (optional) */
	protected String name;
	/** update permission on matched items */
	protected boolean updateAllowed;
	/** delete permission on matched items */
	protected boolean deleteAllowed;
	/** query/pattern */
	protected QueryInfo pattern;
	/** active (by default/initially) */
	protected boolean active;
	/** priority, larger = higher, 0 = default */
	protected float priority;
	/** send pre-existing matches */
	protected boolean matchExisting;
	/** subscription (itme) lifetime, in particular CONVERSATION or CLIENT */
	protected ClientSubscriptionLifetimeType lifetime;
	
}
