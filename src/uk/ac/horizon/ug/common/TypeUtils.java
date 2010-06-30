/**
 * 
 */
package uk.ac.horizon.ug.common;

/**
 * @author cmg
 *
 */
public class TypeUtils {
	public static <C> C coerce(Object value, Class<C> fieldClass) {
		if (value==null)
			return null;
		if (fieldClass.isInstance(value))
			return (C)value;
		
		if (boolean.class.isAssignableFrom(fieldClass) || Boolean.class.isAssignableFrom(fieldClass)) {
			String fieldValueText = value.toString();
			if (fieldValueText.length()==0 || fieldValueText.charAt(0)=='f'  || fieldValueText.charAt(0)=='F' ||fieldValueText.charAt(0)=='n' ||fieldValueText.charAt(0)=='N' ||fieldValueText.charAt(0)=='0')
				return (C)Boolean.FALSE;
			else
				return (C)Boolean.TRUE;
		}
		else if (char.class.isAssignableFrom(Character.class) || Character.class.isAssignableFrom(fieldClass)) {
			String fieldValueText = value.toString();
			return (C)new Character(fieldValueText.charAt(0));
		}
		else if (Number.class.isAssignableFrom(fieldClass) || fieldClass.isPrimitive()) {
			Number number = null;
			if (value instanceof Number) 
				number = (Number)value;
			else {
				String fieldValueText = value.toString();
				if (!fieldValueText.contains("."))
					number = new Long(fieldValueText);
				else
					number = new Double(fieldValueText);
			}
			if (fieldClass.isInstance(number))
				return (C)number;
			if (fieldClass.isAssignableFrom(Double.class) || fieldClass.isAssignableFrom(double.class))
				return (C)new Double(number.doubleValue());
			if (fieldClass.isAssignableFrom(Float.class) || fieldClass.isAssignableFrom(float.class))
				return (C)new Float(number.floatValue());
			if (fieldClass.isAssignableFrom(Long.class) || fieldClass.isAssignableFrom(long.class))
				return (C)new Long(number.longValue());
			if (fieldClass.isAssignableFrom(Integer.class) || fieldClass.isAssignableFrom(int.class))
				return (C)new Integer(number.intValue());
			if (fieldClass.isAssignableFrom(Short.class) || fieldClass.isAssignableFrom(short.class))
				return (C)new Short(number.shortValue());
			if (fieldClass.isAssignableFrom(Byte.class) || fieldClass.isAssignableFrom(byte.class))
				return (C)new Byte(number.byteValue());
		}
		throw new RuntimeException("Cannot coerce "+value+" to "+fieldClass);	
	}
}
