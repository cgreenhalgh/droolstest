/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.restlet.data.MediaType;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.resource.Get;   
import org.restlet.resource.Post;   
import org.restlet.representation.Representation;   

import org.restlet.data.Form;   
import org.restlet.data.MediaType;   
import org.restlet.data.Status;   

import uk.ac.horizon.ug.exserver.RestletApplication;
import uk.ac.horizon.ug.exserver.model.SessionTemplate;
import uk.ac.horizon.ug.exserver.model.SessionType;

import java.util.logging.Logger;

/**
 * @author cmg
 *
 */
public class TemplatesResource extends BaseResource {
	static Logger logger = Logger.getLogger(TemplatesResource.class.getName());
	
//    @Get  
 //   public String toString() {   
  //  }   
	/** XML list of all rep */
    @Get("xml")   
    public Representation toXml() throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
    	EntityManager em = getEntityManager();
    	UserTransaction ut = getTransaction();
    	
		ut.begin();
		Query q = em.createQuery ("SELECT x FROM SessionTemplate x");
		List<SessionTemplate> results = (List<SessionTemplate>) q.getResultList ();
		ut.commit();
		
    	XstreamRepresentation<List<SessionTemplate>> xml = new XstreamRepresentation<List<SessionTemplate>>(MediaType.APPLICATION_XML, results);
    	xml.getXstream().alias("template", SessionTemplate.class);
		// immediate expire?
		xml.setExpirationDate(new Date());
    	return xml;
    }
    
    /**  
     * Handle POST requests: create a new item.  
     */  
    @Post  
    public Representation acceptItem(Representation entity) throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
        Representation result = null;   
        // Parse the given representation and retrieve pairs of   
        // "name=value" tokens.   
        Form form = new Form(entity);   
        SessionTemplate st = new SessionTemplate();
        st.setName(form.getFirstValue("name"));   
        st.setRulesetUrls(form.getValuesArray("rulesetUrls"));   
        st.setFactUrls(form.getValuesArray("factUrls"));  
        logger.info("acceptItem: "+form.getValuesMap());
        logger.info("Name = "+st.getName());
        
    	
    	if (st.getName()==null || st.getName().length()==0)
    	{
    		this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Name not specified");
    		return null;
    	}
    	
    	UserTransaction ut = getTransaction();
		ut.begin();
		try {
			EntityManager em = getEntityManager();

			SessionTemplate est = em.find(SessionTemplate.class, st.getName());
			if (est==null) {
				logger.info("Did not find SessionTemplate "+st.getName()+" - adding");
				em.persist(st);
				// created
				setStatus(Status.SUCCESS_CREATED);   
			} 
			else {
				logger.info("Found SessionTemplate "+st.getName()+" - updating");
				em.merge(st);
				setStatus(Status.SUCCESS_ACCEPTED);   
			}
			logger.info("Transaction status "+ut.getStatus()+" "+(ut.getStatus()==javax.transaction.Status.STATUS_MARKED_ROLLBACK ? "marked rollback!" : ""));
			ut.commit();
			em.close();
		}
		catch (Exception e) {
			this.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			ut.rollback();
			return null;
		}

        return null;   
    }   
}
