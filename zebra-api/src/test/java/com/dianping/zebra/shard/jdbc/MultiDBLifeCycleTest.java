/**
 * Project: ${zebra-client.aid}
 *
 * File Created at 2011-7-6
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

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * @author Leo Liang
 */
public class MultiDBLifeCycleTest extends MultiDBBaseTestCase {

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

	@Test
	public void testSpringInit() throws Exception {
		Assert.assertNotNull(context);
		Assert.assertNotNull(context.getBean("id0"));
	}

	@Test
	public void testSingleRouterResult0() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select name from test where id=0");
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
			Statement stmt = conn.createStatement();
			stmt.execute("select sum(score) score from test where id=0");
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
			Statement stmt = conn.createStatement();
			stmt.execute("select sum(score) score from test");
			ResultSet rs = stmt.getResultSet();
			List<Long> rows = new ArrayList<Long>();
			while (rs.next()) {
				rows.add(rs.getLong("score"));
			}
			Assert.assertEquals(1, rows.size());
			Assert.assertEquals(89, rows.get(0).intValue());
		} catch (Exception e) {
			e.printStackTrace();
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
			Statement stmt = conn.createStatement();
			stmt.execute("select type, sum(score) score from test group by type order by score");
			ResultSet rs = stmt.getResultSet();
			List<Map<String, Long>> rows = new ArrayList<Map<String, Long>>();
			while (rs.next()) {
				Map<String, Long> row = new HashMap<String, Long>();
				row.put(rs.getString("type"), rs.getLong("score"));
				rows.add(row);
			}
			Assert.assertEquals(2, rows.size());
			Assert.assertEquals(42, rows.get(0).get("b").intValue());
			Assert.assertEquals(47, rows.get(1).get("a").intValue());
		} catch (Exception e) {
			e.printStackTrace();
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
			Statement stmt = conn.createStatement();
			stmt.execute("select score from test where id!=1 order by score");
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(14, rows.size());
			List<Integer> expectedResult = Arrays.asList(new Integer[] { 1, 1, 3, 3, 4, 5, 5, 6, 6, 7, 7, 8, 8, 20 });
			for (int i = 0; i < expectedResult.size(); i++) {
				Assert.assertEquals(expectedResult.get(i).intValue(), rows.get(i).intValue());
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
			Statement stmt = conn.createStatement();
			stmt.execute("select distinct name, score, type from test order by score");
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(9, rows.size());

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
			Statement stmt = conn.createStatement();
			stmt.execute("select score from test where id!=1 order by score desc");
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(14, rows.size());
			List<Integer> expectedResult = Arrays.asList(new Integer[] { 20, 8, 8, 7, 7, 6, 6, 5, 5, 4, 3, 3, 1, 1 });
			for (int i = 0; i < expectedResult.size(); i++) {
				Assert.assertEquals(expectedResult.get(i).intValue(), rows.get(i).intValue());
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
			Statement stmt = conn.createStatement();
			stmt.execute("select name, type, sum(score) score from test group by type, name order by score desc");
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
			Assert.assertEquals("leo3", rows.get(0).get("name"));
			Assert.assertEquals("a", rows.get(0).get("type"));
			Assert.assertEquals(24, ((Long) rows.get(0).get("score")).intValue());

			Assert.assertEquals("leo7", rows.get(1).get("name"));
			Assert.assertEquals("b", rows.get(1).get("type"));
			Assert.assertEquals(16, ((Long) rows.get(1).get("score")).intValue());

			Assert.assertEquals("leo6", rows.get(2).get("name"));
			Assert.assertEquals("b", rows.get(2).get("type"));
			Assert.assertEquals(14, ((Long) rows.get(2).get("score")).intValue());

			Assert.assertEquals("leo5", rows.get(3).get("name"));
			Assert.assertEquals("b", rows.get(3).get("type"));
			Assert.assertEquals(12, ((Long) rows.get(3).get("score")).intValue());

			Assert.assertEquals("leo4", rows.get(4).get("name"));
			Assert.assertEquals("a", rows.get(4).get("type"));
			Assert.assertEquals(10, ((Long) rows.get(4).get("score")).intValue());

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
	public void testMultiRouterResult6() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select distinct score from test order by score desc limit 1");
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("score"));
			}
			Assert.assertEquals(1, rows.size());
			Assert.assertEquals(20, rows.get(0).intValue());
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
			Statement stmt = conn.createStatement();
			stmt.execute("select distinct score from test order by score asc limit 1,3");
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
	public void testMultiRouterResult8() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select count(*) total from test");
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
	
	public void testMultiRouterResult8_0() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select count(1) total from test");
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
			Statement stmt = conn.createStatement();
			stmt.execute("select max(score) m_score from test");
			ResultSet rs = stmt.getResultSet();
			List<Integer> rows = new ArrayList<Integer>();
			while (rs.next()) {
				rows.add(rs.getInt("m_score"));
			}
			Assert.assertEquals(20, rows.get(0).intValue());
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
			Statement stmt = conn.createStatement();
			stmt.execute("select min(score) m_score from test");
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
			Statement stmt = conn.createStatement();
			stmt.execute("select name from test");
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
	public void testMultiRouterResult12() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select name from test limit 10,5");
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
	public void testMultiRouterResult12_1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select name from test limit 5 offset 10");
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
	public void testMultiRouterResult13() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select sum(score) score from test where id in (1, 2)");
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
	public void testMultiRouterResult14() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select name from test where id>=1 and id<=2");
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
	public void testMultiRouterResult15() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select distinct score from test order by score asc limit 16,3");
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
	public void testMultiRouterResult16() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select distinct score from test order by score asc limit 7,10");
			ResultSet rs = stmt.getResultSet();
			int count = 0;
			while (rs.next()) {
				count++;
			}
			Assert.assertEquals(2, count);
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
			Statement stmt = conn.createStatement();
			stmt.execute("select name, type, score from test order by type desc, score asc limit 1");
			ResultSet rs = stmt.getResultSet();
			boolean has = false;
			while (rs.next()) {
				Assert.assertEquals("leo5", rs.getString(1));
				Assert.assertEquals("b", rs.getString(2));
				Assert.assertEquals(6, rs.getInt(3));
				has = true;
			}

			Assert.assertTrue(has);
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
			Statement stmt = conn.createStatement();
			stmt.execute("select sum(score) score from test where id>=1 and id <=2");
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
			Statement stmt = conn.createStatement();
			stmt.execute("select distinct name from test where name between 'leo2' and 'leo5'");
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
			Statement stmt = conn.createStatement();
			stmt.execute("select distinct score from test where score between 1 and 2");
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
	public void testInsert() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("insert into test(id,name,score,type,classid) values (43, 'testinsert', 111, 'fff',43)");
			Connection conn2 = DriverManager.getConnection(getDBBaseUrl() + "id1;DB_CLOSE_DELAY=-1");
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
			Statement stmt = conn.createStatement();
			stmt.execute("update test set name='testupdate' where name='leo6'");
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
			Statement stmt = conn.createStatement();
			stmt.execute("update test set name='testupdate' where id=1");
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
	public void testUpdate3() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("update test set name='testupdate' where classid=1");
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
			Statement stmt = conn.createStatement();
			stmt.execute("delete from test where name='leo6'");
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
			Statement stmt = conn.createStatement();
			stmt.execute("delete from test where id=1");
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
			Statement stmt = conn.createStatement();
			stmt.execute("delete from test where classid=1");
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
	public void testMultiRouterResult21() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select type, sum(score) score from test group by type order by score limit 1");
			ResultSet rs = stmt.getResultSet();
			List<Map<String, Long>> rows = new ArrayList<Map<String, Long>>();
			while (rs.next()) {
				Map<String, Long> row = new HashMap<String, Long>();
				row.put(rs.getString("type"), rs.getLong("score"));
				rows.add(row);
			}
			Assert.assertEquals(1, rows.size());
			Assert.assertEquals(42, rows.get(0).get("b").intValue());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testMultiRouterResult22() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			long now = System.currentTimeMillis();
			stmt.execute("select * from test where id in (0,1,2,3,4,5,6,7) ");
			System.out.println("time = " + (System.currentTimeMillis() - now));
			ResultSet rs = stmt.getResultSet();
			List<Map<String, Long>> rows = new ArrayList<Map<String, Long>>();
			while (rs.next()) {
				Map<String, Long> row = new HashMap<String, Long>();
				row.put(rs.getString("type"), rs.getLong("score"));
				rows.add(row);
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
	public void testMultiRouterResult23() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			long now = System.currentTimeMillis();
			stmt.execute("select * from test where id = 3 or id = 4");
			System.out.println("time = " + (System.currentTimeMillis() - now));
			ResultSet rs = stmt.getResultSet();
			List<Map<String, Long>> rows = new ArrayList<Map<String, Long>>();
			while (rs.next()) {
				Map<String, Long> row = new HashMap<String, Long>();
				row.put(rs.getString("type"), rs.getLong("score"));
				rows.add(row);
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
	public void testHintShardColumn() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select name from test where id=1 or classid = 4");
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(3, rows.size());
			Assert.assertTrue("leo1".equals(rows.get(0)));
			Assert.assertTrue("leo1".equals(rows.get(1)));
			Assert.assertTrue("leo0".equals(rows.get(2)));

			stmt = conn.createStatement();
			rs = stmt.executeQuery("/*+zebra:sk=classid*/select name from test where id=1 or classid = 4");
			rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(2, rows.size());
			Assert.assertTrue("leo4".equals(rows.get(0)));
			Assert.assertTrue("leo4".equals(rows.get(1)));
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
}
