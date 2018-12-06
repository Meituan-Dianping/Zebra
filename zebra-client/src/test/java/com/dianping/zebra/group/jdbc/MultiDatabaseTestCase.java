package com.dianping.zebra.group.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.h2.tools.RunScript;
import org.junit.Before;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public abstract class MultiDatabaseTestCase extends InnerSingleDatabaseTestCase {

	private DataSource writeDs;

	private List<DataSource> readDses = new ArrayList<DataSource>();

	@Before
	public void createTableAndImportDataSet() throws Exception {
		for (DataSourceEntry entry : getDataSourceEntryArray()) {
			RunScript.execute(entry.getJdbcUrl(), USER, PASSWORD, getSchema(), "UTF8", false);
			cleanlyInsert(JDBC_DRIVER, entry.getJdbcUrl(), USER, PASSWORD, readDataSet(entry.getDataSets()));
			if (entry.isWrite()) {
				writeDs = createRealDataSource(entry.getJdbcUrl(), USER, PASSWORD, JDBC_DRIVER);
			} else {
				readDses.add(createRealDataSource(entry.getJdbcUrl(), USER, PASSWORD, JDBC_DRIVER));
			}
		}
	}

	private DataSource createRealDataSource(String jdbcUrl, String user, String password, String driverClass)
			throws Exception {
		ComboPooledDataSource ds = new ComboPooledDataSource();
		ds.setJdbcUrl(jdbcUrl);
		ds.setUser(user);
		ds.setPassword(password);
		ds.setDriverClass(driverClass);
		return ds;
	}

	// please do not override it.
	protected String getDataSets() {
		return null;
	}

	protected Object executeOnRealDB(ConnectionCallback callback, boolean write, int index) throws Exception {
		Connection connection = null;
		try {
			if (write) {
				connection = getWriteDataSource().getConnection();
			} else {
				connection = getReadDataSrouce(index).getConnection();
			}

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

	protected int getReadDataSourcesCount() {
		return readDses.size();
	}

	private DataSource getReadDataSrouce(int index) {
		return readDses.get(index);
	}

	private DataSource getWriteDataSource() {
		return writeDs;
	}

	protected abstract DataSourceEntry[] getDataSourceEntryArray();
}
