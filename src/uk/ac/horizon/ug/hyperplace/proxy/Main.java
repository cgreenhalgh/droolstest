/**
 * 
 */
package uk.ac.horizon.ug.hyperplace.proxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.horizon.ug.exserver.clientapi.client.Client;
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
	public Main(int defaultPort) {
		try {
		// TODO Auto-generated constructor stub
			while (serverUrl==null)
				serverUrl = JOptionPane.showInputDialog("Server URL:");
			while (sessionId==null)
				sessionId = JOptionPane.showInputDialog("Session ID:");
			serverSocket = new ServerSocket(defaultPort);
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
    				else {
    					// TODO
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
			// TODO: support for more than one client at once!
			client = new Client(serverUrl, deviceId);
			List<String> clientClassNames =new LinkedList<String>();
			clientClassNames.add("uk.ac.horizon.ug.hyperplace.facts.HyperplaceClient");
			client.connect(clientClassNames);
			logger.info("Registered with server");
			client.poll();
			List<JSONObject> tabs = client.getFacts("HyperplaceTab");
			logger.info("Found "+tabs.size()+" HyperplaceTab s");
			// TODO...
			
			// return success
			JSONObject resp = new JSONObject();
			resp.put("__type", "HPUpdate");
			resp.put("__timestamp", System.currentTimeMillis());
			JSONObject rdata = new JSONObject();
			resp.put("__data", rdata);
			JSONObject response = new JSONObject();
			response.put("action", "REGISTER");
			response.put("success", true);
			response.put("text", "Registered with hyperplace proxy");
			rdata.put("__responseUpdate", response);
			
			JSONObject mainState = new JSONObject();
			mainState.put("__name", "PyramidMainState");
			mainState.put("__type", "HPMainState");
			mainState.put("__completed", true);
			mainState.put("__errorMessage", (Object)null);
			mainState.put("assets", new JSONArray());
			mainState.put("tabs", new JSONArray());
			JSONArray stateUpdates = new JSONArray();
			stateUpdates.put(mainState);
			rdata.put("__stateUpdates", stateUpdates);
			
			send(resp);
		}
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
		new Thread(new Main(DEFAULT_PORT)).start();
	}
	
}
