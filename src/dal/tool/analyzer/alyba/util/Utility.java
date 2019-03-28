package dal.tool.analyzer.alyba.util;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import com.maxmind.geoip.LookupService;

import dal.tool.analyzer.alyba.Constant;
import dal.util.FileUtil;
import dal.util.swt.SWTResourceManager;

public class Utility {

	private static HashMap<String, String> codeMap;
	private static LookupService ipService = null;

	static {
		codeMap = new HashMap<String, String>();
		codeMap.put("1XX", "Informational");
		codeMap.put("100", "Continue");
		codeMap.put("101", "Switching Protocols");

		codeMap.put("2XX", "Success");
		codeMap.put("200", "OK");
		codeMap.put("201", "Created");
		codeMap.put("202", "Accepted");
		codeMap.put("203", "Non-authoritavive Information");
		codeMap.put("204", "Non Content");
		codeMap.put("205", "Reset Content");
		codeMap.put("206", "Partial Content");

		codeMap.put("3XX", "Redirection");
		codeMap.put("300", "Multiple Choices");
		codeMap.put("301", "Moved Permanently");
		codeMap.put("302", "Found");
		codeMap.put("303", "See Other");
		codeMap.put("304", "Not Modified");
		codeMap.put("305", "Use Proxy");
		codeMap.put("306", "Switch Proxy");
		codeMap.put("307", "Temporary Redirect");
		codeMap.put("308", "Permanent Redirect");

		codeMap.put("4XX", "Client Error");
		codeMap.put("400", "Bad Request");
		codeMap.put("401", "Unauthorized");
		codeMap.put("402", "Payment Required");
		codeMap.put("403", "Forbidden");
		codeMap.put("404", "Not Found");
		codeMap.put("405", "Method Not Allowed");
		codeMap.put("406", "Not Acceptable");
		codeMap.put("407", "Proxy Authentication Required");
		codeMap.put("408", "Request Timeout");
		codeMap.put("409", "Conflict");
		codeMap.put("410", "Gone");
		codeMap.put("411", "Length Required");
		codeMap.put("412", "Precondition Failed");
		codeMap.put("413", "Request Entity Too Large");
		codeMap.put("414", "Request-URI Too Long");
		codeMap.put("415", "Unsupported Media Type");
		codeMap.put("416", "Requested Range Not Satisfiable");
		codeMap.put("417", "Expectation Failed");
		codeMap.put("418", "I'm a teapot");
		codeMap.put("420", "Enhance Your Calm");
		codeMap.put("422", "Unprocessable Entity");
		codeMap.put("423", "Locked");
		codeMap.put("424", "Failed Dependency");
		codeMap.put("425", "Unordered Collection");
		codeMap.put("426", "Upgrade Required");
		codeMap.put("428", "Precondition Required");
		codeMap.put("429", "Too Many Requests");
		codeMap.put("431", "Request Header Fields Too Large");
		codeMap.put("444", "No Response");
		codeMap.put("449", "Retry With");
		codeMap.put("450", "Blocked by Windows Parental Controls");
		codeMap.put("499", "Client Closed Request");

		codeMap.put("5XX", "Server Error");
		codeMap.put("500", "Internal Server Error");
		codeMap.put("501", "Not Implemented");
		codeMap.put("502", "Bad Gateway");
		codeMap.put("503", "Service Unavailable");
		codeMap.put("504", "Gateway Timeout");
		codeMap.put("505", "HTTP Version Not Supported");
		codeMap.put("506", "Variant Also Negotiates");
		codeMap.put("507", "Insufficient Storage");
		codeMap.put("508", "Loop Detected");
		codeMap.put("509", "Bandwidth Limit Exceeded");
		codeMap.put("510", "Not Extended");
		codeMap.put("511", "Network Authentication Required");
		codeMap.put("598", "Network read timeout error");
		codeMap.put("599", "Network connect timeout error");
	}

	public static List<String> StringToList(String str, String del, boolean ignore_case) throws Exception {
		if(str == null || str.trim().equals("")) {
			return null;
		} else {
			String[] arr = str.split(del);
			for(int i = 0; i < arr.length; i++) {
				arr[i] = toRegEx(arr[i], ignore_case);
			}
			return Arrays.asList(arr);
		}
	}

	public static List<String> StringToList(String str, String del) throws Exception {
		return StringToList(str, del, false);
	}

	public static String toRegEx(String str, boolean ignore_case) throws Exception {
		String temp = str;
		temp = temp.replaceAll("\\?", "(\\\\s|\\\\S)?");
		temp = temp.replaceAll("\\*", "(\\\\s|\\\\S)*");
		String exp = "^" + ((ignore_case) ? "(?i)" : "") + "(" + temp + ")$";
		return exp;
	}

	public static String toRegEx(String str) throws Exception {
		return toRegEx(str, false);
	}

	public static boolean containsByRegex(List<String> l, String s) throws Exception {
		if(s == null)
			return false;
		String exp;
		for(int i = 0; i < l.size(); i++) {
			exp = (String)l.get(i);
			if(Pattern.matches(exp, s))
				return true;
		}
		return false;
	}

	public static String getContainsKey(HashMap<String, String> hm, String str, boolean ignore_case) throws Exception {
		if(!ignore_case) {
			return ((hm.containsKey(str)) ? str : null);
		} else {
			Iterator<String> it = hm.keySet().iterator();
			String key;
			while(it.hasNext()) {
				key = (String)it.next();
				if(key.equalsIgnoreCase(str))
					return key;
			}
			return null;
		}
	}

	public static String getCodeDescription(String code) {
		try {
			if(code == null || codeMap.get(code) == null) {
				return "UNKNOWN";
			}
			return (String)codeMap.get(code);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getCountryFromIPv4(String ip_address) {
		try {
			if(ipService == null) {
				File datafile = FileUtil.getFileFromResource(Constant.FILE_PATH_GEOIP);
				if(datafile == null) {
					URL url = ClassLoader.getSystemResource(Constant.FILE_PATH_GEOIP);
					datafile = FileUtil.createTemporaryFile(url.openStream(), "ALYBA_GeoIP_", ".dat", null, true);
				}
				ipService = new LookupService(datafile.getAbsolutePath(), LookupService.GEOIP_MEMORY_CACHE);
			}
			if(ip_address != null && (ip_address.equals("127.0.0.1") || ip_address.equals("0:0:0:0:0:0:0:1"))) {
				return "Localhost";
			}
			String name = ipService.getCountry(ip_address).getName();
			if(name.equals("N/A")) {
				return "UNKNOWN";
			} else {
				return name;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Font getFont(String name, int size, int style) {
		return SWTResourceManager.getFont(name, size, style);
	}

	public static Font getFont(int size, int style) {
		return SWTResourceManager.getFont(Constant.DEFAULT_FONT_NAME, size, style);
	}

	public static Font getFont(int style) {
		return SWTResourceManager.getFont(Constant.DEFAULT_FONT_NAME, Constant.DEFAULT_FONT_SIZE, style);
	}
	
	public static Font getFont() {
		return SWTResourceManager.getFont(Constant.DEFAULT_FONT_NAME, Constant.DEFAULT_FONT_SIZE, SWT.NONE);
	}

}
