/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.io.FileReader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.codehaus.janino.Java.TypeDeclaration;
import org.drools.compiler.DrlParser;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.TypeDeclarationDescr;
import org.drools.lang.descr.TypeFieldDescr;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import uk.ac.horizon.ug.exserver.protocol.TypeDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription;

/** return session types.
 * 
 * @author cmg
 *
 */
public class TypesResource extends SessionResource {
	/** return session types */
	@Post("xml")
	@Get("xml")
	public Representation getTypes() {
		List<TypeDescription> tds = DroolsUtils.getTypeDescriptions(this.sessionInfo.getRulesetUrls());
		if (tds==null) {
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		}
		XstreamRepresentation<List<TypeDescription>> xml = new XstreamRepresentation<List<TypeDescription>>(MediaType.APPLICATION_XML, tds);
		xml.getXstream().alias("type", TypeDescription.class);
		xml.getXstream().alias("field", TypeFieldDescription.class);
		//xml.getXstream().alias("types", LinkedList.class);
    	//addAliases(xml);
		// immediate expire?
		xml.setExpirationDate(new Date());
    	return xml;
	}
}
