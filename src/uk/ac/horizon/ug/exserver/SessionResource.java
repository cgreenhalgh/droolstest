/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.drools.runtime.rule.FactHandle;
import org.drools.KnowledgeBase;
import org.drools.definition.type.FactType;
import org.drools.common.InternalFactHandle;

import org.restlet.data.MediaType;
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

import java.util.logging.Logger;

/**
 * @author cmg
 *
 */
public class SessionResource extends BaseResource {
	static Logger logger = Logger.getLogger(SessionResource.class.getName());
	
	/** session id */
	protected String sessionId;
	/** session object */
	protected Session sessionInfo;
	/** drools session */
	protected DroolsSession droolsSession;
	
	@Override  
    protected void doInit() throws ResourceException {   
        // Get the "itemName" attribute value taken from the URI template   
        // /items/{itemName}.   
        this.sessionId = (String) getRequest().getAttributes().get("sessionId");   
  
    	EntityManager em = getEntityManager();
    	this.sessionInfo = em.find(Session.class, sessionId);
    	if (this.sessionInfo==null)
    		throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

    	try {
    		this.droolsSession = DroolsSession.getSession(this.sessionInfo);
    	}   
    	catch (Exception e) {
    		throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not find internal session state for "+sessionInfo.getDroolsId());
    	}
	}  

	/** XML list of all facts */
    @Get("xml")   
    public Representation toXml() throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
    	//
    	List<FactHolder> facts = new LinkedList<FactHolder>();
    	synchronized(droolsSession) {
        	UserTransaction ut = this.getTransaction();
        	ut.begin();
        	try {
        		EntityManager em = getEntityManager();
        		Collection<FactHandle> factHandles = droolsSession.getKsession().getFactHandles();
        		KnowledgeBase kb = droolsSession.ksession.getKnowledgeBase();
        		for (FactHandle fh : factHandles) {
        			logger.info("Looking for fact "+fh+" ("+fh.getClass()+")");
        			// this doesn't work in 5.1.0.M1 but is fixed in SVN
        			//Object fact = droolsSession.getKsession().getObject(fh);
        			//logger.info("Found fact "+fh+": "+fact);
        			FactHolder f = new FactHolder();
        			f.setHandle(fh.toExternalForm());
        			if (fh instanceof InternalFactHandle) {
        				InternalFactHandle ifh = (InternalFactHandle)fh;
        				Object fact = ifh.getObject();
        				if (fact!=null) {
        					logger.info("Found fact "+fact+" ("+fact.getClass()+")");
        					f.setFact(fact);
            				String factTypeName = fact.getClass().getName();
            				int ix = factTypeName.lastIndexOf(".");
            				String packageName = null;
            				if (ix>=0) {
            					packageName = factTypeName.substring(0,ix);
            					factTypeName = factTypeName.substring(ix+1);
            				}
            				FactType ft = kb.getFactType(packageName, factTypeName);
            				if (ft==null) 
            					throw new RuntimeException("Could not find FactType "+packageName+":"+factTypeName);
            				f.setFactType(ft);
            				f.setFields(ft.getAsMap(fact));
            				f.setName(fact.getClass().getName());
        				}
        			}
        			//
        			facts.add(f);
        		}
        		ut.commit();
        		em.close();
        	}
        	catch (Exception e) {
        		ut.rollback();
        		setStatus(Status.SERVER_ERROR_INTERNAL, e);
        		return null;
        	}
    	}
    	XstreamRepresentation<List<FactHolder>> xml = new XstreamRepresentation<List<FactHolder>>(MediaType.APPLICATION_XML, facts);
    	return xml;
    }
    
    /**  
     * Handle POST requests: create a new session.  
     */  
    @Post  
    public Representation addFacts(Representation entity) throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
        Representation result = null;   
        // Parse the given representation and retrieve pairs of   
        // "name=value" tokens.   
        LinkedList<FactHolder> facts = new LinkedList<FactHolder>();
        // parse ... ?!
        try {
        	Form form = new Form(entity);   
        	String factText = form.getFirstValue("facts");   
        	if (factText==null || factText.length()==0) {
        		return null;
        	}
    		KnowledgeBase kb = droolsSession.ksession.getKnowledgeBase();
        	Document doc = new XmlConverter().toObject(new StringRepresentation(factText), Document.class, null);
        	Element root = doc.getDocumentElement();
        	NodeList factNodes = root.getChildNodes();
        	for (int fni=0; fni<factNodes.getLength(); fni++) {
        		if (!(factNodes.item(fni) instanceof Element))
        			continue;
        		FactHolder fh = new FactHolder();
        		Element factNode = (Element)factNodes.item(fni);
        		String factTypeName = factNode.getNodeName();
        		String packageName = factNode.getAttribute(ATTRIBUTE_PACKAGE);
        		if (packageName!=null)
        			fh.setName(packageName+"."+factTypeName);
        		else
        			fh.setName(factTypeName);
        		FactType factType = kb.getFactType(packageName, factTypeName);
        		if (factType==null) {
        			setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Unknonwn fact type "+packageName+":"+factTypeName+" ("+factNode+")");
        			return null;
        		}
        		fh.setFactType(factType);
        		String factHandleText = factNode.getAttribute(ATTRIBUTE_FACT_HANDLE);
        		// ?? DisconnectedFactHandle ?!
        		String operation = factNode.getAttribute(ATTRIBUTE_OPERATION);
        		if (operation!=null && operation.length()>0)
        			fh.setOperation(FactHolder.Operation.valueOf(operation));
        		else if (factHandleText!=null && factHandleText.length()>0)
        			fh.setOperation(FactHolder.Operation.update);
        		
        		HashMap<String,Object> fields = new HashMap<String,Object>();
        		NodeList fieldNodes = factNode.getChildNodes();
        		for (int fi=0; fi<fieldNodes.getLength(); fi++) {
            		if (!(fieldNodes.item(fi) instanceof Element))
            			continue;
            		Element fieldNode = (Element)fieldNodes.item(fi);
            		String fieldName= fieldNode.getNodeName();
            		String fieldValue = fieldNode.getTextContent().trim();
            		// type?
            		fields.put(fieldName, fieldValue);
        		}
        		fh.setFields(fields);
        		facts.add(fh);
        	}
        }
        catch (Exception e) {
        	setStatus(Status.SERVER_ERROR_INTERNAL, e);
        	return null;
        }
        logger.info("facts = "+facts);
        synchronized (droolsSession) {
        	UserTransaction ut = this.getTransaction();
        	ut.begin();
        	try {
        		EntityManager em = this.getEntityManager();
        		for (FactHolder fh : facts) {
        			switch (fh.getOperation()) {
        			case add: {
        				Object fact = fh.getFactType().newInstance();
        				fh.getFactType().setFromMap(fact, fh.getFields());
        				droolsSession.getKsession().insert(fact);
        				logger.info("added "+fact);
        				break;
        			}
        			case update:
        				// ...
        				break;
        			case delete:
        				// ....
        				break;
        			}
        		}
        		ut.commit();
        		em.close();
        	}
        	catch (Exception e) {
        		ut.rollback();
        		setStatus(Status.SERVER_ERROR_INTERNAL, e);
        		return null;
        	}
        }
       
        return null;   
    }  
    public static String ATTRIBUTE_FACT_HANDLE = "handle";
    public static String ATTRIBUTE_OPERATION = "operation";
    public static String ATTRIBUTE_PACKAGE = "package";
}
