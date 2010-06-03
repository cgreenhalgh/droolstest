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
		List<TypeDeclarationDescr> types = new LinkedList<TypeDeclarationDescr>();
		String ruleSetUrls [] = this.sessionInfo.getRulesetUrls();
		DrlParser parser = new DrlParser();
		for (int ti=0; ruleSetUrls!=null && ti<ruleSetUrls.length; ti++) {
			try {
				PackageDescr packageDescr = parser.parse(true, new URL(ruleSetUrls[ti]).openStream());
				types.addAll(packageDescr.getTypeDeclarations());
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error parsing rule set "+ruleSetUrls[ti], e);
				setStatus(Status.SERVER_ERROR_INTERNAL);
				return null;
			}
		}
		List<TypeDescription> tds = new LinkedList<TypeDescription>();
		for (TypeDeclarationDescr type : types) {
			TypeDescription td = new TypeDescription();
			td.setNamespace(type.getNamespace());
			td.setTypeName(type.getTypeName());
			td.setTypeMeta(type.getMetaAttributes());
			Map<String,TypeFieldDescr> fields = type.getFields();
			Map<String,TypeFieldDescription> tfds = new HashMap<String,TypeFieldDescription>();
			for (Map.Entry<String,TypeFieldDescr> entry : fields.entrySet()) {
				TypeFieldDescription tfd = new TypeFieldDescription();
				TypeFieldDescr field = entry.getValue();
				tfd.setFieldMeta(field.getMetaAttributes());
				// pattern has id & objectType; latter seems to be java type.
				logger.info("Field "+entry.getKey()+": pattern="+field.getPattern()+", initExpr="+field.getInitExpr()+", ="+field.getText());
				tfd.setTypeName(field.getPattern().getObjectType());
				tfds.put(entry.getKey(), tfd);
			}
			td.setFields(tfds);
			tds.add(td);
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
