/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.List;
import java.util.Date;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.restlet.data.MediaType;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.resource.Get;   
import org.restlet.resource.Post;   
import org.restlet.representation.Representation;   
import org.restlet.representation.StringRepresentation;   

import org.restlet.data.Form;   
import org.restlet.data.MediaType;   
import org.restlet.data.Status;   

import uk.ac.horizon.ug.commonfacts.SystemTime;
import uk.ac.horizon.ug.exserver.RestletApplication;
import uk.ac.horizon.ug.exserver.model.SessionTemplate;
import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.model.SessionType;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cmg
 *
 */
public class SessionsResource extends BaseResource {
	static Logger logger = Logger.getLogger(SessionsResource.class.getName());
	
//    @Get  
 //   public String toString() {   
  //  }   
	/** XML list of all rep 
	 * @throws NamingException 
	 * @throws SystemException 
	 * @throws NotSupportedException */
    @Get("xml")   
    public Representation toXml() throws NamingException, NotSupportedException, SystemException {
     	UserTransaction ut = getTransaction();
		ut.begin();
    	try {
    		EntityManager em = getEntityManager();

    		Query q = em.createQuery ("SELECT x FROM Session x");
    		List<Session> results = (List<Session>) q.getResultList ();
    		ut.commit();

    		XstreamRepresentation<List<Session>> xml = new XstreamRepresentation<List<Session>>(MediaType.APPLICATION_XML, results);
    		xml.getXstream().alias("session", Session.class);
    		// immediate expire?
    		xml.setExpirationDate(new Date());
    		return xml;
    	} catch (Exception e) {
    		logger.log(Level.WARNING, "error getting/marshalling sessions", e);
    		setStatus(Status.SERVER_ERROR_INTERNAL, e);
    		return null;
    	}
    }
    
    /**  
     * Handle POST requests: create a new session.  
     */  
    @Post  
    public Representation acceptItem(Representation entity) throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
        Representation result = null;   
        // Parse the given representation and retrieve pairs of   
        // "name=value" tokens.   
        Form form = new Form(entity);   
        String templateName = form.getFirstValue("template");   

    	if (templateName==null || templateName.length()==0)
    	{
    		this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Template name not specified");
    		return null;
    	}
    	String type = form.getFirstValue("type");
    	// default
    	SessionType sessionType = SessionType.JPA_SERIALIZED;
    	try {
    		if (type!=null && type.length()>0)
    			sessionType = SessionType.valueOf(type);
    	}
    	catch (Exception e) {
    		this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown session type: "+type);
    		return null;
    	}
        String updateSystemTime = form.getFirstValue("updateSystemTime");

    	UserTransaction ut = getTransaction();
    	// get SessionTemplate
    	ut.begin();
		SessionTemplate st = null;
		try {
			EntityManager em = getEntityManager();

			st = em.find(SessionTemplate.class, templateName);
			if (st==null) {
				this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Template "+templateName+" not found");
				ut.rollback();
				em.close();
				return null;
			}

			ut.commit();
			em.close();
		}
		catch (Exception e) {
			this.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			ut.rollback();
			return null;
		}
		// Now we can make the drools session (outside transaction!)
		DroolsSession ds = DroolsSession.createSession(st, sessionType);

		// now make our record - risk of leak, but non-nested transactions limits us
		ut.begin();
		Session s = new Session();
		try {
			EntityManager em = getEntityManager();

			s.setTemplateName(templateName);
			s.setRulesetUrls(st.getRulesetUrls());

			// create session
			s.setDroolsId(ds.getId());
			s.setSessionType(sessionType);
			
			// TODO: GUID
			s.setId(""+s.getDroolsId());
			s.setCreatedDate(new Date());
	        if (updateSystemTime!=null && updateSystemTime.length()>0)
	        	s.setUpdateSystemTime(true);
			
	        if (s.isUpdateSystemTime()) {
	        	SystemTime systemTime = new SystemTime();
	        	systemTime.setTickCount(0);
	        	systemTime.setTime(System.currentTimeMillis());
	        	s.setSystemTimeHandle(ds.getKsession().insert(systemTime).toExternalForm());
	        }
	        
			em.persist(s);

			ut.commit();
			em.close();
		}
		catch (Exception e) {
			this.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			ut.rollback();
			return null;
		}
		setStatus(Status.SUCCESS_CREATED);
		Result res = new Result();
		res.setStatus("SUCCESS");
		res.setId(s.getId());

		XstreamRepresentation<Result> xml = new XstreamRepresentation<Result>(MediaType.APPLICATION_XML, res);
		xml.getXstream().alias("result", Result.class);
		return xml;
    }   
    /** result class */
    static class Result {
    	protected String status;
    	protected String id;
		/**
		 * @return the status
		 */
		public String getStatus() {
			return status;
		}
		/**
		 * @param status the status to set
		 */
		public void setStatus(String status) {
			this.status = status;
		}
		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}
    	
    }
}
