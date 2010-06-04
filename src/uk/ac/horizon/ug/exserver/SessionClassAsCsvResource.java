/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.io.IOException;
import java.io.BufferedReader;

import java.util.List;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.drools.runtime.rule.FactHandle;
import org.drools.KnowledgeBase;
import org.drools.definition.type.FactField;
import org.drools.definition.type.FactType;
import org.drools.common.InternalFactHandle;

import org.restlet.data.MediaType;
import org.restlet.ext.xstream.XstreamConverter;
import org.restlet.ext.xstream.XstreamRepresentation;
//import org.restlet.ext.xml.XmlRepresentation;
import org.restlet.ext.xml.XmlConverter;
import org.restlet.resource.Get;   
import org.restlet.resource.Post;   
import org.restlet.representation.Representation;   
import org.restlet.representation.StringRepresentation;   
import org.restlet.resource.ResourceException;   

import org.restlet.data.Form;   
import org.restlet.data.MediaType;   
import org.restlet.data.Status;   

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.ac.horizon.ug.exserver.RestletApplication;
import uk.ac.horizon.ug.exserver.model.SessionTemplate;
import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.protocol.Operation;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Read/write session state (one class) as CSV
 * 
 * @author cmg
 *
 */
public class SessionClassAsCsvResource extends SessionResource {
	static Logger logger = Logger.getLogger(SessionClassAsCsvResource.class.getName());
	
	protected String className;
	protected FactType factType;
	
	@Override  
    protected void doInit() throws ResourceException {   
		super.doInit();
		this.className = (String)getRequest().getAttributes().get("className");
		if (className==null) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "className is null");
		}
		int ix = className.lastIndexOf('.');
		String packageName = null;
		String classNameOnly = className;
		if (ix>=0)
		{
			packageName = className.substring(0, ix);
			classNameOnly = className.substring(ix+1);
		}
		factType = droolsSession.getKsession().getKnowledgeBase().getFactType(packageName, classNameOnly);
		if (factType==null) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not find FactType for className "+className);
		}
	}  
	
	public static String FACT_HANDLE_HEADER = "FactHandle";
	
	@Get("csv") 
	public Representation getCsv() throws NamingException, NotSupportedException, SystemException {
		StringBuilder sb = new StringBuilder();
		List<FactField> fields = factType.getFields();
		sb.append(FACT_HANDLE_HEADER);
		for (FactField field : fields) {
			sb.append(","+escapeCsv(field.getName()));			
		}
		sb.append("\n");
        // parse ... ?!
       	synchronized(droolsSession) {
        	UserTransaction ut = this.getTransaction();
        	ut.begin();
        	try {
        		EntityManager em = getEntityManager();
        		Collection<FactHandle> factHandles = droolsSession.getKsession().getFactHandles();
        		KnowledgeBase kb = droolsSession.ksession.getKnowledgeBase();
        		for (FactHandle fh : factHandles) {
        			if (fh instanceof InternalFactHandle) {
        				InternalFactHandle ifh = (InternalFactHandle)fh;
        				Object object = ifh.getObject();
        				if (factType.getFactClass().isInstance(object)) {
        					sb.append(ifh.toExternalForm());
        					for (FactField field : fields) {
        						sb.append(",");
        						Object value = factType.get(object, field.getName());
        						if(value!=null) 
        							sb.append(escapeCsv(value.toString()));			
        					}
        					sb.append("\n");
        				}
        			}
        		}
        		ut.commit();
        		em.close();
        	}
        	catch (Exception e) {
        		logger.log(Level.WARNING,"getting session state", e);
        		ut.rollback();
        		setStatus(Status.SERVER_ERROR_INTERNAL, e);
        		return null;
        	}
    	}
       	
    	Representation result = new StringRepresentation(sb.toString(),MediaType.TEXT_CSV); 
    	result.setExpirationDate(new Date());
    	return result;
	}
	public static String escapeCsv(String s) {
		if (s==null)
			return s;
		StringBuilder sb = new StringBuilder();
		boolean quote = false;
		if (s.contains("\n") || s.contains("'") || s.contains("\"")) {
			quote = true;
			sb.append("\"");
		}
		for (int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '"':
				sb.append("\"\"");
				break;
			case '\n': 
				sb.append("\n");
				break;
			default:
				sb.append(c);
				break;
			}	
		}
		if (quote)
			sb.append("\"");
		return sb.toString();
	}
    /** parse csv line with escaping (, -> "...,..."; "  -> "...""...")
     */
    static String[] parseCsvLine(String line) 
    {
    	int pos = 0;
    	Vector<String> vals = new Vector<String>();
    	while(pos<line.length()) 
    	{
    		StringBuffer text = new StringBuffer();
    		boolean quoted = false;
    		boolean justUnquoted = false;
    		for (; pos<line.length() && 
    		!(line.charAt(pos)==',' && !quoted); pos++) 
    		{

    			if (line.charAt(pos)=='"') 
    			{
    				if (justUnquoted)
    					text.append('"');
    				quoted = !quoted;
    				justUnquoted = !quoted;
    			}
    			else 
    			{
    				justUnquoted = false;
    				text.append(line.charAt(pos));
    			}
    		}
    		vals.addElement(text.toString());
    		if (pos<line.length() && line.charAt(pos)==',') 
    		{
    			pos++;
    			if (pos>=line.length())
    				// trailing empty value
    				vals.addElement("");
    		}
    	}
    	return vals.toArray(new String[vals.size()]);
    }
    @Post
    public Representation postCsv(Representation request) throws IOException, NamingException, SystemException, NotSupportedException, RollbackException, HeuristicRollbackException, HeuristicMixedException, InstantiationException, IllegalAccessException {
    	List<RawFactHolder> facts = new LinkedList<RawFactHolder>();
    	
    	// parse
    	BufferedReader br = new BufferedReader(request.getReader());
    	String header = br.readLine();
    	if (header==null) {
    		setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"No header line present");
    		return null;
    	}
    	String headers[] = parseCsvLine(header);    	
    	FactField fields[] = new FactField[headers.length];
    	boolean includesHandle = headers.length>0 && FACT_HANDLE_HEADER.equals(headers[0]);
    	for (int i=includesHandle ? 1 : 0; i<headers.length; i++) {
    		fields[i] = factType.getField(headers[i]);
    		if (fields[i]==null) {
    			setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Unknown field "+headers[i]);
    			return null;
    		}
    	}
    	while (true) {
    		String line = br.readLine();
    		if (line==null)
    			break;
    		if (line.length()==0)
    			continue;
    		String values[] = parseCsvLine(line);
    		if (values.length==0)
    			continue;
    		Object fact = factType.newInstance();
    		RawFactHolder holder = new RawFactHolder();
    		if (includesHandle && values[0].length()>0) {
    			holder.setHandle(values[0]);
    			if (values.length==1)
    				// only handle => delete (?!)
    				holder.setOperation(Operation.delete);
    			else
    				holder.setOperation(Operation.update);
    		}
    		else
    			holder.setOperation(Operation.add);
    		for (int i=includesHandle ? 1 : 0; i<values.length; i++) {
				Object fieldValue = coerce(values[i], fields[i]);
				// type?
				if (fieldValue!=null)
					fields[i].set(fact, fieldValue);
    		}
    		if (holder.getOperation()!=Operation.delete)
    			holder.setFact(fact);
    		facts.add(holder);
    	}
    	
    	return this.addFacts(facts);
    }
}
