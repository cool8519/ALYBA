package dal.util.thread;

import dal.util.log.Logger;

public abstract class Task extends Thread {

	protected final int id;
	protected ThreadManager manager;
	protected boolean hasError = false;
	protected String errorMessage = null;
	protected boolean startFlag = false;
	protected boolean stopFlag = false;
	protected boolean completeFlag = false;

	private String runningUnit = "COUNT";
	private int thinkTime = 0;
	private int runningNumber = 1;
	private int maxErrorCount = 0;
	private boolean printElapsedTimeInTask = false;
	private boolean printJVMMemoryInTask = false;

	private Runtime rt = Runtime.getRuntime();
	private int totalCount = 0;
	private int errorCount = 0;
	private long elapsedTime = 0L;

	public Task(int id) {
		this.id = id;
		setName("TaskThread-" + id);
	}

	public void setThreadManager(ThreadManager manager) {
		this.manager = manager;
	}

	public boolean hasError() {
		return hasError;
	}

	public void setError() {
		hasError = true;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void addErrorMessage(String msg) {
		if(errorMessage == null) {
			errorMessage = msg;
		} else {
			errorMessage += ("|" + msg);
		}
	}

	public void setRunning(String runningUnit, int runningNumber) {
		this.runningUnit = runningUnit;
		this.runningNumber = runningNumber;
	}

	public void setThinkTime(int thinkTime) {
		this.thinkTime = thinkTime;
	}

	public void setMaxErrorCount(int maxErrorCount) {
		this.maxErrorCount = maxErrorCount;
	}

	public void setPrintElapsedTimeInTask(boolean printElapsedTimeInTask) {
		this.printElapsedTimeInTask = printElapsedTimeInTask;
	}

	public void setPrintJVMMemoryInTask(boolean printJVMMemoryInTask) {
		this.printJVMMemoryInTask = printJVMMemoryInTask;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public long getElapsedTimeWithThinkTime() {
		if(totalCount > 0)
			return elapsedTime + ((totalCount - 1) * thinkTime);
		else
			return elapsedTime;
	}

	public float getTPS() {
		if(totalCount > 0)
			return ((float)totalCount / ((float)getElapsedTimeWithThinkTime() / 1000));
		else
			return 0;
	}

	public void stopThread() throws Exception {
		stopFlag = true;
		if(startFlag) {
			interrupt();
		}
	}

	public boolean isStarted() {
		return startFlag;
	}

	public boolean isStopped() {
		return stopFlag;
	}
	
	public boolean isCompleted() {
		return completeFlag;
	}

	public void run() {
		if(stopFlag) {
			return;
		}
		try {
			startFlag = true;
			preStart();
		} catch(Exception e) {
			Logger.log(this.getClass(), Logger.FATAL, "Thread-" + id + " : Exception in preStart() : " + e.getMessage());
		}
		Logger.log(this.getClass(), Logger.DEBUG, "Thread-" + id + " start");
		long start = System.currentTimeMillis();
		if(runningUnit.equals("TIME")) {
			while(!stopFlag && (runningNumber < 0 || (System.currentTimeMillis() - start) < (runningNumber * 1000))) {
				doTaskInLoop();
				if(errorCount > -1 && errorCount > maxErrorCount) {
					break;
				}
				if(thinkTime > 0) {
					try {
						Thread.sleep(thinkTime);
					} catch(Exception e) {
					}
				}
			}
		} else if(runningUnit.equals("COUNT")) {
			while(!stopFlag && (runningNumber < 0 || runningNumber > totalCount)) {
				doTaskInLoop();
				if(errorCount > -1 && errorCount > maxErrorCount) {
					break;
				}
				if(thinkTime > 0 && (runningNumber < 0 || runningNumber > totalCount)) {
					try {
						Thread.sleep(thinkTime);
					} catch(Exception e) {
					}
				}
			}
		}
		if(!stopFlag) {
			Logger.log(this.getClass(), Logger.DEBUG, "Thread-" + id + " stop");
			try {
				preStop();
			} catch(Exception e) {
				Logger.log(this.getClass(), Logger.FATAL, "Thread-" + id + " : Exception in preStop() : " + e.getMessage());
			}
			manager.completeThread(this);
			completeFlag = true;
		}
	}

	private void doTaskInLoop() {
		try {
			long elapsedTimeInTask = System.currentTimeMillis();
			doTask();
			long elapsedTemp = (System.currentTimeMillis() - elapsedTimeInTask);
			elapsedTime += elapsedTemp;
			totalCount++;
			if(printElapsedTimeInTask) {
				Logger.log(this.getClass(), Logger.INFO, "Thread-" + id + " : " + elapsedTemp + "ms elapsed (" + totalCount + "th)");
			}
			if(printJVMMemoryInTask) {
				long freeMem = rt.freeMemory();
				long totalMem = rt.totalMemory();
				Logger.log(this.getClass(), Logger.INFO, "Thread-" + id + " : Memory(Free)=" + freeMem + "/" + totalMem + " bytes");
			}
		} catch(Exception e) {
			if(!(e instanceof InterruptedException)) {
				errorCount++;
				Logger.log(this.getClass(), Logger.FATAL, "Thread-" + id + " : Exception : " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public abstract void preStart() throws Exception;

	public abstract void doTask() throws Exception;

	public abstract void preStop() throws Exception;

}
