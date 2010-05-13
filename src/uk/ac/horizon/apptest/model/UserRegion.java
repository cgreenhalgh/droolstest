/**
 * 
 */
package uk.ac.horizon.apptest.model;

/**
 * @author cmg
 *
 */
public class UserRegion implements java.io.Serializable {
	/** user id */
	protected String user_id;
	/** region id */
	protected String region_id;
	/** cons */
	public UserRegion() {
		super();
	}
	/**
	 * @return the user_id
	 */
	public String getUser_id() {
		return user_id;
	}
	/**
	 * @param userId the user_id to set
	 */
	public void setUser_id(String userId) {
		user_id = userId;
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
