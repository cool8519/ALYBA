package dal.tool.analyzer.alyba.setting;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import dal.util.DateUtil;

public class AnalyzerSetting {

	private static final SimpleDateFormat SDF_DATETIME_TZ = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");

	public String title;
	public TimeZone analyzerTimezone;
	public Date analyzeDate;
	public List<String> logFileList;
	public List<String> logFileEncodingList;
	public FieldMappingInfo fieldMapping;
	public FilterSettingInfo filterSetting;
	public boolean outputExcelType;
	public boolean outputHtmlType;
	public boolean outputTextType;
	public String outputDirectory;
	public String outputSortBy;
	public boolean multiThreadParsing;
	public boolean checkFieldCount;
	public boolean allowErrors;
	public int allowErrorCount;
	public boolean uriIncludeParams;
	public boolean collectTPM;
	public int tpmUnitMinutes;
	public boolean collectElapsedTime;
	public long collectElapsedTimeMS;
	public boolean collectResponseBytes;
	public int collectResponseBytesKB;
	public boolean collectErrors;
	public boolean collectIP;
	public boolean collectTPS;

	public AnalyzerSetting() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public TimeZone getAnalyzerTimezone() {
		return analyzerTimezone;
	}
	
	public void setAnalyzerTimezone(TimeZone tz) {
		this.analyzerTimezone = tz;
	}
	
	public Date getAnalyzeDate() {
		return analyzeDate;
	}

	public void setAnalyzeDate(Date analyzeDate) {
		this.analyzeDate = analyzeDate;
	}
	
	public List<String> getLogFileList() {
		return logFileList;
	}

	public void setLogFileList(List<String> logFileList) {
		this.logFileList = logFileList;
	}

	public List<String> getLogFileEncodingList() {
		return logFileEncodingList;
	}

	public void setLogFileEncodingList(List<String> logFileEncodingList) {
		this.logFileEncodingList = logFileEncodingList;
	}

	public FieldMappingInfo getFieldMapping() {
		return fieldMapping;
	}

	public void setFieldMapping(FieldMappingInfo fieldMapping) {
		this.fieldMapping = fieldMapping;
	}

	public FilterSettingInfo getFilterSetting() {
		return filterSetting;
	}

	public void setFilterSetting(FilterSettingInfo filterSetting) {
		this.filterSetting = filterSetting;
	}

	public boolean isOutputExcelType() {
		return outputExcelType;
	}

	public void setOutputExcelType(boolean outputExcelType) {
		this.outputExcelType = outputExcelType;
	}

	public boolean isOutputHtmlType() {
		return outputHtmlType;
	}

	public void setOutputHtmlType(boolean outputHtmlType) {
		this.outputHtmlType = outputHtmlType;
	}

	public boolean isOutputTextType() {
		return outputTextType;
	}

	public void setOutputTextType(boolean outputTextType) {
		this.outputTextType = outputTextType;
	}

	public int getOutputCount() {
		int count = 0;
		if(isOutputExcelType()) count++;
		if(isOutputHtmlType()) count++;
		if(isOutputTextType()) count++;
		return count;
	}
	
	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public String getOutputSortBy() {
		return outputSortBy;
	}

	public void setOutputSortBy(String outputSortBy) {
		this.outputSortBy = outputSortBy;
	}

	public boolean isMultiThreadParsing() {
		return multiThreadParsing;
	}

	public void setMultiThreadParsing(boolean multiThreadParsing) {
		this.multiThreadParsing = multiThreadParsing;
	}

	public boolean isCheckFieldCount() {
		return checkFieldCount;
	}
	
	public void setCheckFieldCount(boolean checkFieldCount) {
		this.checkFieldCount = checkFieldCount;
	}

	public boolean isAllowErrors() {
		return allowErrors;
	}

	public void setAllowErrors(boolean allowErrors) {
		this.allowErrors = allowErrors;
	}

	public int getAllowErrorCount() {
		return allowErrorCount;
	}

	public void setAllowErrorCount(int allowErrorCount) {
		this.allowErrorCount = allowErrorCount;
	}

	public boolean isUriIncludeParams() {
		return uriIncludeParams;
	}

	public void setUriIncludeParams(boolean uriIncludeParams) {
		this.uriIncludeParams = uriIncludeParams;
	}

	public boolean isCollectTPM() {
		return collectTPM;
	}

	public void setCollectTPM(boolean collectTPM) {
		this.collectTPM = collectTPM;
	}

	public int getTPMUnitMinutes() {
		return tpmUnitMinutes;
	}

	public void setTPMUnitMinutes(int tpmUnitMinutes) {
		this.tpmUnitMinutes = tpmUnitMinutes;
	}

	public boolean isCollectElapsedTime() {
		return collectElapsedTime;
	}

	public void setCollectElapsedTime(boolean collectElapsedTime) {
		this.collectElapsedTime = collectElapsedTime;
	}

	public long getCollectElapsedTimeMS() {
		return collectElapsedTimeMS;
	}

	public void setCollectElapsedTimeMS(long collectElapsedTimeMS) {
		this.collectElapsedTimeMS = collectElapsedTimeMS;
	}

	public boolean isCollectResponseBytes() {
		return collectResponseBytes;
	}

