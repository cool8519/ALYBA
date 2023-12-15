package dal.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtil {
	
	public static Field getField(Object obj, String name) {
        return getField(obj.getClass(), name);
	}

	public static Field getField(Class<?> cls, String name) {
        while(cls != Object.class) {
        	for(Field f : cls.getDeclaredFields()) {
        		if(f.getName().equals(name)) {
        			return f;
        		}
        	}
            cls = cls.getSuperclass();
        }
        return null;
	}

	public static List<Field> getFields(Object obj) {
		return getFields(obj.getClass());
    }

	public static List<Field> getFields(Class<?> cls) {
        List<Field> fields = new ArrayList<Field>();
        while(cls != Object.class) {
            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        }
        return fields;
    }
	
	public static boolean isNumberType(Field f) {
		Class<?> type = f.getType();
		if(type == int.class || type == Integer.class || type == long.class || type == Long.class ||
		   type == double.class || type == Double.class || type == float.class || type == Float.class ||
		   type == short.class || type == Short.class) {
			return true;
		}
		return false;
	}
	
}
