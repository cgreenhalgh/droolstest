/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

/** Layout-excluded items.
 * 
 * @author cmg
 *
 */
public class ViewItemPaletteCanvas extends JComponent {
	/** items */
	protected List<List<AbstractViewItem>> viewItems = new LinkedList<List<AbstractViewItem>>();

	/** cons */
	public ViewItemPaletteCanvas() {
		super();
	}

	/**
	 * @return the viewItems
	 */
	public List<List<AbstractViewItem>> getViewItems() {
		return viewItems;
	}

	/**
	 * @param viewItems the viewItems to set
	 */
	public void setViewItems(List<List<AbstractViewItem>> viewItems) {
		this.viewItems = viewItems;
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
		int x = 0;
		for (List<AbstractViewItem> vis : viewItems) {
			for (AbstractViewItem vi : vis) {
				if (!vi.isExcludedByLayout())
					continue;
				Rectangle visibleExtent = vi.getVisibleExtent();
				Graphics2D graphics = (Graphics2D) g.create(x, 0, visibleExtent.width+GAP, visibleExtent.height+GAP);
				vi.setX(GAP/2-visibleExtent.x);
				vi.setY(GAP/2-visibleExtent.y);
				vi.draw(graphics);
				x = x+visibleExtent.width+GAP;
			}
		}
	}
	
}
