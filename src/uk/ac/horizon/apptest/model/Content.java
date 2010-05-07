/**
 * 
 */
package uk.ac.horizon.apptest.model;

/**
 * @author cmg
 *
 */
public class Content {
	/** content id */
	protected String id;
	/** description */
	protected String description;
	/** default cons */
	public Content() {
		super();
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
}
