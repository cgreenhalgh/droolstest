/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi.client;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.horizon.ug.bluetoothex.testclient.Main;
import uk.ac.horizon.ug.exserver.clientapi.JsonUtils;
import uk.ac.horizon.ug.exserver.clientapi.protocol.Message;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageStatusType;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageType;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/** Client stub
 * 
 * @author cmg
 *
 */
public class Client {
	static Logger logger = Logger.getLogger(Client.class.getName());
	protected URL conversationUrl;
	protected String clientId;
	/**
	 * @param conversationUrl
	 * @param clientId
	 */
	public Client(URL conversationUrl, String clientId) {
		super();
		this.conversationUrl = conversationUrl;
		this.clientId = clientId;
	}
	/**
	 * @param conversationUrl
	 * @param clientId
	 * @throws MalformedURLException 
	 */
	public Client(String conversationUrl, String clientId) throws MalformedURLException {
		super();
		this.conversationUrl = new URL(conversationUrl);
		this.clientId = clientId;
	}
	
	public URL getConversationUrl() {
		return conversationUrl;
	}
	public String getClientId() {
		return clientId;
	}

	protected int ackSeq = 0;
	protected int seqNo = 1;
	
	/** connect */
	public boolean connect(List<String> classNames) throws JSONException, IOException {
		
		List<Message> messages = new LinkedList<Message>();
		// we are a content display device!
		for (String className : classNames) {
			int ix = className.lastIndexOf('.');
			String namespace = ix>=0 ? className.substring(0, ix) : null;
			String typeName = className.substring(ix+1);
			JSONObject val = new JSONObject();
			val.put("typeName", typeName);
			if(namespace!=null)
				val.put("namespace", namespace);
			val.put("id", clientId);
			
			messages.add(addFactMessage(val.toString()));
		}
		sendMessages(messages);
		return true;
	}	
	/** add fact message */
	public Message addFactMessage(String json) {
		Message msg = new Message();
		msg.setType(MessageType.ADD_FACT);
		msg.setSeqNo(seqNo++);
		msg.setNewVal(json);
		return msg;
	}
	/** internal async send */
	public List<Message> sendMessage(Message msg) throws IOException {
		List<Message> messages = new LinkedList<Message>();
		messages.add(msg);
		return sendMessages(messages);
	}
	/** internal async send */
	public List<Message> sendMessages(List<Message> messages) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) conversationUrl.openConnection();
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
			throw new IOException("Error response ("+status+") from server: "+conn.getResponseMessage());
		}
		InputStream is = conn.getInputStream();
		messages = (List<Message>)xs.fromXML(is);
		logger.info("Response: "+messages);

		// check status(es)
		for (Message response : messages) {
			if (response.getType()==MessageType.ERROR) {
				throw new IOException("Error response "+response.getStatus()+": "+response.getErrorMsg()+" for request "+response.getAckSeq());
			}
		}

		return messages;
	}
	protected List<JSONObject> facts = new LinkedList<JSONObject>();
	public List<JSONObject> getFacts() {
		return facts;
	}
	public List<JSONObject> getFacts(String typeName) {
		LinkedList<JSONObject> fs = new LinkedList<JSONObject>();
		for (JSONObject fact : facts) {
			if (fact.has("typeName")) 
				try {
					if (fact.get("typeName").equals(typeName))
						fs.add(fact);
				} catch (JSONException e) {/*ignore*/}
		}
		return fs;
	}
	/** poll 
	 * @throws JSONException */
	public List<Message> poll() throws IOException, JSONException {
		Message msg = new Message();
		msg.setSeqNo(seqNo++);
		msg.setType(MessageType.POLL);
		//msg.setToFollow(0);
		msg.setAckSeq(ackSeq);
		
		List<Message> messages = sendMessage(msg);
		if (messages==null)
			return messages;
		
		for (Message message : messages) {
			if (message.getSeqNo()>0 && message.getSeqNo()>ackSeq)
				ackSeq = message.getSeqNo();
			if (message.getType()==MessageType.FACT_EX || message.getType()==MessageType.FACT_ADD) {
				JSONObject json = new JSONObject(message.getNewVal());
				String typeName = JsonUtils.getTypeName(json);
				facts.add(json);
			} else if (message.getType()==MessageType.FACT_UPD || message.getType()==MessageType.FACT_DEL) {
				JSONObject json = new JSONObject(message.getOldVal());
				boolean found = false;
				for (int i=0; i<facts.size(); i++) {
					if (jsonEqual(json, facts.get(i))) {
						facts.remove(i);
						found = true;
						logger.info("Removing old fact "+json);
						break;
					}
				}
				if (!found)
					logger.log(Level.WARNING, "Did not find old fact to remove: "+json);
				if (message.getType()==MessageType.FACT_UPD) {
					json = new JSONObject(message.getNewVal());
					facts.add(json);
				}
			}
		}
		return messages;
	}
	private boolean jsonEqual(JSONObject json, JSONObject json2) throws JSONException {
		if (json==json2)
			return true;
		if (json==null || json2==null)
			return false;
		if (json.length()!=json2.length())
			return false;
		Iterator<String> keys = json.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			if (!json2.has(key))
				return false;
			Object val = json.get(key);
			Object val2 = json2.get(key);
			if (!jsonEqual(val, val2))
				return false;
		}
		return true;
	}
	private boolean jsonEqual(Object val, Object val2) throws JSONException {
		if (val==val2)
			return true;
		if (val==null || val2==null)
			return false;
		if (!val.getClass().getName().equals(val2.getClass().getName()))
			return false;
		if (val instanceof JSONObject) {
			if (!jsonEqual((JSONObject)val, (JSONObject)val2))
				return false;
		}
		else if (val instanceof JSONArray) { 
			if (!jsonEqual((JSONArray)val, (JSONArray)val2))
				return false;
		}
		else if (!val.equals(val2))
			return false;
		return true;
	}
	private boolean jsonEqual(JSONArray json, JSONArray json2) throws JSONException {
		if (json==json2)
			return true;
		if (json==null || json2==null)
			return false;
		if (json.length()!=json2.length())
			return false;
		for (int i =0; i<json.length(); i++) {
			Object val = json.get(i);
			Object val2 = json2.get(i);
			if (!jsonEqual(val, val2))
				return false;
		}
		return true;
	}

}
