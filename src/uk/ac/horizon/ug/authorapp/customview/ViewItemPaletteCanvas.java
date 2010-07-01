/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import uk.ac.horizon.ug.authorapp.customview.AbstractViewItemCanvas.MouseEventHandler;


/** Layout-excluded items.
 * 
 * @author cmg
 *
 */
public class ViewItemPaletteCanvas extends AbstractViewItemCanvas {
	/** cons */
	public ViewItemPaletteCanvas() {
		super(true);
		MouseEventHandler handler = new MouseEventHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
	}

	public static final int GAP = 4;

	/** update */
	public void updateItems() {
		int minHeight = 0;
		int minWidth = 0;
		for (List<AbstractViewItem> vis : viewItems) {
			for (AbstractViewItem vi : vis) {
				if (!vi.isExcludedByLayout())
					continue;
				Rectangle visibleExtent = vi.getVisibleExtent();
				if (visibleExtent.getHeight()>minHeight)
					minHeight = (int)visibleExtent.getHeight();
				vi.setY((float)(GAP/2-visibleExtent.getY()));
				vi.setX((float)(GAP/2-visibleExtent.getX()+minWidth));
				minWidth = (int)(minWidth + visibleExtent.getWidth() + GAP);
			}
		}
		Dimension size = new Dimension(minWidth, minHeight+GAP);
		this.setMinimumSize(size);
		this.setPreferredSize(size);
		this.setSize(size);
		validate();
		repaint();
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (List<AbstractViewItem> vis : viewItems) {
			for (AbstractViewItem vi : vis) {
				if (!vi.isExcludedByLayout())
					continue;
				Rectangle visibleExtent = vi.getVisibleExtent();
				Graphics2D graphics = (Graphics2D) g.create();
				graphics.clipRect((int)(vi.getX()-GAP/2+visibleExtent.x), (int)(vi.getY()-GAP/2+visibleExtent.y), visibleExtent.width+GAP, visibleExtent.height+GAP);
				vi.draw(graphics);
			}
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.AbstractViewItemCanvas#updateAfterDrag()
	 */
	@Override
	protected void updateAfterDrag() {
		this.updateItems();
		super.updateAfterDrag();
	}
	
}
