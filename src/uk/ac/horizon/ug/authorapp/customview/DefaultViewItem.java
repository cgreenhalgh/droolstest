/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.LineMetrics;

/**
 * @author cmg
 *
 */
public class DefaultViewItem extends AbstractViewItem {
	/** border width */
	protected float borderWidth;
	/** color */
	protected Color foregroundColor;
	/** background */
	protected Color backgroundColor;
	/** text color */
	protected Color textColor;
	/** text - rows */
	protected String textRows[];
	/** font */
	protected Font font;
	/** user object */
	protected Object userObject;
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.AbstractViewItem#draw(java.awt.Graphics)
	 */
	@Override
	public void draw(Graphics2D graphics) {
		Color background = graphics.getBackground();
		if (backgroundColor!=null) 
			graphics.setBackground(backgroundColor);			
		graphics.clearRect((int)x, (int)y, (int)width, (int)height);
		if (backgroundColor!=null) 
			graphics.setBackground(background);
		if (borderWidth>0) {
			Stroke stroke = graphics.getStroke();
			Stroke s = new BasicStroke(borderWidth);
			Color foreground = graphics.getColor();
			if (foregroundColor!=null)
				graphics.setColor(foregroundColor);
			graphics.setStroke(s);
			graphics.drawRect((int)x, (int)y, (int)width, (int)height);
			graphics.setStroke(stroke);
			if (foregroundColor!=null)
				graphics.setColor(foreground);
		}
		if (textRows!=null) {
			Font f = null;
			Font oldFont = graphics.getFont();
			if (font!=null) { 
				graphics.setFont(font);
				f = font;
			}
			else
				f = oldFont;
			Color foreground = graphics.getColor();
			if (foregroundColor!=null)
				graphics.setColor(foregroundColor);
			float texty = y+borderWidth;
			for (int r=0; r<textRows.length; r++) {
				LineMetrics line = f.getLineMetrics(textRows[r], graphics.getFontRenderContext());
				if (r==0)
					texty += line.getAscent();
				else
					texty += line.getHeight();
				graphics.drawString(textRows[r], x, texty);
			}
			if (font!=null)
				graphics.setFont(oldFont);
			if (foregroundColor!=null)
				graphics.setColor(foreground);
		}
	}
	/**
	 * @return the borderWidth
	 */
	public float getBorderWidth() {
		return borderWidth;
	}
	/**
	 * @param borderWidth the borderWidth to set
	 */
	public void setBorderWidth(float borderWidth) {
		this.borderWidth = borderWidth;
	}
	/**
	 * @return the foregroundColor
	 */
	public Color getForegroundColor() {
		return foregroundColor;
	}
	/**
	 * @param foregroundColor the foregroundColor to set
	 */
	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}
	/**
	 * @return the backgroundColor
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	/**
	 * @param backgroundColor the backgroundColor to set
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	/**
	 * @return the textColor
	 */
	public Color getTextColor() {
		return textColor;
	}
	/**
	 * @param textColor the textColor to set
	 */
	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}
	/**
	 * @return the textRows
	 */
	public String[] getTextRows() {
		return textRows;
	}
	/**
	 * @param textRows the textRows to set
	 */
	public void setTextRows(String[] textRows) {
		this.textRows = textRows;
	}
	/**
	 * @return the font
	 */
	public Font getFont() {
		return font;
	}
	/**
	 * @param font the font to set
	 */
	public void setFont(Font font) {
		this.font = font;
	}
	/**
	 * @return the userObject
	 */
	public Object getUserObject() {
		return userObject;
	}
	/**
	 * @param userObject the userObject to set
	 */
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

}
