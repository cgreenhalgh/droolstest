/**
 * 
 */
package uk.ac.horizon.ug.exserver.devclient;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JApplet;

import uk.ac.horizon.ug.exserver.model.Session;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.HashMap;
import java.util.Map;
/**
 * @author cmg
 *
 */
public class MainPanel extends JPanel {
	DevClientApplet applet;
	JTabbedPane tabbedPane;
	/** cons - build */
	public MainPanel(DevClientApplet applet) {
		super(new BorderLayout());
		this.applet = applet;
		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
		
		settings = new SettingsPanel(applet);
		tabbedPane.add("Settings", settings);

		sessions = new SessionsPanel(applet);
		tabbedPane.add("Sessions", sessions);
	}
	SettingsPanel settings;
	SessionsPanel sessions;
	
	public void setServer(String s) {
		settings.serverField.setText(s);
	}
	public String getServer() {
		return settings.serverField.getText();
	}
	
	/** settings panel */
	static class SettingsPanel extends JPanel {
		JTextField serverField;
		SettingsPanel(JApplet applet) {
			super(new BorderLayout());
			JPanel p = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.weightx = 0;
			p.add(new JLabel("Server"), c);
			serverField = new JTextField(80);
			c.gridx = 1;
			c.weightx = 1;
			p.add(serverField, c);

			c.gridx = 0;
			c.gridy ++;
			
			//JScrollPane sp = new JScrollPane(p);
			add(p, BorderLayout.CENTER);
		}
	}
	/** sessions */
	Map<String,SessionPanel> sessionPanels = new HashMap<String,SessionPanel>();
	/** session facts */
	Map<String,FactsPanel> factsPanels = new HashMap<String,FactsPanel>();
	/** open session tab - called from SessionsPanel in Swing Thread */
	void openSession(Session session) {
		synchronized(sessionPanels) {
			SessionPanel panel = sessionPanels.get(session.getId());
			if (panel==null) {
				panel = new SessionPanel(applet, session);
				sessionPanels.put(session.getId(), panel);
				tabbedPane.add("Session "+session.getId(), panel);
			}
			tabbedPane.setSelectedComponent(panel);
		}
		synchronized(factsPanels) {
			FactsPanel panel = factsPanels.get(session.getId());
			if (panel==null) {
				panel = new FactsPanel(applet, session);
				factsPanels.put(session.getId(), panel);
				tabbedPane.add("Facts "+session.getId(), panel);
			}
			//tabbedPane.setSelectedComponent(panel);
		}
	}
}
