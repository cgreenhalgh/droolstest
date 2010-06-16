/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Post;   
import org.restlet.resource.Get;   
import org.restlet.representation.Representation;   

import uk.ac.horizon.ug.exserver.model.Session;

/**
 * @author cmg
 *
 */
public class ReloadRulesResource extends BaseResource {
	static Logger logger = Logger.getLogger(ReloadRulesResource.class.getName());

	/** session id */
	protected String sessionId;
	/** session object */
	protected Session sessionInfo;
	
	/** cons - not extending SessionResource in case session get fails (what we are here for) */
	@Override  
    protected void doInit() throws ResourceException {   
        // Get the "itemName" attribute value taken from the URI template   
        // /items/{itemName}.   
        this.sessionId = (String) getRequest().getAttributes().get("sessionId");   
  
    	EntityManager em = getEntityManager();
    	this.sessionInfo = em.find(Session.class, sessionId);
    	if (this.sessionInfo==null)
    		throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
	}

	/** post - force reload 
	 * @throws NamingException 
	 * @throws NotSupportedException 
	 * @throws SystemException 
	 * @throws IOException 
	 * @throws SecurityException 
	 * @throws IllegalStateException */
	@Post("xml")
	@Get("xml")
	public Representation doReload() throws NamingException, IllegalStateException, SecurityException, IOException, SystemException, NotSupportedException {
		try {
			logger.info("Attempting reload on session "+this.sessionId);
			DroolsSession ds = DroolsSession.reloadSession(this.sessionInfo, getEntityManager());
		} catch (DroolsSession.RulesetException re) {
			logger.log(Level.WARNING, "Error creating session: "+re);
			//setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return SessionsResource.errorResponse(re);
		}
		return SessionsResource.successResponse(this.sessionInfo);
	}
}
