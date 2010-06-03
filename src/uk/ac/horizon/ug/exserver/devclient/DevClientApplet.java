/**
 * 
 */
package uk.ac.horizon.ug.exserver.devclient;

import java.awt.Cursor;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import uk.ac.horizon.ug.exserver.model.Session;

/** Development/test applet client for Drools server
 * 
 * @author cmg
 *
 */
public class DevClientApplet extends JApplet {

	/* (non-Javadoc)
	 * @see java.applet.Applet#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		super.init();
		try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                	mainPanel = new MainPanel(DevClientApplet.this);
                	mainPanel.setOpaque(true); 
                    setContentPane(mainPanel);     

                    String codeBase = DevClientApplet.this.getCodeBase().toExternalForm();
                    if (codeBase.contains("/devclient"))
                    	codeBase = codeBase.substring(0, codeBase.lastIndexOf("/devclient")+1);
                    mainPanel.setServer(codeBase);
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't complete successfully: "+e);
            e.printStackTrace(System.err);
        }

	}
	
	
	/** UI */
	protected MainPanel mainPanel;
	
	/** get server setting */
	String getServer() {
		return mainPanel.getServer();
	}

	/* (non-Javadoc)
	 * @see java.applet.Applet#start()
	 */
	@Override
	public void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	/* (non-Javadoc)
	 * @see java.applet.Applet#stop()
	 */
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		super.stop();
	}
	
	Cursor getBusyCursor() {
		return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	}

	/** open session tab - called from SessionsPanel in Swing Thread */
	void openSession(Session session) {
		mainPanel.openSession(session);
	}
	/** get a Protocol object to use */
	Protocol getProtocol() {
		// TODO: cache?
		return new Protocol(this.getServer());
	}
}

