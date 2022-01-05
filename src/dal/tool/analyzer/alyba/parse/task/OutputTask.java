package dal.tool.analyzer.alyba.parse.task;

import java.util.List;

import dal.tool.analyzer.alyba.output.LogAnalyzeOutput;
import dal.tool.analyzer.alyba.parse.parser.LogLineParser;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.swt.ProgressBarTask;

public class OutputTask extends ProgressBarTask {

	private LogAnalyzeOutput output;
	
	private List<String> generatedFiles = null;

	public <P extends LogLineParser> OutputTask(LogAnalyzeOutput output) {
		this.output = output;
		status = new int[output.getAnalyzerSetting().getOutputCount()+1];
		tasksPercent = new int[output.getAnalyzerSetting().getOutputCount()+1];
		tasksDetail = new String[output.getAnalyzerSetting().getOutputCount()+1];
		for(int i = 0; i < status.length; i++) {
			status[i] = STATUS_READY;
			tasksPercent[i] = 0;
		}
	}

	public List<String> getGeneratedFiles() {
		return generatedFiles;
	}

	public void doCancel() {
		Logger.debug("Unable to cancel");
	}

	public void doTask() throws Exception {
		generateOutput();
	}

	private void generateOutput() throws Exception {
		generatedFiles = null;
		try {
			setTotal(output.getAnalyzerSetting().getOutputCount()+1);
			setDetailMessage("Generating output files");
			generatedFiles = output.out(this);

			setDetailMessage("Output task is completed");
		} catch(Exception e) {
			Logger.debug(e);
		}
	}

}