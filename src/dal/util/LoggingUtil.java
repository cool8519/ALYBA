package dal.util;

public class LoggingUtil {

	public static void printTraceString(Throwable trace) {
		System.out.println(getTraceString(trace));
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
