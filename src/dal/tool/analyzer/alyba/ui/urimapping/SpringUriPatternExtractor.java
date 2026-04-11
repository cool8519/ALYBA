package dal.tool.analyzer.alyba.ui.urimapping;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dal.tool.analyzer.alyba.ui.Logger;

public class SpringUriPatternExtractor {

	public static final class ConstantResolutionContext {
		final StaticStringConstantCache cache;
		final List<File> scanRoots;
		final String packageName;
		final Map<String, String> typeImports;
		final List<String> wildcardPackages;
		final String currentClassFqn;
		final List<String> parentClassFqns;
		final Map<String, String> staticSingleFieldImports;
		final List<String> staticOnDemandImportTypes;

		public ConstantResolutionContext(StaticStringConstantCache cache, List<File> scanRoots, String packageName,
				Map<String, String> typeImports, List<String> wildcardPackages, String currentClassFqn, List<String> parentClassFqns) {
			this(cache, scanRoots, packageName, typeImports, wildcardPackages, currentClassFqn, parentClassFqns, null, null);
		}

		public ConstantResolutionContext(StaticStringConstantCache cache, List<File> scanRoots, String packageName,
				Map<String, String> typeImports, List<String> wildcardPackages, String currentClassFqn, List<String> parentClassFqns,
				Map<String, String> staticSingleFieldImports, List<String> staticOnDemandImportTypes) {
			this.cache = cache;
			this.scanRoots = scanRoots;
			this.packageName = packageName != null ? packageName : "";
			this.typeImports = typeImports != null ? typeImports : new HashMap<String, String>();
			this.wildcardPackages = wildcardPackages != null ? wildcardPackages : new ArrayList<String>();
			this.currentClassFqn = currentClassFqn;
			this.parentClassFqns = parentClassFqns != null ? parentClassFqns : new ArrayList<String>();
			this.staticSingleFieldImports = staticSingleFieldImports != null ? staticSingleFieldImports : Collections.<String, String>emptyMap();
			this.staticOnDemandImportTypes = staticOnDemandImportTypes != null ? staticOnDemandImportTypes : Collections.<String>emptyList();
		}
	}

	private static final class JavaFileHeaderInfo {
		String packageName = "";
		final Map<String, String> typeImports = new HashMap<String, String>();
		final List<String> wildcardPackages = new ArrayList<String>();
		final Map<String, String> staticSingleFieldImports = new HashMap<String, String>();
		final List<String> staticOnDemandImportTypes = new ArrayList<String>();
		String topLevelClassName = "";
		String extendsTypeRaw = "";
	}

	public static final String LEVEL_FILE = "FILE";
	public static final String LEVEL_CLASS = "CLASS";
	public static final String LEVEL_METHOD = "METHOD";

	public static class FailureDetail {

		public final String filePath;
		public final String level;
		public final int lineNumber;
		public final String scopeDetail;
		public final String patternAttempt;
		public final String reason;

		public FailureDetail(String filePath, String level, int lineNumber, String scopeDetail, String patternAttempt, String reason) {
			this.filePath = filePath;
			this.level = level != null ? level : LEVEL_FILE;
			this.lineNumber = lineNumber;
			this.scopeDetail = scopeDetail != null ? scopeDetail : "";
			this.patternAttempt = patternAttempt != null ? patternAttempt : "";
			this.reason = reason != null ? reason : "";
		}

		public String getFileName() {
			return new File(filePath).getName();
		}

		public String formatDisplayLine() {
			return formatDisplayLine(null);
		}

		public String formatDisplayLine(String pathLabelOverride) {
			String filePart = (pathLabelOverride != null && !pathLabelOverride.trim().isEmpty())
				? pathLabelOverride.trim().replace('\\', '/')
				: getFileName();
			String loc = lineNumber > 0 ? ("line " + lineNumber) : "line ?";
			String scope = scopeDetail.isEmpty() ? "—" : scopeDetail;
			String pa = patternAttempt.isEmpty() ? "" : (" | annotation: " + shorten(patternAttempt, 80));
			return "[" + level + "] " + filePart + " | " + loc + " | " + scope + pa + " → " + reason;
		}

		private static String shorten(String s, int max) {
			if(s.length() <= max) {
				return s;
			}
			return s.substring(0, max) + "…";
		}
	}

	public static class ExtractResult {
		public final List<String> patterns;
		public final List<FailureDetail> failures;
		public final int controllerClassCount;
		public final List<MethodMappingOutcome> methodOutcomes;
		public final List<String> controllerBaseUris;

		public ExtractResult(List<String> patterns, List<FailureDetail> failures, int controllerClassCount,
				List<MethodMappingOutcome> methodOutcomes, List<String> controllerBaseUris) {
			this.patterns = patterns;
			this.failures = failures;
			this.controllerClassCount = controllerClassCount;
			this.methodOutcomes = methodOutcomes != null ? methodOutcomes : new ArrayList<MethodMappingOutcome>();
			if(controllerBaseUris == null || controllerBaseUris.isEmpty()) {
				this.controllerBaseUris = Collections.emptyList();
			} else {
				this.controllerBaseUris = Collections.unmodifiableList(new ArrayList<String>(controllerBaseUris));
			}
		}

		public ExtractResult(List<String> patterns, List<FailureDetail> failures,
				List<MethodMappingOutcome> methodOutcomes) {
			this(patterns, failures, 0, methodOutcomes, null);
		}

		public ExtractResult(List<String> patterns, List<FailureDetail> failures) {
			this(patterns, failures, 0, null, null);
		}
	}

	public static class MethodMappingOutcome {
		public enum Kind {
			OK,
			SKIP,
			ERROR
		}

		public final int lineNumber;
		public final String scopeLabel;
		public final Kind kind;
		public final int extractedPatternCount;
		public final String detail;
		public final List<String> combinedUris;

		public MethodMappingOutcome(int lineNumber, String scopeLabel, Kind kind, int extractedPatternCount, String detail,
				List<String> combinedUris) {
			this.lineNumber = lineNumber;
			this.scopeLabel = scopeLabel != null ? scopeLabel : "";
			this.kind = kind;
			this.extractedPatternCount = extractedPatternCount;
			this.detail = detail != null ? detail : "";
			if(combinedUris == null || combinedUris.isEmpty()) {
				this.combinedUris = Collections.emptyList();
			} else {
				this.combinedUris = Collections.unmodifiableList(new ArrayList<String>(combinedUris));
			}
		}

		public MethodMappingOutcome(int lineNumber, String scopeLabel, Kind kind, int extractedPatternCount, String detail) {
			this(lineNumber, scopeLabel, kind, extractedPatternCount, detail, null);
		}
	}

	private static final Pattern P_REST_OR_CONTROLLER = Pattern.compile("@(?:[A-Za-z_]\\w*\\.)*(?:RestController|Controller)\\b");
	private static final Pattern P_TYPE_DECL = Pattern.compile(
		"\\b(class|interface)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*(?:<[^>]+>)?\\s*(?:extends\\s+[^{]+?)?\\s*(?:implements\\s+[^{]+?)?\\s*\\{");
	private static final Pattern P_REQUEST_MAPPING_LEAD = Pattern.compile("@(?:[A-Za-z_]\\w*\\.)*RequestMapping\\b\\s*",
		Pattern.DOTALL);
	private static final Pattern P_SHORT_MAPPING_LEAD = Pattern.compile("@(?:[A-Za-z_]\\w*\\.)*(Get|Post|Put|Delete|Patch)Mapping\\b\\s*",
		Pattern.DOTALL);
	private static final Pattern P_METHOD_ONLY_INNER = Pattern
		.compile("^[\\s,]*(?:method\\s*=\\s*RequestMethod\\.\\w+\\s*,?\\s*)+$");
	private static final Pattern P_METHOD_ONLY_BRACE_ARRAY = Pattern.compile(
		"^[\\s,]*method\\s*=\\s*\\{\\s*(?:RequestMethod\\.\\w+\\s*,?\\s*)+\\}\\s*,?\\s*$");
	private static final Pattern P_METHOD_NAME_AFTER_MAPPING = Pattern.compile(
		"\\b(?:public|protected|private)\\s+(?:@[A-Za-z_][A-Za-z0-9_.]*\\s+)*(?:[\\w.<>,?\\[\\]]+\\s+)+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(",
		Pattern.DOTALL);

