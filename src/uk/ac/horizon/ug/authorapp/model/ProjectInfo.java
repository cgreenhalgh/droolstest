/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

import java.util.LinkedList;
import java.util.List;

import uk.ac.horizon.ug.authorapp.FactStore;

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
	/** fact "stores" */
	protected List<FactStore> factStores = new LinkedList<FactStore>();
	/** default fact store name */
	protected String defaultFactStoreName = "default";
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
		if (clientTypes==null) 
			clientTypes = new LinkedList<ClientTypeInfo>();
		return clientTypes;
	}
	/**
	 * @param clientTypes the clientTypes to set
	 */
	public void setClientTypes(List<ClientTypeInfo> clientTypes) {
		this.clientTypes = clientTypes;
	}
	/**
	 * @return the factStores
	 */
	public List<FactStore> getFactStores() {
		if (factStores==null)
			factStores = new LinkedList<FactStore>();
		return factStores;
	}
	/**
	 * @param factStores the factStores to set
	 */
	public void setFactStores(List<FactStore> factStores) {
		this.factStores = factStores;
	}
	/**
	 * @return the defaultFactStore
	 */
	public String getDefaultFactStoreName() {
		if (defaultFactStoreName==null)
			return "default";
		return defaultFactStoreName;
	}
	/**
	 * @param defaultFactStore the defaultFactStore to set
	 */
	public void setDefaultFactStoreName(String defaultFactStore) {
		this.defaultFactStoreName = defaultFactStore;
	}
	/** get default fact store */
	public FactStore getDefaultFactStore() {
		for (FactStore fs : getFactStores()) {
			if (fs.getName().equals(getDefaultFactStoreName()))
				return fs;
		}
		FactStore fs = new FactStore(getDefaultFactStoreName());
		fs.setChanged(true);
		factStores.add(fs);
		return fs;
	}
}
