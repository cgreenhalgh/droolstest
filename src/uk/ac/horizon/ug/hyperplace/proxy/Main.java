/**
 * 
 */
package uk.ac.horizon.ug.hyperplace.proxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.codehaus.janino.Java.ThisReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import uk.ac.horizon.ug.exserver.clientapi.client.Client;
import uk.ac.horizon.ug.exserver.clientapi.protocol.Message;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageType;
/** An exploration of using the Hyperplace Android client (currently the one from HP/Pyramid) 
 * with the drools-based game server.
 * 
 * @author cmg
 *
 */
public class Main implements Runnable {
	static Logger logger = Logger.getLogger(Main.class.getName());
	static final int DEFAULT_PORT = 7474;
	/** server socket */
	protected ServerSocket serverSocket;
	protected String serverUrl = null;
	protected String sessionId = null;
	private static Main instance;
	public static synchronized Main getInstance(String serverUrl, String sessionId) {
		if (instance!=null) {
			instance.setServerUrl(serverUrl);
			instance.setSessionId(sessionId);
			return instance;
		}
		instance = new Main(DEFAULT_PORT, serverUrl, sessionId);
		return instance;
	}
	
	/**
	 * @return the serverUrl
	 */
	public String getServerUrl() {
		return serverUrl;
	}

