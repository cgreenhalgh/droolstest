/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi;

import java.util.LinkedList;

import org.restlet.ext.xstream.XstreamRepresentation;

import com.thoughtworks.xstream.XStream;

import uk.ac.horizon.ug.exserver.clientapi.protocol.Message;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.protocol.OperationResult;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;

/**
 * @author cmg
 *
 */
public class XstreamUtils {
	/** configure Xstream for clientapi */
	protected static void addAliases(XStream xs) {
		xs.alias("conversation", ClientConversation.class);
		xs.alias("list", LinkedList.class);    	
		xs.alias("message", Message.class);
	}
	/** configure Xstream for clientapi */
	protected static void addAliases(XstreamRepresentation xr) {
		addAliases(xr.getXstream());
	}
}
