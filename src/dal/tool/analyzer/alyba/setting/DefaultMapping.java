package dal.tool.analyzer.alyba.setting;

import java.util.Locale;

import dal.tool.analyzer.alyba.Constant;

public class DefaultMapping {

	public static final LogFieldMappingInfo APACHE = new LogFieldMappingInfo(Constant.LOG_TYPES[1], " ", "[] \"\"", "dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH, 0, null, new String[] { "URI:5-2", "TIME:4-1", "IP:1", "METHOD:5-1", "VERSION:5-3", "CODE:6", "BYTES:7" });
	public static final LogFieldMappingInfo TOMCAT = new LogFieldMappingInfo(Constant.LOG_TYPES[2], " ", "[] \"\"", "dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH, 0, null, new String[] { "URI:5-2", "TIME:4-1", "IP:1", "METHOD:5-1", "VERSION:5-3", "CODE:6", "BYTES:7" });
	public static final LogFieldMappingInfo WEBTOB = new LogFieldMappingInfo(Constant.LOG_TYPES[3], " ", "[] \"\"", "dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH, 0, null, new String[] { "URI:5-2", "TIME:4-1", "IP:1", "METHOD:5-1", "VERSION:5-3", "CODE:6", "BYTES:7" });
	public static final LogFieldMappingInfo NGINX = new LogFieldMappingInfo(Constant.LOG_TYPES[4], " ", "[] \"\"", "dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH, 0, null, new String[] { "URI:5-2", "TIME:4-1", "IP:1", "METHOD:5-1", "VERSION:5-3", "CODE:6", "BYTES:7" });
	public static final LogFieldMappingInfo JEUS = new LogFieldMappingInfo(Constant.LOG_TYPES[5], " ", "[] \"\"", "yyyy.MM.dd HH:mm:ss", Locale.ENGLISH, 0, "milliseconds", new String[] { "URI:3-2", "TIME:1", "IP:2", "METHOD:3-1", "CODE:4", "ELAPSED:5" });
	public static final LogFieldMappingInfo IIS = new LogFieldMappingInfo(Constant.LOG_TYPES[6], " \t", "[] \"\"", "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH, 0, "milliseconds", new String[] { "URI:5", "TIME:1,2", "IP:9", "METHOD:4", "CODE:11", "ELAPSED:14" });

	public static final ResourceFieldMappingInfo VMSTAT = new ResourceFieldMappingInfo(Constant.FILE_TYPES[1], " \t", "\"\"", "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH, 0, true, false, false, false, new String[] { "TIME:18,19", "CPU:15", "MEM:4", "DISK:10" });
	public static final ResourceFieldMappingInfo SAR = new ResourceFieldMappingInfo(Constant.FILE_TYPES[2], " \t", "() \"\"", "MM/dd/yyyy hh:mm:ss a", Locale.ENGLISH, 0, true, false, false, false, new String[] { "TIME:H4,1,2", "CPU:9" });

}
