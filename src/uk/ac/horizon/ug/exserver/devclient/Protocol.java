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

import org.drools.builder.KnowledgeBuilderError;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.DomDriver;

import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.protocol.RulesetError;
import uk.ac.horizon.ug.exserver.protocol.RulesetErrors;
import uk.ac.horizon.ug.exserver.protocol.SessionBuildResult;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription;

/** Protocol/API for communicating with server.
 * Note that using Xstream in an application requires additional permissions.
 * E.g. <pre>
 * grant codeBase "http://localhost:8080/droolstest/devclient/*" {
 *   permission java.lang.RuntimePermission "createClassLoader";
 *   permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
 *   permission java.lang.RuntimePermission "accessDeclaredMembers";
 * };
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
	static String RELOAD_RULES_PATH = "1/sessions/{sessionId}/reload";
	static String TYPES_PATH = "1/sessions/{sessionId}/types";
	static String SESSION_ID_PATTERN = "{sessionId}";
	static String HTTP_GET = "GET";
	static String HTTP_POST = "POST";
	
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

	/** reload rules 
	 * @throws IOException */
	public SessionBuildResult reloadRules(String sessionId) throws IOException {
		InputStream is = this.doRequest(HTTP_POST, RELOAD_RULES_PATH.replace(SESSION_ID_PATTERN, sessionId));
		XStream xs = new XStream(new PureJavaReflectionProvider(), new DomDriver());
		xs.alias("result", SessionBuildResult.class);
		xs.alias("errors", RulesetErrors.class);
		xs.alias("error", RulesetError.class);

		SessionBuildResult result = (SessionBuildResult) xs.fromXML(is);
		is.close();		
		
		return result;
	}
	
	/** get types 
	 * @throws IOException */
	public List<TypeDescription> getTypes(String sessionId) throws IOException {
		InputStream is = this.doRequest(HTTP_GET, TYPES_PATH.replace(SESSION_ID_PATTERN, sessionId));
		XStream xs = new XStream(new PureJavaReflectionProvider(), new DomDriver());
		xs.alias("type", TypeDescription.class);
		xs.alias("field", TypeFieldDescription.class);
		List<TypeDescription> result = (List<TypeDescription>) xs.fromXML(is);
		is.close();		
		return result;
	}
	
}
