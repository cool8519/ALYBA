package dal.tool.analyzer.alyba.setting;

import java.util.HashMap;

import dal.util.StringUtil;

public class FieldMappingInfo {

	public String fieldDelimeter;
	public String fieldBracelet;
	public HashMap<String, String> mappingInfo = new HashMap<String, String>();
	public int fieldCount;

	public FieldMappingInfo() {
	}

	public FieldMappingInfo(String delimeter, String bracelet, String[] field_idx_arr) {
		this.fieldDelimeter = delimeter;
		this.fieldBracelet = bracelet;
		setMappingInfo(field_idx_arr);
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

	public int getFieldCount() {
		return fieldCount;
	}

	public void setFieldCount(int field_count) {
		this.fieldCount = field_count;
	}

	public HashMap<String, String> getMappingInfo() {
		return mappingInfo;
	}

	public void setMappingInfo(HashMap<String, String> mapping_info) {
		this.mappingInfo = mapping_info;
	}
	
	public void setMappingInfo(String[] field_idx_arr) {
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

}
