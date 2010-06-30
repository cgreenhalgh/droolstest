/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Component;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.horizon.ug.authorapp.ClientTypePanel;
import uk.ac.horizon.ug.authorapp.FactStore;
import uk.ac.horizon.ug.authorapp.model.CustomViewInfo;
import uk.ac.horizon.ug.exserver.devclient.Fact;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription;

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
		nextitem:
		for (AbstractViewItem viewItem : viewItems) {
			if (viewItem.isExcludedByLayout())
				continue;
			// could be MapTileURL property?
			Fact baseFact = viewItem.getBaseFact();
			TypeDescription type = factStore.getType(baseFact.getTypeName());
			if (type!=null) {
				for (Map.Entry<String,TypeFieldDescription> field: type.getFields().entrySet()) {
					if (field.getValue().hasMetaType("MapTileURL")) {
						doLayoutForMapTile(canvas, customViewInfo, viewItem, baseFact.getString(field.getKey()));
						continue nextitem;
					}
				}
			}
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
	/** do Layout for a Fact with a MapTileURL property */
	private void doLayoutForMapTile(ViewCanvas canvas,
			CustomViewInfo customViewInfo, AbstractViewItem viewItem,
			String mapTileURL) {
		logger.info("Layout MapTile: "+mapTileURL);
		try {
			if (mapTileURL==null) {
				viewItem.setExcludedByLayout(true);
				return;
			}
			int qix = mapTileURL.indexOf('?');
			if (qix<0) {
				viewItem.setExcludedByLayout(true);
				return;			
			}
			String params [] = mapTileURL.substring(qix+1).split("[&]");
			double latitude = 0, longitude = 0;
			int zoom = -1;
			int width = -1, height = -1;
			boolean foundLatitude = false, foundLongitude = false, foundZoom = false, foundWidth = false, foundHeight = false;
			for (int i=0; i<params.length; i++) {
				int eix = params[i].indexOf('=');
				if (eix<0)
					continue;
				String pname = params[i].substring(0, eix);
				String pval = params[i].substring(eix+1);
				if (pname.equals("center")) {
					String coords[] = pval.split("[,]");
					latitude = Double.parseDouble(coords[0]);
					foundLatitude = true;
					longitude = Double.parseDouble(coords[1]);
					foundLongitude = true;
				} else if (pname.equals("zoom")) {
					zoom = Integer.parseInt(pval);
					foundZoom = true;
				} else if (pname.equals("size")) {
					String coords[] = pval.split("[x]");
					width = Integer.parseInt(coords[0]);
					foundWidth = true;
					height = Integer.parseInt(coords[1]);					
					foundHeight = true;
				}
			}
			if (!foundLatitude || !foundLongitude || !foundZoom || !foundWidth || !foundHeight) {
				logger.log(Level.WARNING, "Could not find required parameters in URL "+mapTileURL);
				viewItem.setExcludedByLayout(true);
				return;
			}

			double googleX = longitudeToGoogleX(longitude);
			double googleY = latitudeToGoogleY(latitude);
			
			// width -> delta x
			double pixelsAtZoom = 256*Math.pow(2, zoom);
			double deltaX = width/2.0/pixelsAtZoom;
			
			// height -> top/bottom latitude
			double deltaY = height/2.0/pixelsAtZoom;
			
			double x = canvas.getMinx()+(googleX-deltaX)*(canvas.getMaxx()-canvas.getMinx());
			// upside down?
			double y = canvas.getMiny()+(googleY+deltaY)*(canvas.getMaxy()-canvas.getMiny());
			
			viewItem.setX((float)x);
			viewItem.setY((float)y);

			double w = canvas.getMinx()+(2*deltaX)*(canvas.getMaxx()-canvas.getMinx());
			// upside down?
			double h = canvas.getMiny()+(2*deltaY)*(canvas.getMaxy()-canvas.getMiny());
			
			viewItem.setWidth((float)w);
			viewItem.setHeight((float)h);

			logger.info("Map placed at "+x+","+y+" "+w+"x"+h);
		} catch(Exception e) {
			logger.log(Level.WARNING, "Error parsing map URL "+mapTileURL, e);
			viewItem.setExcludedByLayout(true);
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
