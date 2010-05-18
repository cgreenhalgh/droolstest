/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.drools.runtime.rule.FactHandle;
import org.drools.KnowledgeBase;
import org.drools.common.InternalFactHandle;
import org.drools.common.DisconnectedFactHandle;
import org.drools.definition.type.FactType;

import org.restlet.data.MediaType;
import org.restlet.ext.xml.XmlConverter;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.ext.xstream.XstreamConverter;
//import org.restlet.ext.xml.XmlRepresentation;
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

import java.util.logging.Level;
import java.util.logging.Logger;

/** "raw" facts, i.e. Xstream (or equivalent) of RawFactHolders & fact objects
 * 
 * @author cmg
 *
 */
public class RawSessionResource extends SessionResource {
	static Logger logger = Logger.getLogger(RawSessionResource.class.getName());
	
	/** XML list of all facts */
    @Get("xml")   
    public Representation toXml() throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
    	//
    	List<RawFactHolder> facts = new LinkedList<RawFactHolder>();
    	synchronized(droolsSession) {
        	UserTransaction ut = this.getTransaction();
        	ut.begin();
        	try {
        		EntityManager em = getEntityManager();
        		Collection<FactHandle> factHandles = droolsSession.getKsession().getFactHandles();
        		KnowledgeBase kb = droolsSession.ksession.getKnowledgeBase();
        		for (FactHandle fh : factHandles) {
        			//logger.info("Looking for fact "+fh+" ("+fh.getClass()+")");
        			// this doesn't work in 5.1.0.M1 but is fixed in SVN
        			//Object fact = droolsSession.getKsession().getObject(fh);
        			//logger.info("Found fact "+fh+": "+fact);
        			RawFactHolder rf = new RawFactHolder();
        			rf.setHandle(fh.toExternalForm());
        			if (fh instanceof InternalFactHandle) {
        				InternalFactHandle ifh = (InternalFactHandle)fh;
        				rf.setFact(ifh.getObject());
        			}
        			//
        			facts.add(rf);
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
    	XstreamRepresentation<List<RawFactHolder>> xml = new XstreamRepresentation<List<RawFactHolder>>(MediaType.APPLICATION_XML, facts);
    	addAliases(xml);
    	return xml;
    }
    public static final String ELEMENT_HOLDER = "holder";
    
    private static void addAliases(XstreamRepresentation xr) {
    	xr.getXstream().alias(ELEMENT_HOLDER, RawFactHolder.class);
    	xr.getXstream().alias("list", LinkedList.class);    	
//    	xr.getXstream().getConverterLookup().
    }
    /**  
     * Handle POST requests: process facts (add/update/delete).
     * This version attempts to use Xstream but at present this doesn't work with Drools
     * TRL-declared types: they are not accessible via the default class-loader as used by 
     * Xstream. I'm not sure if they are loadable in the normal way or not, since they 
     * are somehow included in the serialized Drools knowledge package. They are not 
     * accessible via the knowledge base class class loader, the context class loader or the 
     * class loader for this class. There is some kind of drools package registry that 
     * may give a clue...
     * (Drools ClassBuilder makes the class bytecode)
     */  
    //@Post  
    public Representation addFactsXstream(Representation entity)
    throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
    	Representation result = null;   
        // Parse the given representation and retrieve pairs of   
        // "name=value" tokens.   
        List<RawFactHolder> facts = new LinkedList<RawFactHolder>();
        // parse ... ?!
        try {
        	Form form = new Form(entity);   
        	String factText = form.getFirstValue("facts");   
        	if (factText==null || factText.length()==0) {
        		return null;
        	}
        	
        	// this won't work if it includes drools defined classes...
        	XstreamRepresentation xml = new XstreamRepresentation<List<RawFactHolder>>(new StringRepresentation(factText));
        	addAliases(xml);

        	facts = (List<RawFactHolder>) new XstreamConverter().toObject(xml, List.class, null);
        }
        catch (Exception e) {
        	logger.log(Level.WARNING, "error parsing input xml", e);
        	setStatus(Status.SERVER_ERROR_INTERNAL, e);
        	return null;
        }
        return addFacts(facts);
    }
    
    /**  
     * Handle POST requests: create a new session.
     * Long-winded parsing of Xstream-type format, but without using Xstream because of the
     * class loading problem noted above.  
     */  
    @Post  
    public Representation addFacts(Representation entity) throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
        // Parse the given representation and retrieve pairs of   
        // "name=value" tokens.   
        LinkedList<RawFactHolder> facts = new LinkedList<RawFactHolder>();
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
        	NodeList factHolderNodes = root.getElementsByTagName(ELEMENT_HOLDER);
        	for (int fni=0; fni<factHolderNodes.getLength(); fni++) {
        		Element factHolderNode = (Element)factHolderNodes.item(fni);
        		RawFactHolder fh = new RawFactHolder();
        		String factHandleText = getTextContentByTagName(factHolderNode, ELEMENT_HANDLE);
        		if (factHandleText!=null)
        			fh.setHandle(factHandleText);
        		// ?? DisconnectedFactHandle ?!
        		String operation = getTextContentByTagName(factHolderNode, ELEMENT_OPERATION);
        		if (operation!=null && operation.length()>0)
        			fh.setOperation(Operation.valueOf(operation));
        		else if (factHandleText!=null && factHandleText.length()>0)
        			fh.setOperation(Operation.update);
        		if (fh.getOperation()!=Operation.add && fh.getHandle()==null) {
        			logger.log(Level.WARNING,"Holder element for "+fh.getOperation()+" does not have handle element");
        			setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Holder element for "+fh.getOperation()+" does not have handle element");
        			return null;
        		}
        		
        		Element factNode = getElementByTagName(factHolderNode, ELEMENT_FACT);
        		if (factNode!=null) {
        			String className = factNode.getAttribute(ATTRIBUTE_CLASS);
        			if (className==null || className.length()==0)  {
        				logger.log(Level.WARNING,"Fact element without class attribute");
            			setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Fact element without class attribute");
            			return null;
        			}
        			int ix = className.lastIndexOf('.');
        			String packageName = null;
        			if (ix>=0)
        			{
        				packageName = className.substring(0, ix);
        				className = className.substring(ix+1);
        			}
        			FactType factType = kb.getFactType(packageName, className);
        			if (factType==null) {
        				logger.log(Level.WARNING,"Unknonwn fact type "+packageName+"."+className);
        				setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Unknonwn fact type "+packageName+"."+className);
        				return null;
        			}
        			Object fact = factType.newInstance();
        		
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
        			factType.setFromMap(fact, fields);
        			fh.setFact(fact);
        		}
        		facts.add(fh);
        	}
        }
        catch (Exception e) {
        	logger.log(Level.WARNING,"Handling raw fact post", e);
        	setStatus(Status.SERVER_ERROR_INTERNAL, e);
        	return null;
        }
        return this.addFacts(facts);
    }  
    public static final String ELEMENT_FACT = "fact";
    public static final String ELEMENT_OPERATION = "operation";
    public static final String ELEMENT_HANDLE = "handle";
    public static final String ATTRIBUTE_CLASS = "class";
    /** util - first element of name */
    public static Element getElementByTagName(Element node, String tagName) {
    	NodeList elements = node.getElementsByTagName(tagName);
    	if (elements.getLength()==0)
    		return null;
    	return (Element)elements.item(0);
    }
    /** util - text content of first element by TagName (or null) */
    public static String getTextContentByTagName(Element node, String tagName) {
    	Element el = getElementByTagName(node, tagName);
    	if (el==null)
    		return null;
    	String text = el.getTextContent().trim();
    	if (text.length()==0)
    		return null;
    	return text;
    }

    /** add facts once parsed */
    public Representation addFacts(List<RawFactHolder> facts) 
    throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
        logger.info("facts = "+facts);
        synchronized (droolsSession) {
        	UserTransaction ut = this.getTransaction();
        	ut.begin();
        	try {
        		EntityManager em = this.getEntityManager();
        		for (RawFactHolder rfh : facts) {
        			switch (rfh.getOperation()) {
        			case add: {
        				FactHandle fh = droolsSession.getKsession().insert(rfh.getFact());
        				logger.info("added "+fh+": "+rfh.getFact());
        				break;
        			}
        			case update: {
        				FactHandle fh = new DisconnectedFactHandle(rfh.getHandle());
        				droolsSession.getKsession().update(fh, rfh.getFact());
        				logger.info("updated "+fh+": "+rfh.getFact());
        				break;
        			}
        			case delete: {
        				FactHandle fh = new DisconnectedFactHandle(rfh.getHandle());
        				droolsSession.getKsession().retract(fh);
        				logger.info("deleted "+fh);
        				break;	
        			}
        			}
        		}
        		ut.commit();
        		em.close();
        	}
        	catch (Exception e) {
            	logger.log(Level.WARNING, "error adding facts "+facts, e);
        		ut.rollback();
        		setStatus(Status.SERVER_ERROR_INTERNAL, e);
        		return null;
        	}
        }
       
        return null;   
    }  
}
