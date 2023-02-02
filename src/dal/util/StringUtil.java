package dal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtil {

	public static String NVL(String str, String def) {
		return (str == null) ? def : str;
	}

	public static String NVL(String str) {
		return NVL(str, "");
	}

	public static String getKorString(String s) {
		try {
			return new String(s.getBytes("8859_1"), "EUC-KR");
		} catch(Exception e) {
			return s;
		}
	}

	public static String expressSecondsAsTime(int seconds) {
		int h = 0;
		int m = 0;
		int s = ((seconds < 0) ? 0 : seconds);
		if(s / 60 >= 1) {
			m = s / 60;
			s = s % 60;
			if(m / 60 >= 1) {
				h = m / 60;
				m = m % 60;
			}
		}
		return getTwoDigitNumber(h) + ":" + getTwoDigitNumber(m) + ":" + getTwoDigitNumber(s);
	}

	public static String getTwoDigitNumber(int num) {
		return ((num < 10) ? "0" : "") + String.valueOf(num);
	}

	public static String removePrefix(String str, String prefix) {
		if(str != null && str.startsWith(prefix)) {
			return str.substring(prefix.length());
		} else {
			return str;
		}
	}

	public static String getStringFromArray(String[] arr, String delimeter) {
		if(delimeter == null)
			delimeter = " ";
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < arr.length; i++) {
			if(i > 0)
				buffer.append(delimeter);
			buffer.append(arr[i]);
		}
		return buffer.toString();
	}

	public static String[] getArrayFromString(String s, String delimeter) {
		if(delimeter == null)
			delimeter = " ";
		List<String> lst = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(s, delimeter);
		while(st.hasMoreTokens()) {
			lst.add(st.nextToken());
		}
		String[] arr = new String[lst.size()];
		lst.toArray(arr);
		return arr;
	}

	public static String replaceMetaCharacter(String s, boolean show) {
		if(show) {
			return s.replaceAll("\t", "\\\\t");
		} else {
			return s.replaceAll("\\\\t", "\t");
		}
	}
	
	public static boolean isNumeric(String str, boolean allowFloat) {
		return NumberUtil.isNumeric(str, allowFloat);
	}

	public static boolean isNumeric(String str) {
		return NumberUtil.isNumeric(str, false);
	}
	
	public static int countCharacters(String s, char c) {
		int cnt = 0;
		for(int i = 0; i < s.length(); i++) {
			if(s.charAt(i) == c) cnt++;
		}
		return cnt;
	}

	public static boolean checkParentheses(String s, String pair) {
		char l_char = pair.charAt(0);
		char r_char = pair.charAt(1);
		try {
			int init = s.indexOf(l_char);
			int end = s.indexOf(r_char, init);
			if(init < 0 && end < 0) {
				// not included parentheses
				return true;
			}
			while(init > -1 && end > -1) {
				int openCnt;
				String added = s.substring(init+1, end);
				while((openCnt = countCharacters(added, l_char)) > 0) {
					// found additional left parentheses
					added = "";
					for(int i = 0; i < openCnt; i++) {						
						init = end;
						end = s.indexOf(r_char, init+1);
						if(end < 0) {
							// string exhausted during check
							return false;
						}
						added += s.substring(init, end);
					}
				}
				end++;
				init = s.indexOf(l_char, end);
				if(init < 0 || init > s.indexOf(r_char,end)) {
					// no more left parentheses
					// or right parentheses before left parentheses(ex. '}1234{')
					break;
				}
				end = s.indexOf(r_char, init);
			}
			if(end < s.length() && s.substring(end).indexOf(r_char) > -1) {
				// included right parenthesis in remaining string
				return false;
			}
		} catch(Exception e) {
			return false;
		}		
		return true;
	}
	
}