	private static final class StrippedSource {
		final String text;
		final int[] textToRaw;

		StrippedSource(String text, int[] textToRaw) {
			this.text = text;
			this.textToRaw = textToRaw;
		}

		int rawIndexForTextPos(int textPos) {
			if(textToRaw.length == 0) {
				return 0;
			}
			if(textPos < 0) {
				return textToRaw[0];
			}
			if(textPos >= textToRaw.length) {
				return textToRaw[textToRaw.length - 1];
			}
			return textToRaw[textPos];
		}
	}

	private static final class ClassDecl {
		final String kind;
		final int headerStart;
		final int openBrace;
		final int closeBrace;

		ClassDecl(String kind, int headerStart, int openBrace, int closeBrace) {
			this.kind = kind != null ? kind : "class";
			this.headerStart = headerStart;
			this.openBrace = openBrace;
			this.closeBrace = closeBrace;
		}
	}

	private static boolean isJavaClassDeclarationPrefixLine(String lineTrimmed) {
		if(lineTrimmed.startsWith("public ") || lineTrimmed.startsWith("protected ") || lineTrimmed.startsWith("private ")
				|| lineTrimmed.startsWith("abstract ") || lineTrimmed.startsWith("static ") || lineTrimmed.startsWith("final ")
				|| lineTrimmed.startsWith("strictfp ") || lineTrimmed.startsWith("sealed ") || lineTrimmed.startsWith("non-sealed ")) {
			return true;
		}
		String t = lineTrimmed.trim();
		return "public".equals(t) || "protected".equals(t) || "private".equals(t) || "static".equals(t) || "abstract".equals(t)
				|| "final".equals(t) || "strictfp".equals(t);
	}

	private static boolean mappingAnnotationAttrNamePrefix(String lineTrimmed, String attr) {
		if(!lineTrimmed.startsWith(attr)) {
			return false;
		}
		if(lineTrimmed.length() == attr.length()) {
			return true;
		}
		return !Character.isJavaIdentifierPart(lineTrimmed.charAt(attr.length()));
	}

	private static boolean isAnnotationBodyContinuationLine(String trim) {
		if(trim.isEmpty()) {
			return false;
		}
		if("(".equals(trim) || ")".equals(trim) || ",".equals(trim) || "{".equals(trim)) {
			return true;
		}
		if(trim.indexOf('"') >= 0) {
			return true;
		}
		char c0 = trim.charAt(0);
		if(c0 == '"' || c0 == '\'') {
			return true;
		}
		if(trim.endsWith(",")) {
			return true;
		}
		if(mappingAnnotationAttrNamePrefix(trim, "value") || mappingAnnotationAttrNamePrefix(trim, "path")
				|| mappingAnnotationAttrNamePrefix(trim, "method") || mappingAnnotationAttrNamePrefix(trim, "params")
				|| mappingAnnotationAttrNamePrefix(trim, "headers") || mappingAnnotationAttrNamePrefix(trim, "consumes")
				|| mappingAnnotationAttrNamePrefix(trim, "produces") || mappingAnnotationAttrNamePrefix(trim, "name")) {
			return true;
		}
		if(trim.matches("^[A-Za-z_][\\w.]*\\.[A-Za-z_]\\w*\\s*,?\\s*$")) {
			return true;
		}
		return false;
	}

	private static boolean isAnnotationArrayPropertyClosingBraceLine(String trim) {
		if(trim.isEmpty() || trim.charAt(0) != '}') {
			return false;
		}
		int i = 1;
		while(i < trim.length()) {
			char c = trim.charAt(i);
			if(c == ' ' || c == '\t') {
				i++;
				continue;
			}
			return c == ',' || c == ';';
		}
		return false;
	}

