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
}
