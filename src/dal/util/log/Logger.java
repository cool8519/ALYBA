package dal.util.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger extends Object {

	public static final int FATAL = 0;
	public static final int INFO = 1;
	public static final int DEBUG = 2;

	private static int showLevel = INFO;
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	public static void setShowLevel(int level) {
		showLevel = level;
	}

	public static String getLevelName(int level) {
		String name;
		switch(level) {
			case FATAL:
				name = "FATAL";
				break;
			case INFO:
				name = "INFO";
				break;
			case DEBUG:
				name = "DEBUG";
				break;
			default:
				name = null;
				break;
		}
		return name;
	}

	public static void log(Object obj, Object msg) {
		log(obj.getClass(), msg);
	}

	public static void log(Class<?> cl, Object msg) {
		String classname = cl.getName();
		classname = (classname.indexOf(".") > 0) ? classname.substring(classname.lastIndexOf(".") + 1) : classname;
		log(classname, showLevel, msg);
	}

	public static void log(String classname, Object msg) {
		log(classname, showLevel, msg);
	}

	public static void log(Class<?> cl, int level, Object msg) {
		String classname = cl.getName();
		classname = (classname.indexOf(".") > 0) ? classname.substring(classname.lastIndexOf(".") + 1) : classname;
		log(classname, level, msg);
	}

	public static void log(String classname, int level, Object msg) {
		String tm = formatter.format(new Date(System.currentTimeMillis()));
		if(level <= showLevel) {
			System.out.println("[" + tm + "][" + getLevelName(level) + "] [" + classname + "] " + msg);
		}
	}

}