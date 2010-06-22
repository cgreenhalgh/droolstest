/**
 * 
 */
package uk.ac.horizon.ug.bluetoothex.testclient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.json.JSONObject;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import equip.ect.components.bluetoothdiscover.BluetoothDiscover;

import uk.ac.horizon.ug.exserver.clientapi.protocol.Message;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageStatusType;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageType;
import uk.ac.horizon.ug.exserver.clientapi.JsonUtils;
import uk.ac.horizon.ug.exserver.clientapi.client.Client;

/**
 * @author cmg
 *
 */
public class Main {
	static Logger logger = Logger.getLogger(Main.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Main();
		
	}
	
	protected JFrame frame;

	/** cons */
	public Main() {
		final JFrame frame = new JFrame("Bluetoothex testclient");
		this.frame = frame;
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		JMenu clientMenu = new JMenu("Client");
		menuBar.add(clientMenu);
		clientMenu.add(new JMenuItem(new AbstractAction("Connect") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				doConnect();
			}
			
		}));
		clientMenu.add(new JMenuItem(new AbstractAction("Poll") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				doPoll();
			}
			
		}));
		clientMenu.add(new JMenuItem(new AbstractAction("Start scanning") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				startScanning();
			}
			
		}));
		clientMenu.add(new JMenuItem(new AbstractAction("Add Sighting") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				addSighting();
			}
			
		}));
		frame.setPreferredSize(new Dimension(400,300));
		frame.pack();
		frame.setVisible(true);	
	}

	protected boolean bluetoothStarted = false;
	protected BluetoothDiscover bluetooth = null;

	protected void startScanning() {
		if (bluetoothStarted)
			return;

		try {
			BluetoothDiscover bt = new BluetoothDiscover()
			{
				synchronized public void updateDevices(final String newDevices)
				{
					logger.info("Scan: "+newDevices);
					super.updateDevices(newDevices);
					if (client!=null)
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								String macs [] = newDevices.trim().split("[,]");
								for (int i=0; i<macs.length; i++) 
									addSighting(macs[i]);								
							}
						});
				}
			};
			bt.setConfigPollinterval(15);
			bt.setConfigured(true);

			bluetoothStarted = true;
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Problem starting bluetooth scanning", e);
			JOptionPane.showMessageDialog(frame, "Problem starting scanning: "+e, "Start Scanning", JOptionPane.ERROR_MESSAGE);
		}
		
	}

	protected void addSighting() {
		String mac = JOptionPane.showInputDialog(frame, "MAC address?", "Sighting", JOptionPane.QUESTION_MESSAGE);
		if (mac==null || mac.length()==0)
			return;
		addSighting(mac);
	}
	protected void addSighting(String mac) {
		logger.info("Add sighting of "+mac+"...");
		try {
			JSONObject json = new JSONObject();
			json.put("typeName", "BluetoothSighting");
			json.put("namespace", "uk.ac.horizon.ug.ubicomp");
			json.put("device_id", client.getClientId());
			json.put("beacon_mac", mac);
			client.sendMessage(client.addFactMessage(json.toString()));
			doPoll();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Error: "+e, "Sighting", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void doPoll() {
		try {
			List<Message> messages = client.poll();
			
			for (Message message : messages) {
				if (message.getType()==MessageType.FACT_EX || message.getType()==MessageType.FACT_ADD) {
					try {
						JSONObject json = new JSONObject(message.getNewVal());
						String typeName = JsonUtils.getTypeName(json);
						if (typeName.equals("ShowContentRequest")) {
							String url = json.getString("content_url");
							logger.info("Show: "+url);
							JEditorPane viewer = new JEditorPane(new URL(url));
							JDialog dialog = new JDialog(frame, "Content");
							dialog.setContentPane(new JScrollPane(viewer));
							dialog.pack();
							dialog.setLocationRelativeTo(frame);
							dialog.getContentPane().setPreferredSize(new Dimension(600, 400));
							dialog.setVisible(true);
						}
						else 
							JOptionPane.showMessageDialog(frame, "New fact: "+json, "Poll", JOptionPane.INFORMATION_MESSAGE);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(frame, "Error parsing response: "+message.getNewVal()+"\n"+e, "Poll", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Error polling: "+e, "Poll", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void doConnect() {
		// TODO Auto-generated method stub
		final JDialog dialog = new JDialog(frame, "Connect", true);
		JPanel panel = new JPanel(new GridBagLayout());
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = c.gridheight = 1;
		c.weightx = c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		panel.add(new JLabel("URL"), c);
		c.gridx = 1;
		final JTextField urlField = new JTextField(30);
		panel.add(urlField, c);
		c.gridx = 0;
		c.gridy = 1;
		panel.add(new JLabel("Client ID"), c);
		c.gridx = 1;
		final JTextField clientIdField = new JTextField(30);
		panel.add(clientIdField, c);
		c.gridx = 0;
		c.gridy = 2;
		panel.add(new JButton(new AbstractAction("OK") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String url = urlField.getText();
				String clientId = clientIdField.getText();
				if (connect(url, clientId))
					dialog.setVisible(false);
			}
			
		}), c);
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}

	protected Client client;
	
	protected boolean connect(String url, String clientId) {
		try {
			client = new Client(url, clientId);
			// we are a content display device!
			List<String> clientClassNames = new LinkedList<String>();
			clientClassNames.add("uk.ac.horizon.ug.ubicomp.ContentDisplayDevice");
			client.connect(clientClassNames);
			return true;
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Error connecting to server", e);
			JOptionPane.showMessageDialog(frame, "Error connecting to server: "+e, "Connect", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}		
	
}
