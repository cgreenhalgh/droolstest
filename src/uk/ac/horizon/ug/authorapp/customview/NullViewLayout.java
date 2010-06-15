/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Component;
import java.util.List;

import uk.ac.horizon.ug.authorapp.model.CustomViewInfo;
import uk.ac.horizon.ug.authorapp.model.ViewLayoutInfo;

/** Excludes all items.
 * 
 * @author cmg
 *
 */
public class NullViewLayout extends AbstractViewLayout {

	public NullViewLayout() {
		super();
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.AbstractViewLayout#doLayout(java.awt.Component, uk.ac.horizon.ug.authorapp.model.CustomViewInfo, java.util.List, java.util.List)
	 */
	@Override
	public void doLayout(Component component, CustomViewInfo customViewInfo,
			List<AbstractViewItem> viewItems,
			List<List<AbstractViewItem>> viewItems2) {
		for (AbstractViewItem viewItem : viewItems) {
			viewItem.setExcludedByLayout(true);
		}
	}

}
