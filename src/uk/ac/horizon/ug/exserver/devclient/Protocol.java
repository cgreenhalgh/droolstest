/**
 * 
 */
package uk.ac.horizon.ug.exserver.devclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.DomDriver;

import uk.ac.horizon.ug.exserver.model.Session;

/** Protocol/API for communicating with server.
 * Note that using Xstream in an application requires additional permissions.
 * E.g. <pre>
 * grant codeBase "http://localhost:8080/droolstest/devclient/*" {
 *   permission java.lang.RuntimePermission "createClassLoader";
 *   permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
 *   permission java.lang.RuntimePermission "accessDeclaredMembers";
 * }
 * </pre>
 * in the user's Applet policy file - use the Java console to check (dump properties,
 * check "deployment.user.security.policy"), e.g.
 * file:////keats/staff$/cmg/Application%20Data/Sun/Java/Deployment/security/java.policy
 * 
 * @author cmg
 *
 */
public class Protocol {
	String server;
	
	/** cons */
	public Protocol(String server) {
		setServer(server);		
	}

	/** get sessions relative path */
	static String GET_SESSIONS_PATH = "1/sessions";
	static String HTTP_GET = "GET";
	
	/** do request 
	 * @throws IOException */
	InputStream doRequest(String method, String path) throws IOException {
		URL url = new URL(server+path);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod(method);
		conn.setDoOutput(false);
		System.err.println("Doing "+method+" on "+url);
		conn.connect();
		int status  = conn.getResponseCode();
		if (status!=HttpURLConnection.HTTP_OK) {
			System.err.println("Error doing "+method+" on "+url+": "+status);
		}
		InputStream is = conn.getInputStream();
		return is;
	}
	
	public List<Session> getSessions() throws IOException {
		InputStream is = doRequest(HTTP_GET, GET_SESSIONS_PATH);
		XStream xs = new XStream(new PureJavaReflectionProvider(), new DomDriver());
		xs.alias("session", Session.class);
		
		List<Session> sessions = (List<Session>) xs.fromXML(is);
		is.close();
		return sessions;
	}
	
	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		if (!server.endsWith("/"))
			server = server+"/";
		this.server = server;
	}
	
}
