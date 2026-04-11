package dal.tool.analyzer.alyba.ui.urimapping;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dal.util.swt.ProgressBarTask;

public class SpringProjectURIPatternExtractTask extends ProgressBarTask {

	private static final int MAX_FAILURE_LOG_LENGTH = 1000;

	private final List<File> roots;
	private final String contextPath;

	public SpringProjectURIPatternExtractTask(List<File> roots, String contextPath) {
		this.roots = roots;
		this.contextPath = contextPath;
	}

	@Override
	public void doCancel() {
	}

	@Override
	public void doTask() throws Exception {
		status = new int[1];
		tasksPercent = new int[1];
		tasksDetail = new String[1];
		status[0] = STATUS_PROCESSING;
		tasksPercent[0] = 0;
		tasksDetail[0] = "Preparing scan";
		setDetailMessage("Discovering java files");
		setDetailSubMessage("Initializing...", false);

		List<File> javaFiles = new ArrayList<File>();
		List<File> controllerFiles = new ArrayList<File>();
		int rootCount = roots != null ? roots.size() : 0;
		for(int ri = 0; ri < roots.size(); ri++) {
			File root = roots.get(ri);
			checkCanceled();
			if(root == null || !root.exists()) {
				continue;
			}
			onSpringScanRootStarted(root);
			setDetailSubMessage("Scanning root " + (ri + 1) + "/" + rootCount + " : " + root.getName(), false);
			if(root.isFile()) {
				if(root.getName().endsWith(".java")) {
					javaFiles.add(root);
				}
				continue;
			}
			List<File> allJava = SpringUriPatternExtractor.collectJavaFilesRecursive(root);
			javaFiles.addAll(allJava);
			setDetailSubMessage("Scanned root " + (ri + 1) + "/" + rootCount + " : " + javaFiles.size() + " file(s) found.", false);
		}

		if(javaFiles.isEmpty()) {
			setFailedMessage("No java files found in selected root(s).");
			isSuccessed = false;
			setResultData(new SpringScanResult(new ArrayList<String>(), "No java files to scan."));
			return;
		}

		setTotal(javaFiles.size());
		setCurrent(0);
		tasksPercent[0] = 0;
		tasksDetail[0] = "0/" + javaFiles.size();
		for(int i = 0; i < javaFiles.size(); i++) {
			checkCanceled();
			File jf = javaFiles.get(i);
			setDetailMessage("Discovering controller classes");
			setDetailSubMessage((i + 1) + "/" + javaFiles.size() + " class(es) checked", false);
			tasksPercent[0] = (int)(((i + 1) * 100L) / javaFiles.size());
			tasksDetail[0] = (i + 1) + "/" + javaFiles.size();
			addCurrent(1);
			if(SpringUriPatternExtractor.isLikelyControllerFile(jf)) {
				controllerFiles.add(jf);
			}
		}

		if(controllerFiles.isEmpty()) {
			setFailedMessage("No Spring controller classes found.");
			isSuccessed = false;
			setResultData(new SpringScanResult(new ArrayList<String>(), "No controller classes to scan."));
			return;
		}

		onSpringControllersDiscovered(controllerFiles.size());

		final StaticStringConstantCache stringConstantCache = new StaticStringConstantCache();
		final List<File> constantScanRoots = SpringProjectURIPatternExtractTask.rootsFromPaths(roots);

		setTotal(javaFiles.size() + controllerFiles.size());
		setCurrent(javaFiles.size());
		tasksPercent[0] = (int)(((long)javaFiles.size() * 100L) / (javaFiles.size() + controllerFiles.size()));
		tasksDetail[0] = "0/" + controllerFiles.size();

		List<String> allPatterns = new ArrayList<String>();
		StringBuilder failureLog = new StringBuilder();
		int totalControllerClasses = 0;

		for(int i = 0; i < controllerFiles.size(); i++) {
			checkCanceled();
			File f = controllerFiles.get(i);
			onSpringControllerScanProgress(i, controllerFiles.size(), f);
			setDetailMessage("Scanning controllers");
			setDetailSubMessage((i + 1) + "/" + controllerFiles.size(), false);
			tasksPercent[0] = (int)(((i + 1) * 100L) / controllerFiles.size());
			tasksDetail[0] = (i + 1) + "/" + controllerFiles.size();
			addCurrent(1);

			SpringUriPatternExtractor.ExtractResult er = SpringUriPatternExtractor.extract(f, contextPath, stringConstantCache, constantScanRoots);
			onSpringControllerFileExtracted(i, controllerFiles.size(), f, er);
			totalControllerClasses += (er != null ? er.controllerClassCount : 0);
			String pathLabel = pathRelativeToScanRoots(f, roots);
			for(SpringUriPatternExtractor.FailureDetail fd : er.failures) {
				if(failureLog.length() < MAX_FAILURE_LOG_LENGTH) {
					failureLog.append(fd.formatDisplayLine(pathLabel)).append('\n');
				}
			}
			allPatterns.addAll(er.patterns);
		}

		status[0] = STATUS_COMPLETE;
		tasksPercent[0] = 100;

		List<String> sorted = SpringUriPatternExtractor.sortAndDedupe(allPatterns);
		String summary = failureLog.length() > 0 ? failureLog.toString().trim() : "";
		setResultData(new SpringScanResult(sorted, summary, totalControllerClasses));
		if(totalControllerClasses > 0) {
			setDetailMessage("Done");
			setDetailSubMessage(sorted.size() + " pattern(s) found in " + totalControllerClasses + " controller(s)", false);
		} else {
			setDetailMessage("Failed");
			setDetailSubMessage("No valid controller classes were found. No patterns extracted.", false);
		}
	}

