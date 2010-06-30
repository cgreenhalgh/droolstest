/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Component;
import java.util.List;
import java.util.logging.Logger;

import uk.ac.horizon.ug.authorapp.ClientTypePanel;
import uk.ac.horizon.ug.authorapp.FactStore;
import uk.ac.horizon.ug.authorapp.model.CustomViewInfo;
import uk.ac.horizon.ug.exserver.devclient.Fact;

/**
 * @author cmg
 *
 */
public class GeoPositionViewLayout extends AbstractViewLayout {
	static Logger logger = Logger.getLogger(GeoPositionViewLayout.class.getName());

	/**
	 * 
	 */
	public GeoPositionViewLayout() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.AbstractViewLayout#doLayout(java.awt.Component, uk.ac.horizon.ug.authorapp.model.CustomViewInfo, java.util.List, java.util.List)
	 */
	@Override
	public void doLayout(ViewCanvas canvas, CustomViewInfo customViewInfo,
			List<AbstractViewItem> viewItems,
			List<List<AbstractViewItem>> viewItems2,  FactStore factStore) {
		for (AbstractViewItem viewItem : viewItems) {
			if (viewItem.isExcludedByLayout())
				continue;
			String id = viewItem.getBaseFactID();
			if (id==null) {
				viewItem.setExcludedByLayout(true);
				continue;
			}
			Fact position = factStore.getFact("GeoPosition", "subjectId", id);
			if (position==null) {
				viewItem.setExcludedByLayout(true);
				continue;
			}
			Double latitude = position.getDouble("latitude");
			Double longitude = position.getDouble("longitude");
			if (latitude==null || longitude==null) {
				viewItem.setExcludedByLayout(true);
				continue;
			}
			logger.info("Found GeoPosition for "+id+": latitude="+latitude+", longitude="+longitude);
			double googleX = longitudeToGoogleX(longitude);
			double googleY = latitudeToGoogleY(latitude);
			
			double x = canvas.getMinx()+googleX*(canvas.getMaxx()-canvas.getMinx());
			// upside down?
			double y = canvas.getMiny()+googleY*(canvas.getMaxy()-canvas.getMiny());
			
			viewItem.setX((float)x);
			viewItem.setY((float)y);
			logger.info("- placed at "+x+","+y);
		}
	}

	/** degrees longitude to 0-1 google map range */
	public static double longitudeToGoogleX(double longitude) {
		double x = longitude/360.0+0.5;
		return x-Math.floor(x); // clamp [0..1)
	}
	/** degrees latitude to 0-1 google map range */
	public static double latitudeToGoogleY(double latitude) {
		double radians = latitude*Math.PI/180;
		double y = Math.log(Math.tan(radians)+1/Math.cos(radians));
		return (1-(y/Math.PI))/2.0;
	}
}
