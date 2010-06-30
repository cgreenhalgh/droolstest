/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

import java.util.LinkedList;
import java.util.List;

/** View of (some) Items
 * 
 * @author cmg
 *
 */
public class ViewItemSetInfo {
	/** Fact type name(s) */
	protected List<String> typeNames;
	/** (optional) fact model name */
	protected String factStoreName;
	/** view layout name */
	protected String viewLayoutName;
	/** item type (e.g. Default... Image...) */
	protected String viewItemType;
	/* TODO custom item viewer(s) paramters? */
	
	/**
	 * 
	 */
	public ViewItemSetInfo() {
		super();
	}

	/**
	 * @return the typeNames
	 */
	public List<String> getTypeNames() {
		if (typeNames==null)
			typeNames = new LinkedList<String>();
		return typeNames;
	}

	/**
	 * @param typeNames the typeNames to set
	 */
	public void setTypeNames(List<String> typeNames) {
		this.typeNames = typeNames;
	}

	/**
	 * @return the factStoreName
	 */
	public String getFactStoreName() {
		return factStoreName;
	}

	/**
	 * @param factStoreName the factStoreName to set
	 */
	public void setFactStoreName(String factStoreName) {
		this.factStoreName = factStoreName;
	}

	/**
	 * @return the viewLayoutName
	 */
	public String getViewLayoutName() {
		return viewLayoutName;
	}

	/**
	 * @param viewLayoutName the viewLayoutName to set
	 */
	public void setViewLayoutName(String viewLayoutName) {
		this.viewLayoutName = viewLayoutName;
	}

	public String getViewItemType() {
		return viewItemType;
	}

	public void setViewItemType(String itemType) {
		this.viewItemType = itemType;
	}
}
