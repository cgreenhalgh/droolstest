/**
 * 
 */
package uk.ac.horizon.ug.exserver.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author cmg
 *
 */
@Entity
public class Session {
	/** unique id */
	@Id
	protected String id;
	/** drools id */
	protected int droolsId;
	/** rule set(s) */
	protected String rulesetUrls[];
	/** template name */
	protected String templateName;
	/** created date */
	protected Date createdDate;
	/** drools session type */
	protected SessionType sessionType = SessionType.JPA_SERIALIZED;
	/** 
	 * cons
	 */
	public Session() {
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
	 * @return the droolsId
	 */
	public int getDroolsId() {
		return droolsId;
	}
	/**
	 * @param droolsId the droolsId to set
	 */
	public void setDroolsId(int droolsId) {
		this.droolsId = droolsId;
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
	 * @return the templateName
	 */
	public String getTemplateName() {
		return templateName;
	}
	/**
	 * @param templateName the templateName to set
	 */
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}
	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	/**
	 * @return the sessionType
	 */
	public SessionType getSessionType() {
		return sessionType;
	}
	/**
	 * @param sessionType the sessionType to set
	 */
	public void setSessionType(SessionType sessionType) {
		this.sessionType = sessionType;
	}
	
}
