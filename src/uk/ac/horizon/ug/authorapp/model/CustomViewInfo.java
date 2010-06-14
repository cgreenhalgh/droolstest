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
public class CustomViewInfo {
	/** name/ title */
	protected String name;
	/** layer s*/
	protected List<ViewLayerInfo> layers;
	/** layout(s) */
	protected List<ViewLayoutInfo> layouts;
	/** minimum width */
	protected int minimumWidth;
	/** minimum height */
	protected int minimumHeight;
	/**
	 * 
	 */
	public CustomViewInfo() {
		super();
	}

	/**
	 * @param name
	 */
	public CustomViewInfo(String name) {
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
	 * @return the layers
	 */
	public List<ViewLayerInfo> getLayers() {
		if (layers==null)
			layers = new LinkedList<ViewLayerInfo>();
		return layers;
	}
	/** get named layer */
	public ViewLayerInfo getLayer(String name) {
		for (ViewLayerInfo vli : getLayers())
			if (name.equals(vli.getName()))
				return vli;
		return null;
	}

	/**
	 * @param layers the layers to set
	 */
	public void setLayers(List<ViewLayerInfo> layers) {
		this.layers = layers;
	}

	/**
	 * @return the layouts
	 */
	public List<ViewLayoutInfo> getLayouts() {
		if (layouts==null)
			layouts = new LinkedList<ViewLayoutInfo>();
		return layouts;
	}
	/** get named layout */
	public ViewLayoutInfo getLayout(String name) {
		for (ViewLayoutInfo vlo : getLayouts()) 
			if (name.equals(vlo.getName()))
				return vlo;
		return null;
	}

	/**
	 * @param layouts the layouts to set
	 */
	public void setLayouts(List<ViewLayoutInfo> layouts) {
		this.layouts = layouts;
	}

	/**
	 * @return the minimumWidth
	 */
	public int getMinimumWidth() {
		return minimumWidth;
	}

	/**
	 * @param minimumWidth the minimumWidth to set
	 */
	public void setMinimumWidth(int minimumWidth) {
		this.minimumWidth = minimumWidth;
	}

	/**
	 * @return the minimumHeight
	 */
	public int getMinimumHeight() {
		return minimumHeight;
	}

	/**
	 * @param minimumHeight the minimumHeight to set
	 */
	public void setMinimumHeight(int minimumHeight) {
		this.minimumHeight = minimumHeight;
	}
	
}
