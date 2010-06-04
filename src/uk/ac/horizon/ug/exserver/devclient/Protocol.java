/**
 * 
 */
package uk.ac.horizon.ug.exserver.devclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.parsers.FactoryConfigurationError; 
import javax.xml.parsers.ParserConfigurationException; 
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException; 
import org.xml.sax.SAXParseException; 
import org.w3c.dom.Document;
import org.w3c.dom.DOMException; 
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.drools.builder.KnowledgeBuilderError;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.DomDriver;

import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.protocol.Operation;
import uk.ac.horizon.ug.exserver.protocol.OperationResult;
import uk.ac.horizon.ug.exserver.protocol.OperationStatus;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;
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
	static String RAWFACTS_PATH = "1/sessions/{sessionId}/rawfacts";
	static String SESSION_ID_PATTERN = "{sessionId}";
	static String HTTP_GET = "GET";
	static String HTTP_POST = "POST";
	
	/** do request 
	 * @throws IOException */
	InputStream doRequest(String method, String path) throws IOException {
		return doRequest(method, path, null);
	}
	
	/** do request 
	 * @throws IOException */
	InputStream doRequest(String method, String path, String body) throws IOException {
		URL url = new URL(server+path);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setUseCaches(false);
		conn.setRequestMethod(method);
		conn.setDoOutput(body!=null);
		System.err.println("Doing "+method+" on "+url);
		conn.connect();
		if (body!=null) {
			// write request
			OutputStream os = conn.getOutputStream();
			os.write(body.getBytes(Charset.forName("UTF-8")));
			os.close();
		}
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
	
	/** get facts
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException */
	public List<RawFactHolder> getFacts(String sessionId) throws IOException, ParserConfigurationException, SAXException {
		InputStream is = this.doRequest(HTTP_GET, RAWFACTS_PATH.replace(SESSION_ID_PATTERN, sessionId));
		List<RawFactHolder> result = new LinkedList<RawFactHolder>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse( is );
		is.close();
		
		//XStream xs = new XStream(new PureJavaReflectionProvider(), new DomDriver());
		//xs.alias("list", LinkedList.class);
		//xs.alias("holder", RawFactHolder.class);
		//xs.alias("fact", HashMap.class);
		
		Element rootEl = document.getDocumentElement();
		NodeList holders = rootEl.getElementsByTagName("holder");
		for (int hi=0; hi<holders.getLength(); hi++) {
			Element holderEl = (Element)holders.item(hi);
			result.add(parseHolder(holderEl));
		}
		return result;
	}
	/** parse a fact holder */
	static RawFactHolder parseHolder(Element holderEl) {
		Element handleEl = getElementByTagName(holderEl, "handle");
		Element factEl = getElementByTagName(holderEl, "fact");
		RawFactHolder fh = new RawFactHolder();
		if (handleEl!=null)
			fh.setHandle(handleEl.getTextContent());
		if (factEl!=null) {
			Fact fact = new Fact();
			String className = factEl.getAttribute("class");
			int ix = className.lastIndexOf(".");
			if (ix<0) 
				fact.setTypeName(className);
			else {
				fact.setNamespace(className.substring(0, ix));
				fact.setTypeName(className.substring(ix+1));
			}
			NodeList children = factEl.getChildNodes();
			Map<String,Object> values = new HashMap<String,Object>();
			for (int ci=0; ci<children.getLength(); ci++) {
				Node childNode = children.item(ci);
				if (childNode instanceof Element) {
					Element childEl = (Element)childNode;
					String fieldName = childEl.getTagName();
					String value = childEl.getTextContent();
					values.put(fieldName, value);
				}
			}
			fact.setFieldValues(values);
			fh.setFact(fact);
		}
		return fh;
	}
	/** get first (only?!) named element */
	static Element getElementByTagName(Element parent, String name) {
		NodeList elements = parent.getElementsByTagName(name);
		if (elements.getLength()==0)
			return null;
		return (Element)elements.item(0);
	}
	
	/** send changes 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 * @throws IOException 
	 * @throws SAXException */
	public List<OperationResult> makeChanges(String sessionId, List<RawFactHolder> changes) throws ParserConfigurationException, TransformerException, IOException, SAXException  {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element root = doc.createElement("list");
		doc.appendChild(root);
		for (RawFactHolder fh : changes) {
			Element holder = doc.createElement("holder");
			root.appendChild(holder);
			Element operation = doc.createElement("operation");
			holder.appendChild(operation);
			operation.appendChild(doc.createTextNode(fh.getOperation().name()));
			if (fh.getHandle()!=null && fh.getHandle().length()>0)
			{
				Element handle = doc.createElement("handle");
				holder.appendChild(handle);
				handle.appendChild(doc.createTextNode(fh.getHandle()));
			}
			if (fh.getFact()!=null && fh.getOperation()!=Operation.delete) {
				Fact fact = (Fact)fh.getFact();
				Element factEl = doc.createElement("fact");
				holder.appendChild(factEl);
				factEl.setAttribute("class", fact.getNamespace()+"."+fact.getTypeName());
				Map<String,Object> fields = fact.getFieldValues();
				for (Map.Entry<String, Object> entry : fields.entrySet()) {
					if (entry.getValue()==null)
						continue;
					Element fieldEl = doc.createElement(entry.getKey());
					factEl.appendChild(fieldEl);
					fieldEl.appendChild(doc.createTextNode(entry.getValue().toString()));					
				}
			}
		}
		
		// convert to String
        //set up a transformer
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        //create string from xml tree
        StringWriter sw = new StringWriter();
        StreamResult request = new StreamResult(sw);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, request);
        String xmlString = sw.toString();

        System.out.println("Writing request: "+xmlString);
        
        // make request
        InputStream is = doRequest(HTTP_POST, RAWFACTS_PATH.replace(SESSION_ID_PATTERN, sessionId), xmlString);
        
        // parse response
		List<OperationResult> result = new LinkedList<OperationResult>();
		//DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse( is );
		is.close();
		
		Element rootEl = document.getDocumentElement();
		NodeList results = rootEl.getElementsByTagName("result");
		for (int hi=0; hi<results.getLength(); hi++) {
			Element resultEl = (Element)results.item(hi);
			OperationResult res = new OperationResult();
			Element statusEl = getElementByTagName(resultEl, "status");
			if (statusEl!=null)
				res.setStatus(OperationStatus.valueOf(statusEl.getTextContent()));
			Element holderEl = getElementByTagName(resultEl, "holder");
			if (holderEl!=null)
				res.setHolder(parseHolder(holderEl));
			Element handleEl = getElementByTagName(resultEl, "handle");
			if (handleEl!=null)
				res.setHandle(handleEl.getTextContent());
			result.add(res);
		}
		return result;
	}
	
}
