package uk.ac.horizon.ug.exserver.clientapi;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.LinkedList;
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
import org.restlet.data.MediaType;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.resource.Post;

import uk.ac.horizon.ug.authorapp.model.ClientSubscriptionLifetimeType;
import uk.ac.horizon.ug.exserver.clientapi.protocol.Message;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageStatusType;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageType;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.model.DbUtils;
import uk.ac.horizon.ug.exserver.model.MessageToClient;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;

public class ClientMessagesResource extends ClientResource {
	static Logger logger = Logger.getLogger(ClientMessagesResource.class.getName());

	@Post("xml")
	public XstreamRepresentation<List<Message>> handleMessages(XstreamRepresentation<List<Message>> req) throws JSONException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException, NamingException, NotSupportedException, SystemException {
		XstreamUtils.addAliases(req);
		List<Message> messages = req.getObject();
		logger.info("Got "+messages.size()+" messages: "+messages);
		List<Message> responses = handleMessages(messages);

    	XstreamRepresentation<List<Message>> xml = new XstreamRepresentation<List<Message>>(MediaType.APPLICATION_XML, responses);
    	XstreamUtils.addAliases(xml);
		// immediate expire?
		xml.setExpirationDate(new Date());
    	return xml;
	}

	/** internal implementation */
	public List<Message> handleMessages(List<Message> messages) {
		List<Message> responses = new LinkedList<Message>();
		for (Message message : messages) 
			handleMessage(message, responses);
		return responses;
	}

	private void handleMessage(Message message, List<Message> responses) {
		if (message.getType()==null) {
			responses.add(createErrorMessage(message, MessageStatusType.INVALID_REQUEST, "No message type"));
			return;
		}
		if (!message.getType().toServer()) {
			responses.add(createErrorMessage(message, MessageStatusType.INVALID_REQUEST, "Message type "+message.getType()+" is not valid to server"));
			return;
		}
		try {
			switch(message.getType()) {

			//case NEW_CONV:// new conversation 
			//case FACT_EX: // fact already exists (matching a subscription)
			//case FACT_ADD: // fact added (matching a subscription)
			//case FACT_UPD: // fact updated (matching a subscription)
			//case FACT_DEL: // fact deleted (matching a subscription)
			//case POLL_RESP: // response to poll (e.g. no. messages still unsent)
			case POLL: // poll request 
				handlePoll(message, responses);
				return;
			case ACK: // acknowledge message

			case ADD_FACT:// request to add fact
			case UPD_FACT:// request to update fact
			case DEL_FACT:// request to delete fact
				//ERROR(false, true), // error response, e.g. to add/update/delete request
			case SUBS_EN:// enable a subscription
			case SUBS_DIS:// disable a subscription
				// TODO implement
			default:
				responses.add(createErrorMessage(message, MessageStatusType.INTERNAL_ERROR, "Message type "+message.getType()+" not supported"));
				break;
			}
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Handling message "+message, e);
			responses.add(createErrorMessage(message, MessageStatusType.INTERNAL_ERROR, "Exception: "+e));			
		}
	}

