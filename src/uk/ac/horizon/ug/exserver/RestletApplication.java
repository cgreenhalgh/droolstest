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
import org.restlet.resource.Directory;
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
        router.attach("/sessions", SessionsResource.class);   
        router.attach("/sessions/{sessionId}", SessionResource.class);   
  
        return router;   
    }   
    
}
