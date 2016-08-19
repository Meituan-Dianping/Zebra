package com.dianping.zebra.shard.jdbc.parallel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class SQLThreadPoolExecutor extends ThreadPoolExecutor {

	public static int corePoolSize = 32;

	public static int maxPoolSize = 64;

	public static int workQueueSize = 500;

	public static long executeTimeOut = 1000L;

	private static volatile SQLThreadPoolExecutor executor = null;

	private SQLThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	public static SQLThreadPoolExecutor getInstance() {
		if (executor == null) {
			synchronized (SQLThreadPoolExecutor.class) {
				if (executor == null) {
					executor = new SQLThreadPoolExecutor(corePoolSize, maxPoolSize, 60L, TimeUnit.SECONDS,
							new LinkedBlockingQueue<Runnable>(workQueueSize), new ThreadFactory() {

								private AtomicInteger counter = new AtomicInteger(1);

								@Override
								public Thread newThread(Runnable r) {
									Thread t = new Thread(r);
									t.setName("Zebra-Shard-Executor-" + counter.getAndIncrement());
									t.setDaemon(true);

									return t;
								}
							});
				}
			}
		}

		return executor;
	}

	public <T> List<Future<T>> invokeSQLs(Collection<? extends Callable<T>> tasks) throws SQLException {
		if (tasks == null)
			throw new NullPointerException();
		long nanos = TimeUnit.MILLISECONDS.toNanos(executeTimeOut);
		ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
		boolean done = false;
		try {
			for (Callable<T> t : tasks) {
				futures.add(newTaskFor(t));
			}

			final long deadline = System.nanoTime() + nanos;
			final int size = futures.size();

			// Interleave time checks and calls to execute in case
			// executor doesn't have any/much parallelism.
			for (int i = 0; i < size; i++) {
				execute((Runnable) futures.get(i));
				nanos = deadline - System.nanoTime();
				if (nanos <= 0L) {
					throw new SQLException(
							"Error! Do not have enough thread to excute sql, you have to increase your thread pool maxsize");
				}
			}

			for (int i = 0; i < size; i++) {
				Future<T> f = futures.get(i);
				if (!f.isDone()) {
					if (nanos <= 0L){
						return futures;
					}
					try {
						f.get(nanos, TimeUnit.NANOSECONDS);
					} catch (CancellationException ce) {
						throw new SQLException(ce);
					} catch (ExecutionException ee) {
						throw new SQLException(ee.getCause());
					} catch (TimeoutException toe) {
						throw new SQLException(
								"One of your sql's execution time is beyond " + executeTimeOut + " milliseconds.", toe);
					} catch (InterruptedException e) {
						throw new SQLException(e);
					}
					nanos = deadline - System.nanoTime();
				}
			}
			done = true;
			return futures;
		} finally {
			if (!done){
				for (int i = 0, size = futures.size(); i < size; i++) {
					futures.get(i).cancel(true);
				}
			}
		}
	}
}
