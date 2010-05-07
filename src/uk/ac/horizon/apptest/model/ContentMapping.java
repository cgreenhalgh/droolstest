/**
 * 
 */
package uk.ac.horizon.apptest.model;

/**
 * @author cmg
 *
 */
public class ContentMapping {
	/** content id */
	protected String content_id;
	/** region id */
	protected String region_id;
	/** default cons. */
	public ContentMapping() {
		super();
	}
	/**
	 * @return the content_id
	 */
	public String getContent_id() {
		return content_id;
	}
	/**
	 * @param contentId the content_id to set
	 */
	public void setContent_id(String contentId) {
		content_id = contentId;
	}
	/**
	 * @return the region_id
	 */
	public String getRegion_id() {
		return region_id;
	}
	/**
	 * @param regionId the region_id to set
	 */
	public void setRegion_id(String regionId) {
		region_id = regionId;
	}
	
}
