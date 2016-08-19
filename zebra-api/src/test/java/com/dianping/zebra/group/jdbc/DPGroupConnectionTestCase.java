package com.dianping.zebra.group.jdbc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraException;

/**
 * 
 * @author damonzhu
 * 
 */
public class DPGroupConnectionTestCase extends MultiDatabaseTestCase {

	@Test(expected = ZebraException.class)
	public void test_sql_connnection_without_config() {
		GroupDataSource dataSource = new GroupDataSource("");
		dataSource.setConfigManagerType("local");
		dataSource.init();
	}

	@Test
	public void test_sql_connection_api_supported() throws SQLException {
		Connection conn = getDataSource().getConnection();

		assertFalse(conn.isClosed());
		assertFalse(conn.isReadOnly());
		assertTrue(conn.getAutoCommit());
		conn.setAutoCommit(false);
		assertFalse(conn.getAutoCommit());
		assertNull(conn.getWarnings());

		assertTrue((conn.createStatement() instanceof GroupStatement));
		assertTrue((conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE) instanceof GroupStatement));
		assertTrue((conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE,
		      ResultSet.HOLD_CURSORS_OVER_COMMIT) instanceof GroupStatement));

		assertTrue((conn.prepareStatement("sql") instanceof GroupPreparedStatement));
		assertTrue((conn.prepareStatement("sql", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE) instanceof GroupPreparedStatement));
		assertTrue((conn.prepareStatement("sql", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE,
		      ResultSet.HOLD_CURSORS_OVER_COMMIT) instanceof GroupPreparedStatement));
		assertTrue((conn.prepareStatement("sql", Statement.RETURN_GENERATED_KEYS) instanceof GroupPreparedStatement));
		assertTrue((conn.prepareStatement("sql", new int[0]) instanceof GroupPreparedStatement));
		assertTrue((conn.prepareStatement("sql", new String[0]) instanceof GroupPreparedStatement));

	}

	private void assertResultInReadDs(String res) {
		Assert.assertTrue(Arrays.asList(new String[] { "reader1", "reader2" }).contains(res));
	}
	
	private void assertResultInWriteDs(String res) {
		Assert.assertTrue(Arrays.asList(new String[] { "writer","writer-new1" }).contains(res));
	}

	@Test
	public void test_callable_statement_use_write_connection() throws Exception {
		execute(new ConnectionCallback() {

			@Override
			public Object doInConnection(Connection conn) throws Exception {
				CallableStatement cstmt = conn.prepareCall("select * from PERSON");
				ResultSet rsSet1 = cstmt.executeQuery();
				rsSet1.next();
				String value = rsSet1.getString(2);
				Assert.assertEquals("writer", value);
				return null;
			}
		});
	}

	@Test
	public void test_create_single_read_statement_on_same_connection() throws Exception {
		execute(new StatementCallback() {

			@Override
			public Object doInStatement(Statement stmt) throws Exception {
				boolean result = stmt.execute("select * from PERSON");

				assertTrue(result);
				ResultSet rsSet = stmt.getResultSet();
				rsSet.next();
				assertResultInReadDs(rsSet.getString(2));
				return null;
			}
		});
	}
	
	@Test
	public void test_create_single_read_statement_on_same_connection_with_force_write_hint() throws Exception {
		execute(new StatementCallback() {

			@Override
			public Object doInStatement(Statement stmt) throws Exception {
				boolean result = stmt.execute("/*+zebra:w*/select * from PERSON");

				assertTrue(result);
				ResultSet rsSet = stmt.getResultSet();
				rsSet.next();
				Assert.assertEquals("writer", rsSet.getString(2));
				return null;
			}
		});
	}

	@Test
	public void test_create_single_write_statement_on_same_connection() throws Exception {
		execute(new StatementCallback() {

			@Override
			public Object doInStatement(Statement stmt) throws Exception {
				boolean result = stmt.execute("update PERSON set AGE=30 where NAME='writer'");
				assertFalse(result);

				// assert read from write db
				Integer age = (Integer) executeOnRealDB(new ConnectionCallback() {

					@Override
					public Object doInConnection(Connection conn) throws Exception {
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery("select AGE from PERSON where NAME='writer'");
						if (rs.next()) {
							return rs.getInt(1);
						} else {
							return null;
						}
					}
				}, true, -1);
				Assert.assertEquals(30, age.intValue());

				// assert read db not modified
				for (int i = 0; i < getReadDataSourcesCount(); i++) {
					age = (Integer) executeOnRealDB(new ConnectionCallback() {

						@Override
						public Object doInConnection(Connection conn) throws Exception {
							Statement stmt = conn.createStatement();
							ResultSet rs = stmt.executeQuery("select AGE from PERSON");
							if (rs.next()) {
								return rs.getInt(1);
							} else {
								return null;
							}
						}
					}, false, i);
					Assert.assertEquals(18, age.intValue());
				}
				return null;
			}
		});
	}

	@Test
	public void test_create_read_write_statement_on_same_connection() throws Exception {
		execute(new ConnectionCallback() {

			@Override
			public Object doInConnection(Connection conn) throws Exception {
				assertResultInReadDs(executeSelectSql(conn, "select * from PERSON"));
				executeOneRowUpdateSql(conn, "update PERSON set NAME = 'writer-new'");

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
				Assert.assertEquals("writer-new", name);

				assertResultInReadDs(executeSelectSql(conn, "select * from PERSON"));
				return null;
			}
		});
	}

	@Test
	public void test_create_write_read_statement_on_same_connection() throws Exception {
		execute(new ConnectionCallback() {

			@Override
			public Object doInConnection(Connection conn) throws Exception {
				executeOneRowUpdateSql(conn, "update PERSON set NAME = 'writer-new'");
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
				Assert.assertEquals("writer-new", name);
				assertResultInReadDs(executeSelectSql(conn, "select * from PERSON"));
				return null;
			}
		});
	}

	@Test
	public void test_create_multi_read_statement_on_same_connection() throws Exception {
		execute(new ConnectionCallback() {
			@Override
			public Object doInConnection(Connection conn) throws Exception {
				String value = executeSelectSql(conn, "select * from PERSON");
				assertResultInReadDs(value);
				Assert.assertEquals(value, executeSelectSql(conn, "select * from PERSON"));
				return null;
			}
		});
	}

	@Test
	public void test_create_multi_write_statement_on_same_connection() throws Exception {
		execute(new ConnectionCallback() {
			@Override
			public Object doInConnection(Connection conn) throws Exception {
				executeOneRowUpdateSql(conn, "update PERSON set NAME = 'writer-new1'");

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
				Assert.assertEquals("writer-new1", name);

				executeOneRowUpdateSql(conn, "update PERSON set NAME = 'writer-new2'");

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
				Assert.assertEquals("writer-new2", name);

				assertResultInReadDs(executeSelectSql(conn, "select * from PERSON"));
				return null;
			}
		});
	}

	@Test
	public void test_create_multi_read_statement_on_different_connection() throws Exception {
		final Set<String> set = new HashSet<String>();

		for (int i = 0; i < 100; i++) {
			execute(new StatementCallback() {
				@Override
				public Object doInStatement(Statement stmt) throws Exception {
					set.add(executeSelectSql(stmt, "select * from PERSON"));
					return null;
				}
			});
		}

		Assert.assertEquals(getReadDataSourcesCount(), set.size());
		for (String value : set) {
			assertResultInReadDs(value);
		}
	}

	@Test
	public void test_create_write_read_statement_on_different_connection() throws Exception {
		final Set<String> set = new HashSet<String>();

		for (int i = 0; i < 100; i++) {
			if (i % 2 == 0) {
				execute(new StatementCallback() {
					@Override
					public Object doInStatement(Statement stmt) throws Exception {
						set.add(executeSelectSql(stmt, "select * from PERSON"));
						return null;
					}
				});

			} else {
				execute(new StatementCallback() {
					@Override
					public Object doInStatement(Statement stmt) throws Exception {
						executeOneRowUpdateSql(stmt, "update PERSON set NAME = 'writer-new'");
						assertResultInReadDs(executeSelectSql(stmt, "select * from PERSON"));
						return null;
					}
				});

			}
		}

		Assert.assertEquals(getReadDataSourcesCount(), set.size());
		for (String value : set) {
			assertResultInReadDs(value);
		}
	}

	@Test
	public void test_write_and_read_with_commit_in_trans() throws Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			conn.setAutoCommit(false);
			executeSelectSql(conn, "select * from PERSON");
			assertResultInWriteDs(executeSelectSql(conn, "select * from PERSON"));
			executeOneRowUpdateSql(conn, "update PERSON set NAME = 'writer-new1'");
			assertResultInWriteDs(executeSelectSql(conn, "select * from PERSON"));

			conn.commit();

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
			Assert.assertEquals("writer-new1", name);

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					// ignore it
				}
			}
		}
	}

	@Test
	public void test_write_and_read_with_rollback_in_trans() throws Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			conn.setAutoCommit(false);
			assertResultInWriteDs(executeSelectSql(conn, "select * from PERSON"));
			executeOneRowUpdateSql(conn, "update PERSON set NAME = 'writer-new1'");
			assertResultInWriteDs(executeSelectSql(conn, "select * from PERSON"));

			conn.rollback();

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

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					// ignore it
				}
			}
		}
	}

	@Test
	public void test_batch() throws Exception {
		execute(new StatementCallback() {

			@Override
			public Object doInStatement(Statement stmt) throws Exception {
				for (int i = 0; i < 100; i++) {
					stmt.addBatch("insert into PERSON (NAME, AGE) values('test-batch', '" + (100 + i) + "')");
				}

				int[] executeBatch = stmt.executeBatch();

				for (int i = 0; i < 100; i++) {
					Assert.assertEquals(1, executeBatch[i]);

					final int age = 100 + i;
					String name = (String) executeOnRealDB(new ConnectionCallback() {

						@Override
						public Object doInConnection(Connection conn) throws Exception {
							Statement stmt = conn.createStatement();
							ResultSet rs = stmt.executeQuery("select * from PERSON where AGE='" + age + "'");
							if (rs.next()) {
								return rs.getString(2);
							} else {
								return null;
							}
						}
					}, true, -1);
					Assert.assertEquals("test-batch", name);
				}

				return null;
			}
		});
	}
	
	@Override
	protected String getConfigManagerType() {
		return Constants.CONFIG_MANAGER_TYPE_LOCAL;
	}

	@Override
	protected String getResourceId() {
		return "sample.ds.v2";
	}

	@Override
	protected String getSchema() {
		return  getClass().getResource("/schema.sql").getPath();
	}

	@Override
	protected DataSourceEntry[] getDataSourceEntryArray() {
		DataSourceEntry[] entries = new DataSourceEntry[3];

		DataSourceEntry entry1 = new DataSourceEntry("jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets.xml", true);
		DataSourceEntry entry2 = new DataSourceEntry("jdbc:h2:mem:test1;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets1.xml",
		      false);
		DataSourceEntry entry3 = new DataSourceEntry("jdbc:h2:mem:test2;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets2.xml",
		      false);

		entries[0] = entry1;
		entries[1] = entry2;
		entries[2] = entry3;

		return entries;
	}

	public void executeOneRowUpdateSql(Connection conn, String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		Assert.assertEquals(1, stmt.getUpdateCount());
	}

	public void executeOneRowUpdateSql(Statement stmt, String sql) throws SQLException {
		stmt.execute(sql);
		Assert.assertEquals(1, stmt.getUpdateCount());
	}

	public String executeSelectSql(Connection conn, String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		ResultSet rsSet = stmt.getResultSet();
		rsSet.next();

		return rsSet.getString(2);
	}

	public String executeSelectSql(Statement stmt, String sql) throws SQLException {
		stmt.execute(sql);
		ResultSet rsSet = stmt.getResultSet();
		rsSet.next();

		return rsSet.getString(2);
	}
}
