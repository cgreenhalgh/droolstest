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
import org.drools.definition.type.FactField;
import org.drools.definition.type.FactType;
import org.drools.common.DisconnectedFactHandle;
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cmg
 *
 */
public class SessionResource extends BaseResource {
	public static final String ELEMENT_HOLDER = "holder";

	public static final String ELEMENT_RESULT = "result";

	static Logger logger = Logger.getLogger(SessionResource.class.getName());
	
	protected static void addAliases(XstreamRepresentation xr) {
		xr.getXstream().alias(ELEMENT_HOLDER, RawFactHolder.class);
		xr.getXstream().alias(ELEMENT_RESULT, OperationResult.class);
		xr.getXstream().alias("list", LinkedList.class);    	
		//    	xr.getXstream().getConverterLookup().
	}

	/** coerce field value */
	public static Object coerce(String fieldValueText, FactField field) {
		if (fieldValueText==null || fieldValueText.length()==0)
			return null;
		Object fieldValue = fieldValueText;
		
		Class<?> fieldClass = field.getType();
		if (boolean.class.isAssignableFrom(fieldClass) || Boolean.class.isAssignableFrom(fieldClass)) {
			if (fieldValueText.length()==0 || fieldValueText.charAt(0)=='f'  || fieldValueText.charAt(0)=='F' ||fieldValueText.charAt(0)=='n' ||fieldValueText.charAt(0)=='N' ||fieldValueText.charAt(0)=='0')
				fieldValue = Boolean.FALSE;
			else
				fieldValue = Boolean.TRUE;
		}
		else if (Number.class.isAssignableFrom(fieldClass) || fieldClass.isPrimitive()) {
			// not bool (see above)
			logger.info("Field "+field.getName()+" is Number class "+fieldClass.getName());
			if (!fieldValueText.contains("."))
				fieldValue = Long.parseLong(fieldValueText);
			else
				fieldValue= Double.parseDouble(fieldValueText);
		}
		else
			logger.info("Field "+field.getName()+" is non-Number class "+fieldClass.getName());
	
		return fieldValue;
	}

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

	/** add facts once parsed */
	public Representation addFacts(List<RawFactHolder> facts) throws java.io.IOException,
	javax.naming.NamingException, javax.transaction.SystemException,
	javax.transaction.NotSupportedException,
	javax.transaction.RollbackException,
	javax.transaction.HeuristicRollbackException,
	javax.transaction.HeuristicMixedException {   
		logger.info("facts = "+facts);
		LinkedList<OperationResult> results = new LinkedList<OperationResult>();
		synchronized (droolsSession) {
			UserTransaction ut = this.getTransaction();
			ut.begin();
			try {
				EntityManager em = this.getEntityManager();
				for (RawFactHolder rfh : facts) {
					OperationResult result = new OperationResult();
					result.setHolder(rfh);
					try {
						switch (rfh.getOperation()) {
						case add: {
							FactHandle fh = droolsSession.getKsession().insert(rfh.getFact());
							result.setHandle(fh.toExternalForm());
							result.setStatus(OperationStatus.SUCCESS);
							logger.info("added "+fh+": "+rfh.getFact());
							break;
						}
						case update: {
							FactHandle fh = new DisconnectedFactHandle(rfh.getHandle());
							droolsSession.getKsession().update(fh, rfh.getFact());
							// return handle??
							FactHandle newfh = droolsSession.getKsession().getFactHandle(rfh.getFact());
							if (newfh!=null)
								result.setHandle(newfh.toExternalForm());
							result.setStatus(OperationStatus.SUCCESS);
							logger.info("updated "+fh+": "+rfh.getFact());
							break;
						}
						case delete: {
							FactHandle fh = new DisconnectedFactHandle(rfh.getHandle());
							droolsSession.getKsession().retract(fh);
							result.setStatus(OperationStatus.SUCCESS);
							logger.info("deleted "+fh);
							break;	
						}
						}
					}
					catch (Exception e) {
						logger.log(Level.WARNING, "error doing add fact: "+rfh, e);
						result.setStatus(OperationStatus.FAILURE);
					}
					results.add(result);
				}

				droolsSession.getKsession().fireAllRules();

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

		XstreamRepresentation<List<OperationResult>> xml = new XstreamRepresentation<List<OperationResult>>(MediaType.APPLICATION_XML, results);
		addAliases(xml);
		return xml;
	}  
}
