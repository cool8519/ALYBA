package dal.tool.analyzer.alyba.parser;

import java.util.List;

public class FieldIndex {

	private String name;
	private int main_idx;
	private int sub_idx;

	public FieldIndex(String name, String idx_str) {
		this.name = name;
		int idx = idx_str.indexOf("-");
		if(idx < 1) {
			this.main_idx = Integer.parseInt(idx_str) - 1;
			this.sub_idx = -1;
		} else {
			this.main_idx = Integer.parseInt(idx_str.substring(0, idx)) - 1;
			this.sub_idx = Integer.parseInt(idx_str.substring(idx + 1)) - 1;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMainIndex() {
		return main_idx;
	}

	public void setMainIndex(int main_idx) {
		this.main_idx = main_idx;
	}

	public int getSubIndex() {
		return sub_idx;
	}

	public void setSubIndex(int sub_idx) {
		this.sub_idx = sub_idx;
	}

	public boolean isSubField() {
		return (sub_idx != -1);
	}

	public String getMainField(List<String> main_flds) {
		if(main_flds == null || main_flds.size() < main_idx + 1) {
			return null;
		} else {
			return main_flds.get(main_idx);
		}
	}

	public String getSubField(List<String> sub_flds) {
		if(sub_flds == null || sub_flds.size() < sub_idx + 1) {
			return null;
		} else {
			return sub_flds.get(sub_idx);
		}
	}

	public String getField(String main_fld, String delimeter, String[] bracelets) {
		if(main_fld == null) {
			return null;
		}
		if(isSubField()) {
			List<String> sub_flds = ParserUtil.getTokenList(main_fld, delimeter, bracelets);
			return getSubField(sub_flds);
		} else {
			return main_fld;
		}
	}

	public String getField(List<String> main_flds, String delimeter, String[] bracelets) {
		return getField(getMainField(main_flds), delimeter, bracelets);
	}

}
