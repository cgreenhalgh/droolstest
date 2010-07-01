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
	private static final String GEO_POSITION_NAMESPACE = "uk.ac.horizon.ug.hyperplace.facts";
	private static final String GEO_POSITION_TYPE_NAME = "GeoPosition";
	private static final String GEO_POSITION_ID = "subjectId";
	private static final String GEO_POSITION_LATITUDE = "latitude";
	private static final String GEO_POSITION_LONGITUDE = "longitude";
	
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
	/** get MapTileURL value (if defined for type) */
	public static String getMapTileURL(AbstractViewItem viewItem) {
		Fact baseFact = viewItem.getBaseFact();
		TypeDescription type = viewItem.getFactStore().getType(baseFact.getTypeName());
		if (type!=null) {
			for (Map.Entry<String,TypeFieldDescription> field: type.getFields().entrySet()) {
				if (field.getValue().hasMetaType("MapTileURL")) {
					return baseFact.getString(field.getKey());
				}
			}
		}
		return null;
	}
	/** get GeoPosition Fact (if defined) for item */
	public static Fact getGeoPosition(AbstractViewItem viewItem) {
		String id = viewItem.getBaseFactID();
		if (id==null) {
			viewItem.setExcludedByLayout(true);
			return null;
		}
		Fact position = viewItem.getFactStore().getFact(GEO_POSITION_TYPE_NAME, "subjectId", id);
		return position;
	}
	/** get google x/y range for item */
	public static Rectangle2D getGoogleRange(AbstractViewItem viewItem, FactStore factStore) {
		// could be MapTileURL property?
		String mapTileURL = getMapTileURL(viewItem);
		if (mapTileURL!=null) 
			return getGoogleRangeForMapTile(mapTileURL);
		Fact position = getGeoPosition(viewItem);
		if (position==null) {
			viewItem.setExcludedByLayout(true);
			return null;
		}
		Double latitude = position.getDouble(GEO_POSITION_LATITUDE);
		Double longitude = position.getDouble(GEO_POSITION_LONGITUDE);
		if (latitude==null || longitude==null) {
			viewItem.setExcludedByLayout(true);
			return null;
		}
		logger.info("Found GeoPosition for "+viewItem.getBaseFactID()+": latitude="+latitude+", longitude="+longitude);
		double googleX = longitudeToGoogleX(longitude);
		double googleY = latitudeToGoogleY(latitude);
		return new Rectangle2D.Double(googleX, googleY, 0, 0);
	}
	@Override
	public void preLayout(AbstractViewItemCanvas component, CustomViewInfo customViewInfo,
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
			placeItem(canvas, viewItem);
		}
	}
	private void placeItem(ViewCanvas canvas, AbstractViewItem viewItem) {
		Rectangle2D itemRange = getGoogleRange(viewItem, viewItem.getFactStore());
		if (itemRange==null) {
			viewItem.setExcludedByLayout(true);
			return;
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
	/** 0-1 google map range to degrees longitude */
	public static double googleXToLongitude(double googleX) {
		return googleX*360-180;		
	}
	/** 0-1 google map range to degrees latitude */
	public static double googleYToLatitude(double googleY) {
		double radians = Math.atan(Math.sinh(Math.PI*(1-2*googleY)));
		return radians * 180 / Math.PI;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.AbstractViewLayout#handleItemMove(uk.ac.horizon.ug.authorapp.customview.ViewCanvas, uk.ac.horizon.ug.authorapp.customview.AbstractViewItem, int, int)
	 */
	@Override
	public void handleItemMove(ViewCanvas canvas, AbstractViewItem viewItem,
			int dx, int dy) {
		Fact position = getGeoPosition(viewItem);
		if (position==null) {
			logger.info("Cannot move item without GeoPosition: "+viewItem);
			return;
		}
//		String mapTileURL = getMapTileURL(viewItem);
//		if (mapTileURL!=null) {
//			logger.info("Cannot move MapTileURL");
//		}
		Double latitude = position.getDouble(GEO_POSITION_LATITUDE);
		Double longitude = position.getDouble(GEO_POSITION_LONGITUDE);
		if (latitude==null || longitude==null) {
			logger.info("Cannot move item with missing GeoPosition latitude/longitude: "+viewItem);
			return;
		}
		double googleX = longitudeToGoogleX(longitude);
		double googleY = latitudeToGoogleY(latitude);
		// scale mouse dx/dy by range & canvas size
		double gdx = dx / (canvas.getMaxx()-canvas.getMinx()) * range.getWidth();
		double gdy = dy / (canvas.getMaxy()-canvas.getMiny()) * range.getHeight();
		
		double newX = googleX+gdx;
		double newY = googleY+gdy;
		double newLongitude = this.googleXToLongitude(newX);
		double newLatitude = this.googleYToLatitude(newY);
		
		// to string?! sig figs?
		position.getFieldValues().put(GEO_POSITION_LATITUDE, newLatitude);
		position.getFieldValues().put(GEO_POSITION_LONGITUDE, newLongitude);
		this.placeItem(canvas, viewItem);
		// signal changed data
		viewItem.getFactStore().setChanged(true);
		logger.info("Moved item to "+latitude+","+longitude+" -> "+viewItem.getX()+","+viewItem.getY());
		canvas.repaint();
	}
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.AbstractViewLayout#handleItemDragOff(uk.ac.horizon.ug.authorapp.customview.AbstractViewItemCanvas, uk.ac.horizon.ug.authorapp.customview.AbstractViewItem)
	 */
	@Override
	public void handleItemDragOff(ViewCanvas component,
			AbstractViewItem viewItem) {
		super.handleItemDragOff(component, viewItem);
		logger.info("DragOff "+viewItem);
		Fact position = getGeoPosition(viewItem);
		if (position==null) {
			logger.log(Level.WARNING, "Drop off already has no GeoPosition: "+viewItem);
			return;
		} 
		viewItem.getFactStore().removeFact(position);
		viewItem.getFactStore().setChanged(true);
	}
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.AbstractViewLayout#handleItemDragOn(uk.ac.horizon.ug.authorapp.customview.AbstractViewItemCanvas, uk.ac.horizon.ug.authorapp.customview.AbstractViewItem, int, int)
	 */
	@Override
	public void handleItemDragOn(ViewCanvas canvas,
			AbstractViewItem viewItem, int x, int y) {
		// TODO Auto-generated method stub
		super.handleItemDragOn(canvas, viewItem, x, y);
		
		logger.info("DragOn "+viewItem+" to "+x+","+y);

		if (viewItem.getBaseFactID()==null) {
			logger.log(Level.WARNING, "cannot drag on item without ID: "+viewItem);
			viewItem.setExcludedByLayout(true);
			return;
		}
		Fact position = getGeoPosition(viewItem);
		if (position!=null) {
			logger.log(Level.WARNING, "Drop on already has GeoPosition: "+viewItem);
		} else {
			position = new Fact();
			position.getFieldValues().put(GEO_POSITION_ID, viewItem.getBaseFactID());
			position.setNamespace(GEO_POSITION_NAMESPACE);
			position.setTypeName(GEO_POSITION_TYPE_NAME);
			viewItem.getFactStore().addFact(position);
		}
		double googleX = x / (canvas.getMaxx()-canvas.getMinx()) * range.getWidth() + range.getMinX();
		double googleY = y / (canvas.getMaxy()-canvas.getMiny()) * range.getHeight() + range.getMinY();
		
		double newLongitude = this.googleXToLongitude(googleX);
		double newLatitude = this.googleYToLatitude(googleY);
		
		// to string?! sig figs?
		position.getFieldValues().put(GEO_POSITION_LATITUDE, newLatitude);
		position.getFieldValues().put(GEO_POSITION_LONGITUDE, newLongitude);
		this.placeItem(canvas, viewItem);
		viewItem.getFactStore().setChanged(true);
		logger.info("Placed item at "+newLatitude+","+newLongitude);
//		canvas.repaint();
	}
}
