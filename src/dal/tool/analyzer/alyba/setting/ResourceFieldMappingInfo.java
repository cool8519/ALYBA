package dal.tool.analyzer.alyba.setting;

import java.util.Locale;

public class ResourceFieldMappingInfo extends FieldMappingInfo {

	public String fileType;
	public String timeFormat;
	public float offsetHour;
	public Locale timeLocale;
	public boolean isCpuIdle;
	public boolean isMemoryIdle;
	public boolean isDiskIdle;
	public boolean isNetworkIdle;

	public ResourceFieldMappingInfo() {
	}

	public ResourceFieldMappingInfo(String fileType, String delimeter, String bracelet, String tm_format, Locale tm_locale, int offset, boolean is_cpu_idle, boolean is_mem_idle, boolean is_disk_idle, boolean is_network_idle, String[] field_idx_arr) {
		super(delimeter, bracelet, field_idx_arr);
		this.fileType = fileType;
		this.timeFormat = tm_format;
		this.timeLocale = tm_locale;
		this.offsetHour = offset;
		this.isCpuIdle = is_cpu_idle;
		this.isMemoryIdle = is_mem_idle;
		this.isDiskIdle = is_disk_idle;
		this.isNetworkIdle = is_network_idle;
	}

	public String getFileType() {
		return fileType;
	}
	
	public void setFileType(String fileType) {
		this.fileType = fileType;
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

	public float getOffsetHour() {
		return offsetHour;
	}

	public void setOffsetHour(float offset_hour) {
		this.offsetHour = offset_hour;
	}

	public boolean isCpuIdle() {
		return isCpuIdle;
	}
	
	public void setCpuIdle(boolean isIdle) {
		this.isCpuIdle = isIdle;
	}
	
	public boolean isMemoryIdle() {
		return isMemoryIdle;
	}
	
	public void setMemoryIdle(boolean isIdle) {
		this.isMemoryIdle = isIdle;
	}

	public boolean isDiskIdle() {
		return isDiskIdle;
	}
	
	public void setDiskIdle(boolean isIdle) {
		this.isDiskIdle = isIdle;
	}
	
	public boolean isNetworkIdle() {
		return isNetworkIdle;
	}
	
	public void setNetworkIdle(boolean isIdle) {
		this.isNetworkIdle = isIdle;
	}
	
	public boolean isMappedCpu() {
		return mappingInfo.containsKey("CPU");
	}
	
	public boolean isMappedMemory() {
		return mappingInfo.containsKey("MEM");
	}
	
	public boolean isMappedDisk() {
		return mappingInfo.containsKey("DISK");
	}

	public boolean isMappedNetwork() {
		return mappingInfo.containsKey("NETWORK");
	}

}
