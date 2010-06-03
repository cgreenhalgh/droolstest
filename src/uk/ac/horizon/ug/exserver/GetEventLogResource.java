/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.persistence.EntityManager;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Post;   
import org.restlet.resource.Get;   
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;   

import uk.ac.horizon.ug.exserver.model.Session;

/**
 * @author cmg
 *
 */
public class GetEventLogResource extends SessionResource {
	static Logger logger = Logger.getLogger(GetEventLogResource.class.getName());

	/** post - force reload 
	 * @throws NamingException */
	@Post("xml")
	@Get("xml")
	public Representation getEventLog() throws NamingException {
		String logFileName = this.droolsSession.rotateLog(getEntityManager(), sessionInfo);
		logger.info("Rotated log, was "+logFileName);
		if (logFileName==null)
		{
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		Representation result = new FileRepresentation(logFileName, MediaType.APPLICATION_XML);
		result.setExpirationDate(new Date());
		return result;
	}
}
