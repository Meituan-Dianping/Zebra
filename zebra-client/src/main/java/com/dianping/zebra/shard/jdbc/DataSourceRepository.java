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
package com.dianping.zebra.shard.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.shard.config.ShardDataSourceCustomConfig;
import com.dianping.zebra.shard.router.rule.RouterRule;
import com.dianping.zebra.shard.router.rule.TableShardRule;
import com.dianping.zebra.shard.router.rule.dimension.DimensionRule;
import com.dianping.zebra.util.JDBCUtils;

/**
 * @author hao.zhu <br>
 */

public class DataSourceRepository {

	private static volatile DataSourceRepository instance = null;

	private Map<String, DataSource> dataSources = new ConcurrentHashMap<String, DataSource>();

	public DataSourceRepository() {
	}

	public static DataSourceRepository getInstance() {
		if (instance == null) {
			synchronized (DataSourceRepository.class) {
				if (instance == null) {
					instance = new DataSourceRepository();
				}
			}
		}

		return instance;
	}

	public DataSource getDataSource(String dsName) {
		return dataSources.get(dsName.toLowerCase());
	}

	public void put(String name, DataSource dataSource) {
		dataSources.put(name, dataSource);
	}

	public void init(Map<String, DataSource> dataSourcePool) {
		for (Entry<String, DataSource> dataSourceEntry : dataSourcePool.entrySet()) {
			String dbIndex = dataSourceEntry.getKey();
			DataSource dataSource = dataSourceEntry.getValue();

			dataSources.put(dbIndex, dataSource);
		}
	}

	public void init(RouterRule routerRule, ShardDataSourceCustomConfig customConfig) {
		for (TableShardRule shardRule : routerRule.getTableShardRules().values()) {
			for (DimensionRule dimensionRule : shardRule.getDimensionRules()) {
				for (String jdbcRef : dimensionRule.getAllDBAndTables().keySet()) {
					if (!dataSources.containsKey(jdbcRef)) {
						GroupDataSource groupDataSource = new GroupDataSource(jdbcRef);

						groupDataSource.setPoolType(customConfig.getPoolType()); // use default pool
						groupDataSource.setLazyInit(customConfig.isLazyInit());

						if (customConfig.getExtraJdbcUrlParams() != null) {
							groupDataSource.setExtraJdbcUrlParams(customConfig.getExtraJdbcUrlParams());
						}
						if (customConfig.getDsConfigProperties() != null && !customConfig.getDsConfigProperties().isEmpty()) {
							groupDataSource.setProperties(customConfig.getDsConfigProperties());
						}

						if (customConfig.getRouterStrategy() != null) {
							groupDataSource.setRouterStrategy(customConfig.getRouterStrategy());
						}
						if (customConfig.getRouterType() != null) {
							groupDataSource.setRouterType(customConfig.getRouterType());
						}
						if (customConfig.getFilter() != null) {
							groupDataSource.setFilter(customConfig.getFilter());
						}

						groupDataSource.init();

						dataSources.put(jdbcRef, groupDataSource);
					}
				}
			}
		}
	}

	public void close() throws SQLException {
		List<SQLException> exps = new ArrayList<SQLException>();

		for (DataSource ds : dataSources.values()) {
			if (ds instanceof GroupDataSource) {
				try {
					((GroupDataSource) ds).close();
				} catch (SQLException e) {
					exps.add(e);
				}
			}
		}

		dataSources.clear();

		JDBCUtils.throwSQLExceptionIfNeeded(exps);
	}
}