/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import uk.ac.horizon.ug.exserver.devclient.Fact;

/**
 * @author cmg
 *
 */
public abstract class AbstractViewItem {
	/** x */
	protected float x;
	/** y */
	protected float y;
	/** nominal width */
	protected float width;
	/** nominal height */
	protected float height;
	/** visible bound relative to x,y */
	protected Rectangle visibleExtent;
	/** selected */
	protected boolean selected;
	/** excluded by layout */
	protected boolean excludedByLayout;
	/** view layout */
	protected AbstractViewLayout viewLayout;
	/** base fact */
	protected Fact baseFact;
	/** base fact ID (if any) */
	protected String baseFactID;
	
	/** draw */
	public abstract void draw(Graphics2D graphics);
	/**
	 * @return the x
	 */
	public float getX() {
		return x;
	}
	/**
	 * @param x the x to set
	 */
	public void setX(float x) {
		this.x = x;
	}
	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}
	/**
	 * @param y the y to set
	 */
	public void setY(float y) {
		this.y = y;
	}
	/**
	 * @return the width
	 */
	public float getWidth() {
		return width;
	}
	/**
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
		updateVisibleExtent();
	}
	/**
	 * @return the height
	 */
	public float getHeight() {
		return height;
	}
	/**
	 * @param height the height to set
	 */
	public void setHeight(float height) {
		this.height = height;
		updateVisibleExtent();
	}
	/**
	 * Visible Extent is relative to x, y!!
	 * @return the visibleBound
	 */
	public Rectangle getVisibleExtent() {
		if (visibleExtent==null) {
			return new Rectangle((int)0, (int)0, (int)width, (int)height);
		}
		return visibleExtent;
	}
	/**
	 * Visible Extent is relative to x, y!!
	 * @param visibleBound the visibleBound to set
	 */
	public void setVisibleExtent(Rectangle visibleBounds) {
		this.visibleExtent = visibleBounds;
	}
	/** override me */
	protected void updateVisibleExtent() {
		if (visibleExtent!=null) {
			if (width+1 > visibleExtent.width)
				visibleExtent.width = (int)width+1;
			if (height+1 > visibleExtent.height)
				visibleExtent.height = (int)height+1;
		}
	}
	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}
	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	/**
	 * @return the excludedByLayout
	 */
	public boolean isExcludedByLayout() {
		return excludedByLayout;
	}
	/**
	 * @param excludedByLayout the excludedByLayout to set
	 */
	public void setExcludedByLayout(boolean excludedByLayout) {
		this.excludedByLayout = excludedByLayout;
	}
	/**
	 * @return the viewLayout
	 */
	public AbstractViewLayout getViewLayout() {
		return viewLayout;
	}
	/**
	 * @param viewLayout the viewLayout to set
	 */
	public void setViewLayout(AbstractViewLayout viewLayout) {
		this.viewLayout = viewLayout;
	}
	/**
	 * @return the baseFact
	 */
	public Fact getBaseFact() {
		return baseFact;
	}
	/**
	 * @param baseFact the baseFact to set
	 */
	public void setBaseFact(Fact baseFact) {
		this.baseFact = baseFact;
	}
	/**
	 * @return the baseFactID
	 */
	public String getBaseFactID() {
		return baseFactID;
	}
	/**
	 * @param baseFactID the baseFactID to set
	 */
	public void setBaseFactID(String baseFactID) {
		this.baseFactID = baseFactID;
	}
	
}
