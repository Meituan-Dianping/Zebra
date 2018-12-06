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
package com.dianping.zebra.monitor.filter;

import java.beans.PropertyChangeEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.dianping.cat.status.StatusExtension;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.status.StatusExtensionRegister;
import com.dianping.zebra.Constants;
import com.dianping.zebra.filter.DefaultJdbcFilter;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.datasources.FailOverDataSource;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.group.util.DaoContextHolder;
import com.dianping.zebra.group.util.SqlAliasManager;
import com.dianping.zebra.monitor.Version;
import com.dianping.zebra.monitor.monitor.SingleDataSourceMonitor;
import com.dianping.zebra.monitor.util.SqlMonitorUtils;
import com.dianping.zebra.monitor.util.Stringizers;
import com.dianping.zebra.shard.jdbc.ShardResultSet;
import com.dianping.zebra.shard.jdbc.ShardStatement;
import com.dianping.zebra.shard.router.RouterResult;
import com.dianping.zebra.single.jdbc.SingleConnection;
import com.dianping.zebra.single.jdbc.SingleDataSource;
import com.dianping.zebra.single.jdbc.SingleResultSet;
import com.dianping.zebra.single.jdbc.SingleStatement;
import com.dianping.zebra.util.SqlUtils;
import com.dianping.zebra.util.StringUtils;

/**
 * Created by Dozer on 9/5/14.
 */
public class CatFilter extends DefaultJdbcFilter {
	private static final String CAT_TYPE = "DAL";

	private static final String SHARD_CAT_TYPE = "ShardSQL";

	private static final Logger LOGGER = LoggerFactory.getLogger(CatFilter.class);

	private static final int MAX_LENGTH = 1000;

	private static final int MAX_ITEM_LENGTH = 50;

	private Map<SingleDataSource, StatusExtension> monitors = new ConcurrentHashMap<SingleDataSource, StatusExtension>();

	@Override
	public void init() {
		if (!Constants.ZEBRA_VERSION.equals(Version.ZEBRA_VERSION)) {
			LOGGER.warn("zebra-cat-client version(" + Version.ZEBRA_VERSION + ") is not same as zebra-client("
			      + Constants.ZEBRA_VERSION + ")");
		}
	}

	@Override
	public void closeSingleDataSource(SingleDataSource source, JdbcFilter chain) throws SQLException {
		chain.closeSingleDataSource(source, chain);
		Cat.logEvent("DataSource.Destoryed", source.getConfig().getId());
		StatusExtension monitor = this.monitors.remove(source);
		if (monitor != null) {
			try {
				StatusExtensionRegister.getInstance().unregister(monitor);
			} catch (Throwable ignore) {
			}
		}
	}

	@Override
	public SingleConnection getSingleConnection(SingleDataSource source, JdbcFilter chain) throws SQLException {
		Transaction t = null;
		try {
			t = Cat.newTransaction("SQL.Conn", source.getConfig().getId());
			SingleConnection conn = chain.getSingleConnection(source, chain);
			t.setStatus(Transaction.SUCCESS);
			return conn;
		} catch (SQLException exp) {
			Transaction sqlTransaction = Cat.newTransaction("SQL", DaoContextHolder.getSqlName());
			try {
				Cat.logEvent("SQL.Database", source.getConfig().getJdbcUrl(), "ERROR", source.getConfig().getId());
				Cat.logError(exp);
				sqlTransaction.setStatus(exp);
			} finally {
				sqlTransaction.complete();
			}
			throw exp;
		} finally {
			if (t != null) {
				t.complete();
			}
		}
	}

	@Override
	public <T> T executeSingleStatement(SingleStatement source, SingleConnection conn, String sql,
	      List<String> batchedSql, boolean isBatched, boolean autoCommit, Object sqlParams, JdbcFilter chain)
	      throws SQLException {
		SqlAliasManager.setSqlAlias(sql);
		Transaction t;
		if (isBatched) {
			t = Cat.newTransaction("SQL", "batched");
			t.addData(Stringizers.forJson().compact().from(batchedSql));
		} else {
			t = Cat.newTransaction("SQL", SqlAliasManager.getSqlAlias());
			t.addData(getSqlwithoutSqlId(sql));
		}
		T result = null;
		String status = Event.SUCCESS;
		try {
			result = chain.executeSingleStatement(source, conn, sql, batchedSql, isBatched, autoCommit, sqlParams, chain);
			t.setStatus(Transaction.SUCCESS);
			if (result instanceof SingleResultSet) {
				((SingleResultSet) result).setInfo(t);
			}
			return result;
		} catch (SQLException exp) {
			status = "ERROR";
			Cat.logError(exp);
			t.setStatus(exp);
			throw exp;
		} finally {
			try {
				logSqlMethodEvent(sql, batchedSql, isBatched, sqlParams);
				logSqlDatabaseEvent(conn, status);
			} catch (Throwable exp) {
				Cat.logError(exp);
			}
			t.complete();
		}
	}

