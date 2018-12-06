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
package com.dianping.zebra.group.router;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.router.region.ZebraRegionManager;
import com.dianping.zebra.group.router.region.ZebraRegionManagerLoader;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.util.NetworkUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class IdcAwareRouter implements DataSourceRouter {
	private static final Logger LOGGER = LoggerFactory.getLogger(IdcAwareRouter.class);

	private final ZebraRegionManager regionManager;

	private WeightDataSourceRouter localIdcWeightedRouter;

	private WeightDataSourceRouter remoteIdcWeightedRouter;

	public IdcAwareRouter(Map<String, DataSourceConfig> dataSourceConfigs, String configManagerType) {
		this(dataSourceConfigs, NetworkUtils.IpHelper.INSTANCE.getLocalHostAddress(), configManagerType);
	}

	// for test purpose
	protected IdcAwareRouter(Map<String, DataSourceConfig> dataSourceConfigs, String localIpAddress,
	      String configManagerType) {
		this.regionManager = ZebraRegionManagerLoader.getRegionManager(configManagerType);
		Map<String, DataSourceConfig> localIdcDataSourceConfigs = new HashMap<String, DataSourceConfig>();
		Map<String, DataSourceConfig> remoteIdcDataSourceConfigs = new HashMap<String, DataSourceConfig>();

		for (Map.Entry<String, DataSourceConfig> entry : dataSourceConfigs.entrySet()) {
			String dsId = entry.getKey();
			DataSourceConfig config = entry.getValue();

			try {
				Matcher matcher = JDBC_URL_PATTERN.matcher(config.getJdbcUrl());
				if (matcher.matches()) {
					String url = matcher.group(1);
					String[] urlAndPort = url.split(":");

					if (urlAndPort != null && urlAndPort.length > 0) {
						if (regionManager.isInLocalIdc(urlAndPort[0])) {
							localIdcDataSourceConfigs.put(dsId, config);
						} else {
							remoteIdcDataSourceConfigs.put(dsId, config);
						}
					}
				} else {
					localIdcDataSourceConfigs.put(dsId, config);
				}
			} catch (Throwable t) {
				LOGGER.warn(String.format(
				      "Cannot recognize the idc for jdbcUrl(%s), so put this datasource in the same idc by default.",
				      config.getJdbcUrl()));
				localIdcDataSourceConfigs.put(dsId, config);
			}
		}

		if (localIdcDataSourceConfigs.size() > 0) {
			this.localIdcWeightedRouter = new WeightDataSourceRouter(localIdcDataSourceConfigs);
		}

		if (remoteIdcDataSourceConfigs.size() > 0) {
			this.remoteIdcWeightedRouter = new WeightDataSourceRouter(remoteIdcDataSourceConfigs);
		}
	}

	@Override
	public String getName() {
		return "idc-aware";
	}

	@Override
	public RouterTarget select(RouterContext routerContext) {
		RouterTarget routerTarget = null;
		if (localIdcWeightedRouter != null) {
			routerTarget = localIdcWeightedRouter.select(routerContext);
		}

		if (routerTarget == null && remoteIdcWeightedRouter != null) {
			routerTarget = remoteIdcWeightedRouter.select(routerContext);
		}

		return routerTarget;
	}
}
