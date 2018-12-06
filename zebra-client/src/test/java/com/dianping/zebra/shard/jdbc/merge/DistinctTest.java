package com.dianping.zebra.shard.jdbc.merge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.SingleDBBaseTestCase;

import junit.framework.Assert;

public class DistinctTest extends SingleDBBaseTestCase {
	/*
	 * (non-Javadoc)
	 *
	 * @see com.dianping.zebra.jdbc.ZebraDBTestCase#getDBUrl()
	 */
	@Override
	protected String getDBUrl() {
		return "jdbc:h2:mem:zebra_ut;DB_CLOSE_DELAY=-1";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.dianping.zebra.jdbc.ZebraDBTestCase#getDataSetFilePath()
	 */
	@Override
	protected String getDataSetFilePath() {
		return "db-datafiles/data-singledb-lifecycle.xml";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.dianping.zebra.jdbc.ZebraDBTestCase#getCreateTableScriptPath()
	 */
	@Override
	protected String getCreateTableScriptPath() {
		return "db-datafiles/createtable-singledb-lifecycle.xml";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.dianping.zebra.jdbc.ZebraDBTestCase#getSpringConfigLocations()
	 */
	@Override
	protected String[] getSpringConfigLocations() {
		return new String[] { "ctx-singledb-lifecycle.xml" };
	}

	@Test
	public void testMultiRouterResult6() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select distinct score from test order by score desc limit 1");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(1, rows.size());
			Assert.assertEquals(8, rows.get(0).intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult7() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select distinct score from test order by score asc limit ?,?");
			stmt.setInt(1, 1);
			stmt.setInt(2, 3);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(3, rows.size());
			Assert.assertEquals(2, rows.get(0).intValue());
			Assert.assertEquals(3, rows.get(1).intValue());
			Assert.assertEquals(4, rows.get(2).intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult16() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select distinct score from test order by score asc limit ?,?");
			stmt.setInt(1, 7);
			stmt.setInt(2, 10);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			int count = 0;
			while (rs.next()) {
				count++;
			}
			Assert.assertEquals(1, count);
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult15() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select distinct score from test order by score asc limit ?,?");
			stmt.setInt(1, 16);
			stmt.setInt(2, 3);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				Assert.fail();
			}
			Assert.assertTrue(true);
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult17() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select distinct score from test where score in (?,?,?,?) ");
			stmt.setInt(1, 10000);
			stmt.setInt(2, 10001);
			stmt.setInt(3, 10002);
			stmt.setInt(4, 10003);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			rs.getMetaData();
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult18() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select distinct score from test where score in (?,?,?,?) ");
			stmt.setInt(1, 1);
			stmt.setInt(2, 10001);
			stmt.setInt(3, 10002);
			stmt.setInt(4, 10003);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			rs.getMetaData();
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
}
