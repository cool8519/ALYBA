package dal.tool.analyzer.alyba.parse.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dal.tool.analyzer.alyba.parse.FileInfo;
import dal.tool.analyzer.alyba.parse.FileReaderThread;
import dal.tool.analyzer.alyba.parse.parser.TransactionCountParser;
import dal.tool.analyzer.alyba.setting.TPMAnalyzerSetting;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.swt.ProgressBarTask;
import dal.util.thread.ThreadManager;

public class TPMAnalyzeTask extends ProgressBarTask {

	private TPMAnalyzerSetting setting;
	
	private Class<TransactionCountParser> parserClass;
	private TransactionCountParser parser;
	private ThreadManager threadManager = null;

	public TPMAnalyzeTask(TPMAnalyzerSetting setting, Class<TransactionCountParser> parser_clazz) {
		this.setting = setting;
		this.parserClass = parser_clazz;
		status = new int[setting.getLogFileList().size()];
		tasksPercent = new int[setting.getLogFileList().size()];
		tasksDetail = new String[setting.getLogFileList().size()];
		for(int i = 0; i < status.length; i++) {
			status[i] = STATUS_READY;
			tasksPercent[i] = 0;
		}
	}

	public void doCancel() {
		if(threadManager != null) {
			threadManager.stopAllThreads();
			threadManager = null;
		}
		if(parser != null) {
			parser.cancel();
			Logger.debug("CANCEL called");
			parser = null;
		} else {
			Logger.debug("Nothing to cancel");
		}
	}

	public void doTask() throws Exception {
		parseFiles();
		if(!isCanceled) {
			makeUpResult();
		}
	}

	private void parseFiles() throws Exception {
		File f;
		List<String> fileList = setting.getLogFileList();
		long totalBytes = 0L;

		setDetailMessage("Initializing counter");
		for(int i = 0; i < fileList.size(); i++) {
			f = new File(fileList.get(i));
			totalBytes += f.length();
		}
		setTotal(totalBytes);

		setDetailMessage("Instantiating parsers");
		parser = (TransactionCountParser)parserClass.getConstructor(TPMAnalyzerSetting.class).newInstance(setting);
		parser.initDatabase();

		try {
			List<FileInfo> fileEntry_arr = new ArrayList<FileInfo>();
			for(int i = 0; i < fileList.size(); i++) {
				FileInfo fileEntry = new FileInfo(new File(fileList.get(i)));
				fileEntry_arr.add(fileEntry);
			}			
			FileReaderThread fileReader = new FileReaderThread(1);
			fileReader.setCaller(this);
			fileReader.setParser(parser);
			fileReader.setFiles(fileEntry_arr);
			setDetailMessage("Starting Single-thread parser");
			Thread.currentThread().setName("ThreadManager");
			threadManager = new ThreadManager(fileReader);
			threadManager.setMaxThread(1);
			threadManager.startAndWait();
			checkCanceled();
			if(threadManager.hasErrors()) {
				setFailedMessage(threadManager.getErrorMessages());
				taskFailed();
			} else {
				setDetailMessage("Finised the parser thread");
			}
			setDetailMessage("Parsing has been completed");
		} catch(Exception e) {
			Logger.debug(e);
			taskFailed();
			if(getFailedMessage() == null) {
				setFailedMessage(e.toString());
			}
		} finally {
			threadManager = null;			
		}
	}

	private void makeUpResult() throws Exception {
		try {
			setDetailMessage("Arranging result data");
			checkCanceled();
			setDetailSubMessage("sorting data");
			parser.sortData();

			setDetailMessage("Aggregating data from different criteria");
			checkCanceled();
			setDetailSubMessage("generating other data");
			parser.generateOtherData();
			
			setDetailMessage("Completing result data");
			checkCanceled();
			setDetailSubMessage("filling null-data");
			parser.fillWithNullTime(this);
			setDetailSubMessage("sorting data");
			parser.sortData();			
			setDetailSubMessage("setting total");
			parser.setTotalToResult();
			
			setDetailMessage("Generating database");
			setDetailSubMessage("writing data to db");
			if(parser.getTotalRequestCount() == 0) {
				isSuccessed = false;
				setFailedMessage("No data to parse.");
				Logger.debug("No data to parse : count=" + parser.getTotalRequestCount());
			} else {
				parser.writeDataToDB();
			}

			setDetailMessage("Clearing memory", "removing parsers");
			parser.closeDatabase();
			parser = null;

			setDetailMessage("Analyzer task is completed");
		} catch(Exception e) {
			Logger.debug("Failed to make up result for the parsers.");
			Logger.error(e);
		}
	}

}