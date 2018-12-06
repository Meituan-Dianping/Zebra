/**
 * Project: ${zebra-client.aid}
 * 
 * File Created at 2011-6-30
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.jdbc;

import junit.framework.Assert;
import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.SingleDBBaseTestCase;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Leo Liang
 * 
 */
public class SingleDBPreparedStatementLifeCycleTest extends SingleDBBaseTestCase {

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
	public void testH2Init() throws Exception {
		Class.forName(getDriverName());
		Connection conn = DriverManager.getConnection(getDBUrl());
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM test_0");
		while (rs.next()) {
			System.out.println(rs.getString("name"));
		}
	}

	@Test
	public void testSpringInit() throws Exception {
		Assert.assertNotNull(context);
		Assert.assertNotNull(context.getBean("ds0"));
	}

	@Test
	public void testSingleRouterResult0() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id=?");
			stmt.setInt(1, 0);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(2, rows.size());
			Assert.assertEquals("leo0", rows.get(0));
			Assert.assertEquals("leo0", rows.get(1));
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testSingleRouterResult1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select sum(score) score from test where id=?");
			stmt.setInt(1, 0);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(1, rows.size());
			Assert.assertEquals(2, rows.get(0).intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult0() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select sum(score) score from test");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Long> rows = new ArrayList<Long>();
			while (rs.next()) {
				rows.add(rs.getLong("score"));
			}
			Assert.assertEquals(1, rows.size());
			Assert.assertEquals(73, rows.get(0).intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select type, sum(score) score from test group by type order by score");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Map<String, Long>> rows = new ArrayList<Map<String, Long>>();
			while (rs.next()) {
				Map<String, Long> row = new HashMap<String, Long>();
				row.put(rs.getString("type"), rs.getLong("score"));
				rows.add(row);
			}
			Assert.assertEquals(2, rows.size());
			Assert.assertEquals(31, rows.get(0).get("a").intValue());
			Assert.assertEquals(42, rows.get(1).get("b").intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select score from test where id!=1 order by score");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(14, rows.size());
			int index = 0;
			for (int i = 1; i <= 8; i++) {
				if (i == 2) {
					continue;
				}
				Assert.assertEquals(i, rows.get(index).intValue());
				Assert.assertEquals(i, rows.get(index + 1).intValue());
				index += 2;
			}
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult3() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select distinct name, score, type from test order by score");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(8, rows.size());

		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult4() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select score from test where id!=1 order by score desc");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(14, rows.size());
			int index = 0;
			for (int i = 8; i >= 1; i--) {
				if (i == 2) {
					continue;
				}
				Assert.assertEquals(i, rows.get(index).intValue());
				Assert.assertEquals(i, rows.get(index + 1).intValue());
				index += 2;
			}
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult5() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select name, type, sum(score) score from test group by type, name order by score desc");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
			while (rs.next()) {
				Map<String, Object> cols = new HashMap<String, Object>();
				cols.put("name", rs.getString("name"));
				cols.put("type", rs.getString("type"));
				cols.put("score", rs.getLong("score"));
				rows.add(cols);
			}
			Assert.assertEquals(8, rows.size());
			Assert.assertEquals("leo7", rows.get(0).get("name"));
			Assert.assertEquals("b", rows.get(0).get("type"));
			Assert.assertEquals(16, ((Long) rows.get(0).get("score")).intValue());

			Assert.assertEquals("leo6", rows.get(1).get("name"));
			Assert.assertEquals("b", rows.get(1).get("type"));
			Assert.assertEquals(14, ((Long) rows.get(1).get("score")).intValue());

			Assert.assertEquals("leo5", rows.get(2).get("name"));
			Assert.assertEquals("b", rows.get(2).get("type"));
			Assert.assertEquals(12, ((Long) rows.get(2).get("score")).intValue());

			Assert.assertEquals("leo4", rows.get(3).get("name"));
			Assert.assertEquals("a", rows.get(3).get("type"));
			Assert.assertEquals(10, ((Long) rows.get(3).get("score")).intValue());

			Assert.assertEquals("leo3", rows.get(4).get("name"));
			Assert.assertEquals("a", rows.get(4).get("type"));
			Assert.assertEquals(8, ((Long) rows.get(4).get("score")).intValue());

			Assert.assertEquals("leo2", rows.get(5).get("name"));
			Assert.assertEquals("a", rows.get(5).get("type"));
			Assert.assertEquals(6, ((Long) rows.get(5).get("score")).intValue());

			Assert.assertEquals("leo1", rows.get(6).get("name"));
			Assert.assertEquals("a", rows.get(6).get("type"));
			Assert.assertEquals(4, ((Long) rows.get(6).get("score")).intValue());

			Assert.assertEquals("leo0", rows.get(7).get("name"));
			Assert.assertEquals("a", rows.get(7).get("type"));
			Assert.assertEquals(3, ((Long) rows.get(7).get("score")).intValue());

		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult8() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select count(*) total from test");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Long> rows = new ArrayList<Long>();
			while (rs.next()) {
				rows.add(rs.getLong("total"));
			}
			Assert.assertEquals(17, rows.get(0).intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	@Test
	public void testMultiRouterResult8_1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select count(1) total from test");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Long> rows = new ArrayList<Long>();
			while (rs.next()) {
				rows.add(rs.getLong("total"));
			}
			Assert.assertEquals(17, rows.get(0).intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult9() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select max(score) m_score from test");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("m_score"));
			}
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
	public void testMultiRouterResult10() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select min(score) m_score from test");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("m_score"));
			}
			Assert.assertEquals(1, rows.get(0).intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult11() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(17, rows.size());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult13() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select sum(score) score from test where id in (?, ?)");
			stmt.setInt(1, 1);
			stmt.setInt(2, 2);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Long> rows = new ArrayList<Long>();
			while (rs.next()) {
				rows.add(rs.getLong("score"));
			}
			Assert.assertEquals(1, rows.size());
			Assert.assertEquals(11, rows.get(0).intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	@Test
	public void testMultiRouterResult13_1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select sum(score) score from test where id = ?");
			stmt.setInt(1, 1);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Long> rows = new ArrayList<Long>();
			while (rs.next()) {
				rows.add(rs.getLong("score"));
			}
			Assert.assertEquals(1, rows.size());
			Assert.assertEquals(5, rows.get(0).intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult14() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id>=? and id <=?");
			stmt.setInt(1, 1);
			stmt.setInt(2, 2);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(5, rows.size());
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
			PreparedStatement stmt = conn.prepareStatement("select sum(score) score from test where id>=? and id <=?");
			stmt.setInt(1, 1);
			stmt.setInt(2, 2);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(11, rs.getLong(1));
			}
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult19() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select distinct name from test where name between ? and ?");
			stmt.setString(1, "leo2");
			stmt.setString(2, "leo5");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(4, rows.size());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult20() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select distinct score from test where score between ? and ?");
			stmt.setInt(1, 1);
			stmt.setInt(2, 2);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(2, rows.size());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	@Test
	public void testMultiRouterResult21() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select score from test where id in (?,?) order by score");
			stmt.setInt(1, 1000);
			stmt.setInt(2, 2001);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			rs.getType();
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
	public void testInsert() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("insert into test(id,name,score,type,classid) values (?, ?, ?, ?,?)");
			stmt.setInt(1, 43);
			stmt.setString(2, "testinsert");
			stmt.setInt(3, 111);
			stmt.setString(4, "fff");
			stmt.setInt(5, 43);
			stmt.execute();
			Connection conn2 = DriverManager.getConnection(getDBUrl());
			Statement stmt2 = conn2.createStatement();
			stmt2.execute("select * from test_3 where name='testinsert'");
			ResultSet rs = stmt2.getResultSet();
			List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
			while (rs.next()) {
				Map<String, Object> cols = new HashMap<String, Object>();
				cols.put("id", rs.getInt("id"));
				cols.put("name", rs.getString("name"));
				cols.put("score", rs.getInt("score"));
				cols.put("type", rs.getString("type"));
				cols.put("classid", rs.getInt("classid"));
				rows.add(cols);
			}
			Assert.assertEquals(1, rows.size());
			Assert.assertEquals(43, rows.get(0).get("id"));
			Assert.assertEquals("testinsert", rows.get(0).get("name"));
			Assert.assertEquals(111, rows.get(0).get("score"));
			Assert.assertEquals("fff", rows.get(0).get("type"));
			Assert.assertEquals(43, rows.get(0).get("classid"));
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testUpdate() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("update test set name='testupdate' where name=?");
			stmt.setString(1, "leo6");
			stmt.execute();
			Statement stmt2 = conn.createStatement();
			stmt2.execute("select name from test where name='testupdate'");
			ResultSet rs = stmt2.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(2, rows.size());

		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testUpdate2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("update test set name='testupdate' where id=?");
			stmt.setInt(1, 1);
			stmt.execute();
			Statement stmt2 = conn.createStatement();
			stmt2.execute("select name from test where id=1");
			ResultSet rs = stmt2.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(3, rows.size());

		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	@Test
	public void testUpdate2_parallel() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("update test set name='testupdate' where id in (?,?)");
			stmt.setInt(1, 1);
			stmt.setInt(2, 2);
			stmt.execute();
			Statement stmt2 = conn.createStatement();
			stmt2.execute("select name from test where id in (1,2)");
			ResultSet rs = stmt2.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(5, rows.size());
			for(String name : rows){
				Assert.assertEquals("testupdate", name);
			}

		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testUpdate3() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("update test set name='testupdate' where classid=?");
			stmt.setInt(1, 1);
			stmt.execute();
			Statement stmt2 = conn.createStatement();
			stmt2.execute("select name from test where id=1");
			ResultSet rs = stmt2.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(3, rows.size());
			Assert.assertTrue("testupdate".equals(rows.get(0)));
			Assert.assertTrue("testupdate".equals(rows.get(1)));
			Assert.assertTrue("testupdate".equals(rows.get(2)));

		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testDelete() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("delete from test where name=?");
			stmt.setString(1, "leo6");
			stmt.execute();
			Statement stmt2 = conn.createStatement();
			stmt2.execute("select name from test where name='leo6'");
			ResultSet rs = stmt2.getResultSet();
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
	public void testDelete2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("delete from test where id=?");
			stmt.setInt(1, 1);
			stmt.execute();
			Statement stmt2 = conn.createStatement();
			stmt2.execute("select name from test where id=1");
			ResultSet rs = stmt2.getResultSet();
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
	public void testDelete3() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("delete from test where classid=?");
			stmt.setInt(1, 1);
			stmt.execute();
			Statement stmt2 = conn.createStatement();
			stmt2.execute("select name from test where id=1");
			ResultSet rs = stmt2.getResultSet();
			while (rs.next()) {
				Assert.fail();
			}

		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	@Test
	public void testMultiRouterResultGroupByNoResult() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select type, sum(score) score from test where id=100 group by type order by score");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			
			Assert.assertFalse(rs.next());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
}
