/**
 * 
 */
package uk.ac.horizon.ug.exserver.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author cmg
 *
 */
@Entity
public class SessionTemplate {
	/** name */
	@Id
	protected String name;
	/** project file URL */
	protected String projectUrl;
	/** cons */
	public SessionTemplate() {	
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
	 * @return the projectUrl
	 */
	public String getProjectUrl() {
		return projectUrl;
	}
	/**
	 * @param projectUrl the projectUrl to set
	 */
	public void setProjectUrl(String projectUrl) {
		this.projectUrl = projectUrl;
	}
}
