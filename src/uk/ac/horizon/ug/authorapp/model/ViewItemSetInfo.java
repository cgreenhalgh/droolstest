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
	protected String factModelName;
	/** view layout name */
	protected String viewLayoutName;
	/* TODO custom item viewer(s) */
	
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
	 * @return the factModelName
	 */
	public String getFactModelName() {
		return factModelName;
	}

	/**
	 * @param factModelName the factModelName to set
	 */
	public void setFactModelName(String factModelName) {
		this.factModelName = factModelName;
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
}
