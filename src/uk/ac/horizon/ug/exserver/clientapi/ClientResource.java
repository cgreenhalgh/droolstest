/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import uk.ac.horizon.ug.exserver.BaseResource;
import uk.ac.horizon.ug.exserver.DroolsSession;
import uk.ac.horizon.ug.exserver.SessionResource;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.model.ConversationStatus;
import uk.ac.horizon.ug.exserver.model.Session;

/**
 * @author cmg
 *
 */
public class ClientResource extends BaseResource {
	static Logger logger = Logger.getLogger(ClientResource.class.getName());
	/** session id */
	protected String conversationId;
	/** conversation object */
	protected ClientConversation conversation;
	/** session */
	protected Session sessionInfo;
	/** drools session */
	protected DroolsSession droolsSession;
	@Override  
	/** initialise */
    public void doInit() throws ResourceException {   
        this.conversationId = (String) getRequest().getAttributes().get("conversationId");   
    	EntityManager em = getEntityManager();
    	this.conversation = em.find(ClientConversation.class, conversationId);
    	if (this.conversation==null)
    		throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    	if (conversation.getStatus()==ConversationStatus.ACTIVE) {
        	sessionInfo = em.find(Session.class, conversation.getSessionId());
        	if (sessionInfo==null) {
        		logger.log(Level.WARNING, "Could not find Session "+conversation.getSessionId());
        		throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
        	}
        	try {
        		this.droolsSession = DroolsSession.getSession(this.sessionInfo, em);
        	} catch (DroolsSession.RulesetException re) {
        		logger.log(Level.WARNING, "Error (rules) creating session "+sessionInfo.getDroolsId()+": "+re);
        		throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create internal session state for "+sessionInfo.getDroolsId()+": "+re);
        	}
        	catch (Exception e) {
        		logger.log(Level.WARNING, "Error creating drools session "+sessionInfo.getDroolsId(), e);
        		throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create internal session state for "+sessionInfo.getDroolsId()+": "+e);
        	}	
    	}
    	else {
    		logger.log(Level.WARNING, "Request for inactive conversation "+conversation);
    		throw new ResourceException(Status.CLIENT_ERROR_GONE);
    	}
	}
	
	/** test method 
	 * @throws JSONException */
	@Get("json")
	public JsonRepresentation hello(JsonRepresentation req) throws JSONException {
		JSONObject resp = new JSONObject();
		resp.put("hello", "test");
		JsonRepresentation reply = new JsonRepresentation(resp);
		reply.setExpirationDate(new Date());
		return reply;
	}
	
}
