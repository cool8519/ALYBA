package dal.tool.analyzer.alyba.parse.parser;

import java.util.Date;

import dal.tool.analyzer.alyba.parse.FileInfo;
import dal.tool.analyzer.alyba.parse.FileReaderThread;

public abstract class FileLineParser {

	protected FileReaderThread thread = null;
	protected boolean isFailed = false;
	protected boolean isCanceled = false;
	protected Date parsed_time = null;

	public FileLineParser() throws Exception {
		this.parsed_time = new Date();
	}
	
	public FileReaderThread getThread() {
		return thread;
	}
	
	public void setThread(FileReaderThread thread) {
		this.thread = thread;
	}
	
	public boolean isFailed() {
		return isFailed;
	}

	public void setFailed() {
		isFailed = true;
	}

	public boolean isCanceled() {
		return isCanceled;
	}
	
	public void cancel() {
		isCanceled = true;
	}
	
	public Date getParsedTime() {
		return parsed_time;
	}

	public abstract void parseLine(String line, FileInfo fileInfo) throws Exception;
	
}
