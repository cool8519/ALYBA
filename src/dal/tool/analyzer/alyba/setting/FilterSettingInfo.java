package dal.tool.analyzer.alyba.setting;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class FilterSettingInfo {

	public boolean allRangeEnable;
	public Date fromDateRange;
	public Date toDateRange;
	public boolean includeFilterEnable;
	public boolean includeFilterAndCheck;
	public boolean includeFilterIgnoreCase;
	public HashMap<String, String> includeFilterInfo = new HashMap<String, String>();
	public boolean excludeFilterEnable;
	public boolean excludeFilterAndCheck;
	public boolean excludeFilterIgnoreCase;
	public HashMap<String, String> excludeFilterInfo = new HashMap<String, String>();

	public FilterSettingInfo() {
	}

	public boolean isAllRangeEnable() {
		return allRangeEnable;
	}

	public void setAllRangeEnable(boolean allRange_enable) {
		this.allRangeEnable = allRange_enable;
	}

	public Date getFromDateRange() {
		return fromDateRange;
	}

	public void setFromDateRange(Date fromDate) {
		this.fromDateRange = fromDate;
	}

	public Date getToDateRange() {
		return toDateRange;
	}

	public void setToDateRange(Date toDate) {
		this.toDateRange = toDate;
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

	public String getIncludeFilterInfoString() {
		if(includeFilterEnable) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(((includeFilterIgnoreCase) ? "IGNORE_CASE," : ""));
			buffer.append(((includeFilterAndCheck) ? "AND" : "OR") + "/");
			buffer.append("{");
			Iterator<String> it = includeFilterInfo.keySet().iterator();
			String key;
			String value;
			while(it.hasNext()) {
				key = (String)it.next();
				value = (String)includeFilterInfo.get(key);
				buffer.append(key + "=\"" + value + "\"");
				if(it.hasNext()) {
					buffer.append(" ");
				}
			}
			buffer.append("}");
			return buffer.toString();
		} else {
			return "NO";
		}
	}

	public String getExcludeFilterInfoString() {
		if(excludeFilterEnable) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(((excludeFilterIgnoreCase) ? "IGNORE_CASE," : ""));
			buffer.append(((excludeFilterAndCheck) ? "AND" : "OR") + "/");
			buffer.append("{");
			Iterator<String> it = excludeFilterInfo.keySet().iterator();
			String key;
			String value;
			while(it.hasNext()) {
				key = (String)it.next();
				value = (String)excludeFilterInfo.get(key);
				buffer.append(key + "=\"" + value + "\"");
				if(it.hasNext()) {
					buffer.append(" ");
				}
			}
			buffer.append("}");
			return buffer.toString();
		} else {
			return "NO";
		}
	}

}
