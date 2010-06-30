/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;

/**
 * @author cmg
 *
 */
public class ViewCanvas extends JComponent {
	static Logger logger = Logger.getLogger(ViewCanvas.class.getName());
	/** list of list of ViewItems */
	protected List<List<AbstractViewItem>> viewItems = new LinkedList<List<AbstractViewItem>>();
	protected float minx;
	protected float maxx;
	protected float miny;
	protected float maxy;
	protected float zoomRatio;
	/** cons */
	public ViewCanvas() {
		super();
		this.minx = 0;
		this.maxx = 100;
		this.miny = 0;
		this.maxy = 100;
		this.zoomRatio = 1;
		//this.setBackground(Color.WHITE);
		this.setForeground(Color.BLACK);
		// default font
		setFont(getDefaultFont());
		updateSize();
	}
	protected void updateSize() {
		Dimension size = new Dimension((int)((maxx-minx)*zoomRatio), (int)((maxy-miny)*zoomRatio));
		this.setMinimumSize(size);
		this.setMaximumSize(size);
		this.setPreferredSize(size);
		this.setSize(size);
	}
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (!(g instanceof Graphics2D)) {
			logger.log(Level.WARNING, "ViewCanvas paint - not Graphics2D: "+g);
			return;
		}
		Graphics2D graphics = (Graphics2D)g.create();
		graphics.setBackground(Color.WHITE);		
		graphics.setFont(getFont());
		graphics.translate(-minx, -miny);
		graphics.scale(zoomRatio, zoomRatio);
		graphics.clearRect((int)minx, (int)miny, (int)(maxx-minx), (int)(maxy-miny));
		for (int i=0; i<viewItems.size(); i++) {
			List<AbstractViewItem> vis = viewItems.get(i);
			for (int j=0; j<vis.size(); j++) {
				AbstractViewItem avi = vis.get(j);
				if (avi.isExcludedByLayout())
					continue;
				avi.draw(graphics);
			}
		}
	}
	/**
	 * @return the viewItems
	 */
	public List<List<AbstractViewItem>> getViewItems() {
		if (viewItems==null)
			viewItems = new LinkedList<List<AbstractViewItem>>();
		return viewItems;
	}
	/**
	 * @param viewItems the viewItems to set
	 */
	public void setViewItems(List<List<AbstractViewItem>> viewItems) {
		this.viewItems = viewItems;
	}
	/**
	 * @return the minx
	 */
	public float getMinx() {
		return minx;
	}
	/**
	 * @param minx the minx to set
	 */
	public void setMinx(float minx) {
		this.minx = minx;
		updateSize();
	}
	/**
	 * @return the maxx
	 */
	public float getMaxx() {
		return maxx;
	}
	/**
	 * @param maxx the maxx to set
	 */
	public void setMaxx(float maxx) {
		this.maxx = maxx;
		updateSize();
	}
	/**
	 * @return the miny
	 */
	public float getMiny() {
		return miny;
	}
	/**
	 * @param miny the miny to set
	 */
	public void setMiny(float miny) {
		this.miny = miny;
		updateSize();
	}
	/**
	 * @return the maxy
	 */
	public float getMaxy() {
		return maxy;
	}
	/**
	 * @param maxy the maxy to set
	 */
	public void setMaxy(float maxy) {
		this.maxy = maxy;
		updateSize();
	}
	/**
	 * @return the zoomRatio
	 */
	public float getZoomRatio() {
		return zoomRatio;
	}
	/**
	 * @param zoomRatio the zoomRatio to set
	 */
	public void setZoomRatio(float zoomRatio) {
		this.zoomRatio = zoomRatio;
		updateSize();
		validate();
	}
	protected static Font defaultFont;
	public static synchronized Font getDefaultFont() {
		if (defaultFont==null)
		{	
			defaultFont = new Font("SansSerif", Font.PLAIN, 11);
			logger.info("Default font: "+defaultFont);
		}
		return defaultFont;
	}
}
