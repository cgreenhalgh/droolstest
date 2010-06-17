/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

/** Something a client is allowed to publish.
 * 
 * @author cmg
 *
 */
public class ClientPublicationFilterInfo {
	/** lifetime of facts matching this filter */
	protected ClientPublicationLifetimeType lifetime;
	/** Query */
	protected QueryInfo pattern;
	/**
	 * 
	 */
	public ClientPublicationFilterInfo() {
		super();
	}
	/**
	 * @return the lifetime
	 */
	public ClientPublicationLifetimeType getLifetime() {
		return lifetime;
	}
	/**
	 * @param lifetime the lifetime to set
	 */
	public void setLifetime(ClientPublicationLifetimeType lifetime) {
		this.lifetime = lifetime;
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
	
}
