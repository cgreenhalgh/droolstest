/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.util.Map;

/** DO NOT USE - FactInspector NOT IMPLEMENTED.
 * Factory/cache by name of FactInspectors
 * 
 * @author cmg
 *
 */
public class FactInspectorFactory {
	/** cache by class name */
	protected Map<String,FactInspector> factInspectors;
	/** get */
	public synchronized FactInspector getFactInspector(Object object) throws UnsupportedOperationException {
		if (object==null)
			return null;
		String className = object.getClass().getName();
		FactInspector fi = factInspectors.get(className);
		if (fi==null) {
			fi = new FactInspector(object.getClass());
			factInspectors.put(className, fi);
		}
		return fi;
	}
}
