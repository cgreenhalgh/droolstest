/**
 * 
 */
package uk.ac.horizon.apptest.server;

import uk.ac.horizon.apptest.model.Region;

import java.io.IOException;
import java.io.Writer;

import java.util.List;

import javax.naming.InitialContext;
import javax.persistence.Persistence;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author cmg
 *
 */
public class JPATestServlet extends HttpServlet {
	static Logger logger = Logger.getLogger(JPATestServlet.class.getName());
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	//@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory( "droolstest" );
			UserTransaction ut =
				  (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );

			ut.begin();

			EntityManager em = emf.createEntityManager();

			Query q = em.createQuery ("SELECT x FROM Region x");
			List<Region> results = (List<Region>) q.getResultList ();
			for (Region r : results) 
				logger.info("Found "+r.getId()+": "+r);
			
			Region r = em.find(Region.class, "R1");
			logger.info("R1 = "+r);
			if (r==null) {
				r = new Region();
				r.setId("R1");
				r.setDescription("New");
				r.setTitle("title");
				em.persist(r);
				logger.info("Persist new "+r);
			}
			else {
				r.setDescription(r.getDescription()+".");
				//em.persist(r);
				r.setDescription(r.getDescription()+".");
				logger.info("Change description to "+r.getDescription());
			}
			Query q2 = em.createQuery ("SELECT x FROM Region x");
			List<Region> results2 = (List<Region>) q.getResultList ();
			for (Region r2 : results2) 
				logger.info("Found2 "+r2.getId()+": "+r2);

			ut.commit();
			
			em.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		Writer w = resp.getWriter();
		w.write("Hello!\n");
		w.close();
	}

}