	public static List<File> rootsFromPaths(List<File> paths) {
		return paths == null ? new ArrayList<File>() : new ArrayList<File>(paths);
	}

	private static String pathRelativeToScanRoots(File file, List<File> roots) {
		if(file == null || roots == null || roots.isEmpty()) {
			return file != null ? file.getName() : "";
		}
		Path filePath;
		try {
			filePath = file.toPath().toAbsolutePath().normalize();
		} catch(Exception e) {
			return file.getName();
		}
		Path bestRoot = null;
		for(File root : roots) {
			if(root == null || !root.exists()) {
				continue;
			}
			Path rootPath;
			try {
				if(root.isFile()) {
					Path rf = root.toPath().toAbsolutePath().normalize();
					if(filePath.equals(rf)) {
						return rf.getFileName().toString().replace('\\', '/');
					}
					Path par = rf.getParent();
					if(par == null) {
						continue;
					}
					rootPath = par;
				} else {
					rootPath = root.toPath().toAbsolutePath().normalize();
				}
			} catch(Exception e) {
				continue;
			}
			if(!filePath.startsWith(rootPath)) {
				continue;
			}
			if(bestRoot == null || rootPath.getNameCount() > bestRoot.getNameCount()) {
				bestRoot = rootPath;
			}
		}
		if(bestRoot == null) {
			return file.getName();
		}
		try {
			String rel = bestRoot.relativize(filePath).toString().replace('\\', '/');
			return rel.isEmpty() ? file.getName() : rel;
		} catch(Exception e) {
			return file.getName();
		}
	}

	protected void onSpringControllersDiscovered(int controllerFileCount) {
	}

	protected void onSpringScanRootStarted(File root) {
	}

	protected void onSpringControllerScanProgress(int index, int total, File file) {
	}

	protected void onSpringControllerFileExtracted(int index, int total, File file, SpringUriPatternExtractor.ExtractResult result) {
	}

	@Override
	public String getCompletionSummaryForDialog() {
		Object o = getResultData();
		if(o instanceof SpringScanResult) {
			String s = ((SpringScanResult)o).getFailureSummary();
			return (s == null || s.trim().isEmpty()) ? null : s;
		}
		return null;
	}
}
