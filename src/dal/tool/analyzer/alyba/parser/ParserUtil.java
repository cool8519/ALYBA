package dal.tool.analyzer.alyba.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import dal.tool.analyzer.alyba.Constant;

public class ParserUtil {

	public static String getField(String s, String fs, int fld) {
		if(fld < 1)
			return null;
		StringTokenizer st;
		if(fs.equals(" ")) {
			st = new StringTokenizer(s);
		} else {
			st = new StringTokenizer(s, fs);
		}
		try {
			for(int i = 0; i < fld - 1; i++)
				st.nextToken();
			return st.nextToken();
		} catch(NoSuchElementException e) {
			return null;
		}
	}

	public static long stringToLong(String str) {
		if(str == null || str.equals("")) {
			return 0L;
		}
		return Long.parseLong(str);
	}

	public static double stringToDouble(String str) {
		if(str == null || str.equals("")) {
			return 0.0D;
		}
		return Double.parseDouble(str);
	}

	public static List<String> getTokenList(String s, String fs, String[] bracelets) {
		if(s == null)
			return null;
		if(bracelets == null)
			bracelets = new String[0];
		int idx = 0;
		StringTokenizer st;
		if(fs == null || fs.equals("") || fs.equals(" ")) {
			st = new StringTokenizer(s);
		} else {
			st = new StringTokenizer(s, fs);
		}
		List<String> l = new ArrayList<String>();
		String token;
		StringBuffer buffer = null;
		String joinStr;
		char endChar = Character.UNASSIGNED;
		try {
			while(st.hasMoreElements()) {
				token = st.nextToken();
				joinStr = null;
				if(endChar == Character.UNASSIGNED) {
					boolean match = false;
					if(token.length() > 0) {
						for(int i = 0; i < bracelets.length; i++) {
							if(token.charAt(0) == bracelets[i].charAt(0)) {
								match = true;
								if(token.length() > 1 && token.charAt(token.length() - 1) == bracelets[i].charAt(1)) {
									token = token.substring(1, token.length() - 1);
									l.add(token);
									break;
								} else {
									buffer = new StringBuffer(token.substring(1));
									endChar = bracelets[i].charAt(1);
								}
							}
						}
					}
					if(!match) {
						l.add(token);
					}
				} else {
					if(endChar != Character.UNASSIGNED && token.charAt(token.length() - 1) == endChar) {
						endChar = Character.UNASSIGNED;
					}
					joinStr = s.substring(idx, s.indexOf(token, idx));
					if(endChar == Character.UNASSIGNED) {
						token = token.substring(0, token.length() - 1);
						buffer.append(joinStr).append(token);
						l.add(buffer.toString());
					} else {
						buffer.append(joinStr).append(token);
					}
				}
				idx = s.indexOf(token, idx) + token.length();
			}
			return l;
		} catch(NoSuchElementException e) {
			return null;
		}
	}

	public static Object[] getMatchedTimeFormat(String[] formats, String time_str, Locale[] locales) {
		for(String format : formats) {
			if(format.equals(Constant.UNIX_TIME_STR)) {
				try {
					if(time_str.matches("^\\d{10}(\\.(\\d){1,6})?$")) {
						return new Object[] { Constant.TIME_LOCALES[0], format };
					}
				} catch(Exception e) {
				}
			} else {
				Locale locale = isMatchedTimeFormat(format, time_str, locales);
				if(locale != null) {
					return new Object[] { locale, format };
				}
			}
		}
		return null;
	}

	public static Locale isMatchedTimeFormat(String format, String time_str, Locale[] locales) {
		if(locales == null) {
			locales = new Locale[] { Locale.getDefault() };
		}
		for(Locale locale : locales) {
			if(isMatchedTimeFormat(format, time_str, locale)) {
				return locale;
			}
		}
		return null;
	}

	public static boolean isMatchedUnixTimeFormat(String time_str) {
		try {
			return time_str.matches("^\\d{10}(\\.(\\d){1,6})?$");
		} catch(Exception e) {
			return false;
		}
	}
	
	public static boolean isMatchedTimeFormat(String format, String time_str, Locale locale) {
		if(locale == null) {
			locale = Locale.getDefault();
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
			sdf.parse(time_str);
			return true;
		} catch(ParseException e) {
			return false;
		}
	}

	public static List<String> getHeaders(File f) {
		if(f == null)
			return null;
		List<String> result = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String s;
			while((s = br.readLine()) != null) {
				if(s.startsWith("#") || s.startsWith("format=")) {
					result.add(s);
				} else {
					break;
				}
			}
		} catch(Exception e) {
			result = null;
		} finally {
			try {
				br.close();
			} catch(Exception e2) {
			}
		}
		return result;
	}

	public static int getStartIndexOfDigit(String s) {
		for(int i = 0; i < s.length(); i++) {
			if(Character.isDigit(s.charAt(i))) {
				return i;
			}
		}
		return -1;
	}

	public static int getEndIndexOfDigit(String s) {
		int i;
		char c;
		for(i = 0; i < s.length(); i++) {
			c = s.charAt(i);
			if(Character.isDigit(c) == false && c != '.') {
				return i;
			}
		}
		return i;
	}

}
