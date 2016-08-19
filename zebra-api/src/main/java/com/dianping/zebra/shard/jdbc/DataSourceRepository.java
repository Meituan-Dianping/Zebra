/**
 * Project: com.dianping.zebra.zebra-client-0.1.0
 * <p/>
 * File Created at 2011-6-15
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
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

	public void init(RouterRule routerRule) {
		for (TableShardRule shardRule : routerRule.getTableShardRules().values()) {
			for (DimensionRule dimensionRule : shardRule.getDimensionRules()) {
				for (String jdbcRef : dimensionRule.getAllDBAndTables().keySet()) {
					if (!dataSources.containsKey(jdbcRef)) {
						GroupDataSource groupDataSource = new GroupDataSource(jdbcRef);
						groupDataSource.setPoolType("tomcat-jdbc");  // use tomcat-jdbc as default pool
						groupDataSource.setForceWriteOnLogin(false); // HACK turn off
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