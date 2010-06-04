/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.Date;
import java.util.logging.Level;

import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * @author cmg
 *
 */
public class FireRulesHandler extends SessionResource {
	@Get("txt")
	@Post("txt")
	public Representation fireRules(Representation entity) throws NamingException, NotSupportedException, SystemException {
        Form form = new Form(entity);   
        String groups [] = form.getValuesArray("agenda-group");
		synchronized (droolsSession) {
			UserTransaction ut = this.getTransaction();
			ut.begin();
			try {
				for (int i=0; i<groups.length; i++) {
					if(groups[i].length()>0)
						try {
							droolsSession.getKsession().getAgenda().getAgendaGroup(groups[i]).setFocus();
						}
					catch (Exception e) {
						logger.log(Level.WARNING, "Problem setting focus to agenda group: "+groups[i], e);
					}
				}
				droolsSession.getKsession().fireAllRules();
				ut.commit();
			}
			catch (Exception e) {
				logger.log(Level.WARNING, "error firing rules", e);
				ut.rollback();
				setStatus(Status.SERVER_ERROR_INTERNAL, e);
				return null;
			}
		}
		Representation result = new StringRepresentation("OK");
		result.setExpirationDate(new Date());
		return result;
	}
}
