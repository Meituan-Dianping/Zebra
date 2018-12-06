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
package com.dianping.zebra.monitor.monitor;

import com.dianping.cat.status.StatusExtension;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.group.util.DataSourceState;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleDataSourceMonitor implements StatusExtension {

	private static Map<String, AtomicInteger> idCounter = new HashMap<String, AtomicInteger>();

	private SingleDataSourceMBean bean;

	private String id;

	public SingleDataSourceMonitor(SingleDataSourceMBean bean) {
		this.bean = bean;
		setupId(bean);
	}

	private void setupId(SingleDataSourceMBean bean) {
		String bid = bean.getConfig().getId();
		AtomicInteger atomicInteger = idCounter.get(bid);

		if (atomicInteger != null) {
			int index = atomicInteger.incrementAndGet() % 4;
			if (index > 0) {
				this.id = bid + String.format(".%d", index);
			} else {
				this.id = bid;
			}
		} else {
			atomicInteger = new AtomicInteger(0);
			idCounter.put(bid, atomicInteger);
			this.id = bid;
		}
	}

	@Override
	public String getDescription() {
		return this.id;
	}

	@Override
	public String getId() {
		return "dal." + id;
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> status = new LinkedHashMap<String, String>();

		if (bean.getState() != DataSourceState.INITIAL) {
			status.put("zebra." + id + ".TotalConnection", Integer.toString(bean.getNumConnections()));
			status.put("zebra." + id + ".BusyConnection", Integer.toString(bean.getNumBusyConnection()));
			status.put("zebra." + id + ".IdleConnection", Integer.toString(bean.getNumIdleConnection()));
		}

		return status;
	}
}
