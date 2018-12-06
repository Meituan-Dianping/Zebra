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
package com.dianping.zebra.group.config;

import com.dianping.zebra.group.jdbc.GroupDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DataSourceConfigRefresh {
	private static volatile DataSourceConfigRefresh instance = null;

	public static DataSourceConfigRefresh getInstance() {
		if (instance == null) {
			synchronized (DataSourceConfigRefresh.class) {
				if (instance == null) {
					instance = new DataSourceConfigRefresh();
					instance.init();
				}
			}
		}

		return instance;
	}

	private Map<GroupDataSource, Long> dataSourceList = new ConcurrentHashMap<GroupDataSource, Long>();

	private final long CHECK_INTERVAL = 60 * 1000L; // 60 seconds

	private DataSourceConfigRefresh() {
	}

	public void init() {
		Thread dataSourceConfigRefreshTask = new Thread(new DataSourceConfigRefreshTask());

		dataSourceConfigRefreshTask.setDaemon(true);
		dataSourceConfigRefreshTask.setName("Zebra-" + DataSourceConfigRefresh.class.getSimpleName());
		dataSourceConfigRefreshTask.start();
	}

	public void register(GroupDataSource ds) {
		dataSourceList.put(ds, 0L);
	}

	public void unregister(GroupDataSource ds) {
		dataSourceList.remove(ds);
	}

	class DataSourceConfigRefreshTask implements Runnable {

		@Override
		public void run() {
			// wait for 5 minutes until application start
			try {
				TimeUnit.MINUTES.sleep(5);
			} catch (InterruptedException e1) {
			}

			while (!Thread.currentThread().isInterrupted()) {
				for (Map.Entry<GroupDataSource, Long> entry : dataSourceList.entrySet()) {
					GroupDataSource ds = entry.getKey();
					long lastRefreshTime = entry.getValue();
					long now = System.currentTimeMillis();
					if ((now - lastRefreshTime) >= CHECK_INTERVAL) {
						try {
							ds.refresh("auto-refresh");
						} catch (Throwable ignore) {
						} finally {
							dataSourceList.put(ds, now);
						}
					}
				}

				try {
					TimeUnit.SECONDS.sleep(30);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
