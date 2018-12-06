package com.dianping.zebra.shard.jdbc;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;

public class ShardConnectionTest extends MultiDBBaseTestCase {

	@Override
	protected String getDBBaseUrl() {
		return "jdbc:h2:mem:";
	}

	@Override
	protected String getCreateScriptConfigFile() {
		return "db-datafiles/createtable-multidb-lifecycle.xml";
	}

	@Override
	protected String getDataFile() {
		return "db-datafiles/data-multidb-lifecycle.xml";
	}

	@Override
	protected String[] getSpringConfigLocations() {
		return new String[] { "ctx-multidb-lifecycle.xml" };
	}

	public DataSource getDataSource() {
		return (DataSource) context.getBean("zebraDS");
	}

	@Test
	public void test_sql_connection_api_supported() throws SQLException {
		Connection conn = getDataSource().getConnection();

		assertFalse(conn.isClosed());
		assertTrue(conn.getAutoCommit());
		conn.setAutoCommit(false);
		assertFalse(conn.getAutoCommit());

		assertTrue((conn.createStatement() instanceof ShardStatement));
		assertTrue((conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE) instanceof ShardStatement));
		assertTrue((conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE,
				ResultSet.HOLD_CURSORS_OVER_COMMIT) instanceof ShardStatement));

		assertTrue((conn.prepareStatement("sql") instanceof ShardPreparedStatement));
		assertTrue((conn.prepareStatement("sql", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE) instanceof ShardPreparedStatement));
		assertTrue((conn.prepareStatement("sql", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE,
				ResultSet.HOLD_CURSORS_OVER_COMMIT) instanceof ShardPreparedStatement));
		assertTrue((conn.prepareStatement("sql", Statement.RETURN_GENERATED_KEYS) instanceof ShardPreparedStatement));
		assertTrue((conn.prepareStatement("sql", new int[0]) instanceof ShardPreparedStatement));
		assertTrue((conn.prepareStatement("sql", new String[0]) instanceof ShardPreparedStatement));

		conn.close();
	}

	@Test
	public void test_transaction_when_hit_specified_conn() throws SQLException {
		Connection conn = getDataSource().getConnection();
		conn.setAutoCommit(false);

		Connection conn1 = getDataSource().getConnection();

		Statement stmt = conn.createStatement();

		stmt.executeUpdate("insert into test(id, name, score, type, classid) values(100, 'damon.zhu', 1, 'a', 1)");
		stmt.close();

		Statement stmt1 = conn1.createStatement();
		stmt1.execute("select * from test where id = 100");

		ResultSet executeSelectSql1 = stmt1.getResultSet();

		assertEquals(executeSelectSql1.next(), false);

		conn.commit();

		stmt1.execute("select * from test where id = 100");

		executeSelectSql1 = stmt1.getResultSet();

		assertEquals(executeSelectSql1.next(), true);

		assertEquals(executeSelectSql1.getString(2), "damon.zhu");

		stmt.close();
		stmt1.close();
		conn.close();
		conn1.close();
	}

	@Test
	public void test_transaction_when_hit_specified_conn2() throws SQLException {
		Connection conn = getDataSource().getConnection();
		conn.setAutoCommit(false);

		Connection conn1 = getDataSource().getConnection();

		Statement stmt = conn.createStatement();

		stmt.executeUpdate("insert into test(id, name, score, type, classid) values(100, 'damon.zhu', 1, 'a', 1)");
		stmt.executeUpdate("insert into test(id, name, score, type, classid) values(92, 'damon.zhu', 1, 'a', 1)");
		stmt.close();

		Statement stmt1 = conn1.createStatement();
		stmt1.execute("select * from test where id = 100");
		ResultSet executeSelectSql1 = stmt1.getResultSet();
		assertEquals(executeSelectSql1.next(), false);

		stmt1.execute("select * from test where id = 92");
		executeSelectSql1 = stmt1.getResultSet();
		assertEquals(executeSelectSql1.next(), false);

		conn.commit();

		stmt1.execute("select * from test where id = 100");
		executeSelectSql1 = stmt1.getResultSet();
		assertEquals(executeSelectSql1.next(), true);
		assertEquals(executeSelectSql1.getString(2), "damon.zhu");

		stmt1.execute("select * from test where id = 92");
		executeSelectSql1 = stmt1.getResultSet();
		assertEquals(executeSelectSql1.next(), true);
		assertEquals(executeSelectSql1.getString(2), "damon.zhu");

		stmt.close();
		stmt1.close();
		conn.close();
		conn1.close();
	}

	//连真实的mysql数据库可测，这里是连beta环境的welife
	//@Test
	public void test_select_idendity() throws SQLException{
		ShardDataSource ds = new ShardDataSource();
		ds.setRuleName("welife");

		ds.init();

		Connection conn = ds.getConnection();

		Statement stmt = conn.createStatement();

		int executeUpdate = stmt.executeUpdate("insert into welife_users(uid,bid) values (16,2127114697)");

		assertEquals(1, executeUpdate);

		ResultSet rs = stmt.executeQuery("select @@identity");

		assertEquals(rs.next(), true);

		System.out.println(rs.getInt(1));

		rs = stmt.executeQuery("select last_insert_id()");

		assertEquals(rs.next(), true);

		System.out.println(rs.getInt(1));
	}

	//@Test
	public void test_insert_ignore() throws SQLException {
		ShardDataSource ds = new ShardDataSource();
		ds.setRuleName("welife");

		ds.init();

		Connection conn = ds.getConnection();
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("insert into welife_users(uid,bid) values (20,2127114697)");
		stmt.close();
		conn.close();
	}


}
