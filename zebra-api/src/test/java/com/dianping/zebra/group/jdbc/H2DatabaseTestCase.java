package com.dianping.zebra.group.jdbc;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.h2.tools.RunScript;
import org.junit.Before;

import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.filter.MockFilterHelper;

public abstract class H2DatabaseTestCase {
	protected static final String JDBC_DRIVER = org.h2.Driver.class.getName();

	protected static final String JDBC_URL = "jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1";

	protected static final String PASSWORD = "";

	protected static final String USER = "sa";

	protected JdbcFilter mockedFilter;

	protected void cleanlyInsert(String driver, String jdbcUrl, String user, String password, IDataSet dataSet)
			throws Exception {
		IDatabaseTester databaseTester = new JdbcDatabaseTester(driver, jdbcUrl, user, password);
		databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
		databaseTester.setDataSet(dataSet);
		databaseTester.onSetup();
	}

	@Before
	public void createTableAndImportDataSet() throws Exception {
		RunScript.execute(JDBC_URL, USER, PASSWORD, getSchema(), "UTF8", false);
		cleanlyInsert(JDBC_DRIVER, JDBC_URL, USER, PASSWORD, readDataSet());
	}

	protected abstract String getDataSets();

	protected abstract String getSchema();

	@Before
	public void mockFilter() {
		MockFilterHelper.injectMockFilter();
		mockedFilter = MockFilterHelper.getMockedFilter();
	}

	private IDataSet readDataSet() throws Exception {
		return new FlatXmlDataSetBuilder().build(getClass().getClassLoader().getResource(getDataSets()));
	}
}
