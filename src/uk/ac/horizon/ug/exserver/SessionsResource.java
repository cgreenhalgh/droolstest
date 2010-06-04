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

import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.compiler.DescrBuildError;
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
import uk.ac.horizon.ug.exserver.protocol.RulesetError;
import uk.ac.horizon.ug.exserver.protocol.RulesetErrors;
import uk.ac.horizon.ug.exserver.protocol.SessionBuildResult;

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
        String loggedText = form.getFirstValue("logged");
        boolean logged = (loggedText!=null && loggedText.length()>0);

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
		DroolsSession ds = null;
		int logId = 1;
		try {
			// Now we can make the drools session (outside transaction!)
			ds = DroolsSession.createSession(st, sessionType, logged, logId);
		}
		catch (DroolsSession.RulesetException re) {
			logger.log(Level.WARNING, "Error creating session", re);
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return errorResponse(re);
		}
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
	        s.setLogged(logged);
	        if (logged) {
	        	s.setLogId(logId);			
	        }
	        
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
		// fire rules - first time
		ut.begin();
		try {
			ds.getKsession().fireAllRules();
			ut.commit();
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Error firing rules in new session", e);
			setStatus(Status.SERVER_ERROR_INTERNAL, e);
			ut.rollback();
			return null;
		}
		setStatus(Status.SUCCESS_CREATED);
		return successResponse(s);
    }
    static Representation successResponse(Session s) {
		SessionBuildResult res = new SessionBuildResult();
		res.setStatus("SUCCESS");
		res.setId(s.getId());

		XstreamRepresentation<SessionBuildResult> xml = new XstreamRepresentation<SessionBuildResult>(MediaType.APPLICATION_XML, res);
		xml.getXstream().alias("result", SessionBuildResult.class);
		xml.setExpirationDate(new Date());
		return xml;
    }   
    /** rule builder error response */
    static Representation errorResponse(DroolsSession.RulesetException re) {
		SessionBuildResult res = new SessionBuildResult();
		res.setStatus("ERROR");
		//res.setId(s.getId());
		RulesetErrors errors[] = new RulesetErrors[re.errors.length];
		for (int ei=0; ei<errors.length; ei++) 
		{
			errors[ei] = new RulesetErrors();
			errors[ei].setRulesetUrl(re.rulesetUrl);
			RulesetError details[] = new RulesetError[re.errors.length];
			for (int di=0; di<details.length; di++) {
				KnowledgeBuilderError kbe = re.errors[di];
				//logger.info("KBError: "+kbe);
				details[di] = new RulesetError(kbe.getClass().getName(), kbe.getErrorLines(), kbe.getMessage(), kbe.toString());
				if (kbe instanceof DescrBuildError) {
					DescrBuildError dbe = (DescrBuildError)kbe;
					//logger.info("DescrBuildError: line="+dbe.getLine()+", descr="+dbe.getDescr()+", object="+dbe.getObject()+", parent="+dbe.getParentDescr()+" ("+dbe+")");
					if (details[di].getErrorLines()==null || details[di].getErrorLines().length==0)
						details[di].setErrorLines(new int [] { dbe.getLine() });
				}
			}
			errors[ei].setErrors(details);
		}
		res.setErrors(errors);

		XstreamRepresentation<SessionBuildResult> xml = new XstreamRepresentation<SessionBuildResult>(MediaType.APPLICATION_XML, res);
		xml.getXstream().alias("result", SessionBuildResult.class);
		xml.getXstream().alias("errors", RulesetErrors.class);
		xml.getXstream().alias("error", RulesetError.class);
		xml.setExpirationDate(new Date());
		return xml;
    }
}
