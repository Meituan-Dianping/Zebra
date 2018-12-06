package com.dianping.zebra.group.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import org.junit.Test;

import junit.framework.Assert;

public class DPGroupPreparedStatementTest extends MultiDatabaseTestCase {

	private String insertSql = "insert into PERSON(NAME,LAST_NAME,AGE) values('damon.zhu','zhu',28)";

	private String selectSql = "select * from PERSON p where p.AGE = ?";

	private String updateSql = "update PERSON p set p.Name = ? where p.NAME = ?";

	private void assertResultInReadDs(String res) {
		Assert.assertTrue(Arrays.asList(new String[] { "reader1", "reader2" }).contains(res));
	}

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
	public void test_preparedStatement_api_support() throws Exception {

		execute(new ConnectionCallback() {
			@Override
			public Object doInConnection(Connection conn) throws Exception {
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
				stmt.setString(1, "writer-new");
				stmt.setString(2, "writer");
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
				return null;
			}
		});
	}

	@Test
	public void test_preparedStatement_batch_support() throws Exception {

		execute(new ConnectionCallback() {
			@Override
			public Object doInConnection(Connection conn) throws Exception {
				PreparedStatement stmt = conn.prepareStatement("insert into PERSON(NAME,LAST_NAME,AGE) values(?,?,?)");

				stmt.setString(1, "zhuhao");
				stmt.setString(2, "zhuhao");
				stmt.setInt(3, 2);
				stmt.addBatch();

				stmt.setString(1, "damon");
				stmt.setString(2, "zhuhao");
				stmt.setInt(3, 12);
				stmt.addBatch();

				int[] updateCounts = stmt.executeBatch();

				Assert.assertEquals(updateCounts.length, 2);
				stmt.close();

				conn.close();
				return null;
			}
		});
	}

	@Test
	public void test_read_and_write_perpared_statement() throws Exception {
		execute(new ConnectionCallback() {
			@Override
			public Object doInConnection(Connection conn) throws Exception {
				String name = (String) executeOnRealDB(new ConnectionCallback() {

					@Override
					public Object doInConnection(Connection conn) throws Exception {
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery("select * from PERSON");
						if (rs.next()) {
							return rs.getString(2);
						} else {
							return null;
						}
					}
				}, true, -1);
				Assert.assertEquals("writer", name);

				PreparedStatement ps = conn.prepareStatement(selectSql);
				ps.setInt(1, 18);
				ps.execute();
				ResultSet rs = ps.getResultSet();
				rs.next();
				assertResultInReadDs(rs.getString(2));
				ps.close();

				PreparedStatement ps1 = conn.prepareStatement(updateSql);
				ps1.setString(1, "writer-new");
				ps1.setString(2, "writer");
				ps1.executeUpdate();
				ps1.close();

				name = (String) executeOnRealDB(new ConnectionCallback() {

					@Override
					public Object doInConnection(Connection conn) throws Exception {
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery("select * from PERSON");
						if (rs.next()) {
							return rs.getString(2);
						} else {
							return null;
						}
					}
				}, true, -1);
				Assert.assertEquals("writer-new", name);

				return null;
			}
		});
	}
}
