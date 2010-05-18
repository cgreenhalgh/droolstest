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

/**
 * @author cmg
 *
 */
public class BaseResource extends ServerResource {

	public RestletApplication getRestletApplication() {
    	return (RestletApplication)getApplication();
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