	private String getSqlwithoutSqlId(String sql) {
		if (sql.trim().startsWith("/*id:")) {
			int pos = sql.indexOf("*/") + 2;
			return sql.substring(pos, sql.length());
		}
		return sql;
	}

	@Override
	public FailOverDataSource.FindMasterDataSourceResult findMasterFailOverDataSource(
	      FailOverDataSource.MasterDataSourceMonitor source, JdbcFilter chain) {
		FailOverDataSource.FindMasterDataSourceResult result = chain.findMasterFailOverDataSource(source, chain);
		if (result != null && result.isChangedMaster()) {
			Cat.logEvent("Zebra.Master", "Found-" + result.getDsId());
		}
		return result;
	}

	@Override
	public void initGroupDataSource(GroupDataSource source, JdbcFilter chain) {
		Transaction transaction = Cat.newTransaction(CAT_TYPE, "GroupDataSource.Init-" + source.getJdbcRef());
		try {
			chain.initGroupDataSource(source, chain);
			transaction.setStatus(Message.SUCCESS);
		} catch (RuntimeException e) {
			Cat.logError(e);
			transaction.setStatus(e);
			throw e;
		} finally {
			transaction.complete();
		}
	}

	@Override
	public DataSource initSingleDataSource(SingleDataSource source, JdbcFilter chain) {
		DataSource result = chain.initSingleDataSource(source, chain);
		SingleDataSourceMonitor monitor = new SingleDataSourceMonitor(source);
		this.monitors.put(source, monitor);
		try {
			StatusExtensionRegister.getInstance().register(monitor);
		} catch (Throwable ignore) {
		}
		Cat.logEvent("SingleDataSource.Created", source.getConfig().getJdbcUrl());
		Cat.logEvent("SingleDataSource.Type", source.getConfig().getType());
		return result;
	}

	@Override
	public void refreshGroupDataSource(GroupDataSource source, String propertiesName, JdbcFilter chain) {
		Transaction t = Cat.newTransaction(CAT_TYPE, "GroupDataSource.Refresh-" + source.getJdbcRef());
		Cat.logEvent("Zebra.Refresh.Property", propertiesName);
		try {
			chain.refreshGroupDataSource(source, propertiesName, chain);
			t.setStatus(Message.SUCCESS);
		} catch (RuntimeException exp) {
			Cat.logError(exp);
			t.setStatus(exp);
			throw exp;
		} finally {
			t.complete();
		}
	}

	private void logSqlDatabaseEvent(SingleConnection conn, String status) throws SQLException {
		SingleConnection singleConnection = conn instanceof SingleConnection ? (SingleConnection) conn : null;
		if (singleConnection != null && conn.getMetaData() != null) {
			Cat.logEvent("SQL.Database", conn.getMetaData().getURL(), status, singleConnection.getDataSourceId());
		}
	}

	private void logSqlLengthEvent(String sql) {
		int length = (sql == null) ? 0 : sql.length();
		if (length <= SqlMonitorUtils.BIG_SQL) {
			Cat.logEvent("SQL.Length", SqlMonitorUtils.getSqlLengthName(length), Message.SUCCESS, "");
		} else {
			Cat.logEvent("SQL.Length", SqlMonitorUtils.getSqlLengthName(length), "long-sql warning", "");
		}
	}

	private void logSqlMethodEvent(String sql, List<String> batchedSql, boolean isBatched, Object sqlParams) {
		String params = Stringizers.forJson().compact()
		      .from(sqlParams, MAX_LENGTH, MAX_ITEM_LENGTH);
		if (isBatched) {
			if (batchedSql != null) {
				for (String bSql : batchedSql) {
					Cat.logEvent("SQL.Method", SqlUtils.buildSqlType(bSql), Event.SUCCESS, params);
					logSqlLengthEvent(sql);
				}
			}
		} else {
			if (sql != null) {
				Cat.logEvent("SQL.Method", SqlUtils.buildSqlType(sql), Event.SUCCESS, params);
				logSqlLengthEvent(sql);
			}
		}
	}

	@Override
	public void switchFailOverDataSource(FailOverDataSource source, JdbcFilter chain) {
		Transaction t = Cat.newTransaction(CAT_TYPE, "FailOver");
		try {
			chain.switchFailOverDataSource(source, chain);
			Cat.logEvent("Zebra.FailOver", "Success");
			t.setStatus(Message.SUCCESS);
		} catch (RuntimeException exp) {
			Cat.logEvent("Zebra.FailOver", "Failed");
			Cat.logError(exp);
			t.setStatus("Fail to find any master database");
			throw exp;
		} finally {
			t.complete();
		}
	}

