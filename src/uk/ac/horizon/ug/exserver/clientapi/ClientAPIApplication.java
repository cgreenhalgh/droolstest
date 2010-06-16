/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi;

import java.util.logging.Logger;

import org.restlet.Application;   
import org.restlet.Restlet;   
import org.restlet.routing.Router;   

/** Client API 
 * @author cmg
 *
 */
public class ClientAPIApplication {
	static Logger logger = Logger.getLogger(ClientAPIApplication.class.getName());
	
	/** register resources */
	public static void init(Router router) {
//      router.attach("/test", TestResource.class);   
//      router.attach("/sessions/{sessionId}/facts", SessionResource.class);   	
		// TODO: should be accessible to lobby server only!
		router.attach("/registerclient", RegisterClientHandler.class);   
		// test
		router.attach("/client/{conversationId}/test", ClientResource.class);
	}
}
