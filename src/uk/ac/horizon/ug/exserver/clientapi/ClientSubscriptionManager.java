/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.horizon.ug.authorapp.model.ClientSubscriptionInfo;
import uk.ac.horizon.ug.authorapp.model.ClientTypeInfo;
import uk.ac.horizon.ug.authorapp.model.QueryConstraintInfo;
import uk.ac.horizon.ug.authorapp.model.QueryInfo;
import uk.ac.horizon.ug.exserver.BaseResource;
import uk.ac.horizon.ug.exserver.DroolsSession;
import uk.ac.horizon.ug.exserver.DroolsSession.RulesetException;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageType;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.model.ConversationStatus;
import uk.ac.horizon.ug.exserver.model.MessageToClient;
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

			if (conversation.getStatus()==ConversationStatus.ACTIVE) {
				insertMessageToClient(conversation, MessageType.NEW_CONV, null, null, null, em);
			}
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
			for (int si=0; si<subscriptions.size(); si++) {
				ClientSubscriptionInfo subscription = subscriptions.get(si);
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
							if (matches(pattern, object, conversation, ds.getKsession().getKnowledgeBase())) {
								insertMessageToClient(conversation, MessageType.FACT_EX, si, null, object, em);
							}
						}
					}
				}
			}
		}
	}

	/** insert a new MessageToClient */
	private static void insertMessageToClient(ClientConversation conversation,
			MessageType type, Integer si, Object oldVal, Object newVal,
			EntityManager em) {
		try {
			MessageToClient msg = new MessageToClient();
			int seqNo = conversation.getNextSeqNo();
			msg.setClientId(conversation.getClientId());
			msg.setConversationId(conversation.getConversationId());
			msg.setSessionId(conversation.getSessionId());
			msg.setSeqNo(seqNo);
			msg.setTime(System.currentTimeMillis());
			msg.setType(type);
			if (si!=null)
				msg.setSubsIx(si);
			if (oldVal!=null) 
				msg.setOldVal(marshallFact(oldVal));
			if (newVal!=null)
				msg.setNewVal(marshallFact(newVal));
			em.persist(msg);
			conversation.setNextSeqNo(seqNo+1);
			//em.merge(conversation);
			logger.info("Added message: "+msg);
		} catch (Exception e)  {
			logger.log(Level.WARNING, "Unable to create/insert message to client", e);			
		}
	}
	/** marshall a fact object to a string for use in a message to client 
	 * @throws JSONException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IntrospectionException 
	 * @throws IllegalArgumentException */
	private static String marshallFact(Object value) throws IllegalArgumentException, IntrospectionException, IllegalAccessException, InvocationTargetException, JSONException {
		// JSON?!
		JSONObject json = JsonUtils.objectToJson(value, true);
		return json.toString();
	}

	private static boolean matches(QueryInfo pattern, Object object, ClientConversation conversation, KnowledgeBase knowledgeBase) {
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
			String sParameter = constraint.getParameter();
			Object parameter = sParameter;
			if (constraint.getConstraintType().requiresValue()) {
				if (parameter==null) {
					logger.log(Level.WARNING, "No parameter specified for constraint "+className+" field "+fieldName);
					return false;
				}
				if (value instanceof Number) {
					if (value instanceof Float || value instanceof Double) {
						parameter = Double.parseDouble(sParameter);
						value = ((Number)value).doubleValue();
					}
					else {
						parameter = Long.parseLong(sParameter);
						value = ((Number)value).longValue();
					}
				} else if (value instanceof Boolean) {
					parameter = (sParameter.startsWith("t") || sParameter.startsWith("T") || sParameter.startsWith("1"));
				}
				else if (value!=null) {
					value = value.toString();
				}
			} else if (value!=null)
				value = value.toString();
			
			switch (constraint.getConstraintType()) {
			// TODO
			case EQUAL_TO: 
				if (value==null)
					return false;
				if (parameter.equals(value))
					break;
				return false;
			case NOT_EQUAL_TO:
				if (value==null)
					return false;
				if (!parameter.equals(value))
					break;
				return false;
			case LESS_THAN:
				if (value==null)
					return false;
				if (value instanceof Long) {
					if (((Long)value).longValue() < ((Long)parameter).longValue())
						break;
				} else if (value instanceof Double) {
					if (((Double)value).doubleValue() < ((Double)parameter).doubleValue())
						break;
				} else if (value instanceof String) {
					if (((String)value).compareTo((String)parameter)<0)
						break;
				}
				return false;
			case LESS_THAN_OR_EQUAL_TO:
				if (value==null)
					return false;
				if (value instanceof Long) {
					if (((Long)value).longValue() <= ((Long)parameter).longValue())
						break;
				} else if (value instanceof Double) {
					if (((Double)value).doubleValue() <= ((Double)parameter).doubleValue())
						break;
				} else if (value instanceof String) {
					if (((String)value).compareTo((String)parameter)<=0)
						break;
				}
				if (value.equals(parameter))
					break;
				return false;
			case GREATER_THAN:
				if (value==null)
					return false;
				if (value instanceof Long) {
					if (((Long)value).longValue() > ((Long)parameter).longValue())
						break;					
				} else if (value instanceof Double) {
					if (((Double)value).doubleValue() > ((Double)parameter).doubleValue())
						break;
				} else if (value instanceof String) {
					if (((String)value).compareTo((String)parameter)>0)
						break;
				}
				return false;
			case GREATER_THAN_OR_EQUAL_TO:
				if (value==null)
					return false;
				if (value instanceof Long) {
					if (((Long)value).longValue() >= ((Long)parameter).longValue())
						break;
				} else if (value instanceof Double) {
					if (((Double)value).doubleValue() >= ((Double)parameter).doubleValue())
						break;
				} else if (value instanceof String) {
					if (((String)value).compareTo((String)parameter)>=0)
						break;
				}
				if (value.equals(parameter))
					break;
				return false;
			case EQUAL_TO_CLIENT_ID: 
				if (value==null)
					return false;
				if (conversation.getClientId().equals(value))
					break;
				return false;
			case EQUAL_TO_CONVERSATION_ID:
				if (value==null)
					return false;
				if (conversation.getConversationId().equals(value))
					break;
				return false;
			case IS_NULL:
				if (value==null)
					break;
				return false;
			case IS_NOT_NULL:
				if (value!=null)
					break;
				return false;
			}
			
		}
		return true;
	}
}
