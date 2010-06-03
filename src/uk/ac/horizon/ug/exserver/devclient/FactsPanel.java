/**
 * 
 */
package uk.ac.horizon.ug.exserver.devclient;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.protocol.SessionBuildResult;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

/**
 * @author cmg
 *
 */
public class FactsPanel extends JPanel {
	DevClientApplet applet;
	Session session;
	/** fact type tabbed pane */
	protected JTabbedPane factTypesPane;
	FactsPanel(DevClientApplet applet, Session session) {
		super(new BorderLayout());
		this.applet = applet;
		this.session = session;
		JPanel buttons = new JPanel(new FlowLayout());
		add(buttons, BorderLayout.NORTH);
		buttons.add(new JButton(new AbstractAction("Refresh Types") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				doRefreshTypes();
			}			
		}));
		factTypesPane = new JTabbedPane();
		add(factTypesPane, BorderLayout.CENTER);
		// ....
	}
	/** type panel */
	class TypePanel extends JPanel {
		TypeDescription type;
		TypePanel(TypeDescription type) {
			super(new BorderLayout());
			this.type = type;
			// ....
		}
	}
	/** type panels */
	protected Map<String,TypePanel> typePanels = new HashMap<String,TypePanel>();
	/** swing thread */
	void doRefreshTypes() {
		System.err.println("Refresh types...");
		Cursor c = applet.getCursor();
		factTypesPane.removeAll();
		typePanels.clear();
		factTypesPane.revalidate();
		try {
			applet.setCursor(applet.getBusyCursor());
			Protocol p = applet.getProtocol();
			List<TypeDescription> types = p.getTypes(session.getId());
			for (TypeDescription type : types) {
				TypePanel typePanel = new TypePanel(type);
				typePanels.put(type.getTypeName(), typePanel);
				factTypesPane.add(type.getTypeName(), typePanel);
			}
			factTypesPane.revalidate();
		}
		catch (Exception e) {
			System.err.println("Error refreshing types: "+e);
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(applet, "Error refreshing types: "+e, "Error refreshing types", JOptionPane.ERROR_MESSAGE);			
		}
		// restore cursor
		applet.setCursor(c);		
	}
}
