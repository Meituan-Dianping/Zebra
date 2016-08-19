package com.dianping.zebra.monitor.filter;

import groovy.sql.Sql;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.group.util.DaoContextHolder;

/**
 * Created by Dozer on 9/9/14.
 */

@Ignore
public class CatFilterTest {

	@BeforeClass
	public static void init() throws SQLException {
		GroupDataSource ds = createDs();

		Sql sql = new Sql(ds.getConnection());
		sql.execute("CREATE TABLE Persons\n" + "(\n" + "Id int,\n" + "LastName varchar(255),\n"
		      + "FirstName varchar(255),\n" + "Address varchar(255),\n" + "City varchar(255)\n" + ")");
		sql.execute("insert into persons (id,lastname,firstname,address,city) values (1,'','','','')");
	}

	public static GroupDataSource createDs() {
		GroupDataSource ds = new GroupDataSource();
		ds.setConfigManagerType(Constants.CONFIG_MANAGER_TYPE_LOCAL);
		ds.setJdbcRef("sample.ds.v2");
		ds.setFilter("!wall");
		ds.init();

		return ds;
	}

	@Test(expected = ZebraException.class, timeout = 30000)
	public void test_connect_fail() throws SQLException {
		GroupDataSource ds = new GroupDataSource();
		ds.setConfigManagerType(Constants.CONFIG_MANAGER_TYPE_LOCAL);
		ds.setJdbcRef("sample.ds.error");
		ds.setFilter("!wall,cat");
		ds.init();
		new Sql(ds.getConnection()).execute("select 1");
	}

//	@Test
//	public void test_connect_retry() throws SQLException {
//		GroupDataSource ds = new GroupDataSource();
//		ds.setConfigManagerType(Constants.CONFIG_MANAGER_TYPE_LOCAL);
//		ds.setJdbcRef("sample.ds.retry");
//		ds.setFilter("cat");
//		ds.init();
//
//		ExecutionContextHolder.getContext().add("sql_statement_name", "testPreparedStatementQuery");
//
//		new Sql(ds.getConnection()).execute("select 1");
//	}

	@Test
	public void test_sql_success() throws SQLException {
		GroupDataSource ds = createDs();

		new Sql(ds.getConnection()).execute(Constants.SQL_FORCE_WRITE_HINT + "select * from Persons");
	}

	@Test
	public void test_sql_success1() throws SQLException {
		GroupDataSource ds = createDs();

		DaoContextHolder.setSqlName("testPreparedStatementQuery");

		new Sql(ds.getConnection()).execute(Constants.SQL_FORCE_WRITE_HINT + "select * from Persons");
	}

	@Test(expected = Exception.class)
	public void test_sql_fail_on_slave() throws SQLException {
		GroupDataSource ds = createDs();

		new Sql(ds.getConnection()).execute("select * from xxx");
	}
	
//	@Test(expected = Exception.class)
//	public void test_sql_fail_on_master() throws SQLException {
//		GroupDataSource ds = new GroupDataSource();
//		ds.setConfigManagerType(Constants.CONFIG_MANAGER_TYPE_LOCAL);
//		ds.setJdbcRef("sample.ds.v3");
//		ds.setFilter("cat");
//		ds.init();
//
//		new Sql(ds.getConnection()).execute("delete from xxx");
//	}

	@Test(expected = SQLException.class)
	public void test_sql_rejected_by_flow_control() throws SQLException {
		GroupDataSource ds = new GroupDataSource();
		ds.setConfigManagerType(Constants.CONFIG_MANAGER_TYPE_LOCAL);
		ds.setJdbcRef("sample.ds.v2");
		ds.setFilter("wall,cat");
		ds.init();

		DaoContextHolder.setSqlName("test");

		new Sql(ds.getConnection()).execute("select 1", new Object[0]);
	}

}