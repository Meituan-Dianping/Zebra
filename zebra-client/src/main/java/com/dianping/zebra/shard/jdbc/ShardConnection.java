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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.shard.jdbc.unsupport.UnsupportedShardConnection;
import com.dianping.zebra.shard.router.ShardRouter;
import com.dianping.zebra.util.JDBCUtils;

/**
 * @author Leo Liang
 * @author hao.zhu
 * 
 */
public class ShardConnection extends UnsupportedShardConnection implements Connection {

	private ShardRouter router;

	private DataSourceRepository dataSourceRepository;

	private Map<String, Connection> actualConnections = new HashMap<String, Connection>();

	private Set<Statement> attachedStatements = new HashSet<Statement>();

	private Map<String, List<Connection>> concurrentConnections = new HashMap<String, List<Connection>>();

	private Map<String, Integer> concurrentConnectionIndexes = new HashMap<String, Integer>();

	private boolean closed = false;

	private boolean readOnly;

	private boolean autoCommit = true;

	private int transactionIsolation = -1;

	private final List<JdbcFilter> filters;

	private int concurrencyLevel = 1; // 单库并发度

	public ShardConnection(List<JdbcFilter> filters) {
		this.filters = filters;
	}

	public ShardConnection(String username, String password, List<JdbcFilter> filters) {
		this.filters = filters;
	}

	public ShardConnection(String username, String password, List<JdbcFilter> filters, int concurrencyLevel) {
		this.filters = filters;
		this.concurrencyLevel = concurrencyLevel;
	}

	private void checkClosed() throws SQLException {
		if (closed) {
			throw new SQLException("No operations allowed after connection closed.");
		}
	}

	@Override
	public void close() throws SQLException {
		if (closed) {
			return;
		}

		List<SQLException> innerExceptions = new ArrayList<SQLException>();

		try {
			for (Statement stmt : attachedStatements) {
				try {
					stmt.close();
				} catch (SQLException e) {
					innerExceptions.add(e);
				}
			}

			for (Map.Entry<String, Connection> entry : actualConnections.entrySet()) {
				try {
					entry.getValue().close();
				} catch (SQLException e) {
					innerExceptions.add(e);
				}
			}

			// table concurrent connection
			for (List<Connection> connections : concurrentConnections.values()) {
				if (connections != null) {
					for (Connection connection : connections) {
						try {
							connection.close();
						} catch (SQLException e) {
							innerExceptions.add(e);
						}
					}
				}
			}
		} finally {
			closed = true;
			attachedStatements.clear();
			actualConnections.clear();
			concurrentConnections.clear();
		}

		JDBCUtils.throwSQLExceptionIfNeeded(innerExceptions);
	}

	@Override
	public void commit() throws SQLException {
		checkClosed();

		if (autoCommit) {
			return;
		}

		List<SQLException> innerExceptions = new ArrayList<SQLException>();

		for (Map.Entry<String, Connection> entry : actualConnections.entrySet()) {
			try {
				entry.getValue().commit();
			} catch (SQLException e) {
				innerExceptions.add(e);
			}
		}

		// table concurrent connection
		for (List<Connection> connections : concurrentConnections.values()) {
			if (connections != null) {
				for (Connection connection : connections) {
					try {
						connection.commit();
					} catch (SQLException e) {
						innerExceptions.add(e);
					}
				}
			}
		}

		JDBCUtils.throwSQLExceptionIfNeeded(innerExceptions);
	}

