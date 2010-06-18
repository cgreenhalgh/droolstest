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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import uk.ac.horizon.ug.exserver.clientapi.protocol.Message;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageType;

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

	/** cons */
	public Main() {
		final JFrame frame = new JFrame("Bluetoothex testclient");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		JMenu clientMenu = new JMenu("Client");
		menuBar.add(clientMenu);
		clientMenu.add(new JMenuItem(new AbstractAction("Connect") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				doConnect(frame);
			}
			
		}));
		frame.setPreferredSize(new Dimension(400,300));
		frame.pack();
		frame.setVisible(true);
	}

	protected void doConnect(JFrame frame) {
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
				if (connect(url, clientId, dialog))
					dialog.setVisible(false);
			}
			
		}), c);
		dialog.pack();
		dialog.setVisible(true);
	}

	protected URL serverUrl;
	protected String clientId;
	protected boolean connect(String url, String clientId, JDialog dialog) {
		try {
			serverUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.addRequestProperty("Content-Type", "application/xml");
			OutputStream os = conn.getOutputStream();
			
			Message msg = new Message();
			msg.setSeqNo(1);
			msg.setType(MessageType.POLL);
			List<Message> messages = new LinkedList<Message>();
			messages.add(msg);
			
			XStream xs = new XStream(new DomDriver());
			xs.alias("list", LinkedList.class);    	
			xs.alias("message", Message.class);
			
			xs.toXML(messages, os);
			os.close();
			
			int status = conn.getResponseCode();
			if (status!=200) {
				logger.log(Level.WARNING, "Error response ("+status+") from server: "+conn.getResponseMessage());
				JOptionPane.showMessageDialog(dialog, "Error response ("+status+") from server: "+conn.getResponseMessage(), "Connect", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			InputStream is = conn.getInputStream();
			messages = (List<Message>)xs.fromXML(is);
			logger.info("Response: "+messages);
			
			return true;
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Error connecting to server", e);
			JOptionPane.showMessageDialog(dialog, "Error connecting to server: "+e, "Connect", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	
	
}
