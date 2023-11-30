package dal.tool.analyzer.alyba.ui;

import org.eclipse.swt.widgets.Display;

import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;
import dal.util.LoggingUtil;

public class Logger {

	public static void logln(String s) {
		printMessage(s, dal.tool.analyzer.alyba.util.Logger.RESULT);
	}

	public static void error(String s) {
		printMessage(s, dal.tool.analyzer.alyba.util.Logger.ERROR);
	}

	public static void error(Throwable t) {
		printMessage(LoggingUtil.getTraceString(t), dal.tool.analyzer.alyba.util.Logger.ERROR);
	}
	
	public static void debug(String s) {
		printMessage(s, dal.tool.analyzer.alyba.util.Logger.DEBUG);
	}

	public static void debug(Throwable t) {
		printMessage(LoggingUtil.getTraceString(t), dal.tool.analyzer.alyba.util.Logger.DEBUG);
	}

	public static void printMessage(final String s, int level) {
		dal.tool.analyzer.alyba.util.Logger.logln(s, level);
		if(AlybaGUI.debugMode) {
			if("main".equals(Thread.currentThread().getName())) {
				AlybaGUI.getDebugConsole().addDebugMessage(s);
			} else {
				Display display = AlybaGUI.instance != null ? AlybaGUI.getInstance().display : ResultAnalyzer.instance.display;
				display.syncExec(new Runnable() {
					public void run() {
						AlybaGUI.getDebugConsole().addDebugMessage(s);
					}
				});
			}
		}
	}

}
