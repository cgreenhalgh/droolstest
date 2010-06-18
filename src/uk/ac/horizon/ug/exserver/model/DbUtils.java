/**
 * 
 */
package uk.ac.horizon.ug.exserver.model;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

/**
 * @author cmg
 *
 */
public class DbUtils {
	private static EntityManagerFactory emf = null;
	public static synchronized EntityManagerFactory getEntityManagerFactory() {
		if (emf!=null)
			return emf;
		emf = Persistence.createEntityManagerFactory( "droolstest" );
		return emf;
	}
	public static EntityManager getEntityManager() {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		return em;
	}
	public static UserTransaction getUserTransaction() throws NamingException {
		UserTransaction ut =
			(UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
		return ut;
	}
}
