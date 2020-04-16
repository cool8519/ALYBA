package dal.tool.analyzer.alyba.parse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import dal.tool.analyzer.alyba.parse.parser.FileLineParser;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.swt.ProgressBarTask;
import dal.util.thread.Task;

public class FileReaderThread extends Task {

	private ProgressBarTask caller;
	private List<FileInfo> files;
	private FileLineParser parser;
	private int maxErrorCnt = -1;
	private boolean checkErrorCnt = false;

	public FileReaderThread(int id) {
		super(id);
	}

	public void setFile(FileInfo file) {
		this.files = new ArrayList<FileInfo>(1);
		this.files.add(file);
	}

	public void setFiles(List<FileInfo> files) {
		this.files = files;
	}

	public void setCaller(ProgressBarTask caller) {
		this.caller = caller;
	}

	public void setParser(FileLineParser parser) {
		this.parser = parser;
		this.parser.setThread(this);
	}
	
	public void setMaxErrorCount(int count) {
		this.maxErrorCnt = count;
		this.checkErrorCnt = (count > -1);
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

		for(int i = 0; i < files.size(); i++) {
			FileInfo fileInfo = files.get(i);
			caller.setDetailStatus(i, ProgressBarTask.STATUS_PROCESSING);
			if(files.size() > 1) {
				caller.setDetailMessage("Parsing a file #" + (i + 1));
			}

			try {
				String line;
				String fileEncoding = fileInfo.getFileMeta("encoding");
				if(fileEncoding == null || fileEncoding.equals("NULL")) {
					br = new BufferedReader(new InputStreamReader(new FileInputStream(fileInfo.getFile())));
				} else {
					br = new BufferedReader(new InputStreamReader(new FileInputStream(fileInfo.getFile()), fileEncoding));
				}
				lr = new LineNumberReader(br);
				int lineCnt = 0;
				int errCnt = 0;
				int lineBytes = 0;
				long totalBytes = fileInfo.getFile().length();
				long currentBytes = 0L;

				while((line = lr.readLine()) != null) {
					lineCnt = lr.getLineNumber();
					lineBytes = line.getBytes().length + 2;
					currentBytes += lineBytes;
					if(line.length() == 0)
						continue;
					try {
						parser.parseLine(line, fileInfo);
						caller.addCurrent(lineBytes);
						caller.setTasksPercent(i, (int)((double)currentBytes / totalBytes * 100));
						if(stopFlag) {
							throw new InterruptedException();
						}
					} catch(InterruptedException ie) {
						Logger.logln("Interrupted thread - " + getName());
						break;
					} catch(Exception e) {
						Logger.logln("Fail to parse line " + lineCnt + " (" + fileInfo.getFilePath() + ") : " + e.getMessage());
						Logger.debug(e);
						errCnt++;
						if(checkErrorCnt && maxErrorCnt > 0 && errCnt > maxErrorCnt) {
							break;
						}
					}
				}

				if(!isInterrupted()) {
					if(checkErrorCnt && maxErrorCnt > 0 && errCnt > maxErrorCnt) {
						setError();
						parser.setFailed();
						addErrorMessage("Exceed allowed errors");
						caller.setDetailStatus(i, ProgressBarTask.STATUS_ERROR);
						caller.setDetailMessage("Stopped parsing file #" + (i + 1));
						Logger.logln("Exceed allowed errors per file(" + maxErrorCnt + "). Check if file format is valid : " + fileInfo.getFile().getName());
					} else {
						caller.setDetailStatus(i, ProgressBarTask.STATUS_COMPLETE);
						if(files.size() > 1) {
							caller.setDetailMessage("Complete parsing file #" + (i + 1));
						}
					}
				}

			} catch(Exception e) {
				setError();
				addErrorMessage(e.toString());
				caller.setDetailStatus(i, ProgressBarTask.STATUS_ERROR);
				Logger.logln("Fail to read file : " + fileInfo.getFile().getName());
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
