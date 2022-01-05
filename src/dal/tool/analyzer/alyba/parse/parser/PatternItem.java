package dal.tool.analyzer.alyba.parse.parser;

import java.util.regex.Pattern;

public class PatternItem {

	public String patternStr;
	public Pattern patternObj;	

	public PatternItem(String pattern_str, Pattern pattern_obj) {
		this.patternStr = pattern_str;
		this.patternObj = pattern_obj;
	}
	
}
