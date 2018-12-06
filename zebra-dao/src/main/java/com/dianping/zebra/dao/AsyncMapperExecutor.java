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
package com.dianping.zebra.dao;

import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncMapperExecutor {

	private static int MAX_QUEUE_SIZE;

	private static int CORE_POOL_SIZE;

	private static int MAX_POOL_SIZE;

	private static volatile ThreadPoolExecutor executorService = null;

	private static void init() {
		if (executorService == null) {
			synchronized (AsyncMapperExecutor.class) {
				if (executorService == null) {
					executorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 60L, TimeUnit.SECONDS,
					      new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE), new ThreadFactory() {

						      private AtomicInteger counter = new AtomicInteger(1);

						      @Override
						      public Thread newThread(Runnable r) {
							      Thread t = new Thread(r);
							      t.setName("Zebra-Dao-Executor-" + counter.getAndIncrement());
							      t.setDaemon(true);

							      return t;
						      }
					      });
				}
			}
		}
	}

	public static void init(int corePoolSize, int maxPoolSize, int queueSize) {
		CORE_POOL_SIZE = corePoolSize;
		MAX_POOL_SIZE = maxPoolSize;
		MAX_QUEUE_SIZE = queueSize;
	}

	public static <T> void executeRunnable(Class<?> mapperInterface, Object mapper, Method method, Object[] args,
	      AsyncDaoCallback<T> callback) {
		init();
		AsyncDaoRunnableExecutor<T> executor = new AsyncDaoRunnableExecutor<T>(mapperInterface, mapper, method, args,
		      callback);

		executorService.execute(executor);
	}

	public static Future<?> submitCallback(Class<?> mapperInterface, Object mapper, Method method, Object[] args) {
		init();
		AsyncDaoCallableExecutor executor = new AsyncDaoCallableExecutor(mapperInterface, mapper, method, args);

		return executorService.submit(executor);
	}

	public static void setCorePoolSize(int corePoolSize) {
		if (executorService != null && CORE_POOL_SIZE != corePoolSize) {
			executorService.setCorePoolSize(corePoolSize);
		}

		CORE_POOL_SIZE = corePoolSize;
	}

	public static void setMaximumPoolSize(int maximumPoolSize) {
		if (executorService != null && MAX_POOL_SIZE != maximumPoolSize) {
			executorService.setMaximumPoolSize(maximumPoolSize);
		}

		MAX_POOL_SIZE = maximumPoolSize;
	}
}
