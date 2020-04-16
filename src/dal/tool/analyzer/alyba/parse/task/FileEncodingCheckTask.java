package dal.tool.analyzer.alyba.parse.task;

import java.io.File;
import java.util.List;
import java.util.Map;

import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.FileUtil;
import dal.util.swt.ProgressBarTask;

public class FileEncodingCheckTask extends ProgressBarTask {

	private List<File> fileList;
	private Map<String,String> fileEncodingMap;
	private boolean stopFlag = false;

	public FileEncodingCheckTask(List<File> files, Map<String,String> fileEncodings) {
		this.fileList = files;
		this.fileEncodingMap = fileEncodings;
		status = new int[files.size()];
		tasksPercent = new int[files.size()];
		for(int i = 0; i < status.length; i++) {
			String encoding = fileEncodingMap.get(fileList.get(i).getPath());
			if(encoding == null) {
				status[i] = STATUS_READY;
				tasksPercent[i] = 0;
			} else {
				status[i] = STATUS_COMPLETE;
				tasksPercent[i] = 100;
				addCurrent(1);
			}
		}
		setTotal(files.size());
	}

	public int getFileCount() {
		return (fileList == null) ? 0 : fileList.size();
	}
	
	public void doCancel() {
		Logger.logln("CANCEL called");
		stopFlag = true;
	}

	public void doTask() throws Exception {
		if(getFileCount() < 1) {
			Logger.logln("Nothing to do");
			return;
		}
		for(int i = 0; i < fileList.size(); i++) {
			if(stopFlag) {
				break;
			}
			if(status[i] == STATUS_COMPLETE) {
				continue;
			}
			String filepath = fileList.get(i).getPath();
			setDetailStatus(i, ProgressBarTask.STATUS_PROCESSING);
			try {
				setDetailMessage("Checking file encoding : " + fileList.get(i).getName());
				checkFileEncoding(filepath);
				setDetailStatus(i, ProgressBarTask.STATUS_COMPLETE);
				addCurrent(1);
				setTasksPercent(i, 100);
			} catch(Exception e) {
				setDetailStatus(i, ProgressBarTask.STATUS_ERROR);
			}
		}
		setResultData(fileEncodingMap);
	}

	private void checkFileEncoding(String filepath) {
		String encoding = fileEncodingMap.get(filepath);
		if(encoding == null) {
			encoding = FileUtil.getFileEncoding(filepath);
			if(encoding == null) {
				encoding = "NULL";
			} else if("WINDOWS-1252".equals(encoding)) {
				String default_encoding = System.getProperty("file.encoding");
				Logger.debug("Unknown file encoding : " + encoding + ". It will be set to default(" + default_encoding + ")");
				encoding = default_encoding;
			}			
			fileEncodingMap.put(filepath, encoding);
		}
		Logger.debug("File encoding : path='" + filepath + "', encoding=" + encoding);
	}
	
}