	private void handlePoll(Message message, List<Message> responses) throws Exception {
		
		// values?!
		UserTransaction ut = DbUtils.getUserTransaction();
		EntityManager em = DbUtils.getEntityManager();
		ut.begin();
		try {
			em.joinTransaction();

			long time = System.currentTimeMillis();
			int ackSeq = message.getAckSeq()!=null ? message.getAckSeq() : 0;
			
			if (ackSeq>0) {
				// ack
				int removed = 0;
				Query q = em.createQuery ("SELECT x FROM MessageToClient x WHERE x.clientId = :clientId AND x.ackedByClient = 0 AND x.seqNo <= :ackSeq");
				q.setParameter("clientId", conversation.getClientId());
				q.setParameter("ackSeq", ackSeq);
				List<MessageToClient> mtcs = (List<MessageToClient>) q.getResultList ();
				for (MessageToClient mtc : mtcs) {
					mtc.setAckedByClient(time);
					
					// delete on ack?
					if (mtc.getLifetime()!=null && mtc.getLifetime()==ClientSubscriptionLifetimeType.UNTIL_ACKNOWLEDGED) {
						em.remove(mtc);
						removed ++;
					}
				}
				if (mtcs.size()>0) {
					logger.info("Acked "+mtcs.size()+" messages to "+conversation.getClientId()+" (seq<="+ackSeq+") - "+removed+" removed");
				}
			}
			
			//ClientConversation cc = em.find(ClientConversation.class, conversation.getConversationId());
			
			boolean checkToFollow = true;
			int sentCount = 0;
			if (message.getToFollow()==null || message.getToFollow()>0) {
				// actually get some messages...
				// TODO from
				Query q = em.createQuery ("SELECT x FROM MessageToClient x WHERE x.clientId = :clientId AND x.seqNo> :ackSeq ORDER BY x.seqNo ASC");
				q.setParameter("clientId", conversation.getClientId());
				q.setParameter("ackSeq", ackSeq);
				if (message.getToFollow()!=null)
					q.setMaxResults(message.getToFollow());
				List<MessageToClient> mtcs = (List<MessageToClient>) q.getResultList ();
				sentCount = mtcs.size();
				if (message.getToFollow()==null || mtcs.size()<message.getToFollow())
					// can't be any left
					checkToFollow = false;
				
				int removed = 0;
				for (MessageToClient mtc : mtcs) {
					Message msg = new Message();
					//int seqNo = cc.getNextSeqNo();
					msg.setSeqNo(mtc.getSeqNo());
					//cc.setNextSeqNo(seqNo+1);
					msg.setType(mtc.getType());
					if (mtc.getOldVal()!=null)
						msg.setOldVal(mtc.getOldVal());
					if (mtc.getNewVal()!=null)
						msg.setNewVal(mtc.getNewVal());
					msg.setSubsIx(mtc.getSubsIx());
					responses.add(msg);
					// mark sent
					// delete on send?
					if (mtc.getLifetime()!=null && mtc.getLifetime()==ClientSubscriptionLifetimeType.UNTIL_SENT) {
						em.remove(mtc);
						removed ++;
					}
					else
						mtc.setSentToClient(time);				
				}
				if (removed>0) {
					logger.info("Sent "+mtcs.size()+" messages to "+conversation.getClientId()+" (seq>"+ackSeq+") - "+removed+" removed");
				}
			}
			// count to follow?
			int toFollow = 0;
			if (checkToFollow) {
				Query q = em.createQuery ("SELECT COUNT(x) FROM MessageToClient x WHERE x.clientId = :clientId AND x.seqNo> :ackSeq ");
				q.setParameter("clientId", conversation.getClientId());
				q.setParameter("ackSeq", ackSeq);
				Number countResult=(Number) q.getSingleResult();
				toFollow = countResult.intValue()-sentCount;
			}
			
			ut.commit();
			// TODO Auto-generated method stub
			Message pollResponse = new Message();
			pollResponse.setAckSeq(message.getSeqNo());
			if (message.getSubsIx()!=null)
				pollResponse.setSubsIx(message.getSubsIx());
			pollResponse.setType(MessageType.POLL_RESP);
			pollResponse.setToFollow(toFollow);
			responses.add(pollResponse);
		}
		catch (Exception e) {
			ut.rollback();
			throw e;
		}
	}

	private Message createErrorMessage(Message message,
			MessageStatusType status, String errorMessage) {
		Message response = new Message();
		response.setAckSeq(message.getSeqNo());
		response.setStatus(status);
		response.setErrorMsg(errorMessage);
		response.setType(MessageType.ERROR);
		logger.log(Level.WARNING, "Error response to client: " +response);
		return response;
	}
	
}
