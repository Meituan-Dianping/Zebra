package com.dianping.zebra.group.jdbc;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

public abstract class InnerSingleDatabaseTestCase extends H2DatabaseTestCase {

	private GroupDataSource dataSource;

	protected Object execute(ConnectionCallback callback) throws Exception {
		Connection connection = null;
		try {
			connection = getDataSource().getConnection();

			return callback.doInConnection(connection);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					// ignore it
				}
			}
		}
	}

	protected Object execute(StatementCallback callback) throws Exception {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();

			return callback.doInStatement(statement);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception e) {
					// ignore it
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					// ignore it
				}
			}
		}
	}
	
	protected abstract String getConfigManagerType();

	protected DataSource getDataSource() {
		if (this.dataSource == null) {
			this.dataSource = new GroupDataSource(getResourceId());
			this.dataSource.setConfigManagerType(getConfigManagerType());
			this.dataSource.init();
		}

		return this.dataSource;
	}

	protected abstract String getResourceId();

	protected IDataSet readDataSet(String dataSets) throws Exception {
		return new FlatXmlDataSetBuilder().build(getClass().getClassLoader().getResource(dataSets));
	}

	protected interface ConnectionCallback {
		public Object doInConnection(Connection conn) throws Exception;
	}

	protected interface StatementCallback {
		public Object doInStatement(Statement stmt) throws Exception;
	}
}