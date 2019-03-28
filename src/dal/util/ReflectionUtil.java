package dal.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtil {
	
	public static List<Field> getFields(Object obj) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> cls = obj.getClass();
        while(cls != Object.class) {
            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        }
        return fields;
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
