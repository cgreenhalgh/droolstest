/**
 * 
 */
package uk.ac.horizon.ug.exserver.devclient;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.protocol.SessionBuildResult;

/**
 * @author cmg
 *
 */
public class SessionPanel extends JPanel {
	DevClientApplet applet;
	Session session;
	SessionPanel(DevClientApplet applet, Session session) {
		super(new BorderLayout());
		this.applet = applet;
		this.session = session;
		JPanel buttons = new JPanel(new FlowLayout());
		add(buttons, BorderLayout.NORTH);
		buttons.add(new JButton(new AbstractAction("Reload rules") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				doReloadRules();
			}			
		}));
		// ....
	}
	/** swing thread */
	void doReloadRules() {
		System.err.println("Reload rules...");
		Cursor c = applet.getCursor();
		try {
			applet.setCursor(applet.getBusyCursor());
			Protocol p = applet.getProtocol();
			SessionBuildResult result = p.reloadRules(session.getId());
			JOptionPane.showMessageDialog(applet, result.toString(), "Reload rules", JOptionPane.INFORMATION_MESSAGE);
			// ....
		}
		catch (Exception e) {
			System.err.println("Error reloading rules: "+e);
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(applet, "Error reloading rules: "+e, "Error reloading rules", JOptionPane.ERROR_MESSAGE);			
		}
		// restore cursor
		applet.setCursor(c);		
	}
}
