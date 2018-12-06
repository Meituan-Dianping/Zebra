/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	public static int readCorePoolSize = 32;

	public static int readMaxPoolSize = 64;

	public static int readWorkQueueSize = 500;

	public static long readExecuteTimeOut = 1000L;

	public static int writeCorePoolSize = 32;

	public static int writeMaxPoolSize = 64;

	public static int writeWorkQueueSize = 500;

	public static long writeExecuteTimeOut = 1000L;

	public static boolean readWriteSplitPool = false;

	private static volatile SQLThreadPoolExecutor writeExecutor = null;

	private static volatile SQLThreadPoolExecutor readExecutor = null;

	private long executeTimeOut;

	private SQLThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, long executeTimeOut, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
		this.executeTimeOut = executeTimeOut;
	}

	public static SQLThreadPoolExecutor getInstance(boolean isRead) {
		if (readWriteSplitPool && isRead) {
			return getReadWriteExecutor(true);
		}
		return getReadWriteExecutor(false);
	}

	private static SQLThreadPoolExecutor getReadWriteExecutor(boolean isRead) {
		if (isRead) {
			if (readExecutor == null) {
				synchronized (SQLThreadPoolExecutor.class) {
					if (readExecutor == null) {
						readExecutor = new SQLThreadPoolExecutor(readCorePoolSize, readMaxPoolSize, 60L, readExecuteTimeOut, TimeUnit.SECONDS,
								new LinkedBlockingQueue<Runnable>(readWorkQueueSize), new SQLPoolThreadFactory("Zebra-Shard-ReadExecutor-"));
					}
				}
			}
			return readExecutor;
		} else {
			if (writeExecutor == null) {
				synchronized (SQLThreadPoolExecutor.class) {
					if (writeExecutor == null) {
						writeExecutor = new SQLThreadPoolExecutor(writeCorePoolSize, writeMaxPoolSize, 60L, writeExecuteTimeOut, TimeUnit.SECONDS,
								new LinkedBlockingQueue<Runnable>(writeWorkQueueSize), new SQLPoolThreadFactory("Zebra-Shard-WriteExecutor-"));
					}
				}
			}
			return writeExecutor;
		}
	}

	public <T> List<Future<T>> invokeSQLs(Collection<? extends Callable<T>> tasks) throws SQLException {
		if (tasks == null) {
			throw new NullPointerException();
		}

		long nanos = TimeUnit.MILLISECONDS.toNanos(this.executeTimeOut);

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
							"Error! Do not have enough thread to execute sql, you have to increase your thread pool maxsize");
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

	private static class SQLPoolThreadFactory implements ThreadFactory {
		private AtomicInteger counter = new AtomicInteger(1);

		private String threadNamePrefix = "Zebra-Shard-Executor-";

		public SQLPoolThreadFactory(String threadNamePrefix) {
			this.threadNamePrefix = threadNamePrefix;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName(threadNamePrefix + counter.getAndIncrement());
			t.setDaemon(true);

			return t;
		}
	}

	// for ut
	public static SQLThreadPoolExecutor getWriteExecutor() {
		return writeExecutor;
	}

	public static SQLThreadPoolExecutor getReadExecutor() {
		return readExecutor;
	}
}
