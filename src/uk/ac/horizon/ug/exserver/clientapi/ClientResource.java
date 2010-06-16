/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi;

import java.util.Date;
import java.util.logging.Level;

import javax.persistence.EntityManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import uk.ac.horizon.ug.exserver.BaseResource;
import uk.ac.horizon.ug.exserver.DroolsSession;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.model.Session;

/**
 * @author cmg
 *
 */
public class ClientResource extends BaseResource {
	/** session id */
	protected String conversationId;
	/** session object */
	protected ClientConversation conversation;
	@Override  
	/** initialise */
    public void doInit() throws ResourceException {   
        this.conversationId = (String) getRequest().getAttributes().get("conversationId");   
    	EntityManager em = getEntityManager();
    	this.conversation = em.find(ClientConversation.class, conversationId);
    	if (this.conversation==null)
    		throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
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
