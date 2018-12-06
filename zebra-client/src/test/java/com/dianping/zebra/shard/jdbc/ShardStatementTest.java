package com.dianping.zebra.shard.jdbc;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;

public class ShardStatementTest extends MultiDBBaseTestCase {

	private String insertSql = "insert into test(name,id,score,type,classid) values('damon.zhu',1,28,'stu',2)";

	private String selectSql = "select * from test where id = 1";

	private String selectForUpdateSql = "select * from test where id = 1 for update";

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
		Statement stmt = conn.createStatement();

		Assert.assertFalse(stmt.execute(insertSql));

		Assert.assertTrue(stmt.execute(selectSql));

		Assert.assertFalse(stmt.execute(insertSql, Statement.RETURN_GENERATED_KEYS));
		Assert.assertTrue(stmt.getGeneratedKeys().next());

		Assert.assertFalse(stmt.execute(insertSql, new int[] { 1 }));
//		Assert.assertTrue(stmt.getGeneratedKeys().next());

		Assert.assertFalse(stmt.execute(insertSql, new String[] { "col" }));
//		Assert.assertTrue(stmt.getGeneratedKeys().next());

		Assert.assertTrue(stmt.executeQuery(selectSql).next());

		Assert.assertEquals(stmt.executeUpdate(insertSql), 1);

		Assert.assertEquals(stmt.executeUpdate(insertSql, Statement.RETURN_GENERATED_KEYS), 1);
		Assert.assertTrue(stmt.getGeneratedKeys().next());

		Assert.assertEquals(stmt.executeUpdate(insertSql, new int[] { 1 }), 1);
//		Assert.assertTrue(stmt.getGeneratedKeys().next());

		Assert.assertEquals(stmt.executeUpdate(insertSql, new String[] { "col" }), 1);
//		Assert.assertTrue(stmt.getGeneratedKeys().next());

		Assert.assertTrue(stmt.execute(selectForUpdateSql));
		stmt.close();
	}
}
