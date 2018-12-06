package com.dianping.zebra.shard.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;

public class ShardPreparedStatementMultiKeyTest extends MultiDBBaseTestCase {

	private String insertSql = "insert into test(name,id,score,type,classid) values('damon.zhu',2,30,'stu',1)";

	private String selectSql1 = "select * from test where classid = ?";

	private String selectSql2 = "select * from test where id = ? and name = ?";

	private String updateSql = "update test set name = ? where id = ? and name = ?";

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
		return new String[] { "ctx-multidb-multikey-lifecycle.xml" };
	}

	public DataSource getDataSource() {
		return (DataSource) context.getBean("zebraDS");
	}

	@Test
	public void test_statement_api_support() throws Exception {
		Connection conn = getDataSource().getConnection();
		PreparedStatement stmt = conn.prepareStatement(insertSql);
		Assert.assertFalse(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(selectSql1);
		stmt.setInt(1, 28);
		Assert.assertTrue(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(selectSql1);
		stmt.setInt(1, 28);
		Assert.assertFalse(stmt.executeQuery().next());
		stmt.close();

		stmt = conn.prepareStatement(selectSql2);
		stmt.setInt(1, 2);
		stmt.setString(2, "damon.zhu");
		Assert.assertTrue(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(updateSql);
		stmt.setString(1, "damon-zhu1");
		stmt.setInt(2, 2);
		stmt.setString(3, "damon.zhu");
		Assert.assertFalse(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
		Assert.assertFalse(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(insertSql, new int[] { 1 });
		Assert.assertFalse(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(insertSql, new String[] { "col" });
		Assert.assertFalse(stmt.execute());

		Assert.assertEquals(stmt.executeUpdate(), 1);
		stmt.close();

		conn.close();
	}
}
