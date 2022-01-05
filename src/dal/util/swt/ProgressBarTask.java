package dal.util.swt;

import java.text.DecimalFormat;

import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.StringUtil;

public abstract class ProgressBarTask implements Runnable {

	public static final int STATUS_READY = 0;
	public static final int STATUS_PROCESSING = 1;
	public static final int STATUS_COMPLETE = 2;
	public static final int STATUS_ERROR = 3;

	protected static DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.#");

	protected int[] status = null;
	protected int[] tasksPercent = null;
	protected String[] tasksDetail = null;	
	protected long total = 0;
	protected long current = 0;
	protected int percent = 0;
	protected String percentMessage = "";
	protected String timeMessage = "";
	protected String detailMessage = "";
	protected String detailSubMessage = "";
	protected boolean isComplete = false;
	protected boolean isCanceled = false;
	protected boolean isSuccessed = true;
	protected String failedMessage = null;
	protected long startTime;
	protected Object resultData = null;

	public abstract void doTask() throws Exception;

	public abstract void doCancel();

	public void run() {
		startTime = System.currentTimeMillis();
		try {
			doTask();
		} catch(InterruptedException ie) {
			Logger.debug("Interrupted thread during the task.");
		} catch(Exception e) {
			isSuccessed = false;
			if(failedMessage == null) {
				failedMessage = e.toString();
			}
			Logger.debug("Failed to complete the task : " + failedMessage);
			Logger.error(e);
		}
		if(!isCanceled) {
			isComplete = true;
		}
	}

	public void cancel() {
		doCancel();
		isCanceled = true;
		isSuccessed = false;
		failedMessage = "Task has been canceled by user.";
	}

	public Object getResultData() {
		return resultData;
	}
	
	public void setResultData(Object data) {
		resultData = data;
	}
	
	public int[] getDetailStatus() {
		return status;
	}

	public void setDetailStatus(int idx, int idx_status) {
		status[idx] = idx_status;
	}

	public int[] getTasksPercent() {
		return tasksPercent;
	}

	public void setTasksPercent(int idx, int taskPercent) {
		tasksPercent[idx] = (taskPercent<0) ? 0 : ((taskPercent>100)?100:taskPercent);
	}

	public String[] getTasksDetail() {
		return tasksDetail;
	}

	public void setTasksDetail(int idx, String taskDetail) {
		tasksDetail[idx] = taskDetail;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getCurrent() {
		return current;
	}

	public void setCurrent(long current) {
		this.current = current;
	}

	public void addCurrent(long offset) {
		this.current += offset;
	}

	public double getPercent() {
		return ((double)current / total) * 100;
	}

	public String getTotalPercentMessage() {
		return percentMessage;
	}

	public void setTotalPercentMessage(String msg) {
		this.percentMessage = msg;
	}

	public String getTotalTimeMessage() {
		return timeMessage;
	}

	public void setTotalTimeMessage(String msg) {
		this.timeMessage = msg;
	}

	public String getDetailMessage() {
		if(detailSubMessage != null && detailSubMessage.length() > 0) {
			return detailMessage + " : " + detailSubMessage;
		} else {
			return detailMessage;
		}
	}

	public void setDetailMessage(String msg) {
		setDetailMessage(msg, null);
	}
	
	public void setDetailMessage(String msg, String sub_msg) {
		this.detailMessage = msg;
		if(sub_msg == null) {
			Logger.debug(detailMessage);
		} else {
			setDetailSubMessage(sub_msg);
		}
	}
	
	public void setDetailSubMessage(String msg) {
		setDetailSubMessage(msg, true);
	}

	public void setDetailSubMessage(String msg, boolean logging) {
		this.detailSubMessage = msg;
		if(logging) {
			Logger.debug(detailMessage + " : " + msg);
		}
	}

	public boolean isComplete() {
		return isComplete;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public boolean isSuccessed() {
		return isSuccessed;
	}

	public void taskFailed() throws InterruptedException {
		isSuccessed = false;
		throw new InterruptedException("Error : send interrupt signal");
	}

	public String getFailedMessage() {
		return failedMessage;
	}

	public void setFailedMessage(String errorMessage) {
		this.failedMessage = errorMessage;
	}

	public void checkCanceled() throws InterruptedException {
		if(isCanceled) {
			throw new InterruptedException("Canceled : send interrupt signal");
		}
	}

	protected void updatePercentAndTimeMessage() {
		try {
			String total_str = "Unknown";
			String elapsed_str = "Unknown";
			long elapsed = System.currentTimeMillis() - startTime;
			double percent = (getPercent() > 100D) ? 100D : getPercent();
			double round_percent = Math.round(percent * 10) / 10.0;
			int elaped_sec = (int)Math.round((double)elapsed / 1000);
			elapsed_str = StringUtil.expressSecondsAsTime(elaped_sec);
			if(round_percent >= 100) {
				total_str = StringUtil.expressSecondsAsTime(elaped_sec);
			} else {
				if(round_percent != 0) {
					int total_sec = (int)Math.round(((((double)elapsed * 100) / percent - elapsed) / 1000)) + elaped_sec;
					total_str = StringUtil.expressSecondsAsTime(total_sec);
				}
			}
			setTotalTimeMessage("[" + elapsed_str + "/" + total_str + "]");
			setTotalPercentMessage("Total " + PERCENT_FORMAT.format(percent) + "% complete");
		} catch(Exception e) {
			Logger.debug("Failed to update percent and time.");
			Logger.error(e);
		}
	}

}