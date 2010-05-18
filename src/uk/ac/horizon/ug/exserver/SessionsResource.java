/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.List;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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

import uk.ac.horizon.ug.exserver.RestletApplication;
import uk.ac.horizon.ug.exserver.model.SessionTemplate;
import uk.ac.horizon.ug.exserver.model.Session;

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
	/** XML list of all rep */
    @Get("xml")   
    public Representation toXml() throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
    	EntityManager em = getEntityManager();
    	UserTransaction ut = getTransaction();
    	
		ut.begin();
		Query q = em.createQuery ("SELECT x FROM Session x");
		List<Session> results = (List<Session>) q.getResultList ();
		ut.commit();
		
    	XstreamRepresentation<List<Session>> xml = new XstreamRepresentation<List<Session>>(MediaType.APPLICATION_XML, results);
    	xml.getXstream().alias("session", Session.class);
    	return xml;
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
		DroolsSession ds = DroolsSession.createSession(st);

		// now make our record - risk of leak, but non-nested transactions limits us
		ut.begin();
		Session s = new Session();
		try {
			EntityManager em = getEntityManager();

			s.setTemplateName(templateName);
			s.setRulesetUrls(st.getRulesetUrls());

			// create session
			s.setDroolsId(ds.getId());
			
			// TODO: GUID
			s.setId(""+s.getDroolsId());
			s.setCreatedDate(new Date());
			
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
        return new StringRepresentation(s.getId());   
    }   
}
