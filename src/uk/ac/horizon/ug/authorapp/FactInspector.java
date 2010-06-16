/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.beans.PropertyDescriptor;
import java.util.Map;

import uk.ac.horizon.ug.exserver.devclient.Fact;

/** Introspect "fact" objects, either Facts or as java beans.
 * 
 * @author cmg
 *
 */
public class FactInspector {
	/** bean only */
	protected String namespace;
	/** bean only */
	protected String typeName;
	/** bean only */
	protected PropertyDescriptor[] properties;
	
	/** fact not bean? */
	protected boolean fact;
	public FactInspector(Class class1) {
		if (class1==Fact.class) {
			fact = true;
		}
		else {
			// TODO Auto-generated constructor stub
		}
	}
	
	public String getNamespace() {
		// TODO
		return null;
	}
	
	public String getTypeName() {
		// TODO
		return null;	
	}
	
	// TODO fields... Map<String,Object>
}
