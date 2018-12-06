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

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.config.RemoteConfigService;
import com.dianping.zebra.exception.ZebraConfigException;

import java.util.Map;

public final class SystemConfigManagerFactory {

	/**
	 * SystemConfigManagerFactory has only one instance of SystemConfigManager <br>
	 * which differs from DataSourceConfigManagerFactory who has its own DataSourceConfigManager for each GroupDataSource
	 */
	private static SystemConfigManager systemConfigManager;

	private SystemConfigManagerFactory() {
	}

	public static SystemConfigManager getConfigManger(String configManagerType, Map<String, Object> serviceConfigs) {
		if (systemConfigManager == null) {
			synchronized (SystemConfigManagerFactory.class) {
				if (systemConfigManager == null) {
					ConfigService configService = ConfigServiceFactory.getConfigService(configManagerType, serviceConfigs);
					systemConfigManager = new DefaultSystemConfigManager(configService);
					systemConfigManager.init();
				}

			}
		}

		return systemConfigManager;
	}
}
