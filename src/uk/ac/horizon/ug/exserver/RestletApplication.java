/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.logging.Logger;
import java.util.Enumeration;
import java.io.File;

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
import uk.ac.horizon.ug.exserver.clientapi.ClientAPIApplication;

import javax.servlet.ServletContext;

/**
 * @author cmg
 *
 */
public class RestletApplication extends Application {
	static Logger logger = Logger.getLogger(RestletApplication.class.getName());
	/**  
     * Creates a root Restlet that will receive all incoming calls.  
     */  
    @Override  
    public synchronized Restlet createInboundRoot() {   
        // Create a router Restlet that routes each call to a   
        // new instance of HelloWorldResource.   
        Router router = new Router(getContext());   

        router.attach("/test", TestResource.class);   
        router.attach("/tick", TickHandler.class);   
        router.attach("/templates", TemplatesResource.class);   
        router.attach("/sessions", SessionsResource.class);   
        router.attach("/sessions/{sessionId}/facts", SessionResource.class);   
        router.attach("/sessions/{sessionId}/rawfacts", RawSessionResource.class);   
        router.attach("/sessions/{sessionId}/csv/{className}", SessionClassAsCsvResource.class);   
        router.attach("/sessions/{sessionId}/reload", ReloadRulesResource.class);   
        router.attach("/sessions/{sessionId}/logs/events/latest", GetEventLogResource.class);
        router.attach("/sessions/{sessionId}/types", TypesResource.class);   
        router.attach("/sessions/{sessionId}/fire", FireRulesHandler.class);   
        router.attach("/sessions/{sessionId}/query/{queryName}", QueryHandler.class);   
        // hack!
        router.attach("/sessions/{sessionId}/query/{queryName}/{p1}", QueryHandler.class);   
        router.attach("/sessions/{sessionId}/query/{queryName}/{p1}/{p2}", QueryHandler.class);   
        router.attach("/sessions/{sessionId}/query/{queryName}/{p1}/{p2}/{p3}", QueryHandler.class);   
        router.attach("/sessions/{sessionId}/query/{queryName}/{p1}/{p2}/{p3}/{p4}", QueryHandler.class);   

        // static file serving, e.g. static forms...
        ServletContext servletContext = (ServletContext)this.getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext");
        if (servletContext!=null) {
        	String path = servletContext.getRealPath("/").replace("\\", "/");
        	logger.info("Path for / is "+path);
        	if (!path.endsWith("/"))
        		path = path+"/";
            router.attach("/sessions/{sessionId}/web/", new Directory(getContext(), "file:///"+path+"WEB-INF/html/session/"));
            DroolsSession.setLogFileDir(path+"WEB-INF/logs/");
        }
        else
        	logger.info("Could not get ServletContext - "+this.getContext().getAttributes());
        
        // also run as lobby, at least for now
        ClientAPIApplication.init(router);
        
        return router;   
    }   
    
}
