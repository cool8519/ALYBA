package dal.tool.analyzer.alyba.setting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import dal.tool.analyzer.alyba.Constant;

public class LogFieldMappingInfo extends FieldMappingInfo {

	public String logType;
	public boolean isJsonMapType;
	public String timestampType;
	public String timeFormat;
	public String elapsedUnit;
	public float offsetHour;
	public Locale timeLocale;

	public LogFieldMappingInfo() {
	}

	public LogFieldMappingInfo(String logType, String delimeter, String bracelet, String tm_format, Locale tm_locale, int offset, String elapsed_unit, String[] field_idx_arr) {
		super(delimeter, bracelet, field_idx_arr);
		this.logType = logType;
		this.isJsonMapType = false;
		this.timestampType = "Res";
		this.timeFormat = tm_format;
		this.timeLocale = tm_locale;
		this.offsetHour = offset;
		this.elapsedUnit = elapsed_unit;
	}

	public LogFieldMappingInfo(String logType, boolean isJsonMapType, String delimeter, String bracelet, String tm_format, Locale tm_locale, int offset, String elapsed_unit, String[] field_idx_arr) {
		super(delimeter, bracelet, field_idx_arr);
		this.logType = logType;
		this.isJsonMapType = isJsonMapType;
		this.timestampType = "Res";
		this.timeFormat = tm_format;
		this.timeLocale = tm_locale;
		this.offsetHour = offset;
		this.elapsedUnit = elapsed_unit;
	}

	public String getLogType() {
		return logType;
	}
	
	public void setLogType(String logType) {
		this.logType = logType;
	}

	public boolean isJsonType() {
		return Constant.LOG_TYPES[7].equals(logType);
	}

	public boolean isJsonMapType() {
		return isJsonMapType;
	}

	public void setJsonMapType(boolean isJsonMapType) {
		this.isJsonMapType = isJsonMapType;
	}
	
	public String getTimestampType() {
		return timestampType;
	}
	
	public void setTimestampType(String timestampType) {
		this.timestampType = timestampType;		
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String time_format) {
		this.timeFormat = time_format;
	}

	public Locale getTimeLocale() {
		return timeLocale;
	}

	public void setTimeLocale(Locale time_locale) {
		this.timeLocale = time_locale;
	}

	public String getElapsedUnit() {
		return elapsedUnit;
	}

	public void setElapsedUnit(String elapsed_unit) {
		this.elapsedUnit = elapsed_unit;
	}

	public float getOffsetHour() {
		return offsetHour;
	}

	public void setOffsetHour(float offset_hour) {
		this.offsetHour = offset_hour;
	}

	public boolean isDateResponse() {
		return "Res".equals(timestampType);
	}

	public boolean isMappedURI() {
		return mappingInfo.containsKey("URI");
	}

	public boolean isMappedElapsed() {
		return mappingInfo.containsKey("ELAPSED");
	}
	
	public boolean isMappedBytes() {
		return mappingInfo.containsKey("BYTES");
	}
	
	public boolean isMappedCode() {
		return mappingInfo.containsKey("CODE");
	}

	public boolean isMappedMethod() {
		return mappingInfo.containsKey("METHOD");
	}

	public boolean isMappedVersion() {
		return mappingInfo.containsKey("VERSION");
	}

	public boolean isMappedIP() {
		return mappingInfo.containsKey("IP");
	}

	public static boolean isMappedURI(HashMap<String, String> mappingInfo) {
		return mappingInfo.containsKey("URI");
	}

	public static boolean isMappedElapsed(HashMap<String, String> mappingInfo) {
		return mappingInfo.containsKey("ELAPSED");
	}

	public static boolean isMappedBytes(HashMap<String, String> mappingInfo) {
		return mappingInfo.containsKey("BYTES");
	}
	
	public static boolean isMappedCode(HashMap<String, String> mappingInfo) {
		return mappingInfo.containsKey("CODE");
	}

	public static boolean isMappedMethod(HashMap<String, String> mappingInfo) {
		return mappingInfo.containsKey("METHOD");
	}

	public static boolean isMappedVersion(HashMap<String, String> mappingInfo) {
		return mappingInfo.containsKey("VERSION");
	}

	public static boolean isMappedIP(HashMap<String, String> mappingInfo) {
		return mappingInfo.containsKey("IP");
	}

	@Override
	public Object clone() {
		LogFieldMappingInfo clone = new LogFieldMappingInfo(logType, isJsonMapType, fieldDelimeter, fieldBracelet, timeFormat, (Locale)timeLocale.clone(), (int)offsetHour, elapsedUnit, null);
		HashMap<String,String> mapppingInfo_clone = new HashMap<String,String>(this.mappingInfo.size());
		Iterator<String> iter = mappingInfo.keySet().iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			mapppingInfo_clone.put(key, mappingInfo.get(key)); 
		}
		clone.mappingInfo = mapppingInfo_clone;
		return clone;
	}
	
}
