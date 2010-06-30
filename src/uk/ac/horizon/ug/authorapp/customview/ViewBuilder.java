/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.horizon.ug.authorapp.FactStore;
import uk.ac.horizon.ug.authorapp.PluginManager;
import uk.ac.horizon.ug.authorapp.model.CustomViewInfo;
import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.authorapp.model.ViewItemSetInfo;
import uk.ac.horizon.ug.authorapp.model.ViewLayerInfo;
import uk.ac.horizon.ug.authorapp.model.ViewLayoutInfo;
import uk.ac.horizon.ug.exserver.devclient.Fact;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

/** Create a view from info and factstores
 * 
 * @author cmg
 *
 */
public class ViewBuilder {
	static Logger logger = Logger.getLogger(ViewBuilder.class.getName());
	/** cons */
	protected ViewBuilder() {		
	}
	/** factory method */
	public static ViewBuilder getViewBuilder() {
		return new ViewBuilder();
	}
	/** generate view, i.e. ViewItem list */
	public List<List<AbstractViewItem>> getView(Project project, CustomViewInfo customViewInfo, ViewCanvas referenceComponent) {
		List<List<AbstractViewItem>> viewItems2 = new LinkedList<List<AbstractViewItem>>();
		
		for (ViewLayerInfo vli : customViewInfo.getLayers()) {
			if (!vli.isVisible())
				continue;
			for (ViewItemSetInfo visi : vli.getViewItemSets()) {
				// item set...
				List<AbstractViewItem> viewItems = new LinkedList<AbstractViewItem>();
				String factStoreName = visi.getFactStoreName();
				FactStore factStore = null;
				if (factStoreName==null)
					factStore = project.getProjectInfo().getDefaultFactStore();
				else {
					factStore = project.getProjectInfo().getFactStore(factStoreName);
					if (factStore==null) {
						logger.log(Level.WARNING, "Unknown fact store "+factStoreName+" in custom view "+customViewInfo.getName()+" layer "+vli.getName());
						continue;
					}
				}
				List<String> factTypeNames = visi.getTypeNames();
				for (String factTypeName : factTypeNames) {
					// get facts..
					List<Fact> facts = factStore.getFacts(factTypeName);
					for (Fact fact : facts) {
						// create views...
						AbstractViewItem viewItem = getViewItem(project, visi, fact, viewItems2, referenceComponent);
						viewItems.add(viewItem);
					}
				}
				// layout...
				String layoutName = visi.getViewLayoutName();
				if (layoutName==null)
					layoutName = "default";
				ViewLayoutInfo viewLayoutInfo = customViewInfo.getLayout(layoutName);
				if (viewLayoutInfo==null) {
					logger.log(Level.WARNING, "Unknown layout "+layoutName+" in custom view "+customViewInfo.getName()+" layer "+vli.getName());					
				} 

				//TODO: should do all layer's pre layout first, then real layout later
				preLayout(customViewInfo, viewLayoutInfo, viewItems, viewItems2, referenceComponent, factStore);
				doLayout(customViewInfo, viewLayoutInfo, viewItems, viewItems2, referenceComponent, factStore);
				viewItems2.add(viewItems);
			}
		}
		
		return viewItems2;
	}
	/** create ViewItem for fact 
	 * @param viewItems2 */
	private AbstractViewItem getViewItem(Project project, ViewItemSetInfo visi,
			Fact fact, List<List<AbstractViewItem>> viewItems2, Component referenceComponent) {
		DefaultViewItem viewItem = new DefaultViewItem(referenceComponent);
		viewItem.setBaseFact(fact);
		String typeName = fact.getTypeName();
		String instanceName = null;
		TypeDescription typeDesc = project.getTypeDescription(typeName);
		if (typeDesc!=null) {
			String idField = typeDesc.getIdFieldName();
			if (idField==null)
				idField = typeDesc.getSubjectFieldName();
			if (idField!=null) 
				instanceName = (String) fact.getFieldValues().get(idField);
		}
		if (instanceName==null)
			instanceName = fact.toString();
		viewItem.setBaseFactID(instanceName);
		viewItem.setLineWidth(1);
		viewItem.setBorderWidth(3);
		viewItem.setTextRows(new String[] { typeName, instanceName });
		Rectangle textBounds = viewItem.getTextBounds();
//		logger.info("Text bounds: "+textBounds.x+","+textBounds.y+","+textBounds.width+","+textBounds.height);
		if (textBounds.getMaxX()+viewItem.getBorderWidth() > viewItem.getWidth())
			viewItem.setWidth((float)(textBounds.getMaxX()+viewItem.getBorderWidth()));
		if (textBounds.getMaxY()+viewItem.getBorderWidth() > viewItem.getHeight())
			viewItem.setHeight((float)(textBounds.getMaxY()+viewItem.getBorderWidth()));
		return viewItem;
	}
	/** view layouts - by name */
	protected Map<String,AbstractViewLayout> viewLayouts = new HashMap<String,AbstractViewLayout>();
	/** get (cached) view layout 
	 * @param viewLayoutInfo */
	public synchronized AbstractViewLayout getViewLayout(String name, String type, ViewLayoutInfo viewLayoutInfo) {
		AbstractViewLayout viewLayout = viewLayouts.get(name);
		if (viewLayout!=null)
			return viewLayout;
		viewLayout = PluginManager.getPluginManager().newViewLayout(type, viewLayoutInfo);
		viewLayouts.put(name, viewLayout);
		return viewLayout;
	}
	/** do layout of new item(s) 
	 * @param customViewInfo 
	 * @param factStore */
	private void preLayout(CustomViewInfo customViewInfo, ViewLayoutInfo viewLayoutInfo,
			List<AbstractViewItem> viewItems, List<List<AbstractViewItem>> viewItems2, ViewCanvas referenceComponent, FactStore factStore) {
		// include by defulat
		for(AbstractViewItem viewItem : viewItems) 
			viewItem.setExcludedByLayout(false);
		// layout implementation
		AbstractViewLayout viewLayout = null;
		if (viewLayoutInfo==null)
			viewLayout = new NullViewLayout();
		else 
			viewLayout = getViewLayout(viewLayoutInfo.getName(), viewLayoutInfo.getLayoutType(), viewLayoutInfo);
		// first pass
		viewLayout.preLayout(referenceComponent, customViewInfo, viewItems, viewItems2, factStore);		
	}
	/** do layout of new item(s) 
	 * @param customViewInfo 
	 * @param factStore */
	private void doLayout(CustomViewInfo customViewInfo, ViewLayoutInfo viewLayoutInfo,
			List<AbstractViewItem> viewItems, List<List<AbstractViewItem>> viewItems2, ViewCanvas referenceComponent, FactStore factStore) {
		AbstractViewLayout viewLayout = null;
		if (viewLayoutInfo==null)
			viewLayout = new NullViewLayout();
		else 
			viewLayout = getViewLayout(viewLayoutInfo.getName(), viewLayoutInfo.getLayoutType(), viewLayoutInfo);
		// first pass
		viewLayout.doLayout(referenceComponent, customViewInfo, viewItems, viewItems2, factStore);		
	}
}
