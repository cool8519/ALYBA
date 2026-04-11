package dal.tool.analyzer.alyba.ui.urimapping;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StaticStringConstantCache {

	private static final Pattern P_STATIC_FINAL_STRING = Pattern.compile("\\bstatic\\s+final\\s+String\\s+(\\w+)\\s*=\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*;");
	private static final Pattern P_STATIC_FINAL_STRING_RHS = Pattern.compile("\\bstatic\\s+final\\s+String\\s+(\\w+)\\s*=\\s*([^;]+);");

	private final ConcurrentHashMap<String, Map<String, String>> classToFields = new ConcurrentHashMap<String, Map<String, String>>();
	private final ConcurrentHashMap<String, Boolean> sourceFileExists = new ConcurrentHashMap<String, Boolean>();

	public String getFieldValue(String classFqn, String fieldName) {
		Map<String, String> m = classToFields.get(classFqn);
		if(m == null || fieldName == null) {
			return null;
		}
		return m.get(fieldName);
	}

	public void ensureLoaded(String classFqn, List<File> scanRoots) {
		if(classFqn == null || classFqn.isEmpty() || scanRoots == null) {
			return;
		}
		if(classToFields.containsKey(classFqn)) {
			return;
		}
		File src = findSourceFile(classFqn, scanRoots);
		if(src == null || !src.isFile()) {
			classToFields.put(classFqn, new HashMap<String, String>());
			return;
		}
		try {
			Map<String, String> fields = parseStaticFinalStringFields(src);
			classToFields.put(classFqn, fields);
		} catch(IOException e) {
			classToFields.put(classFqn, new HashMap<String, String>());
		}
	}

	public boolean hasSourceFileForClass(String classFqn, List<File> scanRoots) {
		if(classFqn == null || classFqn.isEmpty()) {
			return false;
		}
		Boolean b = sourceFileExists.get(classFqn);
		if(b != null) {
			return b.booleanValue();
		}
		File f = findSourceFile(classFqn, scanRoots);
		boolean ok = f != null && f.isFile();
		sourceFileExists.put(classFqn, Boolean.valueOf(ok));
		return ok;
	}

	static Map<String, String> parseStaticFinalStringFields(File javaFile) throws IOException {
		byte[] bytes = Files.readAllBytes(javaFile.toPath());
		String raw;
		try {
			raw = new String(bytes, StandardCharsets.UTF_8);
		} catch(Exception e) {
			raw = new String(bytes, Charset.defaultCharset());
		}
		String text = SpringUriPatternExtractor.stripComments(raw);
		Map<String, String> map = new HashMap<String, String>();
		Matcher m = P_STATIC_FINAL_STRING.matcher(text);
		while(m.find()) {
			map.put(m.group(1), unescapeJavaString(m.group(2)));
		}
		resolveConcatStaticFinalStringFields(text, map);
		return map;
	}

	private static void resolveConcatStaticFinalStringFields(String text, Map<String, String> map) {
		final int maxRounds = 80;
		for(int round = 0; round < maxRounds; round++) {
			boolean progress = false;
			Matcher m = P_STATIC_FINAL_STRING_RHS.matcher(text);
			while(m.find()) {
				String name = m.group(1);
				if(map.containsKey(name)) {
					continue;
				}
				String expr = m.group(2).trim();
				String val = evalStringConcatConstExpr(expr, map);
				if(val != null) {
					map.put(name, val);
					progress = true;
				}
			}
			if(!progress) {
				break;
			}
		}
	}

	private static List<String> splitPlusOutsideDoubleQuotes(String expr) {
		List<String> out = new ArrayList<String>();
		int start = 0;
		int i = 0;
		while(i < expr.length()) {
			char c = expr.charAt(i);
			if(c == '"') {
				i++;
				while(i < expr.length()) {
					char d = expr.charAt(i);
					if(d == '\\' && i + 1 < expr.length()) {
						i += 2;
						continue;
					}
					if(d == '"') {
						i++;
						break;
					}
					i++;
				}
				continue;
			}
			if(c == '+') {
				out.add(expr.substring(start, i));
				i++;
				while(i < expr.length() && Character.isWhitespace(expr.charAt(i))) {
					i++;
				}
				start = i;
				continue;
			}
			i++;
		}
		out.add(expr.substring(start));
		return out;
	}

	private static String evalStringConcatConstExpr(String expr, Map<String, String> map) {
		List<String> parts = splitPlusOutsideDoubleQuotes(expr);
		if(parts.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(String rawPart : parts) {
			String part = rawPart.trim();
			if(part.isEmpty()) {
				return null;
			}
			if(part.charAt(0) == '"') {
				String lit = parseLeadingDoubleQuotedStringContent(part);
				if(lit == null) {
					return null;
				}
				sb.append(lit);
			} else {
				if(!part.matches("[A-Za-z_]\\w*")) {
					return null;
				}
				String v = map.get(part);
				if(v == null) {
					return null;
				}
				sb.append(v);
			}
		}
		return sb.toString();
	}

	private static String parseLeadingDoubleQuotedStringContent(String part) {
		int i = 0;
		while(i < part.length() && Character.isWhitespace(part.charAt(i))) {
			i++;
		}
		if(i >= part.length() || part.charAt(i) != '"') {
			return null;
		}
		StringBuilder lit = new StringBuilder();
		int j = i + 1;
		while(j < part.length()) {
			char c = part.charAt(j);
			if(c == '\\' && j + 1 < part.length()) {
				lit.append(part.charAt(j + 1));
				j += 2;
				continue;
			}
			if(c == '"') {
				j++;
				while(j < part.length() && Character.isWhitespace(part.charAt(j))) {
					j++;
				}
				return j == part.length() ? unescapeJavaString(lit.toString()) : null;
			}
			lit.append(c);
			j++;
		}
		return null;
	}

	private static String unescapeJavaString(String s) {
		return s.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	static File findSourceFile(String classFqn, List<File> scanRoots) {
		if(classFqn == null || scanRoots == null) {
			return null;
		}
		String fullSuffix = classFqn.replace('.', '/') + ".java";
		ArrayList<String> suffixCandidates = new ArrayList<String>();
		suffixCandidates.add(fullSuffix);
		int cut = 0;
		for(int i = 0; i < fullSuffix.length(); i++) {
			if(fullSuffix.charAt(i) == '/') {
				String s = fullSuffix.substring(i + 1);
				if(!s.isEmpty()) {
					suffixCandidates.add(s);
				}
				cut++;
				if(cut >= 10) {
					break;
				}
			}
		}
		for(File root : scanRoots) {
			if(root == null || !root.exists()) {
				continue;
			}
			if(root.isFile()) {
				String ap = root.getAbsolutePath().replace('\\', '/');
				for(String suffix : suffixCandidates) {
					if(ap.endsWith(suffix)) {
						return root;
					}
				}
				continue;
			}
			for(String suffix : suffixCandidates) {
				File direct = fileUnderRoot(root, suffix);
				if(direct != null && direct.isFile()) {
					return direct;
				}
				File deep = findFilePathSuffixUnder(root, suffix);
				if(deep != null) {
					return deep;
				}
			}
		}
		return null;
	}

	private static File fileUnderRoot(File root, String slashPath) {
		String[] parts = slashPath.split("/");
		File cur = root;
		for(String p : parts) {
			cur = new File(cur, p);
		}
		return cur;
	}

	private static File findFilePathSuffixUnder(File dir, String suffixUnix) {
		if(!dir.isDirectory()) {
			return null;
		}
		File[] children = dir.listFiles();
		if(children == null) {
			return null;
		}
		for(File c : children) {
			if(c.isDirectory()) {
				File r = findFilePathSuffixUnder(c, suffixUnix);
				if(r != null) {
					return r;
				}
			} else if(c.isFile()) {
				String p = c.getAbsolutePath().replace('\\', '/');
				if(p.endsWith(suffixUnix)) {
					return c;
				}
			}
		}
		return null;
	}
}
