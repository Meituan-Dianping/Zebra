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
package com.dianping.zebra.filter;

import java.beans.PropertyChangeEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.dianping.zebra.Constants;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.datasources.FailOverDataSource;
import com.dianping.zebra.group.jdbc.GroupConnection;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.shard.jdbc.ShardDataSource;
import com.dianping.zebra.shard.jdbc.ShardResultSet;
import com.dianping.zebra.shard.jdbc.ShardStatement;
import com.dianping.zebra.shard.router.RouterResult;
import com.dianping.zebra.single.jdbc.SingleConnection;
import com.dianping.zebra.single.jdbc.SingleDataSource;
import com.dianping.zebra.single.jdbc.SingleResultSet;
import com.dianping.zebra.single.jdbc.SingleStatement;

/**
 * Created by Dozer on 9/2/14.
 */
public class DefaultJdbcFilter implements JdbcFilter {

	protected String configManagerType = Constants.CONFIG_MANAGER_TYPE_LOCAL;

	protected Map<String, Object> configs = null;

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void closeGroupConnection(GroupConnection source, JdbcFilter chain) throws SQLException {
		chain.closeGroupConnection(source, chain);
	}

	@Override
	public void closeGroupDataSource(GroupDataSource source, JdbcFilter chain) throws SQLException {
		chain.closeGroupDataSource(source, chain);
	}

	@Override
	public void closeSingleConnection(SingleConnection source, JdbcFilter chain) throws SQLException {
		chain.closeSingleConnection(source, chain);
	}

	@Override
	public void closeSingleDataSource(SingleDataSource source, JdbcFilter chain) throws SQLException {
		chain.closeSingleDataSource(source, chain);
	}

	@Override
	public <T> T executeSingleStatement(SingleStatement source, SingleConnection conn, String sql,
	      List<String> batchedSql, boolean isBatched, boolean autoCommit, Object params, JdbcFilter chain)
	      throws SQLException {
		return chain.executeSingleStatement(source, conn, sql, batchedSql, isBatched, autoCommit, params, chain);
	}

	@Override
	public FailOverDataSource.FindMasterDataSourceResult findMasterFailOverDataSource(
	      FailOverDataSource.MasterDataSourceMonitor source, JdbcFilter chain) {
		return chain.findMasterFailOverDataSource(source, chain);
	}

	@Override
	public GroupConnection getGroupConnection(GroupDataSource source, JdbcFilter chain) throws SQLException {
		return chain.getGroupConnection(source, chain);
	}

	@Override
	public SingleConnection getSingleConnection(SingleDataSource source, JdbcFilter chain) throws SQLException {
		return chain.getSingleConnection(source, chain);
	}

	@Override
	public void init() {
		// do something
	}

	@Override
	public void initGroupDataSource(GroupDataSource source, JdbcFilter chain) {
		chain.initGroupDataSource(source, chain);
	}

	@Override
	public DataSource initSingleDataSource(SingleDataSource source, JdbcFilter chain) {
		return chain.initSingleDataSource(source, chain);
	}

	@Override
	public void refreshGroupDataSource(GroupDataSource source, String propertiesName, JdbcFilter chain) {
		chain.refreshGroupDataSource(source, propertiesName, chain);
	}

	public void setConfigManager(String configManagerType, Map<String, Object> configs) {
		this.configManagerType = configManagerType;
		this.configs = configs;
	}

	@Override
	public void switchFailOverDataSource(FailOverDataSource source, JdbcFilter chain) {
		chain.switchFailOverDataSource(source, chain);
	}

	@Override
	public String processSQL(DataSourceConfig dsConfig, SQLProcessContext ctx, JdbcFilter chain) throws SQLException {
		return chain.processSQL(dsConfig, ctx, chain);
	}

	@Override
	public void closeSingleResultSet(SingleResultSet source, JdbcFilter chain) throws SQLException {
		chain.closeSingleResultSet(source, chain);
	}

	@Override
	public ResultSet executeShardQuery(ShardStatement source, String sql, JdbcFilter chain) throws SQLException {
		return chain.executeShardQuery(source, sql, chain);
	}

	@Override
	public void shardRouting(RouterResult rr, JdbcFilter chain) throws SQLException {
		chain.shardRouting(rr, chain);
	}

	@Override
	public int executeShardUpdate(ShardStatement source, String sql, int autoGeneratedKeys, int[] columnIndexes,
	      String[] columnNames, JdbcFilter chain) throws SQLException {
		return chain.executeShardUpdate(source, sql, autoGeneratedKeys, columnIndexes, columnNames, chain);
	}

	@Override
	public void shardMerge(ShardResultSet rs, JdbcFilter chain) throws SQLException {
		chain.shardMerge(rs, chain);
	}

	@Override
	public void configChanged(PropertyChangeEvent evt, JdbcFilter chain) {
		chain.configChanged(evt, chain);
	}

	@Override
	public void initShardDataSource(ShardDataSource source, JdbcFilter chain) {
		chain.initShardDataSource(source, chain);
	}
}
