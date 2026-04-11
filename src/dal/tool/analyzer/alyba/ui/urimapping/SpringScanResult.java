package dal.tool.analyzer.alyba.ui.urimapping;

import java.util.List;

public class SpringScanResult {

	private final List<String> patterns;
	private final String failureSummary;
	private final int controllerClassCount;

	public SpringScanResult(List<String> patterns, String failureSummary) {
		this(patterns, failureSummary, 0);
	}

	public SpringScanResult(List<String> patterns, String failureSummary, int controllerClassCount) {
		this.patterns = patterns;
		this.failureSummary = failureSummary;
		this.controllerClassCount = controllerClassCount;
	}

	public List<String> getPatterns() {
		return patterns;
	}

	public String getFailureSummary() {
		return failureSummary;
	}

	public int getControllerClassCount() {
		return controllerClassCount;
	}
}
