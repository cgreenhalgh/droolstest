/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.restlet.Application;   
import org.restlet.Restlet;   
import org.restlet.routing.Router;   

import uk.ac.horizon.ug.exserver.TemplatesResource;

/**
 * @author cmg
 *
 */
public class RestletApplication extends Application {
	/**  
     * Creates a root Restlet that will receive all incoming calls.  
     */  
    @Override  
    public synchronized Restlet createInboundRoot() {   
        // Create a router Restlet that routes each call to a   
        // new instance of HelloWorldResource.   
        Router router = new Router(getContext());   
  
        router.attach("/test", TestResource.class);   
        router.attach("/templates", TemplatesResource.class);   
  
        return router;   
    }   
    
    /** get persistence entity manager */
    public EntityManager getEntityManager() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory( "droolstest" );
		EntityManager em = emf.createEntityManager();
		return em;
    }
    /** get transaction */
    public UserTransaction getTransaction() throws NamingException {
		UserTransaction ut =
			  (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
		return ut;
    }
}
