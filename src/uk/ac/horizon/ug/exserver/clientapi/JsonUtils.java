/**
 * 
 */
package uk.ac.horizon.ug.exserver.clientapi;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.horizon.ug.authorapp.model.QueryConstraintInfo;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author cmg
 *
 */
public class JsonUtils {
	static Logger logger = Logger.getLogger(JsonUtils.class.getName());
	/** use java bean introspection to convert object to JSONObject. (no class info) 
	 * @throws IntrospectionException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws JSONException */
	public static JSONObject objectToJson(Object object, boolean includeClass) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, JSONException {
		if (object==null)
			return null;
		BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
		PropertyDescriptor properties[] = beanInfo.getPropertyDescriptors();
		JSONObject json = new JSONObject();
		for (int i=0; i<properties.length; i++) {
			String name = properties[i].getName();
			Method readMethod = properties[i].getReadMethod();
			if (readMethod==null) {
				logger.log(Level.WARNING, "Cannot read property "+name+" in "+object.getClass().getName()+" "+object);
				continue;
			}
			Object value = readMethod.invoke(object);
			if (value!=null)
				json.put(name, value);
		}
		if (includeClass) {
			String className = object.getClass().getName();
			int ix = className.lastIndexOf(".");
			String typeName = (ix>=0) ? className.substring(ix+1) : className;
			String namespace = (ix>=0) ? className.substring(0, ix) : null;
			if (namespace!=null)
				json.put("namespace", namespace);
			json.put("typeName", typeName);
		}
		return json;
	}
	/** use java bean introspection to convert object to JSONObject. (no class info) 
	 * @throws IntrospectionException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws JSONException */
	public static JSONObject objectToJson(Object object) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, JSONException {
		return objectToJson(object, false);
	}
	/** use java bean introspection to fill fields of a JSONObject into a java object.
	 * You need to provide the Class in this case (no class metainfo). 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InstantiationException 
	 * @throws IntrospectionException 
	 * @throws JSONException */
	public static <T> T JsonToObject(JSONObject json, Class<T> clazz) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException, JSONException {
		if (json==null)
			return null;
		T object = clazz.newInstance();	
		BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
		PropertyDescriptor properties[] = beanInfo.getPropertyDescriptors();
		for (int i=0; i<properties.length; i++) {
			String name = properties[i].getName();
			Method writeMethod = properties[i].getWriteMethod();
			if (writeMethod==null) {
				logger.log(Level.WARNING, "Cannot write property "+name+" in "+object.getClass().getName()+" "+object);
				continue;
			}
			if (json.has(name)) {
				Object value = json.get(name);
				if (value==null)
					continue;
				//logger.info("Set "+name+" to "+value+" in "+object+" using "+writeMethod+" ("+writeMethod.getParameterTypes().length+" parameters)");
				// coerce?!
				// enum
				Class propertyType = properties[i].getPropertyType();
				if (propertyType.isEnum()) {
					value = Enum.valueOf(propertyType, value.toString());
				}
				writeMethod.invoke(object, value);
			}
		}
		return object;
	}
}
