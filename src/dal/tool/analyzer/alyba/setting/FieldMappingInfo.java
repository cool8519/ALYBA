package dal.tool.analyzer.alyba.setting;

import java.util.HashMap;
import java.util.Locale;

import dal.util.StringUtil;

public class FieldMappingInfo {

	public String fieldDelimeter;
	public String fieldBracelet;
	public String timeFormat;
	public String elapsedUnit;
	public float offsetHour;
	public int logFieldCount;
	public HashMap<String, String> mappingInfo = new HashMap<String, String>();
	public Locale timeLocale;

	public FieldMappingInfo() {
	}

	public FieldMappingInfo(String delimeter, String bracelet, String tm_format, Locale tm_locale, int offset, String elapsed_unit, String[] field_idx_arr) {
		this.fieldDelimeter = delimeter;
		this.fieldBracelet = bracelet;
		this.timeFormat = tm_format;
		this.timeLocale = tm_locale;
		this.offsetHour = offset;
		this.elapsedUnit = elapsed_unit;
		if(field_idx_arr != null) {
			int idx;
			String key;
			String flds;
			for(int i = 0; i < field_idx_arr.length; i++) {
				idx = field_idx_arr[i].indexOf(':');
				key = field_idx_arr[i].substring(0, idx);
				flds = field_idx_arr[i].substring(idx + 1);
				mappingInfo.put(key, flds);
			}
		}
	}

	public String getFieldDelimeter() {
		return fieldDelimeter;
	}

	public void setFieldDelimeter(String field_delimeter) {
		this.fieldDelimeter = field_delimeter;
	}

	public String getFieldBracelet() {
		return fieldBracelet;
	}

	public String[] getFieldBracelets() {
		return StringUtil.getArrayFromString(fieldBracelet, " ");
	}

	public void setFieldBracelet(String field_bracelet) {
		this.fieldBracelet = field_bracelet;
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

	public int getLogFieldCount() {
		return logFieldCount;
	}

	public void setLogFieldCount(int field_count) {
		this.logFieldCount = field_count;
	}

	public HashMap<String, String> getMappingInfo() {
		return mappingInfo;
	}

	public void setMappingInfo(HashMap<String, String> mapping_info) {
		this.mappingInfo = mapping_info;
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

}