	@Override
	public Statement createStatement() throws SQLException {
		checkClosed();

		ShardStatement stmt = new ShardStatement(filters);
		stmt.setRouter(router);
		stmt.setAutoCommit(autoCommit);
		stmt.setReadOnly(readOnly);
		stmt.setConnection(this);
		stmt.setConcurrencyLevel(concurrencyLevel);

		attachedStatements.add(stmt);

		return stmt;
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		ShardStatement stmt = (ShardStatement) createStatement();

		stmt.setResultSetType(resultSetType);
		stmt.setResultSetConcurrency(resultSetConcurrency);

		return stmt;
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
	      throws SQLException {
		ShardStatement stmt = (ShardStatement) createStatement(resultSetType, resultSetConcurrency);

		stmt.setResultSetHoldability(resultSetHoldability);

		return stmt;
	}

	Connection getRealConnection(String jdbcRef, boolean autoCommit) throws SQLException {
		Connection conn = actualConnections.get(jdbcRef);

		if (conn == null) {
			conn = dataSourceRepository.getDataSource(jdbcRef).getConnection();
			conn.setAutoCommit(autoCommit);
			if (transactionIsolation > 0) {
				conn.setTransactionIsolation(transactionIsolation);
			}
			actualConnections.put(jdbcRef, conn);
		}

		return conn;
	}

	// Each call returns a new connection
	Connection getRealConcurrentConnection(String jdbcRef, boolean autoCommit) throws SQLException {
		List<Connection> connections = concurrentConnections.get(jdbcRef);
		Integer index = concurrentConnectionIndexes.get(jdbcRef);
		if (connections == null) {
			connections = new ArrayList<Connection>();
			concurrentConnections.put(jdbcRef, connections);
		}
		if (index == null) {
			index = 0;
		}
		Connection conn = null;
		if (index < connections.size()) {
			conn = connections.get(index);
		} else {
			conn = dataSourceRepository.getDataSource(jdbcRef).getConnection();
			conn.setAutoCommit(autoCommit);
			connections.add(conn);
		}
		concurrentConnectionIndexes.put(jdbcRef, index + 1);
		return conn;
	}

	public Set<Statement> getAttachedStatements() {
		return attachedStatements;
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return autoCommit;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		checkClosed();
		return new ShardDatabaseMetaData();
	}

	public ShardRouter getRouter() {
		return router;
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return transactionIsolation;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return readOnly;
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		checkClosed();

		ShardPreparedStatement stmt = new ShardPreparedStatement(filters);
		stmt.setRouter(router);
		stmt.setAutoCommit(autoCommit);
		stmt.setReadOnly(readOnly);
		stmt.setConnection(this);
		stmt.setSql(sql);
		stmt.setConcurrencyLevel(concurrencyLevel);

		attachedStatements.add(stmt);

		return stmt;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		ShardPreparedStatement stmt = (ShardPreparedStatement) prepareStatement(sql);
		stmt.setAutoGeneratedKeys(autoGeneratedKeys);
		return stmt;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
	      throws SQLException {
		ShardPreparedStatement stmt = (ShardPreparedStatement) prepareStatement(sql);
		stmt.setResultSetType(resultSetType);
		stmt.setResultSetConcurrency(resultSetConcurrency);
		return stmt;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
	      int resultSetHoldability) throws SQLException {
		ShardPreparedStatement stmt = (ShardPreparedStatement) prepareStatement(sql, resultSetType, resultSetConcurrency);
		stmt.setResultSetHoldability(resultSetHoldability);
		return stmt;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		ShardPreparedStatement stmt = (ShardPreparedStatement) prepareStatement(sql);
		stmt.setColumnIndexes(columnIndexes);
		return stmt;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		ShardPreparedStatement stmt = (ShardPreparedStatement) prepareStatement(sql);
		stmt.setColumnNames(columnNames);
		return stmt;
	}

	@Override
	public void rollback() throws SQLException {
		checkClosed();

		if (autoCommit) {
			return;
		}

		List<SQLException> exceptions = new ArrayList<SQLException>();

		for (Map.Entry<String, Connection> entry : actualConnections.entrySet()) {
			try {
				entry.getValue().rollback();
			} catch (SQLException e) {
				exceptions.add(e);
			}
		}

		// table concurrent connection
		for (List<Connection> connections : concurrentConnections.values()) {
			if (connections != null) {
				for (Connection connection : connections) {
					try {
						connection.rollback();
					} catch (SQLException e) {
						exceptions.add(e);
					}
				}
			}
		}

		JDBCUtils.throwSQLExceptionIfNeeded(exceptions);
	}

	public void setActualConnections(Map<String, Connection> actualConnections) {
		this.actualConnections = actualConnections;
	}

	public void setAttachedStatements(Set<Statement> attachedStatements) {
		this.attachedStatements = attachedStatements;
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		checkClosed();
		this.autoCommit = autoCommit;
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		checkClosed();

		this.readOnly = readOnly;
	}

	public void setRouter(ShardRouter router) {
		this.router = router;
	}

	public void setDataSourceRepository(DataSourceRepository dataSourceRepository) {
		this.dataSourceRepository = dataSourceRepository;
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		checkClosed();
		this.transactionIsolation = level;
	}

	public void resetConcurrentConnectionIndexes() {
		this.concurrentConnectionIndexes.clear();
	}
}
