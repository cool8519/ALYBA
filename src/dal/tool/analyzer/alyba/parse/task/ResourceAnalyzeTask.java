package dal.tool.analyzer.alyba.parse.task;

import java.io.File;
import java.util.List;

import dal.tool.analyzer.alyba.parse.FileInfo;
import dal.tool.analyzer.alyba.parse.FileReaderThread;
import dal.tool.analyzer.alyba.parse.parser.ResourceParser;
import dal.tool.analyzer.alyba.setting.ResourceAnalyzerSetting;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.swt.ProgressBarTask;
import dal.util.thread.ThreadManager;

public class ResourceAnalyzeTask extends ProgressBarTask {

	private ResourceAnalyzerSetting setting;
	
	private Class<ResourceParser> parserClass;
	private ResourceParser parser;
	private ThreadManager threadManager = null;

	public ResourceAnalyzeTask(ResourceAnalyzerSetting setting, Class<ResourceParser> parser_clazz) {
		this.setting = setting;
		this.parserClass = parser_clazz;
		status = new int[setting.getFileInfoList().size()];
		tasksPercent = new int[setting.getFileInfoList().size()];
		tasksDetail = new String[setting.getFileInfoList().size()];
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
		List<FileInfo> fileInfoList = setting.getFileInfoList();
		long totalBytes = 0L;

		setDetailMessage("Initializing counter");
		for(int i = 0; i < fileInfoList.size(); i++) {
			f = fileInfoList.get(i).getFile();
			totalBytes += f.length();
		}
		setTotal(totalBytes);

		setDetailMessage("Instantiating parsers");
		parser = (ResourceParser)parserClass.getConstructor(ResourceAnalyzerSetting.class).newInstance(setting);
		parser.initDatabase();

		try {
			FileReaderThread fileReader = new FileReaderThread(1);
			fileReader.setCaller(this);
			fileReader.setParser(parser);
			fileReader.setFiles(fileInfoList);
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

			setDetailMessage("Arranging final data");
			setDetailSubMessage("filling null-data");
			parser.fillWithNullTime(this);
			setDetailSubMessage("sorting data");
			parser.sortData();
			
			setDetailMessage("Generating database");
			setDetailSubMessage("writing data to db");
			if(parser.getTotalCount() > 0) {
				parser.writeDataToDB();
			} else {
				isSuccessed = false;
				setFailedMessage("No data to parse.");
				Logger.debug("No data to parse : count=" + parser.getTotalCount());
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