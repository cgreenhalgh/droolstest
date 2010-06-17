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
	protected ClientSubscriptionLifetimeType lifetime = ClientSubscriptionLifetimeType.CLIENT;
	/**
	 * 
	 */
	public ClientSubscriptionInfo() {
		super();
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the updateAllowed
	 */
	public boolean isUpdateAllowed() {
		return updateAllowed;
	}
	/**
	 * @param updateAllowed the updateAllowed to set
	 */
	public void setUpdateAllowed(boolean updateAllowed) {
		this.updateAllowed = updateAllowed;
	}
	/**
	 * @return the deleteAllowed
	 */
	public boolean isDeleteAllowed() {
		return deleteAllowed;
	}
	/**
	 * @param deleteAllowed the deleteAllowed to set
	 */
	public void setDeleteAllowed(boolean deleteAllowed) {
		this.deleteAllowed = deleteAllowed;
	}
	/**
	 * @return the pattern
	 */
	public QueryInfo getPattern() {
		return pattern;
	}
	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(QueryInfo pattern) {
		this.pattern = pattern;
	}
	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}
	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	/**
	 * @return the priority
	 */
	public float getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(float priority) {
		this.priority = priority;
	}
	/**
	 * @return the matchExisting
	 */
	public boolean isMatchExisting() {
		return matchExisting;
	}
	/**
	 * @param matchExisting the matchExisting to set
	 */
	public void setMatchExisting(boolean matchExisting) {
		this.matchExisting = matchExisting;
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
	
}
