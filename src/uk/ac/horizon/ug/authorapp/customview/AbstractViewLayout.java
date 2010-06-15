/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Component;
import java.util.List;

import uk.ac.horizon.ug.authorapp.model.CustomViewInfo;
import uk.ac.horizon.ug.authorapp.model.ViewLayoutInfo;

/**
 * @author cmg
 *
 */
public abstract class AbstractViewLayout {
	/** config */
	protected ViewLayoutInfo viewLayoutInfo;
	/**
	 * @param viewLayoutInfo
	 */
	public AbstractViewLayout() {
		super();
	}
	
	/**
	 * @return the viewLayoutInfo
	 */
	public ViewLayoutInfo getViewLayoutInfo() {
		return viewLayoutInfo;
	}

	/**
	 * @param viewLayoutInfo the viewLayoutInfo to set
	 */
	public void setViewLayoutInfo(ViewLayoutInfo viewLayoutInfo) {
		this.viewLayoutInfo = viewLayoutInfo;
	}

	/** pre-layout pass over ViewItems (no-op by default) */
	public void preLayout(Component component, CustomViewInfo customViewInfo, 
			List<AbstractViewItem> viewItems, List<List<AbstractViewItem>> viewItems2) {
		// no-op 
	}
	/** pre-layout pass over ViewItems (no-op by default) */
	public abstract void doLayout(Component component, CustomViewInfo customViewInfo, 
			List<AbstractViewItem> viewItems, List<List<AbstractViewItem>> viewItems2);
}
