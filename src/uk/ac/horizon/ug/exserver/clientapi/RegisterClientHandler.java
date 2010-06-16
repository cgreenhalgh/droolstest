/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonConverter;
import org.restlet.ext.json.JsonRepresentation;
import uk.ac.horizon.ug.exserver.BaseResource;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.model.ConversationStatus;
import uk.ac.horizon.ug.exserver.model.Session;

/**
 * @author cmg
 *
 */
public class RegisterClientHandler extends BaseResource {
	static Logger logger = Logger.getLogger(RegisterClientHandler.class.getName());

	/** handle POST registration of client conversation.
	 * e.g. curl -d '{"hello":"chris"}' http://localhost:8182/droolstest/1/registerclient
	 * 
	 * See ClientConversation.
	 * 
	 * <pre>{"conversationId":"1234","clientId":"imei:1234","sessionId":"0","creationTime":123456789,"lastContactTime":0,"status":"ACTIVE"}
	 * </pre>
	 * 
	 * @throws JSONException 
	 * @throws IntrospectionException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NamingException 
	 * @throws SystemException 
	 * @throws NotSupportedException */
	@Post("json")
	public JsonRepresentation register(JsonRepresentation req) throws JSONException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException, NamingException, NotSupportedException, SystemException {
		JSONObject request = req.getJsonObject();
		logger.info("Got request: "+request);

		ClientConversation conversation = JsonUtils.JsonToObject(request, ClientConversation.class);
		logger.info(" -> : "+conversation);
				
		if (conversation.getClientId()==null || conversation.getConversationId()==null && conversation.getSessionId()==null || conversation.getClientType()==null) {
			logger.log(Level.WARNING, "incomplete request: "+conversation);
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}
		
		EntityManager em = this.getEntityManager();
		UserTransaction ut = this.getTransaction();
		ut.begin();
		try {
			em.joinTransaction();
			// Expire old conversations with the same client
    		Query q = em.createQuery ("SELECT x FROM ClientConversation x WHERE x.clientId = :clientId");
    		q.setParameter("clientId", conversation.getClientId());
    		List<ClientConversation> conversations = (List<ClientConversation>) q.getResultList ();

    		ClientConversation currentConversation = null;
    		for (ClientConversation cc : conversations) {
    			if (cc.getConversationId().equals(conversation.getConversationId())) {
    				currentConversation = cc;
    			}
    			else {
    				if (cc.getStatus()==ConversationStatus.ACTIVE) {
    					logger.info("Expire existing conversation: "+cc);
    					cc.setStatus(ConversationStatus.SUPERCEDED);
    				}
    			}
    		}
    		// current?
    		if (currentConversation!=null) {
    			if (!conversation.getClientId().equals(currentConversation.getClientId()) || !conversation.getSessionId().equals(currentConversation.getSessionId()) || !conversation.getClientType().equals(currentConversation.getClientType()))
    			{
    				logger.info("ClientConversation already exists, but does not match: "+conversation+" vs "+currentConversation);
    				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
    			}
        		if (conversation.getStatus()==ConversationStatus.ACTIVE) {
        			if (currentConversation.getStatus()==ConversationStatus.ACTIVE) {
        				logger.info("ClientConversation already exists, both ACTIVE - ignore: "+currentConversation);
        			}
        			else {
        				logger.info("ClientConversation already exists, inactive - refuse: "+currentConversation);
        				throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
        			}
        		}
        		else if (currentConversation.getStatus()==ConversationStatus.ACTIVE) {
        			logger.info("Changing current conversation status: "+conversation);
        			currentConversation.setStatus(conversation.getStatus());
        		}
        		else {
        			// already inactive
    				logger.info("ClientConversation already exists, both inactive - ignore: "+currentConversation);
        		}
    		}
    		else { // not current...
    			// check session status
    			Session session = em.find(Session.class, conversation.getSessionId());
    			if (session==null) 
    				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);    			

    			// Persist the new conversation
    			conversation.setCreationTime(System.currentTimeMillis());
    			conversation.setLastContactTime(0);
    			logger.info("Persisting "+conversation);
    			em.persist(conversation);
    		}
			// construct response (what?)
			// TODO
			ut.commit();
			em.close();
		}
		catch (Exception e) {
			ut.rollback();
			logger.log(Level.WARNING, "Error registering client", e);
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		}
		return null;
	}
}
