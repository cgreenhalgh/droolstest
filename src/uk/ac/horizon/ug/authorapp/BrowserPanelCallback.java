/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

/**
 * @author cmg
 *
 */
public interface BrowserPanelCallback {
	/** open/to front client panel for type - swing thread */
	public void openEntityTablePanel(TypeDescription type);

}
