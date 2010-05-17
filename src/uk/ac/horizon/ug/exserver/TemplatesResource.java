/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.restlet.data.MediaType;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.resource.ServerResource;
import org.restlet.resource.Get;   
import org.restlet.resource.Post;   
import org.restlet.representation.Representation;   

import org.restlet.data.Form;   
import org.restlet.data.MediaType;   
import org.restlet.data.Status;   

import uk.ac.horizon.apptest.model.Region;
import uk.ac.horizon.ug.exserver.RestletApplication;
import uk.ac.horizon.ug.exserver.model.SessionTemplate;

/**
 * @author cmg
 *
 */
public class TemplatesResource extends ServerResource {
  
//    @Get  
 //   public String toString() {   
  //  }   
	/** XML list of all rep */
    @Get("xml")   // XML: @Get("xml")
    public Representation toXml() throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
    	RestletApplication app = (RestletApplication)this.getApplication();
    	EntityManager em = app.getEntityManager();
    	UserTransaction ut = app.getTransaction();
    	
		ut.begin();
		Query q = em.createQuery ("SELECT x FROM SessionTemplate x");
		List<SessionTemplate> results = (List<SessionTemplate>) q.getResultList ();
		ut.commit();
		
    	XstreamRepresentation<List<SessionTemplate>> xml = new XstreamRepresentation<List<SessionTemplate>>(MediaType.APPLICATION_XML, results);
    	return xml;
    }
    
    /**  
     * Handle POST requests: create a new item.  
     */  
 /*   @Post  
    public Representation acceptItem(Representation entity) {   
        Representation result = null;   
        // Parse the given representation and retrieve pairs of   
        // "name=value" tokens.   
        Form form = new Form(entity);   
        String itemName = form.getFirstValue("name");   
        String itemDescription = form.getFirstValue("description");   
  
        // Register the new item if one is not already registered.   
        if (!getItems().containsKey(itemName)   
                && getItems().putIfAbsent(itemName,   
                        new Item(itemName, itemDescription)) == null) {   
            // Set the response's status and entity   
            setStatus(Status.SUCCESS_CREATED);   
            Representation rep = new StringRepresentation("Item created",   
                    MediaType.TEXT_PLAIN);   
            // Indicates where is located the new resource.   
            rep.setIdentifier(getRequest().getResourceRef().getIdentifier()   
                    + "/" + itemName);   
            result = rep;   
        } else { // Item is already registered.   
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);   
            result = generateErrorRepresentation("Item " + itemName   
                    + " already exists.", "1");   
        }   
  
        return result;   
    }   
*/
}