	/**
	 * @param serverUrl the serverUrl to set
	 */
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Main(int defaultPort, String serverUrl2, String sessionId2) {
		try {
			this.serverUrl = serverUrl2;
			this.sessionId = sessionId2;
		// TODO Auto-generated constructor stub
			while (serverUrl==null)
				serverUrl = JOptionPane.showInputDialog("Server URL:");
			while (sessionId==null)
				sessionId = JOptionPane.showInputDialog("Session ID:");
			serverSocket = new ServerSocket(defaultPort);
			new Thread(this).start();
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Error starting proxy on port "+defaultPort, e);
		}
	}
	/** run - accept thread */
	public void run() {
		try {
			logger.info("Accepting connections on "+serverSocket);
			while (true) {
				Socket socket = serverSocket.accept();
				try {
					handleClient(socket);
				}
				catch (Exception e) {
					logger.log(Level.WARNING, "Error with new client on "+socket, e);
				}

			}
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Error accept new connections - stopping", e);			
		}
	}
	private void handleClient(Socket socket) {
		new ClientHandler(socket);
	}
	/** client handler */
	class ClientHandler {
		protected Socket socket;
		protected boolean dead;
		protected boolean registered = false;
		protected String deviceType;
		protected String deviceId;
		protected BufferedWriter out;
		protected Client client;
		protected String conversationUrl;
		ClientHandler(Socket socket) {
			this.socket = socket;
			// read thread
			new Thread() {
				public void run() {
					doSocketInput();
				}
			}.start();
		}
		protected void doSocketInput() {
			try {
				BufferedReader stdIn = new BufferedReader( new InputStreamReader( socket.getInputStream() ));
    			String input;
    			
    			while ( ( input = stdIn.readLine() ) != null) 
    			{
    				JSONObject json = new JSONObject(input);
    				logger.info("Read message from "+socket+": "+input);
    				String msgName = json.getString("__name");
    				if ("REGISTER".equals(msgName))
    				{
    					if (registered) 
    						throw new IOException("Duplicate REGISTER from client "+socket+": "+json);
    					handleRegister(json);
    				}
    				else if (!registered) 
    					throw new IOException("Message when not registered from client "+socket+": "+json);
    				else if (json.has("__data")) {
    					// ActionForm submission?!
    					// E.g. {"__timestamp":1277283960422,"__data":{"Submit":false},"__name":"HPActionForm"}
    					handleAction(json);
    				}
    			}
    			logger.info("Client "+socket+" disconnected");
    			dead = true;
    			socket.close();
			}
			catch (Exception e) {
				logger.log(Level.WARNING, "Error in client input for "+socket, e);
				try {
					socket.close();
				}
				catch (Exception e2) {/*ignore*/}
			}
		}
		/** make a client update 
		 * @throws JSONException */
		private JSONObject getHPUpdate(JSONObject response) throws JSONException {
			// return success
			JSONObject clientUpdate = new JSONObject();
			clientUpdate.put("__type", "HPUpdate");
			clientUpdate.put("__timestamp", System.currentTimeMillis());
			JSONObject clientData = new JSONObject();
			clientUpdate.put("__data", clientData);
			if (response!=null)
				clientData.put("__responseUpdate", response);
			JSONArray stateUpdates = new JSONArray();
			clientData.put("__stateUpdates", stateUpdates);
			JSONArray clientMessages = new JSONArray();
			clientData.put("__messageUpdates", clientMessages);
			return clientUpdate;
		}
		/** get state Updates from HPUpdate 
		 * @throws JSONException */
		private void addClientStateUpdate(JSONObject clientUpdate, JSONObject clientStatelet) throws JSONException {
			JSONObject clientData = clientUpdate.getJSONObject("__data");
			clientData.getJSONArray("__stateUpdates").put(clientStatelet);
		}
		/** get state Updates from HPUpdate 
		 * @throws JSONException */
		private void addClientMessageUpdate(JSONObject clientUpdate, JSONObject clientMsg) throws JSONException {
			JSONObject clientData = clientUpdate.getJSONObject("__data");
			clientData.getJSONArray("__messageUpdates").put(clientMsg);
		}
		private void handleRegister(JSONObject json) throws JSONException, IOException {
			JSONObject data = json.getJSONObject("__data");
			deviceType = data.getString("deviceType");
			if (data.has("deviceId"))
				deviceId = data.getString("deviceId");
			else {
				logger.log(Level.WARNING, "Client "+socket+" did not provide deviceId on registration; assuming default");
				deviceId = "000000000000000";
			}
			registered = true;
			logger.info("Registered "+deviceType+":"+deviceId+" as "+socket);
			
			// register with game server
			conversationUrl = registerClient(deviceId);
			
			// TODO: support for more than one client at once!
			client = new Client(conversationUrl, deviceId);
			List<String> clientClassNames =new LinkedList<String>();
			clientClassNames.add("uk.ac.horizon.ug.hyperplace.facts.HyperplaceClient");
			client.connect(clientClassNames);
			logger.info("Registered with server");

			// return success
			JSONObject clientResponse = new JSONObject();
			clientResponse.put("action", "REGISTER");
			clientResponse.put("success", true);
			clientResponse.put("text", "Registered with hyperplace proxy");

			poll(clientResponse, true, true);
		}
		/** lobby-type registration 
		 * @throws IOException */
		private String registerClient(String deviceId) throws IOException {
			URL registerUrl = new URL(serverUrl+"1/registerclient");
			
			HttpURLConnection conn = (HttpURLConnection) registerUrl.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.addRequestProperty("Content-Type", "application/xml");
			OutputStream os = conn.getOutputStream();
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
			// TODO better
			String conversationId = ""+System.currentTimeMillis();
			pw.print("<conversation><conversationId>"+conversationId+
					"</conversationId><clientId>"+deviceId+"</clientId><clientType>Hyperplace</clientType>"+
					"<sessionId>"+sessionId+"</sessionId><creationTime>"+System.currentTimeMillis()+
					"</creationTime><lastContactTime>0</lastContactTime>"+
					"<status>ACTIVE</status></conversation>");
			pw.flush();
			pw.close();

			int status = conn.getResponseCode();
			if (status!=200) {
				throw new IOException("Error response ("+status+") from server: "+conn.getResponseMessage());
			}
			InputStream is = conn.getInputStream();
			is.close();
			logger.info("Registered client "+deviceId+" with server as conversation "+conversationId);
			
			return serverUrl+"1/client/"+conversationId+"/messages";
		}
		private void poll(JSONObject clientResponse, boolean updateMainState, boolean updateActionState) throws IOException, JSONException {
			
			List<Message> pollResponses = client.poll();			
			
			JSONObject clientUpdate = getHPUpdate(clientResponse);
			
			// messages (and check other updates)
			List<JSONObject> serverMessages = new LinkedList<JSONObject>();
			for (Message pollResponse : pollResponses) {
				if (pollResponse.getType()==MessageType.FACT_ADD || pollResponse.getType()==MessageType.FACT_EX) {
					try {
						JSONObject val = new JSONObject(pollResponse.getNewVal());
						String typeName = val.getString("typeName");
						if (typeName.equals("HyperplaceMessage"))
							serverMessages.add(val);
						else if (!updateMainState && (typeName.equals("HyperplaceTab") || typeName.equals("HyperplaceAsset")))
							updateMainState = true;
						else if (!updateActionState && (typeName.equals("HyperplaceAction")))
							updateActionState = true;
					}
					catch (Exception e) {
						logger.log(Level.WARNING, "Error checking added fact for message: "+pollResponse.getNewVal());
					}
				}
				else if (!updateActionState && (pollResponse.getType()==MessageType.FACT_UPD || pollResponse.getType()==MessageType.FACT_DEL)) {
					// may need to update actions...
					try {
						JSONObject val = new JSONObject(pollResponse.getNewVal());
						String typeName = val.getString("typeName");
						if (!updateActionState && (typeName.equals("HyperplaceAction")))
							updateActionState = true;
					}
					catch (Exception e) {
						logger.log(Level.WARNING, "Error checking added fact for message: "+pollResponse.getNewVal());
					}					
				}
			}
			client.getFacts("HyperplaceMessage");
			logger.info("Found "+serverMessages.size()+" HyperplaceMessage s");
			for (JSONObject serverMessage : serverMessages) {
				try {
					addClientMessageUpdate(clientUpdate, this.getHPMessage(serverMessage));
				}
				catch (Exception e) {
					logger.log(Level.WARNING, "Problem building HPMessage from "+serverMessage, e);
				}
			}

			
			if (updateMainState) {
				List<JSONObject> tabs = client.getFacts("HyperplaceTab");
				logger.info("Found "+tabs.size()+" HyperplaceTab s");

				List<JSONObject> assets = client.getFacts("HyperplaceAsset");
				logger.info("Found "+tabs.size()+" HyperplaceTab s");

				// Main State
				JSONObject mainState = new JSONObject();
				// common to all Statelets: __name, __type, __completed, __errorMessage
				mainState.put("__name", "PyramidMainState");
				mainState.put("__type", "HPMainState");
				mainState.put("__completed", true);
				mainState.put("__errorMessage", (Object)null);
				// specific to HPMainState
				JSONArray mainStateAssets = new JSONArray();
				mainState.put("assets", mainStateAssets);
				JSONArray mainStateTabs = new JSONArray();
				mainState.put("tabs", mainStateTabs);

				for (JSONObject tab : tabs) {
					try {
						mainStateTabs.put(getTab(tab));
					}
					catch (Exception e) {
						logger.log(Level.WARNING, "Error converting HyperplaceTab "+tab+" to HPTab: "+e);
					}
				}
				for (JSONObject asset : assets) {
					try {
						mainStateAssets.put(asset.get("url"));
					}
					catch (Exception e) {
						logger.log(Level.WARNING, "Error converting HyperplaceAsset "+asset+" to HPMainState: "+e);
					}
				}
				addClientStateUpdate(clientUpdate, mainState);
			}			
			// 
			if (updateActionState) {
				List<JSONObject> actions = client.getFacts("HyperplaceAction");
				logger.info("Found "+actions.size()+" HyperplaceAction s");

				try {
					addClientStateUpdate(clientUpdate, getHPActionsState(actions));
				}
				catch (Exception e) {
					logger.log(Level.WARNING, "Problem building HPActionsState", e);
				}
			}
			// send update
			if (clientResponse!=null || updateActionState || updateMainState || serverMessages.size()>0)
				send(clientUpdate);
		}
		public void handleAction(JSONObject json) throws JSONException, IOException {
			// TODO Auto-generated method stub
			// E.g. {"__timestamp":1277283960422,"__data":{"Submit":false},"__name":"HPActionForm"}
			// E.g. MOVE message:
			// {"__data":{"provider":"gps","longitude":-1.1870920658111572,"latitude":52.9539030790329,"accuracy":48,"speed":0},"__name":"MOVE","__timestamp":1277307218485}

			String actionName = json.getString("__name");
			JSONObject data = json.getJSONObject("__data");
			JSONObject val = new JSONObject();
			val.put("namespace", "uk.ac.horizon.ug.hyperplace.facts");
			val.put("time", System.currentTimeMillis());

			if (actionName.equals("MOVE")) {
				// position update
				val.put("typeName", "GeoPosition");
				val.put("subjectId", client.getClientId());
				val.put("latitude", data.getDouble("latitude"));
				val.put("longitude", data.getDouble("longitude"));
				// cast?!
				val.put("accuracy", data.getDouble("accuracy"));
				val.put("provider", data.getString("provider"));
			}
			else {

				// action
				val.put("typeName", "HyperplaceActionPerformed");
				val.put("clientId", client.getClientId());
				val.put("jsonData", data.toString());
				val.put("actionName", actionName);
			}
			logger.info("Sending action to server: "+val);
			client.sendMessage(client.addFactMessage(val.toString()));
			
			poll(null, false, false);
		}
		
