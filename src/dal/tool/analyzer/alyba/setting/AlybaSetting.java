package dal.tool.analyzer.alyba.setting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AlybaSetting implements Serializable {

	private static final long serialVersionUID = 6463452963374853474L;

	private boolean filterCheckAllRangeEnabled;
	private Date filterRangeFromDate;
	private Date filterRangeToDate;
	private boolean filterIncludeEnabled;
	private boolean filterIncludeAndChecked;
	private boolean filterIncludeIgnoreCase;
	private HashMap<String,String> filterIncludeData;
	private boolean filterExcludeEnabled;
	private boolean filterExcludeAndChecked;
	private boolean filterExcludeIgnoreCase;
	private HashMap<String,String> filterExcludeData;
	
	private boolean outputExcelTypeChecked;
	private boolean outputHtmlTypeChecked;
	private boolean outputTextTypeChecked;
	private String outputDirectory;
	private boolean outputSortByCountChecked;	

	private boolean optionMultiThreadChecked;
	private boolean optionFixedFieldsChecked;
	private boolean optionStrictCheckChecked;
	private boolean optionAllowErrorsChecked;
	private int optionAllowErrorCount;
	private boolean optionURLIncludeParamsChecked;
	private boolean optionCheckFileEncodingChecked;
	private boolean optionCollectTPMChecked;
	private int optionTPMUnitMinutes;
	private boolean optionCollectElapsedTimeChecked;
	private int optionCollectElapsedTimeMS;
	private boolean optionCollectResponseBytesChecked;
	private int optionCollectResponseBytesKB;
	private boolean optionCollectErrorsChecked;
	private boolean optionCollectIPChecked;
	private boolean optionCollectTPSChecked;		

	private String mappingLogType;
	private String mappingDelimeter;
	private String mappingBracelet;
	private String mappingTimestampType;
	private float mappingOffsetHour;
	private String mappingTimeFormat;
	private Locale mappingTimeLocale;
	private String mappingElapsedUnit;
	private HashMap<String,String> mappingData;
	private ArrayList<String> uriMappingPatterns;
	
	
	public AlybaSetting() {}

	
	public boolean isFilterCheckAllRangeEnabled() {
		return filterCheckAllRangeEnabled;
	}

	public Date getFilterRangeFromDate() {
		return filterRangeFromDate;
	}

	public Date getFilterRangeToDate() {
		return filterRangeToDate;
	}

	public boolean isFilterIncludeEnabled() {
		return filterIncludeEnabled;
	}

	public boolean isFilterIncludeAndChecked() {
		return filterIncludeAndChecked;
	}
	
	public boolean isFilterIncludeIgnoreCase() {
		return filterIncludeIgnoreCase;
	}

	public HashMap<String,String> getFilterIncludeData() {
		return filterIncludeData;
	}

	public boolean isFilterExcludeEnabled() {
		return filterExcludeEnabled;
	}

	public boolean isFilterExcludeAndChecked() {
		return filterExcludeAndChecked;
	}

	public boolean isFilterExcludeIgnoreCase() {
		return filterExcludeIgnoreCase;
	}

	public HashMap<String,String> getFilterExcludeData() {
		return filterExcludeData;
	}

	public boolean isOutputExcelTypeChecked() {
		return outputExcelTypeChecked;
	}

	public boolean isOutputHtmlTypeChecked() {
		return outputHtmlTypeChecked;
	}

	public boolean isOutputTextTypeChecked() {
		return outputTextTypeChecked;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public boolean isOutputSortByCountChecked() {
		return outputSortByCountChecked;
	}

	public boolean isOptionMultiThreadChecked() {
		return optionMultiThreadChecked;
	}

	public boolean isOptionFixedFieldsChecked() {
		return optionFixedFieldsChecked;
	}
	
	public boolean isOptionStrictCheckChecked() {
		return optionStrictCheckChecked;
	}

	public boolean isOptionAllowErrorsChecked() {
		return optionAllowErrorsChecked;
	}

	public int getOptionAllowErrorCount() {
		return optionAllowErrorCount;
	}

	public boolean isOptionURLIncludeParamsChecked() {
		return optionURLIncludeParamsChecked;
	}
	
	public boolean isOptionCheckFileEncodingChecked() {
		return optionCheckFileEncodingChecked;
	}

	public boolean isOptionCollectTPMChecked() {
		return optionCollectTPMChecked;
	}

	public int getOptionTPMUnitMinutes() {
		return optionTPMUnitMinutes;
	}

	public boolean isOptionCollectElapsedTimeChecked() {
		return optionCollectElapsedTimeChecked;
	}

	public int getOptionCollectElapsedTimeMS() {
		return optionCollectElapsedTimeMS;
	}

	public boolean isOptionCollectResponseBytesChecked() {
		return optionCollectResponseBytesChecked;
	}

	public int getOptionCollectResponseBytesKB() {
		return optionCollectResponseBytesKB;
	}

	public boolean isOptionCollectErrorsChecked() {
		return optionCollectErrorsChecked;
	}

	public boolean isOptionCollectIPChecked() {
		return optionCollectIPChecked;
	}

	public boolean isOptionCollectTPSChecked() {
		return optionCollectTPSChecked;
	}

	public String getMappingLogType() {
		return mappingLogType;
	}

	public String getMappingDelimeter() {
		return mappingDelimeter;
	}

	public String getMappingBracelet() {
		return mappingBracelet;
	}

	public String getMappingTimestampType() {
		return mappingTimestampType;
	}

	public float getMappingOffsetHour() {
		return mappingOffsetHour;
	}

	public String getMappingTimeFormat() {
		return mappingTimeFormat;
	}

	public Locale getMappingTimeLocale() {
		return mappingTimeLocale;
	}

	public String getMappingElapsedUnit() {
		return mappingElapsedUnit;
	}

	public HashMap<String,String> getMappingData() {
		return mappingData;
	}
	
	public List<String> getURIMappingPatterns() {
		return uriMappingPatterns;
	}

	
	public void setFilterCheckAllRangeEnabled(boolean filterCheckAllRangeEnabled) {
		this.filterCheckAllRangeEnabled = filterCheckAllRangeEnabled;
	}

	public void setFilterRangeFromDate(Date filterRangeFromDate) {
		this.filterRangeFromDate = filterRangeFromDate;
	}

	public void setFilterRangeToDate(Date filterRangeToDate) {
		this.filterRangeToDate = filterRangeToDate;
	}

	public void setFilterIncludeEnabled(boolean filterIncludeEnabled) {
		this.filterIncludeEnabled = filterIncludeEnabled;
	}

	public void setFilterIncludeAndChecked(boolean filterIncludeAndChecked) {
		this.filterIncludeAndChecked = filterIncludeAndChecked;
	}

	public void setFilterIncludeIgnoreCase(boolean filterIncludeIgnoreCase) {
		this.filterIncludeIgnoreCase = filterIncludeIgnoreCase;
	}

	public void setFilterIncludeData(HashMap<String, String> filterIncludeData) {
		this.filterIncludeData = filterIncludeData;
	}

	public void setFilterExcludeEnabled(boolean filterExcludeEnabled) {
		this.filterExcludeEnabled = filterExcludeEnabled;
	}

	public void setFilterExcludeAndChecked(boolean filterExcludeAndChecked) {
		this.filterExcludeAndChecked = filterExcludeAndChecked;
	}

	public void setFilterExcludeIgnoreCase(boolean filterExcludeIgnoreCase) {
		this.filterExcludeIgnoreCase = filterExcludeIgnoreCase;
	}

	public void setFilterExcludeData(HashMap<String, String> filterExcludeData) {
		this.filterExcludeData = filterExcludeData;
	}

	public void setOutputExcelTypeChecked(boolean outputExcelTypeChecked) {
		this.outputExcelTypeChecked = outputExcelTypeChecked;
	}

	public void setOutputHtmlTypeChecked(boolean outputHtmlTypeChecked) {
		this.outputHtmlTypeChecked = outputHtmlTypeChecked;
	}

	public void setOutputTextTypeChecked(boolean outputTextTypeChecked) {
		this.outputTextTypeChecked = outputTextTypeChecked;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void setOutputSortByCountChecked(boolean outputSortByCountChecked) {
		this.outputSortByCountChecked = outputSortByCountChecked;
	}

	public void setOptionMultiThreadChecked(boolean optionMultiThreadChecked) {
		this.optionMultiThreadChecked = optionMultiThreadChecked;
	}

	public void setOptionFixedFieldsChecked(boolean optionFixedFieldsChecked) {
		this.optionFixedFieldsChecked = optionFixedFieldsChecked;
	}

	public void setOptionStrictCheckChecked(boolean optionStrictCheckChecked) {
		this.optionStrictCheckChecked = optionStrictCheckChecked;
	}

	public void setOptionAllowErrorsChecked(boolean optionAllowErrorsChecked) {
		this.optionAllowErrorsChecked = optionAllowErrorsChecked;
	}

	public void setOptionAllowErrorCount(int optionAllowErrorCount) {
		this.optionAllowErrorCount = optionAllowErrorCount;
	}

	public void setOptionURLIncludeParamsChecked(boolean optionURLIncludeParamsChecked) {
		this.optionURLIncludeParamsChecked = optionURLIncludeParamsChecked;
	}
	
	public void setOptionCheckFileEncodingChecked(boolean optionCheckFileEncodingChecked) {
		this.optionCheckFileEncodingChecked = optionCheckFileEncodingChecked;
	}

	public void setOptionCollectTPMChecked(boolean optionCollectTPMChecked) {
		this.optionCollectTPMChecked = optionCollectTPMChecked;
	}

	public void setOptionTPMUnitMinutes(int optionTPMUnitMinutes) {
		this.optionTPMUnitMinutes = optionTPMUnitMinutes;
	}

	public void setOptionCollectElapsedTimeChecked(boolean optionCollectElapsedTimeChecked) {
		this.optionCollectElapsedTimeChecked = optionCollectElapsedTimeChecked;
	}

	public void setOptionCollectElapsedTimeMS(int optionCollectElapsedTimeMS) {
		this.optionCollectElapsedTimeMS = optionCollectElapsedTimeMS;
	}

	public void setOptionCollectResponseBytesChecked(boolean optionCollectResponseBytesChecked) {
		this.optionCollectResponseBytesChecked = optionCollectResponseBytesChecked;
	}

	public void setOptionCollectResponseBytesKB(int optionCollectResponseBytesKB) {
		this.optionCollectResponseBytesKB = optionCollectResponseBytesKB;
	}

	public void setOptionCollectErrorsChecked(boolean optionCollectErrorsChecked) {
		this.optionCollectErrorsChecked = optionCollectErrorsChecked;
	}

	public void setOptionCollectIPChecked(boolean optionCollectIPChecked) {
		this.optionCollectIPChecked = optionCollectIPChecked;
	}

	public void setOptionCollectTPSChecked(boolean optionCollectTPSChecked) {
		this.optionCollectTPSChecked = optionCollectTPSChecked;
	}

	public void setMappingLogType(String mappingLogType) {
		this.mappingLogType = mappingLogType;
	}

	public void setMappingDelimeter(String mappingDelimeter) {
		this.mappingDelimeter = mappingDelimeter;
	}

	public void setMappingBracelet(String mappingBracelet) {
		this.mappingBracelet = mappingBracelet;
	}

	public void setMappingTimestampType(String mappingTimestampType) {
		this.mappingTimestampType = mappingTimestampType;
	}

	public void setMappingOffsetHour(float mappingOffsetHour) {
		this.mappingOffsetHour = mappingOffsetHour;
	}

	public void setMappingTimeFormat(String mappingTimeFormat) {
		this.mappingTimeFormat = mappingTimeFormat;
	}

	public void setMappingTimeLocale(Locale mappingTimeLocale) {
		this.mappingTimeLocale = mappingTimeLocale;
	}

	public void setMappingElapsedUnit(String mappingElapsedUnit) {
		this.mappingElapsedUnit = mappingElapsedUnit;
	}

	public void setMappingData(HashMap<String, String> mappingData) {
		this.mappingData = mappingData;
	}
	
	public void setURIMappingPatterns(List<String> uriMappingPatterns) {
		if(uriMappingPatterns != null) {
			this.uriMappingPatterns = new ArrayList<String>(uriMappingPatterns);
		}
	}
	
}
