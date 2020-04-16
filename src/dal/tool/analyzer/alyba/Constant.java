package dal.tool.analyzer.alyba;

import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class Constant {

	public static final String PROGRAM_VERSION = "1.7.0";

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

	public static final String IMAGE_PATH_LOGO = "dal/tool/analyzer/alyba/resource/image/logo.jpg";
	public static final String IMAGE_PATH_TRAYICON = "dal/tool/analyzer/alyba/resource/image/app_icon.png";
	public static final String IMAGE_PATH_PROGRESS = "dal/tool/analyzer/alyba/resource/image/progress.gif";
	public static final String IMAGE_PATH_TRASH = "dal/tool/analyzer/alyba/resource/image/trash.gif";
	public static final String IMAGE_PATH_CHART_LINE = "dal/tool/analyzer/alyba/resource/image/chart_line.png";
	public static final String IMAGE_PATH_CHART_HBAR = "dal/tool/analyzer/alyba/resource/image/chart_hbar.png";
	public static final String IMAGE_PATH_CHART_VBAR = "dal/tool/analyzer/alyba/resource/image/chart_vbar.png";
	public static final String IMAGE_PATH_CHART_PIE = "dal/tool/analyzer/alyba/resource/image/chart_pie.png";
	public static final String IMAGE_PATH_CHART_SCATTER = "dal/tool/analyzer/alyba/resource/image/chart_scatter.png";
	public static final String IMAGE_PATH_CHART_ALL = "dal/tool/analyzer/alyba/resource/image/chart_all.png";
	public static final String IMAGE_PATH_CHART_CPU = "dal/tool/analyzer/alyba/resource/image/chart_cpu.png";
	public static final String IMAGE_PATH_CHART_MEMORY = "dal/tool/analyzer/alyba/resource/image/chart_memory.png";
	public static final String IMAGE_PATH_CHART_DISK = "dal/tool/analyzer/alyba/resource/image/chart_disk.png";
	public static final String IMAGE_PATH_CHART_NETWORK = "dal/tool/analyzer/alyba/resource/image/chart_network.png";
	
	public static final String FILE_PATH_HTMLTEMPLATE = "dal/tool/analyzer/alyba/resource/template/HtmlTemplate.html";
	public static final String FILE_PATH_DEFAULTSETTING = "dal/tool/analyzer/alyba/resource/setting/default.alb";
	public static final String FILE_PATH_GEOIP = "dal/tool/analyzer/alyba/resource/geoip/GeoIP.dat";

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
	public static final String[] FILE_FILTER_NAMES = { "All Files (*.*)" };
	public static final String[] FILE_FILTER_EXTS = { "*.*" };

	public static final String[] LOG_TYPES = { "Customize", "Apache", "Tomcat", "WebtoB", "Nginx", "JEUS", "IIS" };
	public static final String[] FILE_TYPES = { "Customize", "vmstat", "sar" };

	public static final String[] ELAPSED_TIME_UNITS = { "milliseconds", "seconds", "microseconds" };

	public static final Locale[] TIME_LOCALES = { Locale.ENGLISH, Locale.KOREAN };

	public static final String[] TIME_FORMATS = { "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd:HH:mm:ss", "yyyy.MM.dd/HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd:HH:mm:ss", "yyyy-MM-dd/HH:mm:ss", "dd/MMM/yyyy:HH:mm:ss", "dd/MMM/yyyy HH:mm:ss", "dd/MMM/yyyy/HH:mm:ss" };
	public static final String UNIX_TIME_STR = "UnixTime";
	
	public static final int ANALYZER_DATA_PAGESIZE = 50;
}
