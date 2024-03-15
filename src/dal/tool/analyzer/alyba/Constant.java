package dal.tool.analyzer.alyba;

import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class Constant {

	public static final String PROGRAM_VERSION = "1.9.1";

	public static String DEFAULT_FONT_NAME = "Arial";
	public static int DEFAULT_FONT_SIZE = 9;
	
	public static final TimeZone TIMEZONE_DEFAULT = TimeZone.getDefault();
	public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

	public static final int AVAILABLE_CPU = Runtime.getRuntime().availableProcessors();

	public static final int SPLASH_TIME = 3000;
	public static final int MAX_THREAD_COUNT = AVAILABLE_CPU * 2;
	public static final int MAX_SAMPLING_COUNT = 10;
	public static final int WARNING_TXUNIT_COUNT = 50000;

	public static final String SETTING_FILE_HEADER = "[ALYBA Setting File]";

	public static final String OUTPUT_DEFAULT_DIRECTORY = System.getProperty("user.dir");
	public static final String OUTPUT_DEFAULT_TITLE = "Untitled";
	public static final String OUTPUT_TEMPORARY_DIRECTORY = System.getProperty("java.io.tmpdir");
	public static final String OUTPUT_FILENAME_PREFIX = "ALYBA";

	public static final String BASE_PATH = "dal/tool/analyzer/alyba/";
	public static final String RESOURCE_PATH = BASE_PATH + "resource/";
	public static final String IMAGE_PATH = RESOURCE_PATH + "image/";
	
	public static final String IMAGE_PATH_LOGO = IMAGE_PATH + "logo.jpg";
	public static final String IMAGE_PATH_TRAYICON = IMAGE_PATH + "app_icon.png";
	public static final String IMAGE_PATH_PROGRESS = IMAGE_PATH + "progress.gif";
	public static final String IMAGE_PATH_TRASH = IMAGE_PATH + "trash.gif";
	public static final String IMAGE_PATH_CHART_LINE = IMAGE_PATH + "chart_line.png";
	public static final String IMAGE_PATH_CHART_HBAR = IMAGE_PATH + "chart_hbar.png";
	public static final String IMAGE_PATH_CHART_VBAR = IMAGE_PATH + "chart_vbar.png";
	public static final String IMAGE_PATH_CHART_PIE = IMAGE_PATH + "chart_pie.png";
	public static final String IMAGE_PATH_CHART_SCATTER = IMAGE_PATH + "chart_scatter.png";
	public static final String IMAGE_PATH_CHART_ALL = IMAGE_PATH + "chart_all.png";
	public static final String IMAGE_PATH_CHART_CPU = IMAGE_PATH + "chart_cpu.png";
	public static final String IMAGE_PATH_CHART_MEMORY = IMAGE_PATH + "chart_memory.png";
	public static final String IMAGE_PATH_CHART_DISK = IMAGE_PATH + "chart_disk.png";
	public static final String IMAGE_PATH_CHART_NETWORK = IMAGE_PATH + "chart_network.png";
	
	public static final String FILE_PATH_HTMLTEMPLATE = RESOURCE_PATH + "template/HtmlTemplate.html";
	public static final String FILE_PATH_DEFAULTSETTING = RESOURCE_PATH + "setting/default.alb";
	public static final String FILE_PATH_GEOIP = RESOURCE_PATH + "geoip/GeoLite2-Country.mmdb";

	public static final String DIALOG_INIT_PATH = ".";

	public static final String LOG_DEFAULT_DELIMETER = " \t";
	public static final String FILE_DEFAULT_DELIMETER = " \t,";

	public static final String[] LOG_DEFAULT_BRACELETS = { "[]", "\"\"" };
	public static final String[] FILE_DEFAULT_BRACELETS = { "[]", "\"\"" };

	public static final Transfer[] FILE_TRANSFER_TYPE = new Transfer[] { FileTransfer.getInstance() };
	public static final Transfer[] TEXT_TRANSFER_TYPE = new Transfer[] { TextTransfer.getInstance() };

	public static final String[] SETTING_FILTER_NAMES = { "ALYBA Setting Files (*.alb)", "All Files (*.*)" };
	public static final String[] SETTING_FILTER_EXTS = { "*.alb", "*.*" };

	public static final String[] LOG_FILTER_NAMES = { "Log Files (*.log)", "Text Files (*.txt)", "All Files (*.*)" };
	public static final String[] LOG_FILTER_EXTS = { "*.log", "*.txt", "*.*" };
	public static final String[] CSV_FILTER_NAMES = { "CSV Files (*.csv)" };
	public static final String[] CSV_FILTER_EXTS = { "*.csv" };
	public static final String[] DB_FILTER_NAMES = { "ALYBA DB Files (*.adb)" };
	public static final String[] DB_FILTER_EXTS = { "*.adb" };
	public static final String[] ALLFILES_FILTER_NAMES = { "All Files (*.*)" };
	public static final String[] ALLFILES_FILTER_EXTS = { "*.*" };

	public static final String[] LOG_TYPES = { "Customize", "Apache", "Tomcat", "WebtoB", "Nginx", "JEUS", "IIS", "JSON" };
	public static final String[] FILE_TYPES = { "Customize", "vmstat", "sar" };

	public static final String[] ELAPSED_TIME_UNITS = { "milliseconds", "seconds", "microseconds" };

	public static final Locale[] TIME_LOCALES = { Locale.ENGLISH, Locale.KOREAN };

	public static final String[] TIME_FORMATS = {
												  "dd/MMM/yyyy:HH:mm:ss Z",   // Apache, Tomcat, Nginx, CLF
												  "yyyy-MM-dd HH:mm:ss",      // IIS, lighttpd, W3C
												  "yyyy.MM.dd HH:mm:ss",      // JEUS
												  "yyyy.MM.dd HH:mm:ss.SSS",  // Millisecond
												  "yyyy/MM/dd HH:mm:ss",      // Resin
												  "dd/MMM/yyyy HH:mm:ss,SSS", // Wildfly
												  "yyyy-MM-dd'T'HH:mm:ssZ",   // WebSphere
												  "MM/dd/yyyy hh:mm:ss a",     // AM,PM 
												  "yyyy-MM-dd/HH:mm:ss"
												};
	public static final String UNIX_TIME_STR = "UnixTime";
	
	public static final int ANALYZER_DATA_PAGESIZE = 50;
	
}
