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
package com.dianping.zebra.group.router.region;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.util.StringUtils;

public class RemoteRegionManager extends AbstractZebraRegionManager {

	public RemoteRegionManager(String routerManagerType, ConfigService configService) {
		super(routerManagerType, configService);
	}

	public void init() {
		try {
			String centerConfig = getCenterConfig();

			if (StringUtils.isNotBlank(centerConfig)) {
				parseCenterConfig(centerConfig);
			}

			String regionConfig = getRegionConfig();

			if (StringUtils.isNotBlank(regionConfig)) {
				parseRegionConfig(regionConfig);
			}
		} catch (Exception e) {
			new ZebraException("init RemoteRegionManager file", e);
		}
	}

	private String getCenterConfig() {
		if (remoteConfigServce == null) {
			throw new ZebraException("RemoteRegionManager ConfigService won't be null");
		}

		try {
			return remoteConfigServce.getProperty(Constants.ROUTER_CENTER_CONFIG_LION_KEY);
		} catch (Exception e) {
			LOGGER.warn("Read router center connfig from remote failed! " + e.getMessage());
		}

		return null;
	}

	private String getRegionConfig() {
		if (remoteConfigServce == null) {
			throw new ZebraException("RemoteRegionManager ConfigService won't be null");
		}

		try {
			return remoteConfigServce.getProperty(Constants.ROUTER_REGION_CONFIG_LION_KEY);
		} catch (Exception e) {
			LOGGER.warn("Read router region connfig from remote failed! " + e.getMessage());
		}

		return null;
	}
}
