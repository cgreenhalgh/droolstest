/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.drools.runtime.rule.FactHandle;
import org.drools.KnowledgeBase;
import org.drools.common.InternalFactHandle;
import org.drools.definition.type.FactType;
import org.drools.definition.type.FactField;

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
import uk.ac.horizon.ug.exserver.protocol.Operation;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;

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
        			logger.info("Looking for fact "+fh+" = "+fh.toExternalForm()+" ("+fh.getClass()+")");
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
        		logger.log(Level.WARNING,"getting session state", e);
        		ut.rollback();
        		setStatus(Status.SERVER_ERROR_INTERNAL, e);
        		return null;
        	}
    	}
    	XstreamRepresentation<List<RawFactHolder>> xml = new XstreamRepresentation<List<RawFactHolder>>(MediaType.APPLICATION_XML, facts);
    	addAliases(xml);
		// immediate expire?
		xml.setExpirationDate(new Date());
    	return xml;
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
     * Add facts as form - only URL-encoded at present.
     */  
    //@Post  
    public Representation addFactsForm(Representation entity) throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
        // parse ... ?!
       	Form form = new Form(entity);   
       	String factText = form.getFirstValue("facts");   
       	if (factText==null || factText.length()==0) {
       		return null;
       	}
       	return addFacts(new StringRepresentation(factText));
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
    		KnowledgeBase kb = droolsSession.ksession.getKnowledgeBase();

    		Document doc = new XmlConverter().toObject(entity, Document.class, null);
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
        		
        			NodeList fieldNodes = factNode.getChildNodes();
        			for (int fi=0; fi<fieldNodes.getLength(); fi++) {
        				if (!(fieldNodes.item(fi) instanceof Element))
        					continue;
        				Element fieldNode = (Element)fieldNodes.item(fi);
        				String fieldName= fieldNode.getNodeName();
        				String fieldValueText = fieldNode.getTextContent().trim();

        				FactField field = factType.getField(fieldName);
        				Object fieldValue = coerce(fieldValueText, field);
        				// type?
        				if (fieldValue!=null)
        					field.set(fact, fieldValue);
        			}
        			fh.setFact(fact);
        		}
        		else if (fh.getOperation()!=Operation.delete) {
    				logger.log(Level.WARNING,"Holder element without fact element for "+fh.getOperation());
        			setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Holder element without fact element for "+fh.getOperation());
        			return null;
        		}
        		// option to try to get handle for object
        		if (fh.getOperation()!=Operation.add && fh.getHandle()==null && fh.getFact()==null) {
        			logger.log(Level.WARNING,"Holder element for "+fh.getOperation()+" does not have handle (or fact) element");
        			setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Holder element for "+fh.getOperation()+" does not have handle (or fact) element");
        			return null;
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
}
