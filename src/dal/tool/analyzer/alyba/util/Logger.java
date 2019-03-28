package dal.tool.analyzer.alyba.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Logger extends Object {

	public static final int RESULT = 0;
	public static final int ERROR = 1;
	public static final int INFO = 2;
	public static final int DEBUG = 3;

	public static final int BOTH = 10;
	public static final int CONSOLE = 11;
	public static final int FILE = 12;

	private static String fileName = null;
	private static File f = null;
	private static FileWriter fw = null;
	private static BufferedWriter bw = null;
	private static int level;
	private static int target;

	static {
		String s = System.getProperty("alyba.log.level", "INFO").toUpperCase();
		if("RESULT".equals(s)) {
			level = RESULT;
		} else if("ERROR".equals(s)) {
			level = ERROR;
		} else if("INFO".equals(s)) {
			level = INFO;
		} else if("DEBUG".equals(s)) {
			level = DEBUG;
		} else {
			level = INFO;
		}
		target = CONSOLE;
	}

	public static String getFileName() {
		return fileName;
	}

	public static void setLevel(int l) {
		level = l;
	}

	public static void setTarget(int t) {
		target = t;
	}

	public static void openFile(String fname, boolean append) {
		try {
			if(fileName != null) {
				closeFile();
			}
			fileName = fname;
			f = new File(fileName);
			fw = new FileWriter(f, append);
			bw = new BufferedWriter(fw);
		} catch(Exception e) {
			loglnStackTrace(e, DEBUG);
		}
	}

	public static void closeFile() {
		try {
			if(fileName != null) {
				bw.flush();
				bw.close();
			}
			fileName = null;
			if(target == FILE) {
				target = CONSOLE;
			}
		} catch(Exception e) {
			loglnStackTrace(e, DEBUG);
		}
	}

	public static void logConsole(Object msg) {
		logConsole(msg, RESULT);
	}

	public static void logFile(Object msg) {
		logFile(msg, RESULT);
	}

	public static void logToTarget(Object msg) {
		if(fileName == null) {
			logConsole(msg);
		} else {
			logFile(msg);
		}
	}

	public static void log(Object msg) {
		log(msg, RESULT);
	}

	public static void loglnConsole() {
		loglnConsole("");
	}

	public static void loglnFile() {
		loglnFile("");
	}

	public static void loglnToTarget() {
		if(fileName == null) {
			loglnConsole("");
		} else {
			loglnFile("");
		}
	}

	public static void logln() {
		logln("");
	}

	public static void loglnConsole(Object msg) {
		loglnConsole(msg, RESULT);
	}

	public static void loglnFile(Object msg) {
		loglnFile(msg, RESULT);
	}

	public static void loglnToTarget(Object msg) {
		if(fileName == null) {
			loglnConsole(msg);
		} else {
			loglnFile(msg);
		}
	}

	public static void logln(Object msg) {
		logln(msg, RESULT);
	}

	public static void logConsole(Object msg, int l) {
		if(l <= level) {
			log(msg, false, CONSOLE, l);
		}
	}

	public static void logFile(Object msg, int l) {
		if(l <= level) {
			log(msg, false, FILE, l);
		}
	}

	public static void logToTarget(Object msg, int l) {
		if(fileName == null) {
			logConsole(msg, l);
		} else {
			logFile(msg, l);
		}
	}

	public static void log(Object msg, int l) {
		if(l <= level) {
			log(msg, false, BOTH, l);
		}
	}

	public static void loglnConsole(Object msg, int l) {
		if(l <= level) {
			log(msg, true, CONSOLE, l);
		}
	}

	public static void loglnFile(Object msg, int l) {
		if(l <= level) {
			log(msg, true, FILE, l);
		}
	}

	public static void loglnToTarget(Object msg, int l) {
		if(fileName == null) {
			loglnConsole(msg, l);
		} else {
			loglnFile(msg, l);
		}
	}

	public static void logln(Object msg, int l) {
		if(l <= level) {
			log(msg, true, target, l);
		}
	}

	public static void log(Object msg, boolean newline, int target, int l) {
		switch(target) {
			case BOTH:
				logConsole(msg, newline, l);
				logFile(msg, newline, l);
				break;
			case CONSOLE:
				logConsole(msg, newline, l);
				break;
			case FILE:
				logFile(msg, newline, l);
				break;
		}
	}

	public static void logConsole(Object msg, boolean newline, int l) {
		String str = (msg instanceof String) ? (String)msg : msg.toString();
		if(l == ERROR) {
			str = str.replaceAll("\n", "\n[ERROR] ");
			str = "[ERROR] " + str;
		} else if(l == DEBUG) {
			str = str.replaceAll("\n", "\n[DEBUG] ");
			str = "[DEBUG] " + str;
		}
		if(newline) {
			System.out.println(str);
		} else {
			System.out.print(str);
		}
	}

	public static void logFile(Object msg, boolean newline, int l) {
		if(fileName != null) {
			try {
				String str;
				str = (msg instanceof String) ? (String)msg : msg.toString();
				str = str.replaceAll("\r\n", "\n").replaceAll("\n", "\r\n");
				if(l > INFO) {
					str = str.replaceAll("\n", "\n[DEBUG] ");
					str = "[DEBUG] " + str;
				}
				if(newline) {
					bw.write(str);
					bw.newLine();
				} else {
					bw.write(str);
				}
			} catch(Exception e) {
				loglnStackTrace(e, Logger.DEBUG);
			}
		}
	}

	public static void printStackTrace(Throwable t) {
		loglnToTarget(getTraceString(t), ERROR);
	}

	public static void loglnStackTrace(Throwable t, int l) {
		loglnToTarget(getTraceString(t), l);
	}

	public static String getTraceString(Throwable t) {
		StringBuffer strBuffer = new StringBuffer(256);
		strBuffer.append(t + "\n");
		if(t != null) {
			StackTraceElement[] stackTrace = t.getStackTrace();
			for(int i = 0; i < stackTrace.length; i++) {
				strBuffer.append("\tat " + stackTrace[i] + "\n");
			}
			strBuffer.append(getTraceStringAsCause(t));
		}
		return strBuffer.toString();
	}

	public static String getTraceStringAsCause(Throwable t) {
		StringBuffer strBuffer = null;
		Throwable t2 = t.getCause();
		if(t2 != null) {
			strBuffer = new StringBuffer(256);
			StackTraceElement[] stackTrace = t.getStackTrace();
			StackTraceElement[] stackTrace2 = t2.getStackTrace();
			int i = stackTrace2.length - 1;
			for(int j = stackTrace.length - 1; i >= 0 && j >= 0 && stackTrace2[i].equals(stackTrace[j]); j--) {
				i--;
			}
			int k = stackTrace2.length - 1 - i;
			strBuffer.append("Caused by: " + t2 + "\n");
			for(int l = 0; l <= i; l++) {
				strBuffer.append("\tat " + stackTrace2[l] + "\n");
			}
			if(k != 0) {
				strBuffer.append("\t... " + k + " more\n");
			}
			strBuffer.append(getTraceStringAsCause(t2));
		}
		if(strBuffer != null) {
			return strBuffer.toString();
		} else {
			return "";
		}
	}

}