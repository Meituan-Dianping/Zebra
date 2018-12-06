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
import com.dianping.zebra.group.config.HidePasswordVisitor;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.group.monitor.GroupDataSourceMBean;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.group.util.DataSourceState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupDataSourceMonitor implements StatusExtension {

	private final static AtomicInteger groupDsCounter = new AtomicInteger();

	private final int groupDsId = groupDsCounter.incrementAndGet();

	private GroupDataSourceMBean groupDataSourceBean;

	public GroupDataSourceMonitor(GroupDataSourceMBean dataSource) {
		this.groupDataSourceBean = dataSource;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder(1024 * 3);

		SingleDataSourceMBean writeBean = groupDataSourceBean.getWriteSingleDataSourceMBean();
		if (writeBean != null) {
			sb.append("currentWriter:" + writeBean.getId() + " running at state:" + writeBean.getCurrentState() + "\n");
		} else {
			sb.append("currentWriter: No available writer\n");
		}
		Map<String, SingleDataSourceMBean> readerBeans = groupDataSourceBean.getReaderSingleDataSourceMBean();

		if (readerBeans != null) {
			sb.append("currentReader:");
			for (SingleDataSourceMBean bean : readerBeans.values()) {
				sb.append(bean.getId() + " running at state:" + bean.getCurrentState() + "\n");
			}
		} else {
			sb.append("currentReader: No available readers<br>");
		}

		sb.append("\ndataSourceConfig:\n");

		GroupDataSourceConfig groupDataSourceConfig = new GroupDataSourceConfig();
		HidePasswordVisitor visitor = new HidePasswordVisitor(groupDataSourceConfig);
		groupDataSourceBean.getConfig().accept(visitor);

		sb.append(groupDataSourceConfig);

		return sb.toString();
	}

	@Override
	public String getId() {
		return "dal";
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> status = new LinkedHashMap<String, String>();

		Map<String, SingleDataSourceMBean> beans = groupDataSourceBean.getReaderSingleDataSourceMBean();
		if (beans != null) {
			for (Entry<String, SingleDataSourceMBean> entry : beans.entrySet()) {
				putProperty(status, entry.getValue(), "r");
			}
		}

		SingleDataSourceMBean bean = groupDataSourceBean.getWriteSingleDataSourceMBean();

		if (bean != null) {
			putProperty(status, bean, "w");
		}

		return status;
	}

	private void putProperty(Map<String, String> status, SingleDataSourceMBean bean, String mode) {
		if (bean.getState() != DataSourceState.INITIAL) {
			String id = bean.getId();
			status.put(String.format("[%d%s]%s-TotalConnection", groupDsId, mode, id),
					Integer.toString(bean.getNumConnections()));
			status.put(String.format("[%d%s]%s-BusyConnection", groupDsId, mode, id),
					Integer.toString(bean.getNumBusyConnection()));
			status.put(String.format("[%d%s]%s-IdleConnection", groupDsId, mode, id),
					Integer.toString(bean.getNumIdleConnection()));
		}
	}
}
