package com.dianping.zebra.group.jdbc;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Statement;

public class DPGroupStatementTest extends MultiDatabaseTestCase {

	private String insertSql = "insert into PERSON(NAME,LAST_NAME,AGE) values('damon.zhu','zhu',28)";

	private String selectSql = "select * from PERSON";

	private String updateSql = "update PERSON p set p.Name = 'damon.zhu'";

	@Override
	protected String getConfigManagerType() {
		return "local";
	}

	@Override
	protected DataSourceEntry[] getDataSourceEntryArray() {
		DataSourceEntry[] entries = new DataSourceEntry[3];

		entries[0] = new DataSourceEntry("jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets.xml", true);
		entries[1] = new DataSourceEntry("jdbc:h2:mem:test1;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets1.xml", false);
		entries[2] = new DataSourceEntry("jdbc:h2:mem:test2;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets2.xml", false);

		return entries;
	}

	@Override
	protected String getResourceId() {
		return "sample.ds.v2";
	}

	@Override
	protected String getSchema() {
		return getClass().getResource("/schema.sql").getPath();
	}

	@Test
	public void test_statement_api_support() throws Exception {
		execute(new ConnectionCallback() {

			@Override
			public Object doInConnection(Connection conn) throws Exception {
				Statement stmt = conn.createStatement();

				//Statement.execute如果第一个结果为 ResultSet 对象，则返回 true；如果其为更新计数或者不存在任何结果，则返回 false

				Assert.assertFalse(stmt.execute(insertSql));

				Assert.assertTrue(stmt.execute(selectSql));

				Assert.assertFalse(stmt.execute(insertSql, Statement.RETURN_GENERATED_KEYS));
				Assert.assertTrue(stmt.getGeneratedKeys().next());

				Assert.assertFalse(stmt.execute(insertSql, new int[] { 1 }));
				Assert.assertTrue(stmt.getGeneratedKeys().next());

				Assert.assertFalse(stmt.execute(insertSql, new String[] { "col" }));
				Assert.assertTrue(stmt.getGeneratedKeys().next());

				stmt.addBatch(insertSql);
				stmt.addBatch(updateSql);

				int[] updateCounts = stmt.executeBatch();

				Assert.assertEquals(updateCounts.length, 2);

				Assert.assertTrue(stmt.executeQuery(selectSql).next());

				Assert.assertEquals(stmt.executeUpdate(insertSql), 1);

				Assert.assertEquals(stmt.executeUpdate(insertSql, Statement.RETURN_GENERATED_KEYS), 1);
				Assert.assertTrue(stmt.getGeneratedKeys().next());

				Assert.assertEquals(stmt.executeUpdate(insertSql, new int[] { 1 }), 1);
				Assert.assertTrue(stmt.getGeneratedKeys().next());

				Assert.assertEquals(stmt.executeUpdate(insertSql, new String[] { "col" }), 1);
				Assert.assertTrue(stmt.getGeneratedKeys().next());

				stmt.close();

				return null;
			}
		});
	}
}
