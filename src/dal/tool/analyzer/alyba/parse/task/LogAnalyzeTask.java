package dal.tool.analyzer.alyba.parse.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.parse.FileInfo;
import dal.tool.analyzer.alyba.parse.FileReaderThread;
import dal.tool.analyzer.alyba.parse.parser.LogLineParser;
import dal.tool.analyzer.alyba.setting.LogAnalyzerSetting;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.swt.ProgressBarTask;
import dal.util.thread.ThreadManager;

public class LogAnalyzeTask extends ProgressBarTask {

	private LogAnalyzerSetting setting;
	
	@SuppressWarnings("rawtypes")
	private Class parserClass;
	private LogLineParser[] parser;
	private ThreadManager threadManager = null;

	public <P extends LogLineParser> LogAnalyzeTask(LogAnalyzerSetting setting, Class<P> parser_clazz) {
		this.setting = setting;
		this.parserClass = parser_clazz;
		status = new int[setting.getLogFileList().size()];
		tasksPercent = new int[setting.getLogFileList().size()];
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
			List<LogLineParser> parserList = Arrays.asList(parser);
			for(LogLineParser p : parserList) {
				p.cancel();
			}
			Logger.logln("CANCEL called");
			parser = null;
		} else {
			Logger.logln("Nothing to cancel");
		}
	}

	public void doTask() throws Exception {
		parseLogs();
		if(!isCanceled) {
			makeUpResult();
		}
	}

	@SuppressWarnings("unchecked")
	private <P extends LogLineParser> void parseLogs() throws Exception {
		File f;
		List<String> fileList = setting.getLogFileList();
		List<String> encodingList = setting.getLogFileEncodingList();
		long totalBytes = 0L;

		setDetailMessage("Initializing counter");
		for(int i = 0; i < fileList.size(); i++) {
			f = new File(fileList.get(i));
			totalBytes += f.length();
		}
		setTotal(totalBytes);

		setDetailMessage("Instanciating parsers");
		int threadCnt = setting.multiThreadParsing ? fileList.size() : 1;
		parser = new LogLineParser[threadCnt];
		for(int i = 0; i < threadCnt; i++) {
			parser[i] = (LogLineParser)parserClass.getConstructor(LogAnalyzerSetting.class).newInstance(setting);
			parser[i].initDatabase();
		}

		try {
			if(setting.multiThreadParsing) {
				FileReaderThread[] fileReaders = new FileReaderThread[threadCnt];
				for(int i = 0; i < fileList.size(); i++) {
					f = new File(fileList.get(i));
					fileReaders[i] = new FileReaderThread(i + 1);
					fileReaders[i].setCaller(this);
					fileReaders[i].setParser(parser[i]);
					fileReaders[i].setMaxErrorCount(parser[i].getSetting().allowErrorCount);
					FileInfo fileEntry = new FileInfo(f);
					fileEntry.setFileMeta("encoding", encodingList.get(i));
					fileReaders[i].setFile(fileEntry);
				}
				setDetailMessage("Starting Multi-thread parsers");
				Thread.currentThread().setName("ThreadManager");
				threadManager = new ThreadManager(fileReaders);
				threadManager.setMaxThread(Constant.MAX_THREAD_COUNT);
				threadManager.startAndWait();
				checkCanceled();
				if(threadManager.hasErrors()) {
					setFailedMessage(threadManager.getErrorMessages());
					taskFailed();
				} else {
					setDetailMessage("Finised all parser threads");
				}
			} else {
				List<FileInfo> fileEntry_arr = new ArrayList<FileInfo>();
				for(int i = 0; i < fileList.size(); i++) {
					FileInfo fileEntry = new FileInfo(new File(fileList.get(i)));
					fileEntry.setFileMeta("encoding", encodingList.get(i));
					fileEntry_arr.add(fileEntry);
				}
				FileReaderThread fileReader = new FileReaderThread(1);
				fileReader.setCaller(this);
				fileReader.setParser(parser[0]);
				fileReader.setMaxErrorCount(parser[0].getSetting().allowErrorCount);
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
			setDetailMessage("Truncating result data");
			List<LogLineParser> parserList = Arrays.asList(parser);
			int size = parserList.size();
			LogLineParser p = null;
			for(int i = 0; i < size; i++) {
				p = (LogLineParser)parserList.get(i);
				setDetailSubMessage("checking " + (i+1) + "/" + size);
				if(p.isFailed()) {
					parserList.remove(p);
				}
			}
			size = parserList.size();

			setDetailMessage("Arranging result data");
			for(int i = 0; i < size; i++) {
				checkCanceled();
				p = parserList.get(i);
				setDetailSubMessage("sorting data");
				p.sortData();
				setDetailSubMessage("setting total");
				p.setTotalToResult();
			}

			setDetailMessage("Collecting result data");
			LogLineParser firstParser = parserList.get(0);
			for(int i = 1; i < size; i++) {
				checkCanceled();
				setDetailSubMessage("merging " + i + "/" + (size-1));
				firstParser.mergeData(parserList.get(i));
			}

			setDetailMessage("Arranging final data");
			setDetailSubMessage("filling null-data");
			firstParser.fillWithNullTime(this);
			setDetailSubMessage("sorting data");
			firstParser.sortData();
			setDetailSubMessage("setting total");
			firstParser.setTotalToResult();

			setDetailMessage("Generating database");
			setDetailSubMessage("writing data to db");
			if(firstParser.getFilteredRequestCount() > 0) {
				firstParser.writeDataToDB();
			} else {
				isSuccessed = false;
				setFailedMessage("No request to parse.");
				Logger.logln("No request to parse : total=" + firstParser.getTotalRequestCount() + ", filtered= " + firstParser.getFilteredRequestCount());
			}

			setDetailMessage("Clearing memory", "removing parsers");
			for(int i = 0; i < parser.length; i++) {
				parser[i].closeDatabase();
				parser[i] = null;
			}			
			parserList = null;
			firstParser = null;
			this.parser = null;

			setDetailMessage("Analyzer task is completed");
		} catch(Exception e) {
			Logger.debug(e);
		}
	}

}