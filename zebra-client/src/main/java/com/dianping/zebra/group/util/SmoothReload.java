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
package com.dianping.zebra.group.util;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Dozer <br/>
 *         重新加载 DataSource 的时候设定一个延时，防止同一时间大量的 Connection 对数据库服务器造成冲击。
 */

public class SmoothReload {
	public static final int SLEEP_TIME = 100;

	private static final long DEFAULT_MAX_MILLISECOND_INTERVAL = 1000;

	private static Random rnd = new Random();

	private long maxMillisecondInterval = 0;

	private long randomInterval = 0;

	private long startMillisecond = 0;

	public SmoothReload() {
		this(DEFAULT_MAX_MILLISECOND_INTERVAL);
	}

	public SmoothReload(long maxMillisecondInterval) {
		this.maxMillisecondInterval = maxMillisecondInterval;
		this.startMillisecond = System.currentTimeMillis();
		initRandomInterval();
	}

	public void initRandomInterval() {
		this.randomInterval = (long) (rnd.nextDouble() * maxMillisecondInterval);
	}

	public void waitForReload() {
		if (maxMillisecondInterval <= 0) {
			return;
		}

		while (startMillisecond + randomInterval > System.currentTimeMillis()) {
			try {
				TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}
}