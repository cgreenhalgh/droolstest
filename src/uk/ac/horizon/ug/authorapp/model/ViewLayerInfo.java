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
public class ViewLayerInfo {
	/** name/ title */
	protected String name;
	/** visible */
	protected boolean visible;
	/** view item(s) */
	protected List<ViewItemSetInfo> viewItemSets;
	
	/**
	 * 
	 */
	public ViewLayerInfo() {
		super();
	}

	/**
	 * @param name
	 */
	public ViewLayerInfo(String name) {
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
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the viewItemSets
	 */
	public List<ViewItemSetInfo> getViewItemSets() {
		if (viewItemSets==null)
			viewItemSets = new LinkedList<ViewItemSetInfo>();
		return viewItemSets;
	}

	/**
	 * @param viewItemSets the viewItemSets to set
	 */
	public void setViewItemSets(List<ViewItemSetInfo> viewItemSets) {
		this.viewItemSets = viewItemSets;
	}
	
}
