/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.horizon.ug.authorapp.FactStore;
import uk.ac.horizon.ug.exserver.devclient.Fact;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

/**
 * @author cmg
 *
 */
public class DefaultViewItem extends AbstractViewItem {
	static Logger logger = Logger.getLogger(DefaultViewItem.class.getName());
	/** line width */
	protected float lineWidth;
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
	/** text layouts */
	protected TextLayout textLayouts[];
	/** font */
	protected Font font;
	/** user object */
	protected Object userObject;
	/** reference component, e.g. for Font size */
	protected Component referenceComponent;
	/**
	 * @param referenceComponent
	 */
	public DefaultViewItem() {
		super();
	}
	/** get font to use */
	protected Font getFontInternal(Graphics2D graphics) {
		if (font!=null)
			return font;
		if (graphics!=null)
			return graphics.getFont();
		return ViewCanvas.getDefaultFont();		
	}
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
		if (lineWidth>0) {
			Stroke stroke = graphics.getStroke();
			Stroke s = new BasicStroke(lineWidth);
			Color foreground = graphics.getColor();
			if (foregroundColor!=null)
				graphics.setColor(foregroundColor);
			graphics.setStroke(s);
			graphics.drawRect((int)x, (int)y, (int)width, (int)height);
			graphics.setStroke(stroke);
			if (foregroundColor!=null)
				graphics.setColor(foreground);
		}
		if (textLayouts==null && textRows!=null) {
			logger.log(Level.WARNING, "Creating textLayouts on draw (late!)");
			updateTextLayouts(graphics);
		}
		if (textLayouts!=null) {
//			Font oldFont = graphics.getFont();
//			if (font!=null) { 
//				graphics.setFont(font);
//			}
			Color foreground = graphics.getColor();
			if (foregroundColor!=null)
				graphics.setColor(foregroundColor);
			float texty = y+borderWidth;
			for (int r=0; r<textLayouts.length; r++) {
				if (r==0)
					texty += textLayouts[r].getAscent();
				else {
					float lineHeight = textLayouts[r].getAscent()+textLayouts[r].getDescent()+textLayouts[r].getLeading();
					if (lineHeight > textLayouts[r].getBounds().getHeight())
						texty += lineHeight;
					else
						texty += textLayouts[r].getBounds().getHeight();
				}
				textLayouts[r].draw(graphics, x+borderWidth, texty);
			}
//			if (font!=null)
//				graphics.setFont(oldFont);
			if (foregroundColor!=null)
				graphics.setColor(foreground);
		}
		drawSelected(graphics);
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
		updateVisibleExtent();
	}
	/**
	 * @return the lineWidth
	 */
	public float getLineWidth() {
		return lineWidth;
	}
	/**
	 * @param lineWidth the lineWidth to set
	 */
	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
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
		updateTextLayouts();
		updateVisibleExtent();
	}
	/** update text layouts */
	protected void updateTextLayouts() {
		updateTextLayouts((Graphics2D)referenceComponent.getGraphics());
	}
	protected void updateTextLayouts(Graphics2D graphics) {
		if (textRows==null) {
			textLayouts = null;
		}
		else {
			Font f = getFontInternal(null);
			if (graphics==null) {
				textLayouts = null;
				logger.log(Level.WARNING, "Could not getGraphics for reference component "+referenceComponent);
			} else {
				textLayouts = new TextLayout[textRows.length];
				for (int i=0; i<textRows.length; i++) {
					textLayouts[i] = new TextLayout(textRows[i], f, graphics.getFontRenderContext());
				}
			}
		}
	}
	/** get text width & height */
	public Rectangle getTextBounds() {
		Rectangle bounds = new Rectangle();
		if (textLayouts==null) {
			if (textRows!=null)
				logger.log(Level.WARNING, "getTextBounds with null textLayouts");
			return bounds;
		}
		bounds.y = (int)borderWidth;
		float texty = 0;
		for (int i=0; i<textLayouts.length; i++) {
			Rectangle2D b = textLayouts[i].getBounds();
			if(b.getMinX() < bounds.x) {
				bounds.width += (int)(bounds.x-b.getMinX());
				bounds.x = (int)b.getMinX();
			}
			if (b.getMaxX() > bounds.getMaxX())
				bounds.width = (int)b.getMaxX()-bounds.x;
			float height = (float) b.getHeight();
			float lineHeight = textLayouts[i].getAscent()+textLayouts[i].getDescent();
			if (i>0)
				lineHeight += textLayouts[i].getLeading();
			if (height < lineHeight)
				height = lineHeight;
			texty = texty+height;
//			logger.info("getTextBounds, i="+i+", height="+height+", texty="+texty+", leading="+textLayouts[i].getLeading()+", bound.height="+b.getHeight());
		}
		bounds.height = (int)texty;
		bounds.x += (int)borderWidth;
		return bounds;
	}
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.AbstractViewItem#updateVisibleExtent()
	 */
	@Override
	protected void updateVisibleExtent() {
		// TODO Auto-generated method stub
		super.updateVisibleExtent();
		Rectangle textBounds = getTextBounds();
		if (textBounds!=null) {
			Rectangle visibleExtent = getVisibleExtent();
			if (textBounds.x < visibleExtent.x)
				visibleExtent.x = textBounds.x;
			if (textBounds.y < visibleExtent.y)
				visibleExtent.y = textBounds.y;
			if (textBounds.getMaxX() > visibleExtent.getMaxX())
				visibleExtent.width = (int) (textBounds.getMaxX()-visibleExtent.getX());
			if (textBounds.getMaxY() > visibleExtent.getMaxY())
				visibleExtent.height = (int) (textBounds.getMaxY()-visibleExtent.getY());
			setVisibleExtent(visibleExtent);
		}
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
		updateTextLayouts();
		this.updateVisibleExtent();
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
	public void initialise(Fact fact, TypeDescription typeDesc, Component referenceComponent, FactStore factStore) {
		super.initialise(fact, typeDesc, referenceComponent, factStore);
		this.referenceComponent = referenceComponent;

		setLineWidth(1);
		setBorderWidth(3);
		//String typeName = fact.getTypeName();
		setTextRows(getDefaultText());
		Rectangle textBounds = getTextBounds();
//		logger.info("Text bounds: "+textBounds.x+","+textBounds.y+","+textBounds.width+","+textBounds.height);
		if (textBounds.getMaxX()+this.getBorderWidth() > this.getWidth())
			this.setWidth((float)(textBounds.getMaxX()+this.getBorderWidth()));
		if (textBounds.getMaxY()+this.getBorderWidth() > this.getHeight())
			this.setHeight((float)(textBounds.getMaxY()+this.getBorderWidth()));
	}
	protected String [] getDefaultText() {
		return new String[] { getBaseFact().getTypeName(), getBaseFactID() };
	}
}
