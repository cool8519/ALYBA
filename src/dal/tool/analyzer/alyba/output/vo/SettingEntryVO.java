package dal.tool.analyzer.alyba.output.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;

@Entity
public class SettingEntryVO implements Serializable {

	private static final long serialVersionUID = 1L;

	public String title;
	public Date analyzeDate;
	public List<String> logFileList;
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

	public String mappingFieldDelimeter;
	public String mappingFieldBracelet;
	public String mappingTimeFormat;
	public String mappingTimeLocale;
	public String mappingElapsedUnit;
	public float mappingOffsetHour;
	public int mappingFieldCount;
	public HashMap<String, String> logMappingInfo = new HashMap<String, String>();
	
	public boolean dateFilterEnable;
	public Date dateFilterFromRange;
	public Date dateFilterToRange;
	public boolean includeFilterEnable;
	public boolean includeFilterAndCheck;
	public boolean includeFilterIgnoreCase;
	public HashMap<String, String> includeFilterInfo = new HashMap<String, String>();
	public boolean excludeFilterEnable;
	public boolean excludeFilterAndCheck;
	public boolean excludeFilterIgnoreCase;
	public HashMap<String, String> excludeFilterInfo = new HashMap<String, String>();
	
	public SettingEntryVO() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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


	public String getMappingFieldDelimeter() {
		return mappingFieldDelimeter;
	}

	public void setMappingFieldDelimeter(String field_delimeter) {
		this.mappingFieldDelimeter = field_delimeter;
	}

	public String getMappingFieldBracelet() {
		return mappingFieldBracelet;
	}

	public void setMappingFieldBracelet(String field_bracelet) {
		this.mappingFieldBracelet = field_bracelet;
	}

	public String getMappingTimeFormat() {
		return mappingTimeFormat;
	}

	public void setMappingTimeFormat(String time_format) {
		this.mappingTimeFormat = time_format;
	}

	public String getMappingTimeLocale() {
		return mappingTimeLocale;
	}

	public void setMappingTimeLocale(String time_locale) {
		this.mappingTimeLocale = time_locale;
	}

	public String getMappingElapsedUnit() {
		return mappingElapsedUnit;
	}

	public void setMappingElapsedUnit(String elapsed_unit) {
		this.mappingElapsedUnit = elapsed_unit;
	}

	public float getMappingOffsetHour() {
		return mappingOffsetHour;
	}

	public void setMappingOffsetHour(float offset_hour) {
		this.mappingOffsetHour = offset_hour;
	}

	public int getMappingFieldCount() {
		return mappingFieldCount;
	}

	public void setMappingFieldCount(int field_count) {
		this.mappingFieldCount = field_count;
	}

	public HashMap<String, String> getLogMappingInfo() {
		return logMappingInfo;
	}

	public void setLogMappingInfo(HashMap<String, String> mapping_info) {
		this.logMappingInfo = mapping_info;
	}
	
	
	public boolean isDateFilterEnable() {
		return dateFilterEnable;
	}

	public void setDateFilterEnable(boolean dateFilter_enable) {
		this.dateFilterEnable = dateFilter_enable;
	}

	public Date getDateFilterFromRange() {
		return dateFilterFromRange;
	}

	public void setDateFilterFromRange(Date fromDate) {
		this.dateFilterFromRange = fromDate;
	}

	public Date getDateFilterToRange() {
		return dateFilterToRange;
	}

	public void setDateFilterToRange(Date toDate) {
		this.dateFilterToRange = toDate;
	}

	public boolean isIncludeFilterEnable() {
		return includeFilterEnable;
	}

	public void setIncludeFilterEnable(boolean includeFilter_enable) {
		this.includeFilterEnable = includeFilter_enable;
	}

	public boolean isIncludeFilterAndCheck() {
		return includeFilterAndCheck;
	}

	public void setIncludeFilterAndCheck(boolean includeFilter_andCheck) {
		this.includeFilterAndCheck = includeFilter_andCheck;
	}

	public boolean isIncludeFilterIgnoreCase() {
		return includeFilterIgnoreCase;
	}

	public void setIncludeFilterIgnoreCase(boolean includeFilter_ignoreCase) {
		this.includeFilterIgnoreCase = includeFilter_ignoreCase;
	}

	public HashMap<String, String> getIncludeFilterInfo() {
		return includeFilterInfo;
	}

	public void setIncludeFilterInfo(HashMap<String, String> includeFilterInfo) {
		this.includeFilterInfo = includeFilterInfo;
	}

	public boolean isExcludeFilterEnable() {
		return excludeFilterEnable;
	}

	public void setExcludeFilterEnable(boolean excludeFilter_enable) {
		this.excludeFilterEnable = excludeFilter_enable;
	}

	public boolean isExcludeFilterAndCheck() {
		return excludeFilterAndCheck;
	}

	public void setExcludeFilterAndCheck(boolean excludeFilter_andCheck) {
		this.excludeFilterAndCheck = excludeFilter_andCheck;
	}

	public boolean isExcludeFilterIgnoreCase() {
		return excludeFilterIgnoreCase;
	}

	public void setExcludeFilterIgnoreCase(boolean excludeFilter_ignoreCase) {
		this.excludeFilterIgnoreCase = excludeFilter_ignoreCase;
	}

	public HashMap<String, String> getExcludeFilterInfo() {
		return excludeFilterInfo;
	}

	public void setExcludeFilterInfo(HashMap<String, String> excludeFilter_info) {
		this.excludeFilterInfo = excludeFilter_info;
	}
	
}
