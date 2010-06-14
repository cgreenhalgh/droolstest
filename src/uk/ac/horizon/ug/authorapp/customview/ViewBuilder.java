/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.horizon.ug.authorapp.FactStore;
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
	public ViewBuilder getViewBuilder() {
		return new ViewBuilder();
	}
	/** generate view, i.e. ViewItem list */
	public List<List<AbstractViewItem>> getView(Project project, CustomViewInfo customViewInfo) {
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
						AbstractViewItem viewItem = getViewItem(project, visi, fact, viewItems2);
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
				} else {
					doLayout(customViewInfo, viewLayoutInfo, viewItems, viewItems2);
				}
				viewItems2.add(viewItems);
			}
		}
		
		return viewItems2;
	}
	/** create ViewItem for fact 
	 * @param viewItems2 */
	private AbstractViewItem getViewItem(Project project, ViewItemSetInfo visi,
			Fact fact, List<List<AbstractViewItem>> viewItems2) {
		DefaultViewItem viewItem = new DefaultViewItem();
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
		viewItem.setTextRows(new String[] { typeName, instanceName });
		return viewItem;
	}
	/** do layout of new item(s) 
	 * @param customViewInfo */
	private void doLayout(CustomViewInfo customViewInfo, ViewLayoutInfo viewLayoutInfo,
			List<AbstractViewItem> viewItems, List<List<AbstractViewItem>> viewItems2) {
		// TODO Auto-generated method stub
		
	}
}
