/**
 * Project: ${zebra-client.aid}
 * 
 * File Created at 2011-6-7 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
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

	private boolean closed = false;

	private boolean readOnly;

	private boolean autoCommit = true;

	private int transactionIsolation = -1;

	public ShardConnection() {
	}

	public ShardConnection(String username, String password) {
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
		} finally {
			closed = true;
			attachedStatements.clear();
			actualConnections.clear();
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

		JDBCUtils.throwSQLExceptionIfNeeded(innerExceptions);
	}

	@Override
	public Statement createStatement() throws SQLException {
		checkClosed();

		ShardStatement stmt = new ShardStatement();
		stmt.setRouter(router);
		stmt.setAutoCommit(autoCommit);
		stmt.setReadOnly(readOnly);
		stmt.setConnection(this);

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
			actualConnections.put(jdbcRef, conn);
		}

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

		ShardPreparedStatement stmt = new ShardPreparedStatement();
		stmt.setRouter(router);
		stmt.setAutoCommit(autoCommit);
		stmt.setReadOnly(readOnly);
		stmt.setConnection(this);
		stmt.setSql(sql);

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
		ShardPreparedStatement stmt = (ShardPreparedStatement) prepareStatement(sql, resultSetType,
				resultSetConcurrency);
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
}
