package dal.tool.analyzer.alyba.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

import dal.tool.analyzer.alyba.task.AnalyzeTask;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.thread.Task;

public class LogReaderThread extends Task {

	private AnalyzeTask caller;
	private File[] logfiles;
	private LogLineParser parser;
	private int[] fileIdxs;
	private int allowErrorCnt;
	private boolean checkErrorCnt;

	public LogReaderThread(int id) {
		super(id);
	}

	public void setLogfile(File logfile) {
		this.logfiles = new File[] { logfile };
	}

	public void setLogfiles(File[] logfiles) {
		this.logfiles = logfiles;
	}

	public void setLogfileIndex(int fileIdx) {
		this.fileIdxs = new int[] { fileIdx };
	}

	public void setLogfileIndexes(int[] fileIdxs) {
		this.fileIdxs = fileIdxs;
	}

	public void setCaller(AnalyzeTask caller) {
		this.caller = caller;
	}

	public void setParser(LogLineParser parser) {
		this.parser = parser;
		this.parser.setThread(this);
		this.allowErrorCnt = parser.getSetting().allowErrorCount;
		this.checkErrorCnt = parser.getSetting().allowErrors;
	}

	public void preStart() throws Exception {
		// task before the thread starts
		// Ex) Warm up through loading data in memory
	}

	public void preStop() throws Exception {
		// task before the thread stops
	}

	public void doTask() throws Exception {
		// customize the task

		BufferedReader br = null;
		LineNumberReader lr = null;

		for(int i = 0; i < logfiles.length; i++) {
			caller.setDetailStatus(fileIdxs[i], AnalyzeTask.STATUS_PROCESSING);
			if(logfiles.length > 1) {
				caller.setDetailMessage("Parsing a logfile #" + (i + 1));
			}

			try {
				String line;
				br = new BufferedReader(new FileReader(logfiles[i]));
				lr = new LineNumberReader(br);
				int lineCnt = 0;
				int errCnt = 0;
				int lineBytes = 0;
				long totalBytes = logfiles[i].length();
				long currentBytes = 0L;

				while((line = lr.readLine()) != null) {
					lineCnt = lr.getLineNumber();
					lineBytes = line.getBytes().length + 2;
					currentBytes += lineBytes;
					if(line.length() == 0)
						continue;
					try {
						parser.parseLine(line);
						caller.addCurrent(lineBytes);
						caller.setTasksPercent(fileIdxs[i], (int)((double)currentBytes / totalBytes * 100));
						if(stopFlag) {
							throw new InterruptedException();
						}
					} catch(InterruptedException ie) {
						Logger.logln("Interrupted thread - " + getName());
						break;
					} catch(Exception e) {
						Logger.logln("Fail to parse log line " + lineCnt + " (" + logfiles[i] + ") : " + e.getMessage());
						Logger.debug(e);
						errCnt++;
						if(checkErrorCnt && allowErrorCnt > 0 && errCnt > allowErrorCnt) {
							break;
						}
					}
				}

				if(!isInterrupted()) {
					if(checkErrorCnt && allowErrorCnt > 0 && errCnt > allowErrorCnt) {
						setError();
						parser.setFailed();
						addErrorMessage("Exceed allowed errors");
						caller.setDetailStatus(fileIdxs[i], AnalyzeTask.STATUS_ERROR);
						caller.setDetailMessage("Stopped parsing logfile #" + (fileIdxs[i] + 1));
						Logger.logln("Exceed allowed errors per file(" + allowErrorCnt + "). Check if log-format is valid : " + logfiles[i].getName());
					} else {
						caller.setDetailStatus(fileIdxs[i], AnalyzeTask.STATUS_COMPLETE);
						if(logfiles.length > 1) {
							caller.setDetailMessage("Complete parsing logfile #" + (fileIdxs[i] + 1));
						}
					}
				}

			} catch(Exception e) {
				setError();
				addErrorMessage(e.toString());
				caller.setDetailStatus(fileIdxs[i], AnalyzeTask.STATUS_ERROR);
				Logger.logln("Fail to read log file : " + logfiles[i].getName());
				Logger.debug(e);
			} finally {
				if(br != null) {
					try {
						br.close();
					} catch(Exception e) {
					}
				}
			}
		}

	}

}
