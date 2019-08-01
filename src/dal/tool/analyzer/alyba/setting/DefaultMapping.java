package dal.tool.analyzer.alyba.setting;

import java.util.Locale;

import dal.tool.analyzer.alyba.Constant;

public class DefaultMapping {

	public static final FieldMappingInfo APACHE = new FieldMappingInfo(Constant.LOG_TYPES[1], " ", "[] \"\"", "dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH, 0, null, new String[] { "URI:5-2", "TIME:4-1", "IP:1", "METHOD:5-1", "VERSION:5-3", "CODE:6", "BYTES:7" });
	public static final FieldMappingInfo TOMCAT = new FieldMappingInfo(Constant.LOG_TYPES[2], " ", "[] \"\"", "dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH, 0, null, new String[] { "URI:5-2", "TIME:4-1", "IP:1", "METHOD:5-1", "VERSION:5-3", "CODE:6", "BYTES:7" });
	public static final FieldMappingInfo WEBTOB = new FieldMappingInfo(Constant.LOG_TYPES[3], " ", "[] \"\"", "dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH, 0, null, new String[] { "URI:5-2", "TIME:4-1", "IP:1", "METHOD:5-1", "VERSION:5-3", "CODE:6", "BYTES:7" });
	public static final FieldMappingInfo NGINX = new FieldMappingInfo(Constant.LOG_TYPES[4], " ", "[] \"\"", "dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH, 0, null, new String[] { "URI:5-2", "TIME:4-1", "IP:1", "METHOD:5-1", "VERSION:5-3", "CODE:6", "BYTES:7" });
	public static final FieldMappingInfo JEUS = new FieldMappingInfo(Constant.LOG_TYPES[5], " ", "[] \"\"", "yyyy.MM.dd HH:mm:ss", Locale.ENGLISH, 0, "milliseconds", new String[] { "URI:3-2", "TIME:1", "IP:2", "METHOD:3-1", "CODE:4", "ELAPSED:5" });
	public static final FieldMappingInfo IIS = new FieldMappingInfo(Constant.LOG_TYPES[6], " \t", "[] \"\"", "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH, 0, "milliseconds", new String[] { "URI:5", "TIME:1,2", "IP:9", "METHOD:4", "CODE:11", "ELAPSED:14" });

}
