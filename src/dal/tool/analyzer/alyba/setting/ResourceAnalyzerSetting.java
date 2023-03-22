package dal.tool.analyzer.alyba.setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import dal.tool.analyzer.alyba.parse.FileInfo;

public class ResourceAnalyzerSetting {

	public TimeZone analyzerTimezone;
	public Date analyzeDate;
	public List<FileInfo> fileInfoList;
	public ResourceFieldMappingInfo fieldMapping;
	public int unitMinutes;
	public boolean checkStrict;

	public ResourceAnalyzerSetting() {
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
	
	public List<FileInfo> getFileInfoList() {
		return fileInfoList;
	}

	public void setFileInfoList(List<FileInfo> fileInfoList) {
		this.fileInfoList = fileInfoList;
	}
	
	public List<String> getFileList() {
		List<String> lst = new ArrayList<String>();
		for(FileInfo fileInfo : fileInfoList) {
			lst.add(fileInfo.getFilePath());
		}
		return lst;
	}
	
	public List<String> getNames(String group) {
		List<String> lst = new ArrayList<String>();
		for(FileInfo info : fileInfoList) {
			String l_group = info.getFileMeta("group");
			if(group.equalsIgnoreCase(l_group)) {
				lst.add(info.getFileMeta("name"));
			}
		}
		return lst;
	}
	
	public String getGroup(String name) {
		for(FileInfo info : fileInfoList) {
			String l_name = info.getFileMeta("name");
			if(name.equalsIgnoreCase(l_name)) {
				return info.getFileMeta("group");
			}
		}
		return null;
	}

	public ResourceFieldMappingInfo getFieldMapping() {
		return fieldMapping;
	}

	public void setFieldMapping(ResourceFieldMappingInfo fieldMapping) {
		this.fieldMapping = fieldMapping;
	}
	
	public int getUnitMinutes() {
		return unitMinutes;
	}
	
	public void setUnitMinutes(int unitMinutes) {
		this.unitMinutes = unitMinutes;
	}
	
	public boolean getCheckStrict() {
		return checkStrict;
	}

	public void setCheckStrict(boolean checkStrict) {
		this.checkStrict = checkStrict;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("-----------------------------------------------------------\n");
		sb.append("Files\n");
		sb.append("    FILEINFO LIST : " + getFileInfoList().toString() + "\n");
		sb.append("Mapping\n");
		sb.append("    FILE TYPE : " + fieldMapping.getFileType() + "\n");
		sb.append("    DELIMETER : " + fieldMapping.getFieldDelimeter() + "\n");
		sb.append("    BRACELET : " + Arrays.asList(fieldMapping.getFieldBracelets()) + "\n");
		sb.append("    MAPPING DATA : " + fieldMapping.getMappingInfo().toString() + "\n");
		sb.append("    OFFSET HOUR : " + fieldMapping.getOffsetHour() + "\n");
		sb.append("    TIME FORMAT : " + fieldMapping.getTimeFormat() + "\n");
		sb.append("    TIME LOCALE : " + fieldMapping.getTimeLocale() + "\n");
		sb.append("    FIELD COUNT : " + fieldMapping.getFieldCount() + "\n");
		sb.append("Option\n");
		sb.append("    UNIT MINUTES : " + unitMinutes + "\n");		
		sb.append("-----------------------------------------------------------\n");
		return sb.toString();
	}

}
