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

import org.restlet.resource.ServerResource;

import uk.ac.horizon.ug.exserver.model.DbUtils;

/**
 * @author cmg
 *
 */
public class BaseResource extends ServerResource {

	public RestletApplication getRestletApplication() {
    	return (RestletApplication)getApplication();
    }
	
    /** get persistence entity manager */
    public static EntityManager getEntityManager() {
    	EntityManager em = DbUtils.getEntityManager();
		return em;
    }
    /** get transaction */
    public static UserTransaction getTransaction() throws NamingException {
    	return DbUtils.getUserTransaction();
    }

}
