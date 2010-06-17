/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.drools.KnowledgeBase;
import org.drools.definition.type.FactField;
import org.drools.definition.type.FactType;
import org.drools.runtime.rule.FactHandle;

import uk.ac.horizon.ug.authorapp.model.ClientSubscriptionInfo;
import uk.ac.horizon.ug.authorapp.model.ClientTypeInfo;
import uk.ac.horizon.ug.authorapp.model.QueryConstraintInfo;
import uk.ac.horizon.ug.authorapp.model.QueryInfo;
import uk.ac.horizon.ug.exserver.BaseResource;
import uk.ac.horizon.ug.exserver.DroolsSession;
import uk.ac.horizon.ug.exserver.DroolsSession.RulesetException;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.model.ConversationStatus;
import uk.ac.horizon.ug.exserver.model.Session;

/**
 * @author cmg
 *
 */
public class ClientSubscriptionManager {
	static Logger logger = Logger.getLogger(ClientSubscriptionManager.class.getName());
	
	/** handle change (in state) of conversation.
	 * if ACTIVE must be new client.
	 * Transaction may be active. 
	 * @throws NamingException 
	 * @throws SystemException 
	 * @throws NotSupportedException */
	public static void handleConversationChange(ClientConversation conversation) throws NamingException, NotSupportedException, SystemException {
		EntityManager em = BaseResource.getEntityManager();
		UserTransaction ut = BaseResource.getTransaction();
		boolean localTransaction = false;
		if (ut.getStatus()!=Status.STATUS_ACTIVE) {
			localTransaction = true;
			ut.begin();
		}
		try {
			em.joinTransaction();

			handleSubscriptions(conversation, em);
			
			if (localTransaction)
				ut.commit();
			em.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, "handling conversation change: "+conversation, e);
			ut.rollback();
			// silent fail?!
		}
	}

	private static void handleSubscriptions(ClientConversation conversation,
			EntityManager em) throws IllegalStateException, SecurityException, NamingException, RulesetException, IOException, SystemException, NotSupportedException {
		
		// check session status
		Session session = em.find(Session.class, conversation.getSessionId());
		if (session==null) {
			logger.log(Level.WARNING, "Could not find conv. session "+conversation.getSessionId());
			// silent fail?!
			return;
		}

		DroolsSession ds = DroolsSession.getSession(session, em);
		synchronized(ds) {
			ClientTypeInfo clientTypeInfo = ds.getProjectInfo().getClientType(conversation.getClientType());
			if (clientTypeInfo==null) {
				// should have been checked before
				logger.log(Level.WARNING, "Could not find client type "+conversation.getClientType());
				return;
			}

			List<ClientSubscriptionInfo> subscriptions = clientTypeInfo.getSubscriptions();
			for (ClientSubscriptionInfo subscription : subscriptions) {
				if (conversation.getStatus()!=ConversationStatus.ACTIVE) {
					// tidy up?
					// TODO
				}
				else // active
				{				
					// initialise subscription
					QueryInfo pattern = subscription.getPattern();
					if (pattern==null) {
						logger.log(Level.WARNING, "Subscription with no pattern for client type "+clientTypeInfo.getName());
						continue;
					}
					if (pattern.getTypeName()==null) {
						logger.log(Level.WARNING, "Subscription with pattern with null type name for client type "+clientTypeInfo.getName());
						continue;
					}
					logger.info("Initialise subscription to "+pattern);

					// initial values?
					if (subscription.isMatchExisting()) {
						Collection<Object> objects = ds.getKsession().getObjects();
						for (Object object : objects) {
							if (matches(pattern, object, ds.getKsession().getKnowledgeBase())) {
								
							}
						}
					}
					// TODO
				}
			}
		}
	}

	private static boolean matches(QueryInfo pattern, Object object, KnowledgeBase knowledgeBase) {
		if (object==null)
			return false;
		String className = object.getClass().getName();
		int ix = className.lastIndexOf(".");
		String typeName = (ix>=0) ? className.substring(ix+1) : className;
		if (!typeName.equals(pattern.getTypeName()))
			return false;
		List<QueryConstraintInfo> constraints = pattern.getConstraints();
		if (constraints.size()==0)
			return true;
		String namespace = (ix>=0) ? className.substring(0, ix) : "";
		FactType factType = knowledgeBase.getFactType(namespace, typeName);
		if (factType==null) {
			logger.log(Level.WARNING, "Could not get FactType for "+className);
			return false;
		}
		for (QueryConstraintInfo constraint : constraints) {
			String fieldName = constraint.getFieldName();
			if (fieldName==null)
				continue;
			FactField field = factType.getField(fieldName);
			if (field==null)
			{
				logger.log(Level.WARNING, "Could not get FactField for "+className+" field "+fieldName);
				return false;
			}
			Object value = field.get(object);
			//switch (constraint.getConstraintType()) {
			// TODO
			//}
			
		}
		return false;
	}
}
