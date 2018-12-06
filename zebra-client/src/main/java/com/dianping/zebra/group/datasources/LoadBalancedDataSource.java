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
package com.dianping.zebra.group.datasources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.system.entity.SystemConfig;
import com.dianping.zebra.group.exception.SlaveDsDisConnectedException;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.group.router.*;
import com.dianping.zebra.group.router.BackupDataSourceRouter;
import com.dianping.zebra.group.router.DataSourceRouter;
import com.dianping.zebra.group.util.SqlAliasManager;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.single.jdbc.AbstractDataSource;
import com.dianping.zebra.single.jdbc.SingleDataSource;
import com.dianping.zebra.single.manager.SingleDataSourceManager;
import com.dianping.zebra.single.manager.SingleDataSourceManagerFactory;
import com.dianping.zebra.util.JDBCUtils;
import com.dianping.zebra.util.JdbcDriverClassHelper;
import com.dianping.zebra.util.StringUtils;

public class LoadBalancedDataSource extends AbstractDataSource {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancedDataSource.class);

	private SingleDataSourceManager dataSourceManager;

	private Map<String, SingleDataSource> dataSources;

	private Map<String, DataSourceConfig> loadBalancedConfigMap;

	private volatile DataSourceRouter router;

	private String configManagerType;

	private SystemConfig systemConfig;

	private volatile String routerStrategy;

	public LoadBalancedDataSource(Map<String, DataSourceConfig> loadBalancedConfigMap, List<JdbcFilter> filters,
	      SystemConfig systemConfig, String configManagerType, String routerStrategy) {
		this.dataSources = new HashMap<String, SingleDataSource>();
		this.loadBalancedConfigMap = loadBalancedConfigMap;
		this.filters = filters;
		this.configManagerType = configManagerType;
		this.systemConfig = systemConfig;
		this.routerStrategy = routerStrategy;
	}

	public void close() throws SQLException {
		if (dataSources != null) {
			for (SingleDataSource ds : dataSources.values()) {
				dataSourceManager.destoryDataSource(ds);
			}
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		RouterContext context = new RouterContext();

		for (SingleDataSource dataSource : this.dataSources.values()) {
			if (dataSource.isDown() || dataSource.isClosed()) {
				context.addExcludeTarget(dataSource.getId());
			}
		}
		if (context.getExcludeTargets().size() > 0) {
			LOGGER.info("excludeTargets：" + context.getExcludeTargets().toString());
		}

		RouterTarget target = this.router.select(context);

		if (target != null) {
			int tmpRetryTimes = -1;
			Set<RouterTarget> excludeTargets = new HashSet<RouterTarget>();
			List<SQLException> exceptions = new ArrayList<SQLException>();

			while (tmpRetryTimes++ < this.systemConfig.getRetryTimes()) {
				try {
					if (tmpRetryTimes > 0) {
						SqlAliasManager.setRetrySqlAlias();
					}
					SingleDataSource targetDataSource = this.dataSources.get(target.getId());
					if (targetDataSource == null) {
						throw new ZebraException("can't get SingleDataSource by dsId:" + target.getId() + ",a config "
						      + "refresh event may have occurred");
					}
					return targetDataSource.getConnection();
				} catch (SQLException e) {
					exceptions.add(e);
					excludeTargets.add(target);
					context = new RouterContext(excludeTargets);
					target = router.select(context);
					if (target == null) {
						break;
					}
				}
			}

			if (!exceptions.isEmpty()) {
				JDBCUtils.throwSQLExceptionIfNeeded(exceptions);
			}
		} else {
			throw new SQLException("No available dataSource after exclude：" + context.getExcludeTargets().toString());
		}

		throw new SQLException("Can not acquire connection");
	}

	public Map<String, SingleDataSourceMBean> getCurrentDataSourceMBean() {
		Map<String, SingleDataSourceMBean> beans = new HashMap<String, SingleDataSourceMBean>();
		beans.putAll(dataSources);
		return beans;
	}

	public void init() {
		this.dataSourceManager = SingleDataSourceManagerFactory.getDataSourceManager();

		for (DataSourceConfig config : loadBalancedConfigMap.values()) {
			checkConfig(config);
		}

		for (DataSourceConfig config : loadBalancedConfigMap.values()) {
			SingleDataSource dataSource = dataSourceManager.createDataSource(config, this.filters);
			this.dataSources.put(config.getId(), dataSource);
		}

		this.router = new BackupDataSourceRouter(loadBalancedConfigMap, configManagerType, routerStrategy);
	}

	private void checkConfig(DataSourceConfig config) {
		try {
			JdbcDriverClassHelper.loadDriverClass(config.getDriverClass(), config.getJdbcUrl());
			Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());

			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			String password = config.getPassword();
			if (StringUtils.isNotBlank(password)) {
				String prefix = password.substring(0, 3);
				String postfix = password.substring(password.length() - 3);
				password = prefix + "***" + postfix;
			}
			throw new SlaveDsDisConnectedException("Cannot connect datasource(" + config.getJdbcUrl() + ":"
			      + config.getUsername() + ":" + password + ").", e);
		}
	}

	public synchronized void buildRouter(String routerStrategy) {
		this.router = new BackupDataSourceRouter(loadBalancedConfigMap, configManagerType, routerStrategy);
		this.routerStrategy = routerStrategy;
	}

	public synchronized void refresh(Map<String, DataSourceConfig> newLoadBalancedConfigMap)
	      throws SlaveDsDisConnectedException {
		if (loadBalancedConfigMap.toString().equals(newLoadBalancedConfigMap.toString())) {
			return;
		}

		Map<String, DataSourceConfig> needCreateDataSourceConfigs = new HashMap<String, DataSourceConfig>();
		Map<String, SingleDataSource> needRemoveDataSources = new HashMap<String, SingleDataSource>();
		Map<String, SingleDataSource> noChangeDataSources = new HashMap<String, SingleDataSource>();

		for (Map.Entry<String, DataSourceConfig> oldEntry : loadBalancedConfigMap.entrySet()) {
			String oldDataSourceId = oldEntry.getKey();
			DataSourceConfig oldConfig = oldEntry.getValue();
			SingleDataSource oldDataSource = dataSources.get(oldDataSourceId);
			if (newLoadBalancedConfigMap.containsKey(oldDataSourceId)) {
				DataSourceConfig newConfig = newLoadBalancedConfigMap.get(oldDataSourceId);
				if (oldConfig.toString().equals(newConfig.toString())) {
					noChangeDataSources.put(oldDataSourceId, oldDataSource);
				} else {
					needRemoveDataSources.put(oldDataSourceId, oldDataSource);
					needCreateDataSourceConfigs.put(oldDataSourceId, newConfig);
				}
			} else {
				needRemoveDataSources.put(oldDataSourceId, oldDataSource);
			}
		}

		for (Map.Entry<String, DataSourceConfig> newEntry : newLoadBalancedConfigMap.entrySet()) {
			String newDatasourceId = newEntry.getKey();
			if (!loadBalancedConfigMap.containsKey(newDatasourceId)) {
				needCreateDataSourceConfigs.put(newDatasourceId, newEntry.getValue());
			}
		}

		for (Map.Entry<String, DataSourceConfig> entry : needCreateDataSourceConfigs.entrySet()) {
			DataSourceConfig config = entry.getValue();
			checkConfig(config);
		}

		Map<String, SingleDataSource> newDataSources = new HashMap<String, SingleDataSource>();
		try {
			for (Map.Entry<String, DataSourceConfig> entry : needCreateDataSourceConfigs.entrySet()) {
				String datasourceId = entry.getKey();
				DataSourceConfig config = entry.getValue();
				SingleDataSource dataSource = dataSourceManager.createDataSource(config, filters);
				newDataSources.put(datasourceId, dataSource);
			}
		} catch (Exception e) {
			for (SingleDataSource singleDataSource : newDataSources.values()) {
				try {
					singleDataSource.close();
				} catch (SQLException ignore) {
				}
			}
			throw new ZebraException(e);
		}

		// switch first
		Map<String, SingleDataSource> targetDataSourceMap = new HashMap<String, SingleDataSource>();
		targetDataSourceMap.putAll(newDataSources);
		targetDataSourceMap.putAll(noChangeDataSources);

		this.dataSources = targetDataSourceMap;
		this.loadBalancedConfigMap = newLoadBalancedConfigMap;
		this.router = new BackupDataSourceRouter(loadBalancedConfigMap, configManagerType, routerStrategy);

		// close after
		for (Map.Entry<String, SingleDataSource> entry : needRemoveDataSources.entrySet()) {
			SingleDataSource dataSource = entry.getValue();
			dataSourceManager.destoryDataSource(dataSource);
		}
	}
}
