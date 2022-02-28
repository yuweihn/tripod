package com.yuweix.assist4j.schedule.base;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author yuwei
 */
public abstract class AbstractThreadPoolTask<T> extends AbstractTask {
	private static final Logger log = LoggerFactory.getLogger(AbstractThreadPoolTask.class);


	/**
	 * 默认的线程池大小
	 **/
	private static final int DEFAULT_CORE_POOL_SIZE = 10;

	private static ExecutorService defaultExecutorService = null;
	private static final ReentrantLock lock = new ReentrantLock();


	@Override
	protected void executeTask() {
		ExecutorService executor = getExecutorService();
		if (executor == null) {
			executor = getDefaultExecutorService();
		}
		executeInThreadPool(executor);
	}

	protected void executeInThreadPool(ExecutorService executor) {
		List<T> taskList = findTaskList();
		executeInThreadPool(executor, taskList);
	}

	protected void executeInThreadPool(ExecutorService executor, List<T> taskList) {
		log.info("JobName: {}, Size: {}", this.getClass().getName(), taskList == null ? 0 : taskList.size());
		if (taskList == null || taskList.size() <= 0) {
			return;
		}

		List<Future<Result>> futureList = new ArrayList<Future<Result>>();
		taskList.forEach(task -> futureList.add(executor.submit(() -> new Result(task, processTask(task)))));

		/**
		 * 收集执行结果，用于处理失败的记录。
		 */
		List<T> failList = new ArrayList<T>();
		for (Future<Result> future: futureList) {
			try {
				Result result = future.get();
				if (!result.success) {
					failList.add(result.task);
				}
			} catch (Exception e) {
				handleThreadFutureException(e);
			}
		}
		if (failList.size() > 0) {
			failure(failList);
		}
	}

	protected void handleThreadFutureException(Throwable t) {
		log.error("{}", t.getMessage());
	}
	protected void failure(List<T> failList) {

	}

	/**
	 * 查询需要处理的任务集合
	 */
	protected abstract List<T> findTaskList();
	/**
	 * 处理单条任务
	 */
	protected abstract boolean processTask(T task);


	protected ExecutorService getExecutorService() {
		return null;
	}

	private ExecutorService getDefaultExecutorService() {
		if (defaultExecutorService == null || defaultExecutorService.isShutdown()) {
			lock.lock();
			try {
				if (defaultExecutorService == null || defaultExecutorService.isShutdown()) {
					defaultExecutorService = new ThreadPoolExecutor(DEFAULT_CORE_POOL_SIZE, DEFAULT_CORE_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
				}
			} finally {
				lock.unlock();
			}
		}
		return defaultExecutorService;
	}

	private class Result {
		private T task;
		private boolean success;

		Result(T task, boolean success) {
			this.task = task;
			this.success = success;
		}
	}
}


