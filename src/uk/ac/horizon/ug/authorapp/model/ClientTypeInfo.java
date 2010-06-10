/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

import java.util.LinkedList;
import java.util.List;

/** Client type.
 * 
 * @author cmg
 *
 */
public class ClientTypeInfo {
	/** client type name/title */
	protected String name;
	/** list of individual @client TypeDescription names */
	protected List<String> clientTypeNames = new LinkedList<String>();
	/** cons */
	/**
	 * 
	 */
	public ClientTypeInfo() {
		super();
	}
	/**
	 * @param name
	 */
	public ClientTypeInfo(String name) {
		super();
		this.name = name;
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
	 * @return the clientTypeNames
	 */
	public List<String> getClientTypeNames() {
		return clientTypeNames;
	}
	/**
	 * @param clientTypeNames the clientTypeNames to set
	 */
	public void setClientTypeNames(List<String> clientTypeNames) {
		this.clientTypeNames = clientTypeNames;
	}
	
}
