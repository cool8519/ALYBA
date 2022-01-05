package dal.util.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import dal.util.log.Logger;

public class ThreadManager {

	protected BlockingQueue<Task> waitingJobs;
	protected List<Task> doingJobs;
	protected List<Task> completedJobs;

	protected int maxThreadCount = -1;
	protected int startInterval = 0;
	protected int taskCnt = 0;
	protected int completeCount = 0;
	protected int totalCount = 0;
	protected long elapsedTime = 0L;
	protected long elapsedTime2 = 0L;
	protected float tps = 0F;

	public ThreadManager(Task task) throws Exception {
		this.waitingJobs = new LinkedBlockingQueue<Task>();
		this.doingJobs = new ArrayList<Task>();
		this.completedJobs = new ArrayList<Task>();
		this.taskCnt = 1;
		for(int i = 0; i < taskCnt; i++) {
			task.setThreadManager(this);
			waitingJobs.add(task);
		}
	}

	public ThreadManager(Task[] tasks) throws Exception {
		this.waitingJobs = new LinkedBlockingQueue<Task>();
		this.doingJobs = new ArrayList<Task>();
		this.completedJobs = new ArrayList<Task>();
		this.taskCnt = tasks.length;
		for(int i = 0; i < taskCnt; i++) {
			tasks[i].setThreadManager(this);
			waitingJobs.add(tasks[i]);
		}
	}

	public void setMaxThread(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
	}

	public void setStartInterval(int interval) {
		this.startInterval = interval;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public long getElapsedTimeWithThinkTime() {
		return elapsedTime2;
	}

	public float getTPS() {
		return tps;
	}

	public synchronized void completeThread(Task task) {
		doingJobs.remove(task);
		completedJobs.add(task);
		completeCount++;
		if(task.isInterrupted() || task.hasError()) {
			Logger.log(this.getClass(), Logger.INFO, "The Thread(" + task.getName() + ") has been interrupted. The ThreadManager will be stopped.");
			this.notify();
			return;
		}
		Logger.log(this.getClass(), Logger.INFO, "The Thread(" + task.getName() + ") has been completed. Complete task count : " + completeCount);
		if(completeCount == taskCnt) {
			Logger.log(this.getClass(), Logger.DEBUG, "All threads has been completed. The main-thread will be notified.");
			for(int i = 0; i < taskCnt; i++) {
				totalCount += task.getTotalCount();
				elapsedTime += task.getElapsedTime();
				elapsedTime2 += task.getElapsedTimeWithThinkTime();
				tps += task.getTPS();
			}
			this.notify();
		} else {
			startTask();
		}
	}

	public void start() {
		for(int i = 0; i < maxThreadCount; i++) {
			startTask();
		}
	}

	protected void startTask() {
		Task nextTask = waitingJobs.poll();
		if(nextTask != null && !nextTask.isStopped()) {
			if(startInterval > 0) {
				try {
					Thread.sleep(startInterval);
				} catch(Exception e) {
				}
			}
			try {
				doingJobs.add(nextTask);
			} catch(Exception e) {
			}
			nextTask.start();
		}
	}

	public synchronized void startAndWait() throws Exception {
		start();
		Logger.log(this.getClass(), Logger.INFO, "The tasks have been started. Total task count : " + taskCnt);
		try {
			this.wait();
			Logger.log(this.getClass(), Logger.INFO, "All tasks have been finished. Total elapsed time : " + elapsedTime);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void stopAllThreads() {
		synchronized(waitingJobs) {
			for(int i = 0; i < waitingJobs.size(); i++) {
				Task task = (Task)waitingJobs.poll();
				try {
					task.stopThread();
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}
		}
		synchronized (doingJobs) {
			for(int i = 0; i < doingJobs.size(); i++) {
				Task task = (Task)doingJobs.get(i);
				try {
					task.stopThread();
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}
		}
		this.notify();
	}

	public boolean hasErrors() {
		for(int i = 0; i < completeCount; i++) {
			Task task = (Task)completedJobs.get(i);
			if(task.hasError()) {
				return true;
			}
		}
		return false;
	}

	public String getErrorMessages() {
		StringBuffer sb = new StringBuffer("Errors of threads : ");
		String s;
		for(int i = 0; i < completeCount; i++) {
			Task task = (Task)completedJobs.get(i);
			s = task.getErrorMessage();
			if(s != null) {
				sb.append("\n[" + task.getName() + "] " + s);
			}
		}
		return sb.toString();
	}

}