		static final String DEFAULT_ICON = "http://www.mrl.nott.ac.uk/~cmg/unknown-icon.png";
		/** convert game server HyperplaceTab to HPTab.
		 * HyperplaceTab has: name, type, rank (int), stateGroup.
		 * HPTab has: target (HPActionTab, HPARTab, HPDebugTab, HPWebTab, HPMapTab), title, enabled (boolean), visible (boolean), icon, stateUpdates (String[]).
		 * 
		 * @param hyperplaceTab
		 * @return
		 * @throws JSONException 
		 */
		private JSONObject getTab(JSONObject hyperplaceTab) throws JSONException {
			JSONObject json = new JSONObject();
			json.put("target", hyperplaceTab.get("type"));
			if (hyperplaceTab.has("name"))
				json.put("title", hyperplaceTab.get("name"));
			else
				json.put("title", hyperplaceTab.get("type"));
			json.put("enabled", true);
			json.put("visible", true);
			// icon?
			if (hyperplaceTab.has("icon"))
				json.put("icon", hyperplaceTab.get("icon"));
			else
				// default
				json.put("icon", DEFAULT_ICON);
			JSONArray stateUpdates = new JSONArray();
			json.put("stateUpdates", stateUpdates);
			if (hyperplaceTab.has("stateGroup"))
				stateUpdates.put(hyperplaceTab.get("stateGroup"));
			else {
				String stateGroup = hyperplaceTab.getString("type");
				if (stateGroup.startsWith("HP"))
					stateGroup = stateGroup.substring(2);
				if (stateGroup.endsWith("Tab"))
					stateGroup = stateGroup.substring(0, stateGroup.length()-3);
				stateUpdates.put(stateGroup);			
			}
			return json;
		}
		/** make HPActionsState.
		 * Like all statelets it has: __name (to match stateGroup?), __type (HPActionsState), __completed (boolean), __errorMessage (null).
		 * Also has actions = array of HPActionForm.
		 * HPActionForm has: __title, __name (name of class! default HPActionForm) and elements (array of ActionFormElement).
		 * ActionFormElement has: __type (class name), __name, __returnable (boolean)
		 * Subclasses of ActionFormElement:
		 * - SubmitButton (returnable = false)
		 * - HiddenBoolean/Double/Int/String: has value
		 * - ComboBox: has label, defaultOption (int), options (map String->String)
		 * - TextArea: has text.
		 * @throws JSONException 
		 */
		private JSONObject getHPActionsState(List<JSONObject> actions ) throws JSONException {
			JSONObject json = new JSONObject();
			json.put("__name", "Action");
			json.put("__type", "HPActionsState");
			json.put("__completed", true);
			json.put("__errorMessage", (Object)null);
			JSONArray actionForms = new JSONArray();
			json.put("actions", actionForms);
			for (JSONObject action : actions) {
				JSONObject actionForm = new JSONObject();
				actionForms.put(actionForm);
				actionForm.put("__title", action.get("title"));
				if (action.has("actionName"))
					actionForm.put("__name", action.get("actionName"));
				else if (action.has("title"))
					actionForm.put("__name", action.get("title"));
				else
					actionForm.put("__name", "HPActionForm");
				if (action.has("jsonData")) {
					// ...?
				}
				JSONArray elements = new JSONArray();
				actionForm.put("elements", elements);
				
				// TODO
				
				JSONObject submit = new JSONObject();
				submit.put("__type", "SubmitButton");
				submit.put("__name", "Submit");
				submit.put("__returnable", false);
				elements.put(submit);
			}
			
			return json;
		}
		/** convert HyperplaceMessage to HPMessage.
		 * 	HyperplaceMessage:
		 * standard : boolean // for all clients
	clientId : String @to("HyperplaceClient") // may be null for standard
	message : String
	vibrate : int // duration
	dialog : boolean // dialog (rather than toast) message (may not be supported in Hyperplace client)
	dialogTitle : String // dialog title (may not be supported in Hyperplace client)
	dialogButtonText : String // (may not be supported in Hyperplace client)
	dialogIcon : String // (may not be supported in Hyperplace client)
		 * @throws JSONException 

		 * 
		 */
		private JSONObject getHPMessage(JSONObject serverMsg) throws JSONException {
			JSONObject clientMsg = new JSONObject();
			if (serverMsg.has("message"))
				clientMsg.put("message", serverMsg.get("message"));
			if (serverMsg.has("vibrate")) {
				int vibrate= serverMsg.getInt("vibrate");
				clientMsg.put("vibrate", vibrate);
			}
			if (serverMsg.has("dialog") && serverMsg.getBoolean("dialog")) {
				clientMsg.put("messageType", "dialog");
				JSONObject clientDialog = new JSONObject();
				clientMsg.put("dialogConfiguration", clientDialog);
				if (serverMsg.has("dialogTitle"))
					clientDialog.put("title", serverMsg.get("dialogTitle"));
				if (serverMsg.has("dialogButtonTest"))
					clientDialog.put("buttonText", serverMsg.get("dialogButtonText"));
				if (serverMsg.has("dialogIcon"))
					clientDialog.put("icon", serverMsg.get("dialogIcon"));
			} else if (serverMsg.has("messageType"))
				clientMsg.put("messageType", serverMsg.get("messageType"));
			else
				clientMsg.put("messageType", "shortToast");
			return clientMsg;
		}
		/** HPMapState is a statelet with __type HPMapState, __name classname, __completed, __errorMessage
		 * optional showMyLocation : boolean, 
		 * optional viewCentreUpdate: { viewCenterLatitude : double, viewCenterLongitude: double,
		 * 	                            viewZoom : int, animateToView : boolean, reachedMessage : String }
		 * plus locations - array of { [id : int - unused], longitude : double , latitude : double , 
		 * 				               marker : String (image URL) (required), label : String .
		 *                             optional dialogMessage : String }
		 * @param rdata
		 * @throws IOException
		 */
		/*
		 * @param rdata
		 * @throws IOException
		 */
		private void send(JSONObject rdata) throws IOException {
			// TODO Auto-generated method stub
			if (out==null)
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(rdata.toString());
			out.write("\n");
			out.flush();
			logger.info("Sent to "+socket+": "+rdata);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame("HyperplaceProxy");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new JLabel("Port: "+DEFAULT_PORT));
		frame.pack();
		frame.setVisible(true);
		
		new Main(DEFAULT_PORT, null, null);
	}
}