	private static boolean isLoneClosingBraceBeforeAnnotationTail(String text, String lineTrim, int indexAfterLine) {
		if(!"}".equals(lineTrim)) {
			return false;
		}
		int i = indexAfterLine;
		while(i < text.length()) {
			char c = text.charAt(i);
			if(c == ' ' || c == '\t' || c == '\n' || c == '\r') {
				i++;
				continue;
			}
			if(c == ',') {
				return true;
			}
			if(Character.isJavaIdentifierStart(c)) {
				int j = i;
				while(j < text.length() && Character.isJavaIdentifierPart(text.charAt(j))) {
					j++;
				}
				String word = text.substring(i, j);
				if("method".equals(word) || "params".equals(word) || "value".equals(word) || "path".equals(word)
						|| "headers".equals(word) || "consumes".equals(word) || "produces".equals(word) || "name".equals(word)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	private static boolean isClosingBraceLineFollowedOnlyByCloseParen(String text, int indexAfterLine) {
		int i = indexAfterLine;
		while(i < text.length()) {
			char c = text.charAt(i);
			if(c == ' ' || c == '\t' || c == '\n' || c == '\r') {
				i++;
				continue;
			}
			if(c == ')') {
				i++;
				while(i < text.length() && (text.charAt(i) == ' ' || text.charAt(i) == '\t')) {
					i++;
				}
				return i >= text.length() || text.charAt(i) == '\n' || text.charAt(i) == '\r';
			}
			return false;
		}
		return false;
	}

	private static boolean isClosingBraceWithCloseParenSameLine(String lineTrim) {
		if(lineTrim == null) {
			return false;
		}
		return lineTrim.matches("^\\}\\s*\\)\\s*,?\\s*$");
	}

	private static int findClassHeaderStart(String text, int classKeywordPos) {
		int cur = classKeywordPos;
		while(cur > 0 && text.charAt(cur - 1) != '\n' && text.charAt(cur - 1) != '\r') {
			cur--;
		}
		int headerStart = cur;
		while(cur > 0) {
			int prevLineEnd = cur - 1;
			while(prevLineEnd >= 0 && (text.charAt(prevLineEnd) == '\n' || text.charAt(prevLineEnd) == '\r')) {
				prevLineEnd--;
			}
			if(prevLineEnd < 0) {
				return headerStart;
			}
			int prevLineStart = prevLineEnd;
			while(prevLineStart > 0 && text.charAt(prevLineStart - 1) != '\n' && text.charAt(prevLineStart - 1) != '\r') {
				prevLineStart--;
			}
			String prevLineTrim = text.substring(prevLineStart, prevLineEnd + 1).trim();
			if(prevLineTrim.isEmpty()) {
				cur = prevLineStart;
				continue;
			}
			if(prevLineTrim.startsWith("import ") || prevLineTrim.startsWith("package ")) {
				break;
			}
			if(prevLineTrim.startsWith("}")) {
				if(isClosingBraceWithCloseParenSameLine(prevLineTrim)) {
					headerStart = prevLineStart;
					cur = prevLineStart;
					continue;
				}
				if(isClosingBraceLineFollowedOnlyByCloseParen(text, prevLineEnd + 1)) {
					headerStart = prevLineStart;
					cur = prevLineStart;
					continue;
				}
				if(isAnnotationArrayPropertyClosingBraceLine(prevLineTrim)) {
					headerStart = prevLineStart;
					cur = prevLineStart;
					continue;
				}
				if(isLoneClosingBraceBeforeAnnotationTail(text, prevLineTrim, prevLineEnd + 1)) {
					headerStart = prevLineStart;
					cur = prevLineStart;
					continue;
				}
				break;
			}
			if(prevLineTrim.startsWith("@") || isJavaClassDeclarationPrefixLine(prevLineTrim)) {
				headerStart = prevLineStart;
				cur = prevLineStart;
				continue;
			}
			if(isAnnotationBodyContinuationLine(prevLineTrim)) {
				headerStart = prevLineStart;
				cur = prevLineStart;
				continue;
			}
			break;
		}
		return headerStart;
	}

	private static List<ClassDecl> enumerateClassDeclarations(String text) {
		List<ClassDecl> list = new ArrayList<ClassDecl>();
		Matcher m = P_TYPE_DECL.matcher(text);
		while(m.find()) {
			int openBrace = m.end() - 1;
			int closeBrace = findMatchingClosingBrace(text, openBrace);
			if(closeBrace < 0) {
				continue;
			}
			int headerStart = findClassHeaderStart(text, m.start());
			String kind = m.group(1);
			list.add(new ClassDecl(kind, headerStart, openBrace, closeBrace));
		}
		return list;
	}

	private static boolean isDirectInnerClassOf(ClassDecl parent, ClassDecl child, List<ClassDecl> all) {
		if(child.openBrace <= parent.openBrace || child.closeBrace >= parent.closeBrace) {
			return false;
		}
		for(ClassDecl other : all) {
			if(other == parent || other == child) {
				continue;
			}
			if(other.openBrace <= parent.openBrace || other.closeBrace >= parent.closeBrace) {
				continue;
			}
			if(other.openBrace < child.openBrace && other.closeBrace > child.closeBrace) {
				return false;
			}
		}
		return true;
	}

	private static String buildClassBodySliceForMappingScan(String text, ClassDecl self, List<ClassDecl> allDecls) {
		int from = self.openBrace;
		int to = self.closeBrace;
		int len = to - from + 1;
		if(len <= 0) {
			return "";
		}
		char[] buf = text.substring(from, to + 1).toCharArray();
		for(ClassDecl child : allDecls) {
			if(child == self) {
				continue;
			}
			if(!isDirectInnerClassOf(self, child, allDecls)) {
				continue;
			}
			int blankFrom = Math.max(child.headerStart, from);
			int blankTo = Math.min(child.closeBrace, to);
			if(blankFrom > blankTo) {
				continue;
			}
			for(int i = blankFrom; i <= blankTo; i++) {
				int bi = i - from;
				if(bi >= 0 && bi < buf.length) {
					buf[bi] = ' ';
				}
			}
		}
		return new String(buf);
	}

	public static ExtractResult extract(File javaFile, String contextPath) {
		return extract(javaFile, contextPath, null, null);
	}

	public static ExtractResult extract(File javaFile, String contextPath, StaticStringConstantCache constantCache,
			List<File> scanRoots) {
		String path = javaFile.getAbsolutePath();
		List<FailureDetail> failures = new ArrayList<FailureDetail>();
		List<String> patterns = new ArrayList<String>();
		String raw;
		try {
			raw = readFileUtf8(javaFile);
		} catch(IOException e) {
			failures.add(new FailureDetail(path, LEVEL_FILE, 0, "read", "", "File read failed."));
			return new ExtractResult(patterns, failures, 0, null, null);
		}

		StrippedSource st = stripCommentsWithMap(raw);
		String text = st.text;

		ConstantResolutionContext constCtx = null;
		if(constantCache != null && scanRoots != null && !scanRoots.isEmpty()) {
			JavaFileHeaderInfo jh = parseJavaFileHeaderInfo(text);
			List<File> effectiveRoots = augmentScanRootsWithinAllowed(javaFile, scanRoots, jh.packageName);
			String selfFqn = buildClassFqn(jh.packageName, jh.topLevelClassName);
			List<String> parents = resolveParentClassChain(jh, constantCache, effectiveRoots);
			constCtx = new ConstantResolutionContext(constantCache, effectiveRoots, jh.packageName, jh.typeImports, jh.wildcardPackages, selfFqn, parents,
					jh.staticSingleFieldImports, jh.staticOnDemandImportTypes);
		}

		List<ClassDecl> allDecls = enumerateClassDeclarations(text);
		if(allDecls.isEmpty()) {
			int ln = firstLineMatching(raw, Pattern.compile("\\b(?:class|interface)\\b"));
			failures.add(new FailureDetail(path, LEVEL_CLASS, ln, "type declaration", "",
				"Only class/interface types are supported."));
			return new ExtractResult(patterns, failures, 0, null, null);
		}

		List<ClassDecl> controllerClasses = new ArrayList<ClassDecl>();
		for(ClassDecl cd : allDecls) {
			String hdr = text.substring(cd.headerStart, cd.openBrace);
			if(P_REST_OR_CONTROLLER.matcher(hdr).find()) {
				controllerClasses.add(cd);
			}
		}
		if(controllerClasses.isEmpty()) {
			int ln = firstLineMatching(raw, P_REST_OR_CONTROLLER);
			failures.add(new FailureDetail(path, LEVEL_CLASS, ln, "@Controller / @RestController", "",
				"Controller annotation not found."));
			return new ExtractResult(patterns, failures, 0, null, null);
		}

		String contextNorm = normalizeContextPath(contextPath);
		List<String> controllerBaseUris = new ArrayList<String>();
		List<MethodMappingOutcome> methodOutcomes = new ArrayList<MethodMappingOutcome>();
		List<ClassDecl> targetDecls = new ArrayList<ClassDecl>(controllerClasses);
		for(ClassDecl cd : allDecls) {
			if(!"interface".equals(cd.kind)) {
				continue;
			}
			if(!targetDecls.contains(cd)) {
				targetDecls.add(cd);
			}
		}

		for(ClassDecl cd : targetDecls) {
			String header = text.substring(cd.headerStart, cd.openBrace);

			List<MappingOccurrence> classMappings = findRequestMappings(header, cd.headerStart);
			List<FailureDetail> classFailures = new ArrayList<FailureDetail>();
			for(MappingOccurrence mo : classMappings) {
				PathParseResult ppr = parseMappingInnerFull(mo.inner, constCtx);
				if(!ppr.paths.isEmpty()) {
					continue;
				}
				if(isAcceptableMethodOnlyMapping(mo.inner)) {
					continue;
				}
				int line = lineNumberInRaw(raw, st.rawIndexForTextPos(mo.start));
				classFailures.add(new FailureDetail(path, LEVEL_CLASS, line, "class-level @RequestMapping", shortenInner(mo.inner),
					buildPathExtractionMessage(ppr, mo.inner)));
			}
			if(!classFailures.isEmpty()) {
				failures.addAll(classFailures);
				continue;
			}

			List<String> classBases = extractClassLevelRequestMappings(header, constCtx);
			if(classBases.isEmpty()) {
				classBases.add("");
			}
			if(P_REST_OR_CONTROLLER.matcher(header).find()) {
				for(String cb : classBases) {
					String baseUri = joinPaths(contextNorm, cb, "");
					if(baseUri != null) {
						controllerBaseUris.add(baseUri);
					}
				}
			}

			String bodySlice = buildClassBodySliceForMappingScan(text, cd, allDecls);
			List<MappingOccurrence> methodMappings = findAllMethodMappings(bodySlice, cd.openBrace);
			for(MappingOccurrence mo : methodMappings) {
				PathParseResult ppr = parseMappingInnerFull(mo.inner, constCtx);
				int line = lineNumberInRaw(raw, st.rawIndexForTextPos(mo.start));
				String methodName = guessMethodName(bodySlice, mo.end - cd.openBrace);
				String scope = mo.shortName + " → " + methodName + "()";

				if(!ppr.paths.isEmpty()) {
					List<String> matched = new ArrayList<String>();
					List<String> skipCandidates = new ArrayList<String>();
					for(String cb : classBases) {
						for(String mp : ppr.paths) {
							String combined = joinPaths(contextNorm, cb, mp);
							if(combined == null) {
								continue;
							}
							if(combined.indexOf('{') >= 0 && combined.indexOf('}') >= 0) {
								patterns.add(combined);
								matched.add(combined);
							} else {
								skipCandidates.add(combined);
							}
						}
					}
					if(!matched.isEmpty()) {
						methodOutcomes.add(new MethodMappingOutcome(line, scope, MethodMappingOutcome.Kind.OK, matched.size(), "",
								matched));
					} else {
						methodOutcomes.add(new MethodMappingOutcome(line, scope, MethodMappingOutcome.Kind.SKIP, 0,
							"No path variable in URI.", skipCandidates));
					}
					continue;
				}
				if(isAcceptableMethodOnlyMapping(mo.inner)) {
					List<String> matched = new ArrayList<String>();
					List<String> skipCandidates = new ArrayList<String>();
					for(String cb : classBases) {
						String combined = joinPaths(contextNorm, cb, "");
						if(combined == null) {
							continue;
						}
						if(combined.indexOf('{') >= 0 && combined.indexOf('}') >= 0) {
							patterns.add(combined);
							matched.add(combined);
						} else {
							skipCandidates.add(combined);
						}
					}
					if(!matched.isEmpty()) {
						methodOutcomes.add(new MethodMappingOutcome(line, scope, MethodMappingOutcome.Kind.OK, matched.size(), "",
								matched));
					} else {
						methodOutcomes.add(new MethodMappingOutcome(line, scope, MethodMappingOutcome.Kind.SKIP, 0,
							"No path variable in URI.", skipCandidates));
					}
					continue;
				}
				failures.add(new FailureDetail(path, LEVEL_METHOD, line, scope, shortenInner(mo.inner),
					buildPathExtractionMessage(ppr, mo.inner)));
				methodOutcomes.add(new MethodMappingOutcome(line, scope, MethodMappingOutcome.Kind.ERROR, 0,
					buildPathExtractionMessage(ppr, mo.inner), Collections.<String>emptyList()));
			}
		}

		return new ExtractResult(patterns, failures, controllerClasses.size(), methodOutcomes, controllerBaseUris);
	}

	private static List<File> augmentScanRootsWithinAllowed(File javaFile, List<File> allowedRoots, String packageName) {
		if(javaFile == null || allowedRoots == null || allowedRoots.isEmpty()) {
			return allowedRoots;
		}
		File inferred = inferSourceRootFromPackage(javaFile, packageName);
		if(inferred == null) {
			return allowedRoots;
		}
		Path inferredPath;
		try {
			inferredPath = inferred.toPath().toAbsolutePath().normalize();
		} catch(Exception e) {
			return allowedRoots;
		}
		boolean ok = false;
		for(File r : allowedRoots) {
			if(r == null || !r.exists()) {
				continue;
			}
			File dir = r.isFile() ? r.getParentFile() : r;
			if(dir == null) {
				continue;
			}
			try {
				Path rp = dir.toPath().toAbsolutePath().normalize();
				if(inferredPath.startsWith(rp)) {
					ok = true;
					break;
				}
			} catch(Exception e) {
				continue;
			}
		}
		if(!ok) {
			return allowedRoots;
		}
		for(File r : allowedRoots) {
			if(r != null) {
				try {
					if(r.toPath().toAbsolutePath().normalize().equals(inferredPath)) {
						return allowedRoots;
					}
				} catch(Exception e) {
				}
			}
		}
		ArrayList<File> out = new ArrayList<File>(allowedRoots.size() + 1);
		out.add(inferred);
		out.addAll(allowedRoots);
		return out;
	}

	private static File inferSourceRootFromPackage(File javaFile, String packageName) {
		if(javaFile == null) {
			return null;
		}
		File dir = javaFile.getParentFile();
		if(dir == null) {
			return null;
		}
		if(packageName == null || packageName.trim().isEmpty()) {
			return dir;
		}
		String pkg = packageName.trim().replace('.', '/');
		Path dirPath;
		try {
			dirPath = dir.toPath().toAbsolutePath().normalize();
		} catch(Exception e) {
			return null;
		}
		Path pkgPath = Paths.get(pkg);
		if(dirPath.getNameCount() < pkgPath.getNameCount()) {
			return null;
		}
		for(int i = 0; i < pkgPath.getNameCount(); i++) {
			Path a = dirPath.getName(dirPath.getNameCount() - pkgPath.getNameCount() + i);
			Path b = pkgPath.getName(i);
			if(!a.toString().equals(b.toString())) {
				return null;
			}
		}
		Path root = dirPath;
		for(int i = 0; i < pkgPath.getNameCount(); i++) {
			root = root.getParent();
			if(root == null) {
				return null;
			}
		}
		return root.toFile();
	}

	private static String shortenInner(String inner) {
		if(inner == null) {
			return "";
		}
		String t = inner.replaceAll("\\s+", " ").trim();
		return t.length() > 120 ? t.substring(0, 120) + "…" : t;
	}

	private static String buildPathExtractionMessage(PathParseResult ppr, String inner) {
		String tr = inner == null ? "" : inner.trim();
		if(tr.isEmpty()) {
			return "Mapping has no path.";
		}
		return "Path expression is not supported.";
	}

	private static boolean isAcceptableMethodOnlyMapping(String inner) {
		if(inner == null) {
			return true;
		}
		String s = inner.trim();
		if(s.isEmpty()) {
			return true;
		}
		if(P_METHOD_ONLY_INNER.matcher(s).matches() || P_METHOD_ONLY_BRACE_ARRAY.matcher(s).matches()) {
			return true;
		}
		return looksLikeNonPathRequestMappingAttributesOnly(s);
	}

	private static final Set<String> NON_PATH_MAPPING_ATTRS = new HashSet<String>(
		Arrays.asList("method", "params", "headers", "consumes", "produces", "name"));

	private static boolean looksLikeNonPathRequestMappingAttributesOnly(String innerTrimmed) {
		if(innerTrimmed.contains("value") || innerTrimmed.contains("path")) {
			return false;
		}
		Set<String> names = extractAttributeNames(innerTrimmed);
		if(names.isEmpty()) {
			return false;
		}
		for(String n : names) {
			if(!NON_PATH_MAPPING_ATTRS.contains(n)) {
				return false;
			}
		}
		return true;
	}

	private static Set<String> extractAttributeNames(String s) {
		Set<String> out = new HashSet<String>();
		if(s == null || s.isEmpty()) {
			return out;
		}
		StringBuilder sb = new StringBuilder(s.length());
		int i = 0;
		while(i < s.length()) {
			char c = s.charAt(i);
			if(c == '"') {
				i++;
				while(i < s.length()) {
					char d = s.charAt(i);
					if(d == '\\' && i + 1 < s.length()) {
						i += 2;
						continue;
					}
					if(d == '"') {
						i++;
						break;
					}
					i++;
				}
				sb.append(' ');
				continue;
			}
			sb.append(c);
			i++;
		}
		Matcher m = Pattern.compile("\\b([A-Za-z_]\\w*)\\s*=").matcher(sb.toString());
		while(m.find()) {
			out.add(m.group(1));
		}
		return out;
	}

	private static final class MappingOccurrence {
		final int start;
		final int end;
		final String inner;
		final String shortName;

		MappingOccurrence(int start, int end, String inner, String shortName) {
			this.start = start;
			this.end = end;
			this.inner = inner;
			this.shortName = shortName;
		}
	}

	private static int skipWs(String s, int i) {
		while(i < s.length() && Character.isWhitespace(s.charAt(i))) {
			i++;
		}
		return i;
	}

	private static int skipDoubleQuotedString(String s, int openQuoteIdx) {
		int i = openQuoteIdx + 1;
		while(i < s.length()) {
			char c = s.charAt(i);
			if(c == '\\' && i + 1 < s.length()) {
				i += 2;
				continue;
			}
			if(c == '"') {
				return i + 1;
			}
			i++;
		}
		return s.length();
	}

	private static boolean isHexDigit(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	private static int skipCharLiteral(String s, int tickIdx) {
		if(tickIdx >= s.length() || s.charAt(tickIdx) != '\'') {
			return tickIdx;
		}
		int j = tickIdx + 1;
		if(j >= s.length()) {
			return s.length();
		}
		if(s.charAt(j) == '\\') {
			j++;
			if(j >= s.length()) {
				return s.length();
			}
			char e = s.charAt(j);
			if(e == 'u') {
				j++;
				int d = 0;
				while(j < s.length() && d < 4 && isHexDigit(s.charAt(j))) {
					j++;
					d++;
				}
			} else {
				j++;
			}
		} else {
			j++;
		}
		if(j < s.length() && s.charAt(j) == '\'') {
			j++;
		}
		return j;
	}

	private static int findAnnotationArgsClosingParen(String s, int openParenIdx) {
		if(openParenIdx >= s.length() || s.charAt(openParenIdx) != '(') {
			return -1;
		}
		int depth = 0;
		int i = openParenIdx;
		while(i < s.length()) {
			char c = s.charAt(i);
			if(c == '"') {
				i = skipDoubleQuotedString(s, i);
				continue;
			}
			if(c == '\'') {
				i = skipCharLiteral(s, i);
				continue;
			}
			if(c == '(') {
				depth++;
				i++;
				continue;
			}
			if(c == ')') {
				depth--;
				if(depth == 0) {
					return i;
				}
				i++;
				continue;
			}
			i++;
		}
		return -1;
	}

	private static MappingOccurrence parseRequestMappingOccurrence(Matcher mLead, String region, int absOffset) {
		int relStart = mLead.start();
		int j = skipWs(region, mLead.end());
		if(j >= region.length() || region.charAt(j) != '(') {
			return new MappingOccurrence(absOffset + relStart, absOffset + j, "", "@RequestMapping");
		}
		int closeIdx = findAnnotationArgsClosingParen(region, j);
		if(closeIdx < 0) {
			return null;
		}
		String inner = region.substring(j + 1, closeIdx);
		return new MappingOccurrence(absOffset + relStart, absOffset + closeIdx + 1, inner, "@RequestMapping");
	}

	private static MappingOccurrence parseShortMappingOccurrence(Matcher mLead, String region, int absOffset) {
		String verb = mLead.group(1);
		String shortName = "@" + verb + "Mapping";
		int relStart = mLead.start();
		int j = skipWs(region, mLead.end());
		if(j >= region.length() || region.charAt(j) != '(') {
			return new MappingOccurrence(absOffset + relStart, absOffset + j, "", shortName);
		}
		int closeIdx = findAnnotationArgsClosingParen(region, j);
		if(closeIdx < 0) {
			return null;
		}
		String inner = region.substring(j + 1, closeIdx);
		return new MappingOccurrence(absOffset + relStart, absOffset + closeIdx + 1, inner, shortName);
	}

	private static List<MappingOccurrence> findRequestMappings(String region, int absOffset) {
		List<MappingOccurrence> list = new ArrayList<MappingOccurrence>();
		Matcher m = P_REQUEST_MAPPING_LEAD.matcher(region);
		while(m.find()) {
			MappingOccurrence mo = parseRequestMappingOccurrence(m, region, absOffset);
			if(mo != null) {
				list.add(mo);
			}
		}
		return list;
	}

	private static List<MappingOccurrence> findAllMethodMappings(String body, int bodyAbsStart) {
		List<MappingOccurrence> list = new ArrayList<MappingOccurrence>();
		Matcher m1 = P_SHORT_MAPPING_LEAD.matcher(body);
		while(m1.find()) {
			MappingOccurrence mo = parseShortMappingOccurrence(m1, body, bodyAbsStart);
			if(mo != null) {
				list.add(mo);
			}
		}
		Matcher m2 = P_REQUEST_MAPPING_LEAD.matcher(body);
		while(m2.find()) {
			MappingOccurrence mo = parseRequestMappingOccurrence(m2, body, bodyAbsStart);
			if(mo != null) {
				list.add(mo);
			}
		}
		Collections.sort(list, (a, b) -> Integer.compare(a.start, b.start));
		return list;
	}

	private static String guessMethodName(String fullBody, int mappingEndInBody) {
		if(mappingEndInBody < 0 || mappingEndInBody >= fullBody.length()) {
			return "?";
		}
		String tail = fullBody.substring(mappingEndInBody, Math.min(fullBody.length(), mappingEndInBody + 1200));
		Matcher m = P_METHOD_NAME_AFTER_MAPPING.matcher(tail);
		if(m.find()) {
			return m.group(1);
		}
		return "?";
	}

	private static int lineNumberInRaw(String raw, int rawIndex) {
		if(rawIndex < 0) {
			rawIndex = 0;
		}
		int line = 1;
		for(int i = 0; i < rawIndex && i < raw.length(); i++) {
			if(raw.charAt(i) == '\n') {
				line++;
			}
		}
		return line;
	}

	private static int firstLineMatching(String raw, Pattern p) {
		Matcher m = p.matcher(raw);
		if(!m.find()) {
			return 0;
		}
		return lineNumberInRaw(raw, m.start());
	}

	private static StrippedSource stripCommentsWithMap(String raw) {
		StringBuilder sb = new StringBuilder(raw.length());
		int[] map = new int[Math.max(raw.length(), 1)];
		int mi = 0;
		int i = 0;
		while(i < raw.length()) {
			char c = raw.charAt(i);
			if(c == '"') {
				if(mi >= map.length) {
					map = Arrays.copyOf(map, map.length * 2);
				}
				map[mi++] = i;
				sb.append(c);
				i++;
				while(i < raw.length()) {
					if(i + 1 < raw.length() && raw.charAt(i) == '\\') {
						if(mi >= map.length) {
							map = Arrays.copyOf(map, map.length * 2);
						}
						map[mi++] = i;
						sb.append(raw.charAt(i));
						i++;
						if(mi >= map.length) {
							map = Arrays.copyOf(map, map.length * 2);
						}
						map[mi++] = i;
						sb.append(raw.charAt(i));
						i++;
						continue;
					}
					if(mi >= map.length) {
						map = Arrays.copyOf(map, map.length * 2);
					}
					map[mi++] = i;
					char d = raw.charAt(i);
					sb.append(d);
					i++;
					if(d == '"') {
						break;
					}
				}
				continue;
			}
			if(i + 1 < raw.length() && c == '/' && raw.charAt(i + 1) == '/') {
				while(i < raw.length() && raw.charAt(i) != '\n' && raw.charAt(i) != '\r') {
					i++;
				}
				continue;
			}
			if(i + 1 < raw.length() && c == '/' && raw.charAt(i + 1) == '*') {
				i += 2;
				while(i + 1 < raw.length() && !(raw.charAt(i) == '*' && raw.charAt(i + 1) == '/')) {
					i++;
				}
				if(i + 1 < raw.length()) {
					i += 2;
				}
				continue;
			}
			if(mi >= map.length) {
				map = Arrays.copyOf(map, map.length * 2);
			}
			map[mi++] = i;
			sb.append(c);
			i++;
		}
		return new StrippedSource(sb.toString(), Arrays.copyOf(map, mi));
	}

	private static final class PathParseResult {
		final List<String> paths = new ArrayList<String>();
	}

	private static PathParseResult parseMappingInnerFull(String inner, ConstantResolutionContext constCtx) {
		PathParseResult r = new PathParseResult();
		if(inner == null) {
			return r;
		}
		String s = inner.trim();
		if(s.isEmpty()) {
			r.paths.add("");
			return r;
		}
		if(s.charAt(0) == '{') {
			int close = findMatchingClosingBrace(s, 0);
			if(close > 0) {
				addPathsFromMappingArrayBody(s.substring(1, close), constCtx, r.paths);
				if(!r.paths.isEmpty()) {
					return r;
				}
			}
		}
		extractValueAndPathAttributes(s, r.paths, constCtx);
		if(!r.paths.isEmpty()) {
			return r;
		}
		r.paths.addAll(parseLeadingQuotedPathLiterals(s));
		if(!r.paths.isEmpty()) {
			return r;
		}
		tryParseLeadingPositionalExpression(s, r.paths, constCtx);
		if(!r.paths.isEmpty()) {
			return r;
		}
		if(!s.contains("=")) {
			String ev = evaluateStringExpression(s, constCtx);
			if(ev != null) {
				r.paths.add(ev);
			}
		}
		if(!r.paths.isEmpty()) {
			return r;
		}
		tryAppendResolvedStringConstants(s, r, constCtx);
		return r;
	}

	private static final Pattern P_NAMED_ATTR_SEGMENT = Pattern.compile("^[A-Za-z_]\\w*\\s*=");

	private static void tryParseLeadingPositionalExpression(String s, List<String> paths, ConstantResolutionContext constCtx) {
		int i = 0;
		while(i < s.length() && Character.isWhitespace(s.charAt(i))) {
			i++;
		}
		if(i >= s.length() || s.charAt(i) == '"') {
			return;
		}
		int end = scanAnnotationArgumentEnd(s, i);
		String seg = s.substring(i, end).trim();
		if(seg.isEmpty()) {
			return;
		}
		if(P_NAMED_ATTR_SEGMENT.matcher(seg).find()) {
			return;
		}
		String v = evaluateStringExpression(seg, constCtx);
		if(v != null) {
			paths.add(v);
		}
	}

	private static int scanAnnotationArgumentEnd(String s, int start) {
		int i = start;
		int paren = 0;
		int brace = 0;
		int bracket = 0;
		while(i < s.length()) {
			char c = s.charAt(i);
			if(c == '"') {
				i++;
				while(i < s.length()) {
					char d = s.charAt(i);
					if(d == '\\' && i + 1 < s.length()) {
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
			if(c == '(') {
				paren++;
			} else if(c == ')') {
				paren--;
			} else if(c == '{') {
				brace++;
			} else if(c == '}') {
				brace--;
			} else if(c == '[') {
				bracket++;
			} else if(c == ']') {
				bracket--;
			} else if(c == ',' && paren == 0 && brace == 0 && bracket == 0) {
				return i;
			}
			i++;
		}
		return s.length();
	}

	private static void addPathsFromMappingArrayBody(String arrayBody, ConstantResolutionContext constCtx, List<String> paths) {
		if(arrayBody == null) {
			return;
		}
		int start = 0;
		int i = 0;
		int paren = 0;
		int brace = 0;
		int bracket = 0;
		while(i <= arrayBody.length()) {
			boolean atSplit = (i == arrayBody.length()) || (paren == 0 && brace == 0 && bracket == 0 && i < arrayBody.length()
				&& arrayBody.charAt(i) == ',');
			if(atSplit) {
				String tok = arrayBody.substring(start, i).trim();
				if(!tok.isEmpty()) {
					String v = evaluateStringExpression(tok, constCtx);
					if(v != null) {
						paths.add(v);
					}
				}
				if(i == arrayBody.length()) {
					break;
				}
				i++;
				start = i;
				continue;
			}
			char c = arrayBody.charAt(i);
			if(c == '"') {
				i++;
				while(i < arrayBody.length()) {
					char d = arrayBody.charAt(i);
					if(d == '\\' && i + 1 < arrayBody.length()) {
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
			if(c == '(') {
				paren++;
			} else if(c == ')') {
				paren--;
			} else if(c == '{') {
				brace++;
			} else if(c == '}') {
				brace--;
			} else if(c == '[') {
				bracket++;
			} else if(c == ']') {
				bracket--;
			}
			i++;
		}
	}

	static int findMatchingClosingBrace(String s, int openBraceIndex) {
		if(openBraceIndex < 0 || openBraceIndex >= s.length() || s.charAt(openBraceIndex) != '{') {
			return -1;
		}
		int depth = 0;
		int i = openBraceIndex;
		while(i < s.length()) {
			char c = s.charAt(i);
			if(c == '"') {
				i++;
				while(i < s.length()) {
					char d = s.charAt(i);
					if(d == '\\' && i + 1 < s.length()) {
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
			if(c == '{') {
				depth++;
			} else if(c == '}') {
				depth--;
				if(depth == 0) {
					return i;
				}
			}
			i++;
		}
		return -1;
	}

	static String evaluateStringExpression(String expr, ConstantResolutionContext constCtx) {
		if(expr == null) {
			return null;
		}
		expr = expr.trim();
		if(expr.isEmpty()) {
			return null;
		}
		List<String> parts = splitPlusOutsideQuotes(expr);
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
				String lit = parseLeadingQuotedToken(part, 0);
				if(lit == null) {
					return null;
				}
				sb.append(lit);
			} else {
				if(constCtx == null) {
					return null;
				}
				String v = resolveStaticStringConstant(part, constCtx);
				if(v == null) {
					return null;
				}
				sb.append(v);
			}
		}
		return sb.toString();
	}

	private static List<String> splitPlusOutsideQuotes(String expr) {
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

	private static String parseLeadingQuotedToken(String expr, int from) {
		int i = from;
		while(i < expr.length() && Character.isWhitespace(expr.charAt(i))) {
			i++;
		}
		if(i >= expr.length() || expr.charAt(i) != '"') {
			return null;
		}
		StringBuilder lit = new StringBuilder();
		int j = i + 1;
		while(j < expr.length()) {
			char c = expr.charAt(j);
			if(c == '\\' && j + 1 < expr.length()) {
				lit.append(expr.charAt(j + 1));
				j += 2;
				continue;
			}
			if(c == '"') {
				j++;
				while(j < expr.length() && Character.isWhitespace(expr.charAt(j))) {
					j++;
				}
				return j == expr.length() ? unescapeJavaString(lit.toString()) : null;
			}
			lit.append(c);
			j++;
		}
		return null;
	}

	private static final Pattern P_VALUE_PATH_NAMED_CONST = Pattern
		.compile("(?:value|path)\\s*=\\s*([A-Za-z_]\\w*(?:\\.[A-Za-z_]\\w*)+)");
	private static final Pattern P_LEADING_CONST_BEFORE_COMMA = Pattern
		.compile("^\\s*([A-Za-z_]\\w*(?:\\.[A-Za-z_]\\w*)+)\\s*,");
	private static final Pattern P_SINGLE_CONST_EXPR = Pattern.compile("^\\s*([A-Za-z_]\\w*(?:\\.[A-Za-z_]\\w*)+)\\s*$");

	private static void tryAppendResolvedStringConstants(String s, PathParseResult r, ConstantResolutionContext constCtx) {
		if(constCtx == null || constCtx.cache == null || constCtx.scanRoots == null || constCtx.scanRoots.isEmpty()) {
			return;
		}
		Matcher mn = P_VALUE_PATH_NAMED_CONST.matcher(s);
		while(mn.find()) {
			String val = resolveStaticStringConstant(mn.group(1), constCtx);
			if(val != null) {
				r.paths.add(val);
			}
		}
		if(!r.paths.isEmpty()) {
			return;
		}
		Matcher mc = P_LEADING_CONST_BEFORE_COMMA.matcher(s);
		if(mc.find()) {
			String val = resolveStaticStringConstant(mc.group(1), constCtx);
			if(val != null) {
				r.paths.add(val);
				return;
			}
		}
		Matcher ms = P_SINGLE_CONST_EXPR.matcher(s);
		if(ms.find()) {
			String val = resolveStaticStringConstant(ms.group(1), constCtx);
			if(val != null) {
				r.paths.add(val);
			}
		}
	}

	static String resolveStaticStringConstant(String dottedExpr, ConstantResolutionContext constCtx) {
		if(dottedExpr == null || constCtx == null) {
			return null;
		}
		dottedExpr = dottedExpr.trim();
		if(dottedExpr.isEmpty()) {
			return null;
		}
		if(dottedExpr.indexOf('.') < 0) {
			String v = resolveSimpleNameStaticStringConstant(dottedExpr, constCtx);
			return v;
		}
		String[] parts = dottedExpr.split("\\.");
		if(parts.length < 2) {
			return null;
		}
		String fieldName = parts[parts.length - 1];
		String classFqn;
		if(parts.length == 2) {
			classFqn = resolveImportedTypeFqn(parts[0], constCtx);
		} else {
			StringBuilder sb = new StringBuilder(parts[0]);
			for(int i = 1; i < parts.length - 1; i++) {
				sb.append('.').append(parts[i]);
			}
			classFqn = sb.toString();
		}
		if(classFqn == null || classFqn.isEmpty()) {
			return null;
		}
		constCtx.cache.ensureLoaded(classFqn, constCtx.scanRoots);
		return constCtx.cache.getFieldValue(classFqn, fieldName);
	}

	private static String resolveSimpleNameStaticStringConstant(String fieldName, ConstantResolutionContext constCtx) {
		if(fieldName == null || fieldName.isEmpty()) {
			return null;
		}
		if(constCtx.currentClassFqn != null && !constCtx.currentClassFqn.isEmpty()) {
			constCtx.cache.ensureLoaded(constCtx.currentClassFqn, constCtx.scanRoots);
			String v = constCtx.cache.getFieldValue(constCtx.currentClassFqn, fieldName);
			if(v != null) {
				return v;
			}
		}
		if(constCtx.parentClassFqns != null) {
			for(String pf : constCtx.parentClassFqns) {
				if(pf == null || pf.isEmpty()) {
					continue;
				}
				constCtx.cache.ensureLoaded(pf, constCtx.scanRoots);
				String v = constCtx.cache.getFieldValue(pf, fieldName);
				if(v != null) {
					return v;
				}
			}
		}
		String singleImp = constCtx.staticSingleFieldImports.get(fieldName);
		if(singleImp != null && !singleImp.isEmpty()) {
			String v = resolveStaticStringConstant(singleImp, constCtx);
			if(v != null) {
				return v;
			}
		}
		for(String onDemandType : constCtx.staticOnDemandImportTypes) {
			if(onDemandType == null || onDemandType.isEmpty()) {
				continue;
			}
			constCtx.cache.ensureLoaded(onDemandType, constCtx.scanRoots);
			String v = constCtx.cache.getFieldValue(onDemandType, fieldName);
			if(v != null) {
				return v;
			}
		}
		return null;
	}

	private static String resolveImportedTypeFqn(String simpleTypeName, ConstantResolutionContext constCtx) {
		if(simpleTypeName == null || simpleTypeName.isEmpty()) {
			return null;
		}
		String direct = constCtx.typeImports.get(simpleTypeName);
		if(direct != null) {
			return direct;
		}
		for(String wp : constCtx.wildcardPackages) {
			String candidate = wp + "." + simpleTypeName;
			if(constCtx.cache.hasSourceFileForClass(candidate, constCtx.scanRoots)) {
				return candidate;
			}
		}
		if(constCtx.packageName.isEmpty()) {
			return simpleTypeName;
		}
		return constCtx.packageName + "." + simpleTypeName;
	}

	private static JavaFileHeaderInfo parseJavaFileHeaderInfo(String strippedText) {
		JavaFileHeaderInfo info = new JavaFileHeaderInfo();
		if(strippedText == null) {
			return info;
		}
		Matcher mp = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;").matcher(strippedText);
		if(mp.find()) {
			info.packageName = mp.group(1);
		}
		Matcher mi = Pattern.compile("(?m)^\\s*import\\s+(static\\s+)?([\\w.*]+)\\s*;").matcher(strippedText);
		while(mi.find()) {
			if(mi.group(1) != null) {
				String st = mi.group(2).trim();
				if(st.endsWith(".*")) {
					info.staticOnDemandImportTypes.add(st.substring(0, st.length() - 2));
				} else {
					int ld = st.lastIndexOf('.');
					if(ld > 0) {
						String mem = st.substring(ld + 1);
						info.staticSingleFieldImports.put(mem, st);
					}
				}
				continue;
			}
			String imp = mi.group(2);
			if(imp.endsWith(".*")) {
				info.wildcardPackages.add(imp.substring(0, imp.length() - 2));
			} else {
				int lastDot = imp.lastIndexOf('.');
				if(lastDot > 0) {
					String simple = imp.substring(lastDot + 1);
					info.typeImports.put(simple, imp);
				}
			}
		}
		/* top-level class name + extends */
		Matcher mc = Pattern.compile("\\bclass\\s+([A-Za-z_]\\w*)\\b").matcher(strippedText);
		if(mc.find()) {
			info.topLevelClassName = mc.group(1);
			int from = mc.end();
			int brace = strippedText.indexOf('{', from);
			int to = brace > 0 ? brace : Math.min(strippedText.length(), from + 400);
			String tail = strippedText.substring(from, to);
			Matcher me = Pattern.compile("\\bextends\\s+([A-Za-z_][\\w.]*(?:\\s*<[^>]+>)?)").matcher(tail);
			if(me.find()) {
				info.extendsTypeRaw = me.group(1).trim();
			}
		}
		return info;
	}

	private static String buildClassFqn(String pkg, String cls) {
		if(cls == null || cls.trim().isEmpty()) {
			return "";
		}
		String c = cls.trim();
		String p = pkg == null ? "" : pkg.trim();
		return p.isEmpty() ? c : (p + "." + c);
	}

	private static List<String> resolveParentClassChain(JavaFileHeaderInfo current, StaticStringConstantCache cache, List<File> scanRoots) {
		List<String> out = new ArrayList<String>();
		if(current == null || cache == null || scanRoots == null || scanRoots.isEmpty()) {
			return out;
		}
		String nextRaw = current.extendsTypeRaw;
		/* avoid infinite loops; typical controller inheritance depth is shallow */
		for(int depth = 0; depth < 6; depth++) {
			String parentFqn = resolveExtendsTypeToFqn(nextRaw, current, cache, scanRoots);
			if(parentFqn == null || parentFqn.isEmpty()) {
				break;
			}
			if(out.contains(parentFqn)) {
				break;
			}
			out.add(parentFqn);
			/* read parent source to continue chain */
			File src = StaticStringConstantCache.findSourceFile(parentFqn, scanRoots);
			if(src == null || !src.isFile()) {
				break;
			}
			String raw;
			try {
				raw = readFileUtf8(src);
			} catch(Exception e) {
				break;
			}
			String stripped = stripCommentsWithMap(raw).text;
			JavaFileHeaderInfo parentInfo = parseJavaFileHeaderInfo(stripped);
			nextRaw = parentInfo.extendsTypeRaw;
			current = parentInfo;
		}
		return out;
	}

	private static String resolveExtendsTypeToFqn(String extendsRaw, JavaFileHeaderInfo scope, StaticStringConstantCache cache,
			List<File> scanRoots) {
		if(extendsRaw == null) {
			return null;
		}
		String t = extendsRaw.trim();
		if(t.isEmpty()) {
			return null;
		}
		int lt = t.indexOf('<');
		if(lt > 0) {
			t = t.substring(0, lt).trim();
		}
		if(t.isEmpty()) {
			return null;
		}
		/* qualified already */
		if(t.indexOf('.') >= 0) {
			return t;
		}
		/* resolve using that file's own imports */
		ConstantResolutionContext tmp = new ConstantResolutionContext(cache, scanRoots, scope.packageName, scope.typeImports, scope.wildcardPackages, "", null,
				scope.staticSingleFieldImports, scope.staticOnDemandImportTypes);
		return resolveImportedTypeFqn(t, tmp);
	}

	private static void extractValueAndPathAttributes(String s, List<String> paths, ConstantResolutionContext constCtx) {
		Matcher m = Pattern.compile("(?:value|path)\\s*=").matcher(s);
		while(m.find()) {
			int pos = m.end();
			while(pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
				pos++;
			}
			if(pos >= s.length()) {
				continue;
			}
			if(s.charAt(pos) == '{') {
				int close = findMatchingClosingBrace(s, pos);
				if(close < 0) {
					continue;
				}
				addPathsFromMappingArrayBody(s.substring(pos + 1, close), constCtx, paths);
				continue;
			}
			int end = scanAnnotationArgumentEnd(s, pos);
			String expr = s.substring(pos, end).trim();
			if(!expr.isEmpty()) {
				String ev = evaluateStringExpression(expr, constCtx);
				if(ev != null) {
					paths.add(ev);
				}
			}
		}
	}

	private static List<String> parseLeadingQuotedPathLiterals(String s) {
		List<String> out = new ArrayList<String>();
		int i = 0;
		int n = s.length();
		while(i < n && Character.isWhitespace(s.charAt(i))) {
			i++;
		}
		while(i < n) {
			if(s.charAt(i) != '"') {
				break;
			}
			int j = i + 1;
			StringBuilder lit = new StringBuilder();
			boolean closed = false;
			while(j < n) {
				char c = s.charAt(j);
				if(c == '\\' && j + 1 < n) {
					lit.append(s.charAt(j + 1));
					j += 2;
					continue;
				}
				if(c == '"') {
					j++;
					closed = true;
					break;
				}
				lit.append(c);
				j++;
			}
			if(!closed) {
				break;
			}
			out.add(unescapeJavaString(lit.toString()));
			i = j;
			while(i < n && Character.isWhitespace(s.charAt(i))) {
				i++;
			}
			if(i < n && s.charAt(i) == ',') {
				i++;
				while(i < n && Character.isWhitespace(s.charAt(i))) {
					i++;
				}
				continue;
			}
			break;
		}
		return out;
	}

	static List<String> parseMappingInner(String inner) {
		return new ArrayList<String>(parseMappingInnerFull(inner, null).paths);
	}

	private static String readFileUtf8(File f) throws IOException {
		byte[] bytes = Files.readAllBytes(f.toPath());
		try {
			return new String(bytes, StandardCharsets.UTF_8);
		} catch(Exception e) {
			return new String(bytes, Charset.defaultCharset());
		}
	}

	static String stripComments(String s) {
		return stripCommentsWithMap(s).text;
	}

	static List<String> extractClassLevelRequestMappings(String headerBeforeClassBrace) {
		return extractClassLevelRequestMappings(headerBeforeClassBrace, null);
	}

	static List<String> extractClassLevelRequestMappings(String headerBeforeClassBrace, ConstantResolutionContext constCtx) {
		List<String> paths = new ArrayList<String>();
		int classKw = -1;
		Matcher mc = Pattern.compile("\\bclass\\s+[A-Za-z_]").matcher(headerBeforeClassBrace);
		if(mc.find()) {
			classKw = mc.start();
		}
		String head = classKw > 0 ? headerBeforeClassBrace.substring(0, classKw) : headerBeforeClassBrace;
		Matcher m = P_REQUEST_MAPPING_LEAD.matcher(head);
		while(m.find()) {
			MappingOccurrence mo = parseRequestMappingOccurrence(m, head, 0);
			if(mo != null) {
				paths.addAll(new ArrayList<String>(parseMappingInnerFull(mo.inner, constCtx).paths));
			}
		}
		return paths;
	}

	static List<String> extractMethodMappings(String classBody) {
		List<String> out = new ArrayList<String>();
		Matcher m1 = P_SHORT_MAPPING_LEAD.matcher(classBody);
		while(m1.find()) {
			MappingOccurrence mo = parseShortMappingOccurrence(m1, classBody, 0);
			if(mo != null) {
				out.addAll(new ArrayList<String>(parseMappingInnerFull(mo.inner, null).paths));
			}
		}
		Matcher m2 = P_REQUEST_MAPPING_LEAD.matcher(classBody);
		while(m2.find()) {
			MappingOccurrence mo = parseRequestMappingOccurrence(m2, classBody, 0);
			if(mo != null) {
				out.addAll(new ArrayList<String>(parseMappingInnerFull(mo.inner, null).paths));
			}
		}
		return out;
	}

	private static String unescapeJavaString(String s) {
		return s.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	static String normalizeContextPath(String ctx) {
		if(ctx == null) {
			return "";
		}
		String t = ctx.trim();
		if(t.isEmpty()) {
			return "";
		}
		if(!t.startsWith("/")) {
			t = "/" + t;
		}
		while(t.endsWith("/") && t.length() > 1) {
			t = t.substring(0, t.length() - 1);
		}
		return t;
	}

	static String joinPaths(String context, String classBase, String methodPath) {
		String cb = classBase == null ? "" : classBase.trim();
		String mp = methodPath == null ? "" : methodPath.trim();
		if(!cb.isEmpty() && !cb.startsWith("/")) {
			cb = "/" + cb;
		}
		if(!mp.isEmpty() && !mp.startsWith("/")) {
			mp = "/" + mp;
		}
		StringBuilder sb = new StringBuilder();
		if(context != null && context.length() > 0) {
			sb.append(context);
		}
		if(cb.length() > 0) {
			if(sb.length() > 0 && sb.charAt(sb.length() - 1) == '/' && cb.startsWith("/")) {
				sb.append(cb.substring(1));
			} else if(sb.length() > 0 && sb.charAt(sb.length() - 1) != '/' && !cb.startsWith("/")) {
				sb.append('/').append(cb);
			} else {
				sb.append(cb);
			}
		}
		if(mp.length() > 0) {
			if(sb.length() > 0 && sb.charAt(sb.length() - 1) == '/' && mp.startsWith("/")) {
				sb.append(mp.substring(1));
			} else if(sb.length() > 0 && sb.charAt(sb.length() - 1) != '/' && !mp.startsWith("/")) {
				sb.append('/').append(mp);
			} else {
				sb.append(mp);
			}
		}
		if(sb.length() == 0) {
			return "/";
		}
		if(sb.charAt(0) != '/') {
			sb.insert(0, '/');
		}
		return sb.toString();
	}

	public static List<File> collectJavaFilesRecursive(File root) {
		List<File> list = new ArrayList<File>();
		collectJavaFilesRecursive(root, list);
		return list;
	}

	private static void collectJavaFilesRecursive(File f, List<File> out) {
		if(f == null || !f.exists()) {
			return;
		}
		if(f.isFile()) {
			if(f.getName().endsWith(".java")) {
				out.add(f);
			}
			return;
		}
		File[] ch = f.listFiles();
		if(ch == null) {
			return;
		}
		for(File c : ch) {
			collectJavaFilesRecursive(c, out);
		}
	}

	public static boolean isLikelyControllerFile(File javaFile) {
		try {
			String t = stripComments(readFileUtf8(javaFile));
			return P_REST_OR_CONTROLLER.matcher(t).find();
		} catch(Exception e) {
			Logger.debug(e);
			return false;
		}
	}

	public static List<String> sortAndDedupe(List<String> patterns) {
		if(patterns == null || patterns.isEmpty()) {
			return new ArrayList<String>();
		}
		ArrayList<String> copy = new ArrayList<String>(patterns.size());
		for(String p : patterns) {
			if(p == null) {
				continue;
			}
			String t = p.trim();
			if(t.isEmpty()) {
				continue;
			}
			if(isUriMappingPatternRegexCompilable(t)) {
				copy.add(t);
			}
		}
		Collections.sort(copy, new UriPatternComparator());
		LinkedHashSet<String> set = new LinkedHashSet<String>(copy);
		return new ArrayList<String>(set);
	}

	private static boolean isUriMappingPatternRegexCompilable(String patternStr) {
		try {
			String regex = buildAlybaUriMappingRegex(patternStr);
			if(regex == null) {
				return false;
			}
			Pattern.compile("^" + regex + "$");
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	private static String buildAlybaUriMappingRegex(String patternStr) {
		if(patternStr == null) {
			return null;
		}
		String s = patternStr.trim();
		if(s.isEmpty() || !s.startsWith("/") || s.indexOf('{') < 0 || s.indexOf('}') < 0) {
			return null;
		}
		StringBuilder out = new StringBuilder(s.length() + 16);
		int i = 0;
		while(i < s.length()) {
			char c = s.charAt(i);
			if(c != '{') {
				if(c == '/') {
					out.append("\\/");
				} else if(c == '.') {
					out.append("\\\\.");
				} else {
					out.append(c);
				}
				i++;
				continue;
			}
			int depth = 1;
			int j = i + 1;
			while(j < s.length() && depth > 0) {
				char d = s.charAt(j);
				if(d == '{') {
					depth++;
				} else if(d == '}') {
					depth--;
					if(depth == 0) {
						break;
					}
				}
				j++;
			}
			if(depth != 0 || j >= s.length()) {
				return null;
			}
			String inner = s.substring(i + 1, j);
			String regexInner = "[^/]+";
			int colon = inner.indexOf(':');
			if(colon >= 0 && colon + 1 < inner.length()) {
				regexInner = inner.substring(colon + 1);
				regexInner = regexInner.replaceAll("\\\\\\\\", "\\\\");
			}
			out.append('(').append(regexInner).append(')');
			i = j + 1;
		}
		return out.toString();
	}

}
