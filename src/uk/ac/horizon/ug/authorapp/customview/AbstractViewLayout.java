/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Component;
import java.util.List;

import uk.ac.horizon.ug.authorapp.FactStore;
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
	public void preLayout(AbstractViewItemCanvas component, CustomViewInfo customViewInfo, 
			List<AbstractViewItem> viewItems, List<List<AbstractViewItem>> viewItems2, FactStore factStore) {
		// no-op
	}
	/** pre-layout pass over ViewItems (no-op by default) */
	public abstract void doLayout(ViewCanvas component, CustomViewInfo customViewInfo, 
			List<AbstractViewItem> viewItems, List<List<AbstractViewItem>> viewItems2, FactStore factStore);
	/** mouse interaction handler (no-op by default) */
	public void handleItemMove(ViewCanvas component, AbstractViewItem viewItem, int dx, int dy) {
		// default: no-op
	}
	/** mouse interaction handler (no-op by default) */
	public void handleItemDragOff(ViewCanvas component, AbstractViewItem viewItem) {
		// default: no-op
	}
	/** mouse interaction handler (no-op by default) */
	public void handleItemDragOn(ViewCanvas component, AbstractViewItem viewItem, int x, int y) {
		// default: no-op
	}
}
