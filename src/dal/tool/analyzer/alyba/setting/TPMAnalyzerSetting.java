package dal.tool.analyzer.alyba.setting;

import java.util.Arrays;

public class TPMAnalyzerSetting extends LogAnalyzerSetting {

	public TPMAnalyzerSetting() {
	}

	public TPMFieldMappingInfo getFieldMapping() {
		return (TPMFieldMappingInfo)fieldMapping;
	}

	public void setFieldMapping(TPMFieldMappingInfo fieldMapping) {
		this.fieldMapping = fieldMapping;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("-----------------------------------------------------------\n");
		sb.append("Title : " + title + "\n");
		sb.append("Files\n");
		sb.append("    FILELIST : " + getLogFileList() + "\n");
		sb.append("Mapping\n");
		sb.append("    FILE TYPE : " + fieldMapping.getLogType() + "\n");
		sb.append("    DELIMETER : " + fieldMapping.getFieldDelimeter() + "\n");
		sb.append("    BRACELET : " + Arrays.asList(fieldMapping.getFieldBracelets()) + "\n");
		sb.append("    MAPPING DATA : " + fieldMapping.getMappingInfo().toString() + "\n");
		sb.append("    OFFSET HOUR : " + fieldMapping.getOffsetHour() + "\n");
		sb.append("    TIME FORMAT : " + fieldMapping.getTimeFormat() + "\n");
		sb.append("    TIME LOCALE : " + fieldMapping.getTimeLocale() + "\n");
		sb.append("    FIELD COUNT : " + fieldMapping.getFieldCount() + "\n");
		sb.append("Option\n");
		sb.append("    COUNT UNIT : " + ((TPMFieldMappingInfo)fieldMapping).getCountUnit() + "\n");
		sb.append("    COUNT TYPE : " + (((TPMFieldMappingInfo)fieldMapping).isTPS()?"TPS":"TPM") + "\n");
		sb.append("-----------------------------------------------------------\n");
		return sb.toString();
	}

}
