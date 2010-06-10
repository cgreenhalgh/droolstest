/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

import java.util.LinkedList;
import java.util.List;

/**
 * @author cmg
 *
 */
public class ProjectInfo {
	/** project name */
	protected String name;
	/** rule files */
	protected List<String> ruleFiles = new LinkedList<String>();
	/** client types */
	protected List<ClientTypeInfo> clientTypes = new LinkedList<ClientTypeInfo>();
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
	 * @return the ruleFiles
	 */
	public List<String> getRuleFiles() {
		return ruleFiles;
	}
	/**
	 * @param ruleFiles the ruleFiles to set
	 */
	public void setRuleFiles(List<String> ruleFiles) {
		this.ruleFiles = ruleFiles;
	}
	/**
	 * @return the clientTypes
	 */
	public List<ClientTypeInfo> getClientTypes() {
		return clientTypes;
	}
	/**
	 * @param clientTypes the clientTypes to set
	 */
	public void setClientTypes(List<ClientTypeInfo> clientTypes) {
		this.clientTypes = clientTypes;
	}
	
}
