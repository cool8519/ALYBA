package dal.tool.analyzer.alyba.task;

import java.util.List;

import dal.tool.analyzer.alyba.output.AnalyzeOutput;
import dal.tool.analyzer.alyba.parser.LogLineParser;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.swt.ProgressBarTask;

public class OutputTask extends ProgressBarTask {

	private AnalyzeOutput output;
	
	private List<String> generatedFiles = null;

	public <P extends LogLineParser> OutputTask(AnalyzeOutput output) {
		this.output = output;
		status = new int[output.getAnalyzerSetting().getOutputCount()+1];
		tasksPercent = new int[output.getAnalyzerSetting().getOutputCount()+1];
		for(int i = 0; i < status.length; i++) {
			status[i] = STATUS_READY;
			tasksPercent[i] = 0;
		}
	}

	public List<String> getGeneratedFiles() {
		return generatedFiles;
	}

	public void doCancel() {
		Logger.logln("Unable to cancel");
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