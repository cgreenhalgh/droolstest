/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Component;
import java.awt.geom.Rectangle2D;
//import java.awt.geom.Rectangle2D.Double;
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
	private static final double MINIMUM_WIDTH = Math.pow(0.5, 30); // zoom 30?!
	static Logger logger = Logger.getLogger(GeoPositionViewLayout.class.getName());
	/** doing pre-layout? */
	private boolean isPre = false;
	/** min/max */
	private Rectangle2D range;
	/**
	 * 
	 */
	public GeoPositionViewLayout() {
		// TODO Auto-generated constructor stub
	}
	/** get google x/y range for item */
	public static Rectangle2D getGoogleRange(AbstractViewItem viewItem, FactStore factStore) {
		// could be MapTileURL property?
		Fact baseFact = viewItem.getBaseFact();
		TypeDescription type = factStore.getType(baseFact.getTypeName());
		if (type!=null) {
			for (Map.Entry<String,TypeFieldDescription> field: type.getFields().entrySet()) {
				if (field.getValue().hasMetaType("MapTileURL")) {
					return getGoogleRangeForMapTile(baseFact.getString(field.getKey()));
				}
			}
		}
		String id = viewItem.getBaseFactID();
		if (id==null) {
			viewItem.setExcludedByLayout(true);
			return null;
		}
		Fact position = factStore.getFact("GeoPosition", "subjectId", id);
		if (position==null) {
			viewItem.setExcludedByLayout(true);
			return null;
		}
		Double latitude = position.getDouble("latitude");
		Double longitude = position.getDouble("longitude");
		if (latitude==null || longitude==null) {
			viewItem.setExcludedByLayout(true);
			return null;
		}
		logger.info("Found GeoPosition for "+id+": latitude="+latitude+", longitude="+longitude);
		double googleX = longitudeToGoogleX(longitude);
		double googleY = latitudeToGoogleY(latitude);
		return new Rectangle2D.Double(googleX, googleY, 0, 0);
	}
	@Override
	public void preLayout(ViewCanvas component, CustomViewInfo customViewInfo,
			List<AbstractViewItem> viewItems,
			List<List<AbstractViewItem>> viewItems2, FactStore factStore) {
		// TODO Auto-generated method stub
		super.preLayout(component, customViewInfo, viewItems, viewItems2, factStore);
		if (!isPre) {
			// is now
			isPre = true;
			range = null;
		}
		nextitem:
			for (AbstractViewItem viewItem : viewItems) {
				if (viewItem.isExcludedByLayout())
					continue;
				Rectangle2D itemRange = getGoogleRange(viewItem, factStore);
				if (itemRange==null) {
					viewItem.setExcludedByLayout(true);
					continue;
				}
				if (range==null)
					range = itemRange;
				else
					if (!range.contains(itemRange))
						range = range.createUnion(itemRange);
			}		
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.AbstractViewLayout#doLayout(java.awt.Component, uk.ac.horizon.ug.authorapp.model.CustomViewInfo, java.util.List, java.util.List)
	 */
	@Override
	public void doLayout(ViewCanvas canvas, CustomViewInfo customViewInfo,
			List<AbstractViewItem> viewItems,
			List<List<AbstractViewItem>> viewItems2,  FactStore factStore) {
		if (isPre) {
			// not now 
			isPre = false;
			if (range==null) {
				logger.log(Level.WARNING, "GeoPositionViewLayout doing layout with no pre-layout range");
				range = new Rectangle2D.Double(0, 0, 1, 1);
			} else if (range.getWidth()<MINIMUM_WIDTH || range.getHeight()<MINIMUM_WIDTH) {
				range.setRect(range.getMinX(), range.getMinY(), MINIMUM_WIDTH, MINIMUM_WIDTH);
				logger.log(Level.WARNING, "GeoPositionViewLayout doing layout with too-small pre-layout range");
			}
			else {
				logger.info("Range: "+range);
			}
		}
		nextitem:
		for (AbstractViewItem viewItem : viewItems) {
			if (viewItem.isExcludedByLayout())
				continue;
			Rectangle2D itemRange = getGoogleRange(viewItem, factStore);
			if (itemRange==null) {
				viewItem.setExcludedByLayout(true);
				continue;
			}
			
			double x = canvas.getMinx()+((itemRange.getMinX()-range.getMinX())/range.getWidth())*(canvas.getMaxx()-canvas.getMinx());
			// upside down?
			double y = canvas.getMiny()+((itemRange.getMinY()-range.getMinY())/range.getHeight())*(canvas.getMaxy()-canvas.getMiny());
						
			viewItem.setX((float)x);
			viewItem.setY((float)y);

			double w = canvas.getMinx()+(itemRange.getWidth()/range.getWidth())*(canvas.getMaxx()-canvas.getMinx());
			// upside down?
			double h = canvas.getMiny()+(itemRange.getHeight()/range.getHeight())*(canvas.getMaxy()-canvas.getMiny());
			
			viewItem.setWidth((float)w);
			viewItem.setHeight((float)h);

			logger.info("Item placed at "+x+","+y+" "+w+"x"+h);
		}
	}
	/** do Layout for a Fact with a MapTileURL property */
	public static Rectangle2D getGoogleRangeForMapTile(String mapTileURL) {
		logger.info("Layout MapTile: "+mapTileURL);
		try {
			if (mapTileURL==null) {
				return null;
			}
			int qix = mapTileURL.indexOf('?');
			if (qix<0) {
				return null;			
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
				return null;
			}

			double googleX = longitudeToGoogleX(longitude);
			double googleY = latitudeToGoogleY(latitude);
			
			// width -> delta x
			double pixelsAtZoom = 256*Math.pow(2, zoom);
			double deltaX = width/2.0/pixelsAtZoom;
			
			// height -> top/bottom latitude
			double deltaY = height/2.0/pixelsAtZoom;
			
			return new Rectangle2D.Double(googleX-deltaX, googleY-deltaY, 2*deltaX, 2*deltaY);
		} catch(Exception e) {
			logger.log(Level.WARNING, "Error parsing map URL "+mapTileURL, e);
			return null;
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
