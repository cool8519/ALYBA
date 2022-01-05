package dal.tool.analyzer.alyba.setting;

import java.util.HashMap;
import java.util.Locale;

public class TPMFieldMappingInfo extends LogFieldMappingInfo {

	public int countUnit;
	public boolean isTPS;

	public TPMFieldMappingInfo() {
	}

	public TPMFieldMappingInfo(String fileType, String delimeter, String bracelet, String tm_format, Locale tm_locale, int offset, boolean is_tps, String[] field_idx_arr) {
		super(fileType, false, delimeter, bracelet, tm_format, tm_locale, offset, null, field_idx_arr);
		this.isTPS = is_tps;
	}

	public boolean isTPS() {
		return isTPS;
	}
	
	public void setTPS(boolean isTPS) {
		this.isTPS = isTPS;
	}

	public int getCountUnit() {
		return countUnit;
	}
	
	public void setCountUnit(int countUnit) {
		this.countUnit = countUnit;
	}

	public boolean isMappedCount() {
		return mappingInfo.containsKey("COUNT");
	}

	public static boolean isMappedCount(HashMap<String, String> mappingInfo) {
		return mappingInfo.containsKey("COOUNT");
	}

}
