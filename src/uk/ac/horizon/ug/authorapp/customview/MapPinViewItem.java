/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * @author cmg
 *
 */
public class MapPinViewItem extends DefaultViewItem {
	static final int PIN_RADIUS = 6;
	static final int STEM_WIDTH = 3;
	static final int STEM_HEIGHT = 8;
	protected Color pinColor;
	/**
	 * 
	 */
	public MapPinViewItem() {
		super();
	}
	/**
	 * @return the pinColor
	 */
	public Color getPinColor() {
		return pinColor;
	}
	/**
	 * @param pinColor the pinColor to set
	 */
	public void setPinColor(Color pinColor) {
		this.pinColor = pinColor;
	}
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.DefaultViewItem#updateVisibleExtent()
	 */
	@Override
	protected void updateVisibleExtent() {
		// TODO Auto-generated method stub
		super.updateVisibleExtent();
		this.visibleExtent.add(-PIN_RADIUS, -(PIN_RADIUS*2+STEM_HEIGHT));
	}
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.DefaultViewItem#draw(java.awt.Graphics2D)
	 */
	@Override
	public void draw(Graphics2D graphics) {
		// TODO Auto-generated method stub
		super.draw(graphics);
		//Stroke s = new BasicStroke(lineWidth);
		Color foreground = graphics.getColor();
		if (pinColor!=null)
			graphics.setColor(pinColor);
		else if (foregroundColor!=null)
			graphics.setColor(foregroundColor);
		//graphics.setStroke(s);
		graphics.fillOval((int)x-PIN_RADIUS, (int)y-2*PIN_RADIUS-STEM_HEIGHT, 2*PIN_RADIUS, 2*PIN_RADIUS);
		graphics.fillPolygon(new int[] { (int)x-STEM_WIDTH, (int)x, (int)x+STEM_WIDTH },
				new int[] { (int)y-STEM_HEIGHT-PIN_RADIUS, (int)y, (int)y-STEM_HEIGHT-PIN_RADIUS }, 3);
		if (foreground!=null)
			graphics.setColor(foreground);
	}
	@Override
	protected String [] getDefaultText() {
		return new String[] { getBaseFactID() };
	}

}
