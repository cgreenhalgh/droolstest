/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.net.URLDecoder;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.drools.QueryResult;
import org.drools.runtime.rule.QueryResults;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;

/**
 * @author cmg
 *
 */
public class QueryHandler extends SessionResource {
	String queryName;
	String urlParameters[];
	
	@Override
	public void doInit() throws ResourceException {   
		super.doInit();
        // Get the "itemName" attribute value taken from the URI template   
        // /items/{itemName}.   
		try {
			this.queryName = URLDecoder.decode((String) getRequest().getAttributes().get("queryName"),"UTF-8");
			LinkedList<String> ps = new LinkedList<String>();
			for (int i=1; i<10; i++) {
				if (getRequest().getAttributes().get("p"+i)!=null)
					ps.add(URLDecoder.decode((String) getRequest().getAttributes().get("p"+i),"UTF-8"));
				else
					break;
			}
			if (ps.size()>0) {
				logger.info("URL parameters: "+ps);
				urlParameters = ps.toArray(new String[ps.size()]);
			}
		}
		catch(Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,e);
		}
        if (queryName==null)
        	throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No queryName for QueryHandler");
 	}
	@Post("xml")
	@Get("xml")
	public Representation doQuery(Representation entity) {
		// path-encoded parameters?
        String parameters [] = urlParameters;
        if (parameters==null) {
        	Form form = new Form(entity);   
        	parameters = form.getValuesArray("parameter");
        }
        logger.info("Query '"+queryName+"' with "+parameters.length+" parameters");
		QueryResults qr = droolsSession.getKsession().getQueryResults(queryName, parameters);
    	XstreamRepresentation<QueryResults> xml = new XstreamRepresentation<QueryResults>(MediaType.APPLICATION_XML, qr);
    	addAliases(xml);
		// immediate expire?
		xml.setExpirationDate(new Date());
    	return xml;
	}
}
