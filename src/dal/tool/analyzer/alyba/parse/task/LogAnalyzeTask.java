package dal.tool.analyzer.alyba.parse.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
			List<LogLineParser> parserList = Arrays.asList(parser);
			for(LogLineParser p : parserList) {
				p.cancel();
			}
			Logger.debug("CANCEL called");
			parser = null;
		} else {
			Logger.debug("Nothing to cancel");
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
					fileReaders[i].setMaxErrorCount(parser[i].getSetting().allowErrors ? parser[i].getSetting().allowErrorCount : -1);
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
				fileReader.setMaxErrorCount(parser[0].getSetting().allowErrors ? parser[0].getSetting().allowErrorCount : -1);
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
			LogLineParser p = null;
			for(int i = 0; i < parserList.size(); i++) {
				p = (LogLineParser)parserList.get(i);
				setDetailSubMessage("checking " + (i+1) + "/" + parserList.size());
				if(p.isFailed()) {
					parserList.remove(p);
				}
			}

			setDetailMessage("Arranging result data");
			for(int i = 0; i < parserList.size(); i++) {
				checkCanceled();
				p = parserList.get(i);
				setDetailSubMessage("sorting data");
				p.sortData();
				setDetailSubMessage("setting total");
				p.setTotalToResult();
			}

			setDetailMessage("Collecting result data");
			LogLineParser mergedParser = mergeParser(parserList);
			
			setDetailMessage("Arranging final data");
			setDetailSubMessage("filling null-data");
			mergedParser.fillWithNullTime(this);
			setDetailSubMessage("sorting data");
			mergedParser.sortData();
			setDetailSubMessage("setting total");
			mergedParser.setTotalToResult();

			setDetailMessage("Generating database");
			setDetailSubMessage("writing data to db");
			if(mergedParser.getFilteredRequestCount() > 0) {
				mergedParser.writeDataToDB();
			} else {
				isSuccessed = false;
				setFailedMessage("No request to parse.");
				Logger.debug("No request to parse : total=" + mergedParser.getTotalRequestCount() + ", filtered=" + mergedParser.getFilteredRequestCount());
			}

			setDetailMessage("Clearing memory", "removing parsers");
			for(int i = 0; i < parser.length; i++) {
				parser[i].closeDatabase();
				parser[i] = null;
			}			
			parserList = null;
			mergedParser = null;
			this.parser = null;

			setDetailMessage("Analyzer task is completed");
		} catch(Exception e) {
			Logger.debug("Failed to make up result for the parsers.");
			Logger.error(e);
		}
	}

	private LogLineParser mergeParser(List<LogLineParser> parserList) throws Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(Constant.MAX_THREAD_COUNT);
		try {
			class MergeTask implements Callable<LogLineParser> {
				private LogLineParser p1;
				private LogLineParser p2;
				public MergeTask(LogLineParser p1, LogLineParser p2) {
					this.p1 = p1;
					this.p2 = p2;
				}
				public LogLineParser call() throws Exception {
					p1.mergeData(p2);
					return p1;
				}
			}
			Queue<LogLineParser> parserQueue = new LinkedList<LogLineParser>(parserList);
			Queue<Future<LogLineParser>> futureQueue = new LinkedList<Future<LogLineParser>>();
			while(parserQueue.size() > 1) {
				setDetailSubMessage("merging from " + parserQueue.size() + " to " + (int)Math.ceil((double)parserQueue.size()/2));
				while(parserQueue.size() > 1) {
					checkCanceled();
					LogLineParser p1 = parserQueue.poll();
					if(parserQueue.isEmpty()) {
						parserQueue.offer(p1);
					} else {
						LogLineParser p2 = parserQueue.poll();
						futureQueue.offer(executorService.submit(new MergeTask(p1, p2)));
					}
				}
				while(!futureQueue.isEmpty()) {
					checkCanceled();
					Future<LogLineParser> f = futureQueue.poll();
					parserQueue.offer(f.get());
				}
			}
			return parserQueue.poll();
		} finally {
			executorService.shutdownNow();
		}		
	}

	@SuppressWarnings("unused")
	private LogLineParser mergeParser2(List<LogLineParser> parserList) throws Exception {
		ExecutorService threadPool = Executors.newFixedThreadPool(Constant.MAX_THREAD_COUNT);
		BlockingQueue<LogLineParser> parserQueue = new LinkedBlockingQueue<LogLineParser>(parserList);
		Queue<Future<LogLineParser>> futureQueue = new LinkedList<Future<LogLineParser>>();
		
		class MergeTask implements Callable<LogLineParser> {
			private LogLineParser p1;
			private LogLineParser p2;
			public MergeTask(LogLineParser p1, LogLineParser p2) {
				this.p1 = p1;
				this.p2 = p2;
			}
			public LogLineParser call() throws Exception {
				p1.mergeData(p2);
				return p1;
			}
		}
		
		class ResultChecker implements Runnable {
			private int maxMergeCnt;
			private boolean flag = true;
			public ResultChecker(int count) { this.maxMergeCnt = count; }
			@Override
			public void run() {
				int mergeCnt = 0;
				setDetailSubMessage("merged parser : " + mergeCnt + " / " + maxMergeCnt);
				while(flag) {
					if(futureQueue.isEmpty()) {
						try {
							Thread.sleep(100L);
						} catch(InterruptedException e) {
							flag = false;
							break;
						}
						continue;
					}
					Future<LogLineParser> f = futureQueue.poll();
					try {
						LogLineParser parser = f.get(500L, TimeUnit.MILLISECONDS);
						parserQueue.offer(parser);
						mergeCnt++;
						setDetailSubMessage("merged parser : " + mergeCnt + " / " + maxMergeCnt);
						if(mergeCnt == maxMergeCnt) {
							flag = false;							
						}
					} catch(TimeoutException toe) {
						futureQueue.offer(f);
					} catch(InterruptedException ie) {
						flag = false;
					} catch(Exception e) {
						e.printStackTrace();
					}						
				}
			}				
		}
		
		int maxMergeCount = parserList.size() - 1;
		int maxTakeCount = parserList.size() * 2 - 1;
		ResultChecker resultChecker = new ResultChecker(maxMergeCount);
		new Thread(resultChecker).start();
		LogLineParser resultParser = null;

		try {			
			int takeCnt = 0;
			while(true) {
				LogLineParser p1 = parserQueue.take();
				takeCnt++;
				if(takeCnt == maxTakeCount) {
					resultParser = p1;
					break;		
				}
				LogLineParser p2 = parserQueue.take();
				takeCnt++;
				futureQueue.offer(threadPool.submit(new MergeTask(p1, p2)));
			}
			return resultParser;
		} finally {
			resultChecker.flag = false;
			threadPool.shutdownNow();
		}
	}

}