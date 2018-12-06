package com.dianping.zebra.shard.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;

public class ShardPreparedStatementTest extends MultiDBBaseTestCase {

	private String insertSql = "insert into test(name,id,score,type,classid) values('damon.zhu',2,30,'stu',1)";

	private String selectSql = "select * from test where id = ?";

	private String updateSql = "update test set name = ? where id = ?";

	private String selectForUpdateSql = "select * from test where id = ? for update";

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
	public void test_statement_api_support() throws Exception {
		Connection conn = getDataSource().getConnection();
		PreparedStatement stmt = conn.prepareStatement(insertSql);
		// Statement.execute如果第一个结果为 ResultSet 对象，则返回 true；如果其为更新计数或者不存在任何结果，则返回 false
		Assert.assertFalse(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(selectSql);
		stmt.setInt(1, 28);
		Assert.assertTrue(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(selectSql);
		stmt.setInt(1, 28);
		Assert.assertFalse(stmt.executeQuery().next());
		stmt.close();

		stmt = conn.prepareStatement(updateSql);
		stmt.setString(1, "damon-zhu1");
		stmt.setInt(2, 2);
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

		stmt = conn.prepareStatement(selectForUpdateSql);
		stmt.setInt(1, 28);
		Assert.assertTrue(stmt.execute());
		stmt.close();

		conn.close();
	}
}
