/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import org.restlet.Application;   
import org.restlet.Restlet;   
import org.restlet.routing.Router;   

import uk.ac.horizon.ug.exserver.model.TestResource;

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
  
        // Defines only one route   
        router.attach("/test", TestResource.class);   
  
        return router;   
    }   
}
