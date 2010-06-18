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

import org.drools.runtime.rule.FactHandle;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonConverter;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.xstream.XstreamRepresentation;

import uk.ac.horizon.ug.authorapp.model.ClientTypeInfo;
import uk.ac.horizon.ug.authorapp.model.ProjectInfo;
import uk.ac.horizon.ug.exserver.BaseResource;
import uk.ac.horizon.ug.exserver.DroolsSession;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.model.ConversationStatus;
import uk.ac.horizon.ug.exserver.model.Session;

/**
 * @author cmg
 *
 */
public class RegisterClientHandler extends BaseResource {
	static Logger logger = Logger.getLogger(RegisterClientHandler.class.getName());

	/** handle JSON POST registration of client conversation.
	 * e.g. curl -d '...' http://localhost:8182/droolstest/1/registerclient
	 * 
	 * See ClientConversation. NB sessionId below will be wrong.
	 * 
	 * <pre>{"conversationId":"1234","clientId":"imei:1234","clientType":"Mobile","sessionId":"0","creationTime":123456789,"lastContactTime":0,"status":"ACTIVE"}
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
	//@Post("json")
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
		if (!registerInternal(conversation)) {
  			setStatus(Status.SERVER_ERROR_INTERNAL);
  			return null;
		}
		// construct response (what?)
		// TODO
		return null;
	}
	
	/** handle JSON POST registration of client conversation.
	 * e.g. curl -H Content-Type:application/xml -d '...' http://localhost:8182/droolstest/1/registerclient
	 * 
	 * See ClientConversation. NB sessionId below will be wrong.
	 * 
	 * <pre><conversation><conversationId>1234</conversationId><clientId>imei:1234</clientId><clientType>Mobile</clientType><sessionId>0</sessionId><creationTime>123456789</creationTime><lastContactTime>0</lastContactTime><status>ACTIVE</status></conversation>
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
	@Post("xml")
	public XstreamRepresentation register(XstreamRepresentation req) throws JSONException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException, NamingException, NotSupportedException, SystemException {
		XstreamUtils.addAliases(req);
		ClientConversation conversation = (ClientConversation)req.getObject();
		logger.info("Got request: "+conversation);
				
		if (conversation.getClientId()==null || conversation.getConversationId()==null && conversation.getSessionId()==null || conversation.getClientType()==null) {
			logger.log(Level.WARNING, "incomplete request: "+conversation);
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}
		if (!registerInternal(conversation)) {
  			setStatus(Status.SERVER_ERROR_INTERNAL);
  			return null;
		}
		// construct response (what?)
		// TODO
		return null;
	}
	
	public static boolean registerInternal(ClientConversation conversation) throws IllegalStateException, SecurityException, SystemException, NamingException, NotSupportedException {
		EntityManager em = getEntityManager();
		UserTransaction ut = getTransaction();
		ut.begin();
		try {
			DroolsSession droolsSession = null;
			em.joinTransaction();
			// Expire old conversations with the same client
    		Query q = em.createQuery ("SELECT x FROM ClientConversation x WHERE x.clientId = :clientId AND x.sessionId = :sessionId");
    		q.setParameter("clientId", conversation.getClientId());
    		q.setParameter("sessionId", conversation.getSessionId());
    		List<ClientConversation> conversations = (List<ClientConversation>) q.getResultList ();

    		ClientConversation currentConversation = null;
    		int nextSeqNo = 1;
    		for (ClientConversation cc : conversations) {
    			if (cc.getNextSeqNo()>nextSeqNo)
    				nextSeqNo = cc.getNextSeqNo();
    			if (cc.getConversationId().equals(conversation.getConversationId())) {
    				currentConversation = cc;
    			}
    			else {
    				if (cc.getStatus()==ConversationStatus.ACTIVE) {
    					logger.info("Expire existing conversation: "+cc);
    					cc.setStatus(ConversationStatus.SUPERCEDED);
    	    			ClientSubscriptionManager.handleConversationChange(cc);

    	    			// check session status
    	    			Session session = em.find(Session.class, cc.getSessionId());
    	    			if (session==null) {
    	    				logger.log(Level.WARNING, "Could not find current conv. session "+cc.getSessionId());
    	    				continue;
    	    			}

    	    			// check client type
    	    			DroolsSession ds = DroolsSession.getSession(session, em);
    	    			FactHandle droolsConversation = ds.getKsession().getFactHandle(cc);    			
    	    			if (droolsConversation!=null) {
    	    				logger.info("Expiring in session");
    	    				// update problem with JPA
    	    				ds.getKsession().retract(droolsConversation);
    	    				ds.getKsession().insert(cc);
    	    				ds.getKsession().fireAllRules();
    	    			}
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

    			// check session status
    			Session session = em.find(Session.class, currentConversation.getSessionId());
    			if (session==null) {
    				logger.log(Level.WARNING, "Could not find session "+conversation.getSessionId());
    				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);    			
    			}

    			// check client type
    			droolsSession = DroolsSession.getSession(session, em);
    			FactHandle droolsConversation = droolsSession.getKsession().getFactHandle(currentConversation);    			
    			if (droolsConversation==null) {
    	 			logger.log(Level.WARNING, "Current conversation not found in session: "+currentConversation);
    	 			droolsConversation = droolsSession.getKsession().insert(currentConversation);
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
        			logger.info("Changing current conversation status (also Drools): "+conversation);
        			currentConversation.setStatus(conversation.getStatus());
	    			ClientSubscriptionManager.handleConversationChange(currentConversation);
        			droolsSession.getKsession().retract(droolsConversation);
        			droolsSession.getKsession().insert(currentConversation);
        		}
        		else {
        			// already inactive
    				logger.info("ClientConversation already exists, both inactive - ignore: "+currentConversation);
        		}
    		}
    		else { // not current...
    			// check session status
    			Session session = em.find(Session.class, conversation.getSessionId());
    			if (session==null) {
    				logger.log(Level.WARNING, "Could not find session "+conversation.getSessionId());
    				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);    			
    			}

    			// check client type
    			droolsSession = DroolsSession.getSession(session, em);
    			ProjectInfo pi = droolsSession.getProjectInfo();
    			ClientTypeInfo ct = pi.getClientType(conversation.getClientType());
    			if (ct==null) {
    				logger.log(Level.WARNING, "Could not find client tyep "+conversation.getClientType());
    				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);    			
    			}
    			
    			// Persist the new conversation
    			conversation.setCreationTime(System.currentTimeMillis());
    			conversation.setLastContactTime(0);
    			conversation.setNextSeqNo(nextSeqNo);
    			logger.info("Persisting "+conversation);
    			em.persist(conversation);
    			
    			ClientSubscriptionManager.handleConversationChange(conversation);

    			// also in drools
    			droolsSession.getKsession().insert(conversation);
    			
    			// TODO flush conversation state (e.g. incremental query cache)
    			
    		}
    		ut.commit();
			em.close();

    		// drools rules
			if (droolsSession!=null)
				droolsSession.getKsession().fireAllRules();
		}
		catch (Exception e) {
			ut.rollback();
			logger.log(Level.WARNING, "Error registering client", e);
			return false;
		}
		return true;
	}
}
