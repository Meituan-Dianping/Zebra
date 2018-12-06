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

import java.util.*;
import java.util.regex.Matcher;

public class CenterAwareRouter implements DataSourceRouter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CenterAwareRouter.class);

	private final ZebraRegionManager regionManager;

	private DataSourceRouter localCenterRouter;

	private List<WeightDataSourceRouter> priorityCenterIdcAwareRouters = new ArrayList<WeightDataSourceRouter>();

	protected CenterAwareRouter(Map<String, DataSourceConfig> dataSourceConfigs, String configManagerType,
	      boolean idcAware) {
		this.regionManager = ZebraRegionManagerLoader.getRegionManager(configManagerType);
		Map<String, DataSourceConfig> localIdcSourceConfigs = new HashMap<String, DataSourceConfig>();
		Map<String, DataSourceConfig> localCenterSourceConfigs = new HashMap<String, DataSourceConfig>();
		Map<String, Map<String, DataSourceConfig>> remoteCenterSourceConfigs = new HashMap<String, Map<String, DataSourceConfig>>();

		for (Map.Entry<String, DataSourceConfig> entry : dataSourceConfigs.entrySet()) {
			String dsId = entry.getKey();
			DataSourceConfig config = entry.getValue();

			try {
				Matcher matcher = JDBC_URL_PATTERN.matcher(config.getJdbcUrl());
				if (matcher.matches()) {
					String url = matcher.group(1);
					String[] urlAndPort = url.split(":");

					if (urlAndPort != null && urlAndPort.length > 0) {
						if (regionManager.isInLocalIdc(urlAndPort[0])) { // 同机房
							localIdcSourceConfigs.put(dsId, config);
						} else if (regionManager.isInLocalCenter(urlAndPort[0])) { // 同中心
							localCenterSourceConfigs.put(dsId, config);
						} else { // 根据center name分组
							String centerName = regionManager.findCenter(urlAndPort[0]);
							Map<String, DataSourceConfig> centerDataSourceConfigs = remoteCenterSourceConfigs.get(centerName);
							if (centerDataSourceConfigs == null) {
								centerDataSourceConfigs = new HashMap<String, DataSourceConfig>();
								remoteCenterSourceConfigs.put(centerName, centerDataSourceConfigs);
							}
							centerDataSourceConfigs.put(dsId, config);
						}
					}
				} else {
					localCenterSourceConfigs.put(dsId, config);
				}
			} catch (Throwable t) {
				LOGGER.warn(String.format(
				      "Cannot recognize the idc for jdbcUrl(%s), so put this datasource in the same center by default.",
				      config.getJdbcUrl()));
				localCenterSourceConfigs.put(dsId, config);
			}
		}

		// 同一个idc或同一个center
		if (idcAware) {
			localCenterSourceConfigs.putAll(localIdcSourceConfigs);
			if (localCenterSourceConfigs.size() > 0) {
				this.localCenterRouter = new IdcAwareRouter(localCenterSourceConfigs, configManagerType);
			}
		} else {
			String localCenter = regionManager.getLocalCenter();
			if ((localCenter == null || ZebraRegionManager.NO_CENTER.equals(localCenter))
			      && localCenterSourceConfigs.size() <= 0) {
				// 不在中心内
				if (localIdcSourceConfigs.size() > 0) {
					Map<String, DataSourceConfig> centerDataSourceConfigs = remoteCenterSourceConfigs
					      .get(ZebraRegionManager.NO_CENTER);
					if (centerDataSourceConfigs == null) {
						centerDataSourceConfigs = new HashMap<String, DataSourceConfig>();
						remoteCenterSourceConfigs.put(ZebraRegionManager.NO_CENTER, centerDataSourceConfigs);
					}
					centerDataSourceConfigs.putAll(localIdcSourceConfigs);
				}
			} else {
				// 在中心内
				localCenterSourceConfigs.putAll(localIdcSourceConfigs);
				this.localCenterRouter = new WeightDataSourceRouter(localCenterSourceConfigs);
			}
		}

		// 其他center, 按优先级排列
		if (remoteCenterSourceConfigs.size() > 0) {
			List<CenterDsConfigWrapper> centerDsConfigWrappers = sortByCenterPriority(remoteCenterSourceConfigs);
			for (CenterDsConfigWrapper wrapper : centerDsConfigWrappers) {
				priorityCenterIdcAwareRouters.add(new WeightDataSourceRouter(wrapper.getCenterSourceConfigs()));
			}
		}
	}

	/**
	 * idcAware = true: 1.优先同机房 2.同中心不同机房 3.按中心优先级选择 4.非中心 idcAware = false: 1.按中心优先级选择 2.非中心(上海侧都不在中心内, 优先级相同)
	 */
	@Override
	public RouterTarget select(RouterContext routerContext) {
		RouterTarget routerTarget = null;
		if (localCenterRouter != null) {
			routerTarget = localCenterRouter.select(routerContext);
		}

		if (routerTarget == null) {
			for (WeightDataSourceRouter weightDataSourceRouter : priorityCenterIdcAwareRouters) {
				routerTarget = weightDataSourceRouter.select(routerContext);
				if (routerTarget != null) {
					return routerTarget;
				}
			}
		}

		return routerTarget;
	}

	@Override
	public String getName() {
		return "center-aware";
	}

	private List<CenterDsConfigWrapper> sortByCenterPriority(
	      Map<String, Map<String, DataSourceConfig>> otherCenterSourceConfigs) {
		List<CenterDsConfigWrapper> sortedList = new ArrayList<CenterDsConfigWrapper>();
		if (otherCenterSourceConfigs.size() > 0) {
			for (Map.Entry<String, Map<String, DataSourceConfig>> entry : otherCenterSourceConfigs.entrySet()) {
				String centerName = entry.getKey();
				Map<String, DataSourceConfig> dsConfig = entry.getValue();
				int priority = regionManager.getCenterPriority(centerName);
				sortedList.add(new CenterDsConfigWrapper(priority, dsConfig));
			}
			// 值越大,优先级越高
			Collections.sort(sortedList, new Comparator<CenterDsConfigWrapper>() {
				@Override
				public int compare(CenterDsConfigWrapper o1, CenterDsConfigWrapper o2) {
					return (o2.getPriority() - o1.getPriority());
				}
			});
		}

		return sortedList;
	}

	private static class CenterDsConfigWrapper {
		private int priority;

		private Map<String, DataSourceConfig> centerSourceConfigs = new HashMap<String, DataSourceConfig>();

		public CenterDsConfigWrapper(int priority, Map<String, DataSourceConfig> centerSourceConfigs) {
			this.priority = priority;
			this.centerSourceConfigs = centerSourceConfigs;
		}

		public int getPriority() {
			return priority;
		}

		public Map<String, DataSourceConfig> getCenterSourceConfigs() {
			return centerSourceConfigs;
		}
	}
}
