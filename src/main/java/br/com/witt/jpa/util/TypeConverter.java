package br.com.witt.jpa.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.Transient;

/**
 * Converter used in SQL query result conversions.
 * 
 * @author lucianowtt@gmail.com
 *
 */
public class TypeConverter {

	/**
	 * Converts an array of objects to an instance of the given DTO class. The class
	 * must have a constructor with all the columns returned by the query as
	 * arguments, in the same order as declared in the SQL statement, and with
	 * compatible types.
	 * 
	 * @param <T>         the type of the DTO to be returned, resolved at runtime
	 * @param result      the query result
	 * @param resultClass the class of the DTO to be returned
	 * @return the DTO instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(Object[] result, Class<?> resultClass) {
		try {
			Field[] fields = resultClass.getDeclaredFields();
			List<Field> persistentFields = new ArrayList<Field>();
			for (Field field : fields) {
				int mod = field.getModifiers();
				if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod) && !field.isAnnotationPresent(Transient.class)) {
					persistentFields.add(field);
				}
			}

			if (persistentFields.size() > result.length) {
				throw new IllegalArgumentException("Wrong number of columns");
			}

			List<Object> args = new ArrayList<Object>();
			List<Class<?>> argsClasses = new ArrayList<Class<?>>();
			for (int i = 0; i < persistentFields.size(); i++) {
				Class<?> type = persistentFields.get(i).getType();
				argsClasses.add(type);
				args.add(convertValue(result[i], type));
			}

			Class<?>[] constructorArgClasses = argsClasses.toArray(new Class<?>[argsClasses.size()]);
			Object[] constructorArgs = args.toArray(new Object[args.size()]);

			Constructor<?> constructor = resultClass.getDeclaredConstructor(constructorArgClasses);
			T converted = (T) constructor.newInstance(constructorArgs);

			return converted;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts an object to the expected type, if compatible.
	 * 
	 * @param object the object to be converted
	 * @param type   the expected resulting type
	 * @return the resulting instance of the expected type
	 */
	public static Object convertValue(Object object, Class<?> type) {
		if (Objects.isNull(object)) {
			return null;

		} else if (type.isAssignableFrom(object.getClass())) {
			return object;

		} else if (Long.class.isAssignableFrom(type)) {
			if (Number.class.isAssignableFrom(object.getClass())) {
				return Long.valueOf(((Number) object).longValue());
			} else {
				return Long.valueOf(object.toString());
			}

		} else if (Integer.class.isAssignableFrom(type)) {
			if (Number.class.isAssignableFrom(object.getClass())) {
				return Integer.valueOf(((Number) object).intValue());
			} else {
				return Integer.valueOf(object.toString());
			}

		} else if (Short.class.isAssignableFrom(type)) {
			if (Number.class.isAssignableFrom(object.getClass())) {
				return Short.valueOf(((Number) object).shortValue());
			} else {
				return Short.valueOf(object.toString());
			}

		} else if (Double.class.isAssignableFrom(type)) {
			if (Number.class.isAssignableFrom(object.getClass())) {
				return Double.valueOf(((Number) object).doubleValue());
			} else {
				return Double.valueOf(object.toString());
			}

		} else if (Float.class.isAssignableFrom(type)) {
			if (Number.class.isAssignableFrom(object.getClass())) {
				return Float.valueOf(((Number) object).floatValue());
			} else {
				return Float.valueOf(object.toString());
			}

		} else if (Date.class.isAssignableFrom(type)) {
			if (java.sql.Date.class.isAssignableFrom(object.getClass())) {
				return new Date(((java.sql.Date) object).getTime());
			} else if (java.sql.Timestamp.class.isAssignableFrom(object.getClass())) {
				return new Date(((java.sql.Timestamp) object).getTime());
			} else if (java.sql.Time.class.isAssignableFrom(object.getClass())) {
				return new Date(((java.sql.Time) object).getTime());
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					return sdf.parse(object.toString());
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			}
		}

		return null;
	}
}
