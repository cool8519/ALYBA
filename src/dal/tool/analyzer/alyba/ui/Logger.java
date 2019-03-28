package dal.tool.analyzer.alyba.ui;

import dal.util.LoggingUtil;

public class Logger {

	public static void logln(String s) {
		dal.tool.analyzer.alyba.util.Logger.logln(s, dal.tool.analyzer.alyba.util.Logger.RESULT);
	}

	public static void error(Throwable t) {
		dal.tool.analyzer.alyba.util.Logger.printStackTrace(t);
		printDebugMessage(LoggingUtil.getTraceString(t), false);
	}
	
	public static void debug(String s) {
		printDebugMessage(s, true);
	}

	public static void debug(Throwable t) {
		printDebugMessage(LoggingUtil.getTraceString(t), true);
	}

	public static void printDebugMessage(final String s, boolean debug) {
		dal.tool.analyzer.alyba.util.Logger.logln(s, dal.tool.analyzer.alyba.util.Logger.DEBUG);
		if(AlybaGUI.debugMode) {
			AlybaGUI.getInstance().display.syncExec(new Runnable() {
				public void run() {
					AlybaGUI.getInstance().debug(s);
				}
			});
		}
	}

}
