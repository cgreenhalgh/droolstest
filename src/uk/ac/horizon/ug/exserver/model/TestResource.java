/**
 * 
 */
package uk.ac.horizon.ug.exserver.model;

import uk.ac.horizon.apptest.model.ContentMapping;

import org.restlet.resource.Get;   
import org.restlet.resource.ServerResource;   
import org.restlet.data.MediaType;

import org.restlet.ext.xstream.XstreamConverter;
import org.restlet.ext.xstream.XstreamRepresentation;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import java.util.logging.Logger;

/**
 * @author cmg
 *
 */
public class TestResource extends ServerResource  {
	  
	static Logger logger = Logger.getLogger(TestResource.class.getName());
	
	/** JSON rep */
    @Get("json")   // XML: @Get("xml")
    public XstreamRepresentation<ContentMapping> toXml() throws java.io.IOException {   
    	ContentMapping cm= new ContentMapping();
    	cm.setContent_id("C1");
    	cm.setRegion_id("R1");
    	// JSON
    	XstreamRepresentation<ContentMapping> xml = new XstreamRepresentation<ContentMapping>(MediaType.APPLICATION_JSON, cm);
    	// XML
    	//XstreamRepresentation<ContentMapping> xml = new XstreamRepresentation<ContentMapping>(MediaType.APPLICATION_XML, cm);
    	// JSON - include this line, else default to XML
    	xml.setXstream(new XStream(new JettisonMappedXmlDriver()));

    	xml.getXstream().alias("ContentMapping", ContentMapping.class);
    	// test reverse route
    	ContentMapping cm2 = new XstreamConverter().toObject(xml, ContentMapping.class, this);
    	logger.info("TestResource "+cm+" -> "+xml+" -> "+cm2+" ("+(cm2!=null ? cm2.getContent_id() : "null")+","+(cm2!=null ? cm2.getRegion_id() : "null")+")");
    	return xml;
    }   
  
}
