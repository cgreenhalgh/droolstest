/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.List;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.drools.common.DisconnectedFactHandle;
import org.drools.runtime.rule.FactHandle;
import org.restlet.resource.Get;   
import org.restlet.resource.Post;   
import org.restlet.representation.Representation;   
import org.restlet.representation.StringRepresentation;   

import uk.ac.horizon.ug.commonfacts.SystemTime;
import uk.ac.horizon.ug.exserver.model.Session;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cmg
 *
 */
public class TickHandler extends BaseResource {
	static Logger logger = Logger.getLogger(TickHandler.class.getName());
	
	/** temporary global tickCount - workaround getObject not working */
	static int tickCount = 0;
	
	@Post
	@Get
	public Representation tick() throws NamingException, NotSupportedException, SystemException {   
    	synchronized(TickHandler.class) {
    		tickCount++;
    	}
    	int updated = 0;
		UserTransaction ut = getTransaction();
    	ut.begin();
		try {
	    	EntityManager em = getEntityManager();
			Query q = em.createQuery ("SELECT x FROM Session x");
			List<Session> sessions = (List<Session>) q.getResultList ();

			for (Session session : sessions) {
				if (!session.isUpdateSystemTime())
					continue;
				try {
					DroolsSession droolsSession = DroolsSession.getSession(session);
					synchronized(droolsSession) {
						SystemTime systemTime = new SystemTime();
						systemTime.setTickCount(tickCount);
						systemTime.setTime(System.currentTimeMillis());
						if (session.getSystemTimeHandle()!=null) {
							FactHandle fh = new DisconnectedFactHandle(session.getSystemTimeHandle());
							droolsSession.getKsession().update(fh, systemTime);
							FactHandle newfh = droolsSession.getKsession().getFactHandle(systemTime);
							if (newfh!=null) 
								session.setSystemTimeHandle(newfh.toExternalForm());
						} else {
							session.setSystemTimeHandle(droolsSession.getKsession().insert(systemTime).toExternalForm());	    				
						}
						em.persist(session);
					}
					updated++;
				} catch (Exception e) {
					logger.log(Level.WARNING,"problem updating time in session "+session.getId()+" for time handle "+session.getSystemTimeHandle(), e);
				}
			}

			ut.commit();
			em.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, "doing tick", e);
			ut.rollback();
		}
		return new StringRepresentation("Updated "+updated+" session's SystemTime");
	}
}
