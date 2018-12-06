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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;
import com.dianping.zebra.group.config.system.transform.DefaultSaxParser;
import com.dianping.zebra.util.AppPropertiesUtils;
import com.dianping.zebra.util.StringUtils;

public class DefaultSystemConfigManager extends AbstractConfigManager implements SystemConfigManager {

	public static final String DEFAULT_LOCAL_CONFIG = "zebra.system";

	public static final int DEFAULT_BUCKET_NUMBER = 108;

	private SystemConfig systemConfig = new SystemConfig();

	private volatile Map<String, SqlFlowControl> sqlFlowControlMap = new HashMap<String, SqlFlowControl>();

	public DefaultSystemConfigManager(ConfigService configService) {
		super(configService);
	}

	@Override
	public void addListerner(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	private String getKey(String namespace, String key) {
		return String.format("%s.%s.%s", namespace, "system", key);
	}

	@Override
	public SystemConfig getSystemConfig() {
		return this.systemConfig;
	}

	@Override
	public Map<String, SqlFlowControl> getSqlFlowControlMap() {
		return sqlFlowControlMap;
	}

	@Override
	public void init() {
		try {
			this.systemConfig = initSystemConfig();
		} catch (Exception e) {
			throw new ZebraConfigException(String.format(
			      "Fail to initialize DefaultSystemConfigManager with config file[%s].", DEFAULT_LOCAL_CONFIG), e);
		}
	}

	public SystemConfig initSystemConfig() {
		SystemConfig config = new SystemConfig();

		buildRetryTimes(config);
		buildFlowControl(config);

		return config;
	}

	private void buildRetryTimes(SystemConfig config) {
		String appName = AppPropertiesUtils.getAppName();

		if (!Constants.APP_NO_NAME.equals(appName)) {
			config.setRetryTimes(getProperty(getKey(appName + ".zebra", Constants.ELEMENT_RETRY_TIMES),
			      config.getRetryTimes()));
		}
	}

	private void buildFlowControl(SystemConfig config) {
		String appName = AppPropertiesUtils.getAppName();
		int bucketId = Math.abs(appName.hashCode()) % DEFAULT_BUCKET_NUMBER;
		String flowControlConfig = getProperty(
		      getKey(Constants.DEFAULT_DATASOURCE_ZEBRA_SQL_BLACKLIST_PRFIX, Constants.ELEMENT_FLOW_CONTROL + "."
		            + bucketId), null);
		if (StringUtils.isNotBlank(flowControlConfig)) {
			logger.info("start to build flow control...");

			try {
				SystemConfig flowControl = DefaultSaxParser.parse(flowControlConfig);
				List<SqlFlowControl> tempConfig = new ArrayList<SqlFlowControl>();

				if (!Constants.APP_NO_NAME.equals(appName)) {
					for (SqlFlowControl sqlFlowControl : flowControl.getSqlFlowControls()) {
						if (sqlFlowControl != null) {
							String app = sqlFlowControl.getApp();

							if ("_global_".equalsIgnoreCase(app) || appName.equalsIgnoreCase(app)) {
								tempConfig.add(sqlFlowControl);
								logger.info(String.format("get new flow control [ %s : %d ]", sqlFlowControl.getSqlId(),
								      sqlFlowControl.getAllowPercent()));
							}
						}
					}
				} else {
					tempConfig.addAll(flowControl.getSqlFlowControls());
				}

				config.getSqlFlowControls().clear();
				config.getSqlFlowControls().addAll(tempConfig);
				Map<String, SqlFlowControl> newSqlFlowControlMap = new HashMap<String, SqlFlowControl>();
				for (SqlFlowControl sqlFlowControl : tempConfig) {
					newSqlFlowControlMap.put(sqlFlowControl.getSqlId(), sqlFlowControl);
				}
				this.sqlFlowControlMap = newSqlFlowControlMap;
			} catch (Exception ignore) {
			}
		}
	}

	protected void onPropertyUpdated(PropertyChangeEvent evt) {
		String key = evt.getPropertyName();

		synchronized (this.systemConfig) {
			SystemConfig config = this.systemConfig;
			String appName = AppPropertiesUtils.getAppName();
			int bucketId = Math.abs(appName.hashCode()) % DEFAULT_BUCKET_NUMBER;
			if (key.equals(getKey(appName + ".zebra", Constants.ELEMENT_RETRY_TIMES))) {
				config.setRetryTimes(getProperty(getKey(appName + ".zebra", Constants.ELEMENT_RETRY_TIMES),
				      config.getRetryTimes()));
			} else if (key.equals(getKey(Constants.DEFAULT_DATASOURCE_ZEBRA_SQL_BLACKLIST_PRFIX,
			      Constants.ELEMENT_FLOW_CONTROL + "." + bucketId))) {
				buildFlowControl(config);
			}
		}
	}
}
