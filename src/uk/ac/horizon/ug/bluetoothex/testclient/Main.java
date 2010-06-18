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
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageStatusType;
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
				// TODO Auto-generated method stub
				doConnect();
			}
			
		}));
		clientMenu.add(new JMenuItem(new AbstractAction("Poll") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				doPoll();
			}
			
		}));
		clientMenu.add(new JMenuItem(new AbstractAction("Add Sighting") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				addSighting();
			}
			
		}));
		frame.setPreferredSize(new Dimension(400,300));
		frame.pack();
		frame.setVisible(true);	
	}

	protected void addSighting() {
		String mac = JOptionPane.showInputDialog(frame, "MAC address?", "Sighting", JOptionPane.QUESTION_MESSAGE);
		if (mac==null || mac.length()==0)
			return;
		Message msg = new Message();
		msg.setSeqNo(seqNo++);
		msg.setType(MessageType.ADD_FACT);
		msg.setNewVal("{\"typeName\":\"BluetoothSighting\",\"namespace\":\"uk.ac.horizon.ug.ubicomp\",\"device_id\":\""+clientId+"\",\"beacon_mac\":\""+mac+"\",\"time\":"+System.currentTimeMillis()+"}");
		List<Message> responses = sendMessage(msg);
		boolean ok = false;
		MessageStatusType status = MessageStatusType.OK;
		String errorMessage = null;
		for (Message response : responses) {
			if (response.getType()==MessageType.ACK && response.getAckSeq()!=null && response.getAckSeq()==msg.getSeqNo())  {
				ok = true;
				break;
			}
			else if (response.getType()==MessageType.ERROR && response.getAckSeq()!=null && response.getAckSeq()==msg.getSeqNo())  {
				status = response.getStatus();
				errorMessage = response.getErrorMsg();
			}
		}
		if (ok) 
			JOptionPane.showMessageDialog(frame, "OK", "Sighting", JOptionPane.INFORMATION_MESSAGE);
		else
			JOptionPane.showMessageDialog(frame, "Error: "+status+" ("+errorMessage+")", "Sighting", JOptionPane.ERROR_MESSAGE);
			
	}

	protected void doPoll() {
		Message msg = new Message();
		msg.setSeqNo(seqNo++);
		msg.setType(MessageType.POLL);
		//msg.setToFollow(0);
		msg.setAckSeq(ackSeq);
		
		List<Message> messages = sendMessage(msg);
		if (messages==null)
			return;
		
		for (Message message : messages) {
			if (message.getSeqNo()>0 && message.getSeqNo()>ackSeq)
				ackSeq = message.getSeqNo();
			if (message.getType()==MessageType.FACT_EX || message.getType()==MessageType.FACT_ADD) {
				JOptionPane.showMessageDialog(frame, "New fact: "+message.getNewVal(), "Poll", JOptionPane.INFORMATION_MESSAGE);
			}
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
		dialog.setVisible(true);
	}

	protected URL serverUrl;
	protected String clientId;
	protected int ackSeq = 0;
	protected int seqNo = 1;
	
	protected boolean connect(String url, String clientId) {
		
		try {
			serverUrl = new URL(url);
			this.clientId = clientId;	

			// we are a content display device!
			Message msg = new Message();
			msg.setSeqNo(seqNo++);
			msg.setType(MessageType.ADD_FACT);
			msg.setNewVal("{\"typeName\":\"ContentDisplayDevice\",\"namespace\":\"uk.ac.horizon.ug.ubicomp\",\"id\":\""+clientId+"\"}");

			sendMessage(msg);
			return true;
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Error connecting to server", e);
			JOptionPane.showMessageDialog(frame, "Error connecting to server: "+e, "Connect", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}	
	protected List<Message> sendMessage(Message msg) {
		List<Message> messages = new LinkedList<Message>();
		messages.add(msg);
		return sendMessages(messages);
	}
	protected List<Message> sendMessages(List<Message> messages) {
		try {
			HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.addRequestProperty("Content-Type", "application/xml");
			OutputStream os = conn.getOutputStream();
			
			XStream xs = new XStream(new DomDriver());
			xs.alias("list", LinkedList.class);    	
			xs.alias("message", Message.class);
			
			xs.toXML(messages, os);
			os.close();
			
			int status = conn.getResponseCode();
			if (status!=200) {
				logger.log(Level.WARNING, "Error response ("+status+") from server: "+conn.getResponseMessage());
				JOptionPane.showMessageDialog(frame, "Error response ("+status+") from server: "+conn.getResponseMessage(), "Connect", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			InputStream is = conn.getInputStream();
			messages = (List<Message>)xs.fromXML(is);
			logger.info("Response: "+messages);
			
			return messages;
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Error connecting to server", e);
			JOptionPane.showMessageDialog(frame, "Error connecting to server: "+e, "Connect", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	
}
