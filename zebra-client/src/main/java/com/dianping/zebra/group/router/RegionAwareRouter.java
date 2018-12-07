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

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.router.region.ZebraRegionManager;
import com.dianping.zebra.group.router.region.ZebraRegionManagerLoader;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class RegionAwareRouter implements DataSourceRouter {
	private static final Logger LOGGER = LoggerFactory.getLogger(RegionAwareRouter.class);

	private final ZebraRegionManager regionManager;

	private DataSourceRouter localRegionRouter;

	private DataSourceRouter remoteRegionRouter;

	public RegionAwareRouter(Map<String, DataSourceConfig> dataSourceConfigs, String configManagerType,
	      ConfigService configService, String routerStrategy) {
		this.regionManager = ZebraRegionManagerLoader.getRegionManager(configManagerType, configService);

		Map<String, DataSourceConfig> localRegionDataSourceConfigs = new HashMap<String, DataSourceConfig>();
		Map<String, DataSourceConfig> remoteRegionDataSourceConfigs = new HashMap<String, DataSourceConfig>();

		for (Map.Entry<String, DataSourceConfig> entry : dataSourceConfigs.entrySet()) {
			String dsId = entry.getKey();
			DataSourceConfig config = entry.getValue();

			try {
				Matcher matcher = JDBC_URL_PATTERN.matcher(config.getJdbcUrl());
				if (matcher.matches()) {
					String url = matcher.group(1);
					String[] urlAndPort = url.split(":");

					if (urlAndPort != null && urlAndPort.length > 0) {
						if (this.regionManager.isInLocalRegion(urlAndPort[0])) {
							localRegionDataSourceConfigs.put(dsId, config);
						} else {
							remoteRegionDataSourceConfigs.put(dsId, config);
						}
					}
				} else {
					remoteRegionDataSourceConfigs.put(dsId, config);
				}
			} catch (Throwable t) {
				LOGGER.warn(String.format(
				      "Cannot recognize the idc for jdbcUrl(%s), so put this datasource in the other region by default.",
				      config.getJdbcUrl()));
				remoteRegionDataSourceConfigs.put(dsId, config);
			}
		}

		if (Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER.equals(routerStrategy)) {
			// 区域内使用权重路由
			if (localRegionDataSourceConfigs.size() > 0) {
				this.localRegionRouter = new WeightDataSourceRouter(localRegionDataSourceConfigs);
			}
			if (remoteRegionDataSourceConfigs.size() > 0) {
				this.remoteRegionRouter = new WeightDataSourceRouter(remoteRegionDataSourceConfigs);
			}
		} else {
			// 区域内使用中心路由或机房路由
			boolean idcAware = false;
			if (Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER.equals(routerStrategy)) {
				idcAware = true;
			}
			if (localRegionDataSourceConfigs.size() > 0) {
				this.localRegionRouter = new CenterAwareRouter(localRegionDataSourceConfigs, configManagerType, configService, idcAware);
			}
			if (remoteRegionDataSourceConfigs.size() > 0) {
				this.remoteRegionRouter = new CenterAwareRouter(remoteRegionDataSourceConfigs, configManagerType, configService, idcAware);
			}
		}
	}

	@Override
	public String getName() {
		return "region-aware";
	}

	@Override
	public RouterTarget select(RouterContext routerContext) {
		RouterTarget routerTarget = null;

		if (localRegionRouter != null) {
			routerTarget = localRegionRouter.select(routerContext);
		}

		if (routerTarget == null && remoteRegionRouter != null) {
			routerTarget = remoteRegionRouter.select(routerContext);
		}

		return routerTarget;
	}
}