	public void setCollectResponseBytes(boolean collectResponseBytes) {
		this.collectResponseBytes = collectResponseBytes;
	}

	public int getCollectResponseBytesKB() {
		return collectResponseBytesKB;
	}

	public void setCollectResponseBytesKB(int collectResponseBytesKB) {
		this.collectResponseBytesKB = collectResponseBytesKB;
	}

	public boolean isCollectErrors() {
		return collectErrors;
	}

	public void setCollectErrors(boolean collectErrors) {
		this.collectErrors = collectErrors;
	}

	public boolean isCollectIP() {
		return collectIP;
	}
	
	public void setCollectIP(boolean collectIP) {
		this.collectIP = collectIP;
	}

	public boolean isCollectTPS() {
		return collectTPS;
	}
	
	public void setCollectTPS(boolean collectTPS) {
		this.collectTPS = collectTPS;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("-----------------------------------------------------------\n");
		sb.append("Title : " + title + "\n");
		sb.append("Files\n");
		sb.append("    FILELIST : " + getLogFileList() + "\n");
		sb.append("    ENCODINGLIST : " + getLogFileEncodingList() + "\n");
		sb.append("Mapping\n");
		sb.append("    LOG TYPE : " + fieldMapping.getLogType() + "\n");
		sb.append("    DELIMETER : " + fieldMapping.getFieldDelimeter() + "\n");
		sb.append("    BRACELET : " + Arrays.asList(fieldMapping.getFieldBracelets()) + "\n");
		sb.append("    MAPPING DATA : " + fieldMapping.getMappingInfo().toString() + "\n");
		sb.append("    OFFSET HOUR : " + fieldMapping.getOffsetHour() + "\n");
		sb.append("    TIME FORMAT : " + fieldMapping.getTimeFormat() + "\n");
		sb.append("    TIME LOCALE : " + fieldMapping.getTimeLocale() + "\n");
		sb.append("    ELAPSED UNIT : " + fieldMapping.getElapsedUnit() + "\n");
		sb.append("    FIELD COUNT : " + fieldMapping.getLogFieldCount() + "\n");
		sb.append("Filter\n");
		sb.append("    CHECK ALL RANGE : " + filterSetting.isAllRangeEnable() + "\n");
		sb.append("        FROM DATE RANGE : " + DateUtil.dateToString(filterSetting.getFromDateRange(), SDF_DATETIME_TZ) + "\n");
		sb.append("        TO DATE RANGE : " + DateUtil.dateToString(filterSetting.getToDateRange(), SDF_DATETIME_TZ) + "\n");
		sb.append("    INCLUDE FILETER ENABLE : " + filterSetting.isIncludeFilterEnable() + "\n");
		sb.append("        CHECK OPERATOR : " + (filterSetting.isIncludeFilterAndCheck() ? "AND" : "OR") + "\n");
		sb.append("        IGNORE CASE : " + filterSetting.isIncludeFilterIgnoreCase() + "\n");
		sb.append("        FILETER INFO : " + filterSetting.getIncludeFilterInfo().toString() + "\n");
		sb.append("    EXCLUDE FILETER ENABLE : " + filterSetting.isExcludeFilterEnable() + "\n");
		sb.append("        CHECK OPERATOR : " + (filterSetting.isExcludeFilterAndCheck() ? "AND" : "OR") + "\n");
		sb.append("        IGNORE CASE : " + filterSetting.isExcludeFilterIgnoreCase() + "\n");
		sb.append("        EXCLUDE FILETER : " + filterSetting.getExcludeFilterInfo().toString() + "\n");
		sb.append("Output\n");
		sb.append("    FILE TYPE : " + (outputExcelType ? "EXCEL " : "") + (outputHtmlType ? "HTML " : "") + (outputTextType ? "TEXT " : "") + "\n");
		sb.append("    OUTPUT DIR : " + outputDirectory + "\n");
		sb.append("    SORT BY : " + outputSortBy + "\n");
		sb.append("Option\n");
		sb.append("    MULTI THREAD : " + multiThreadParsing + "\n");
		sb.append("    CHECK FIELD COUNT : " + checkFieldCount + "\n");
		sb.append("    ALLOW ERRORS : " + allowErrors + "\n");
		sb.append("    ALLOW ERROR COUNT : " + allowErrorCount + "\n");
		sb.append("    URI INCLUDE PARAMETER : " + uriIncludeParams + "\n");
		sb.append("    COLLECT TPM : " + collectTPM + "\n");
		sb.append("    TPM UNIT MINUTES : " + tpmUnitMinutes + "\n");
		sb.append("    COLLECT ELAPSED : " + collectElapsedTime + "\n");
		sb.append("    COLLECT ELAPSED MS : " + collectElapsedTimeMS + "\n");
		sb.append("    COLLECT BYTES : " + collectResponseBytes + "\n");
		sb.append("    COLLECT BYTES KB : " + collectResponseBytesKB + "\n");
		sb.append("    COLLECT ERRORS : " + collectErrors + "\n");
		sb.append("    COLLECT IP : " + collectIP + "\n");
		sb.append("    COLLECT TPS ON THE MOST REQUESTED DAY : " + collectTPS + "\n");
		sb.append("-----------------------------------------------------------\n");
		return sb.toString();
	}

}