	@Override
	public void closeSingleResultSet(SingleResultSet source, JdbcFilter chain) throws SQLException {
		if (source != null) {
			int rowCount = source.getRowCount();
			Object info = source.getInfo();
			if (info instanceof DefaultTransaction) {
				if (rowCount <= SqlMonitorUtils.BIG_RESPONSE) {
					Message message = new DefaultEvent("SQL.Rows", SqlMonitorUtils.getSqlRowsName(rowCount));
					message.setStatus(Message.SUCCESS);
					message.addData(String.valueOf(rowCount));
					((DefaultTransaction) info).addChild(message);
				} else {
					Message message = new DefaultEvent("SQL.Rows", SqlMonitorUtils.getSqlRowsName(rowCount));
					message.setStatus("too much rows returned");
					message.addData(String.valueOf(rowCount));
					((DefaultTransaction) info).addChild(message);
					((DefaultTransaction) info).setStatus("fail");
				}
			}
		}
		chain.closeSingleResultSet(source, chain);
	}

	@Override
	public void initShardDataSource(com.dianping.zebra.shard.jdbc.ShardDataSource source, JdbcFilter chain) {
		String ruleName = source.getRuleName();
		if (StringUtils.isBlank(ruleName)) {
			ruleName = "localMode";
		}

		Transaction transaction = Cat.newTransaction(CAT_TYPE, "ShardDataSource.Init-" + ruleName);

		try {
			chain.initShardDataSource(source, chain);
			transaction.setStatus(Message.SUCCESS);
		} catch (RuntimeException e) {
			Cat.logError(e);
			transaction.setStatus(e);
			throw e;
		} finally {
			transaction.complete();
		}
	};

	@Override
	public ResultSet executeShardQuery(ShardStatement source, String sql, JdbcFilter chain) throws SQLException {
		SqlAliasManager.setSqlAlias(sql);
		Transaction t = Cat.newTransaction(SHARD_CAT_TYPE, SqlAliasManager.getSqlAlias());
		ResultSet result = null;
		try {
			result = chain.executeShardQuery(source, sql, chain);
			t.setStatus(Message.SUCCESS);
		} catch (Throwable exp) {
			Cat.logError(exp);
			t.setStatus(exp);
			throw new SQLException(exp);
		} finally {
			t.complete();
		}
		return result;
	}

	@Override
	public void shardRouting(RouterResult rr, JdbcFilter chain) throws SQLException {
		if (rr != null) {
			if (rr.getSqls() != null) {
				for (RouterResult.RouterTarget routerTarget : rr.getSqls()) {
					Cat.logEvent("ShardSQL.Route", routerTarget.getDatabaseName(), Message.SUCCESS,
					      String.valueOf(routerTarget.getSqls().size()));
				}
			} else if (rr.getParams() != null) {
				Cat.logEvent("ShardSQL.Route", null, "Invalid Params", Stringizers.forJson().compact()
				      .from(rr.getParams()));
			}
		}
		chain.shardRouting(rr, chain);
	}

	@Override
	public int executeShardUpdate(ShardStatement source, String sql, int autoGeneratedKeys, int[] columnIndexes,
	      String[] columnNames, JdbcFilter chain) throws SQLException {
		SqlAliasManager.setSqlAlias(sql);
		Transaction t = Cat.newTransaction(SHARD_CAT_TYPE, SqlAliasManager.getSqlAlias());
		int result;
		try {
			result = chain.executeShardUpdate(source, sql, autoGeneratedKeys, columnIndexes, columnNames, chain);
			t.setStatus(Message.SUCCESS);
		} catch (Throwable exp) {
			Cat.logError(exp);
			t.setStatus(exp);
			throw new SQLException(exp);
		} finally {
			t.complete();
		}
		return result;
	}

	@Override
	public void shardMerge(ShardResultSet rs, JdbcFilter chain) throws SQLException {
		Cat.logEvent("ShardSQL.Merge", "Begin");
		chain.shardMerge(rs, chain);
	}

	@Override
	public void configChanged(PropertyChangeEvent evt, JdbcFilter chain) {
		if (evt != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String name = evt.getPropertyName() + "-" + format.format(new Date());
			String newValue = (evt.getNewValue() == null ? null : evt.getNewValue().toString());
			Cat.logEvent("SQL.ConfigChange", name, "0", newValue);
			chain.configChanged(evt, chain);
		}
	}
}
