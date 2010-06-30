/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import uk.ac.horizon.ug.exserver.devclient.Fact;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription;

/**
 * @author cmg
 *
 */
public class MapTileViewItem extends AbstractViewItem {
	static Logger logger = Logger.getLogger(MapTileViewItem.class.getName());

	protected TypeDescription typeDesc;
	
	@Override
	public void initialise(Fact fact, TypeDescription typeDesc,
			Component referenceComponent) {
		// TODO Auto-generated method stub
		super.initialise(fact, typeDesc, referenceComponent);
		this.typeDesc = typeDesc;
		for (Map.Entry<String, TypeFieldDescription> field : typeDesc.getFields().entrySet()) {
			if (field.getValue().hasMetaType("MapTileURL")) {
				setImage(fact.getString(field.getKey()));
			}
		}
	}
	private BufferedImage image = null;
	private void setImage(String url) {
		logger.info("Trying to read "+url);
		try {
			image = null;
			image = ImageIO.read(new URL(url));
			logger.info("ok: "+image);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Reading image "+url, e);
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customview.AbstractViewItem#draw(java.awt.Graphics2D)
	 */
	@Override
	public void draw(Graphics2D graphics) {
		// TODO Auto-generated method stub
		BufferedImage i = image;
		if (i!=null) {
			AffineTransform transform = new AffineTransform();
			transform.translate(getX(), getY());
			transform.scale(getWidth()/i.getWidth(), getHeight()/i.getHeight());
			graphics.drawImage(i, transform, null);
			logger.info("Drew "+image+" at "+transform);
		}
	}

}
