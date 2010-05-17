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
	/** rule set(s) */
	protected String rulesetUrls[];
	/** fact files */
	protected String dataUrls[];
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
	 * @return the rulesetUrls
	 */
	public String[] getRulesetUrls() {
		return rulesetUrls;
	}
	/**
	 * @param rulesetUrls the rulesetUrls to set
	 */
	public void setRulesetUrls(String[] rulesetUrls) {
		this.rulesetUrls = rulesetUrls;
	}
	/**
	 * @return the dataUrls
	 */
	public String[] getDataUrls() {
		return dataUrls;
	}
	/**
	 * @param dataUrls the dataUrls to set
	 */
	public void setDataUrls(String[] dataUrls) {
		this.dataUrls = dataUrls;
	}
	
}
