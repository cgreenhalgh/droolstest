/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cmg
 *
 */
public class ViewLayoutInfo {
	/** name */
	protected String name;
	/** layout type */
	protected String layoutType;
	/** properties */
	protected Map<String,String> properties;
	/**
	 * 
	 */
	public ViewLayoutInfo() {
		super();
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
	 * @return the layoutType
	 */
	public String getLayoutType() {
		return layoutType;
	}
	/**
	 * @param layoutType the layoutType to set
	 */
	public void setLayoutType(String layoutType) {
		this.layoutType = layoutType;
	}
	/**
	 * @return the properties
	 */
	public Map<String, String> getProperties() {
		if (properties==null)
			properties = new HashMap<String,String>();
		return properties;
	}
	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
}
