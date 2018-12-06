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
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Leo Liang
 * 
 */
public class SpecialSQLTest extends SingleDBBaseTestCase {

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
		return "db-datafiles/data-specialsql.xml";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.zebra.jdbc.ZebraDBTestCase#getCreateTableScriptPath()
	 */
	@Override
	protected String getCreateTableScriptPath() {
		return "db-datafiles/createtable-specialsql.xml";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.zebra.jdbc.ZebraDBTestCase#getSpringConfigLocations()
	 */
	@Override
	protected String[] getSpringConfigLocations() {
		return new String[] { "ctx-specialsql.xml" };
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
	public void testExpressionSelectColumn() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select score+amount as b from test order by b desc limit 5");
			stmt.execute();
			List<Integer> expected = Arrays.asList(new Integer[] { 10, 9, 9, 8, 8});
			ResultSet rs = stmt.getResultSet();
			int count = 0;
			while (rs.next()) {
				Assert.assertEquals(expected.get(count).intValue(), rs.getInt(1));
				count++;
			}
			Assert.assertEquals(expected.size(), count);
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	@Test
	public void testExpressionSelectColumn2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select score+amount as b from test where name in ('leo2', 'leo0') order by b desc limit 4");
			stmt.execute();
			List<Integer> expected = Arrays.asList(new Integer[] { 4, 4, 2, 2});
			ResultSet rs = stmt.getResultSet();
			int count = 0;
			while (rs.next()) {
				Assert.assertEquals(expected.get(count).intValue(), rs.getInt(1));
				count++;
			}
			Assert.assertEquals(expected.size(), count);
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	@Test
	public void testExpressionSelectColumn3() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select score+amount as b from test where name in ('leo2', 'leo0') order by b desc limit 10");
			stmt.execute();
			List<Integer> expected = Arrays.asList(new Integer[] { 4, 4, 2, 2, 2});
			ResultSet rs = stmt.getResultSet();
			int count = 0;
			while (rs.next()) {
				Assert.assertEquals(expected.get(count).intValue(), rs.getInt(1));
				count++;
			}
			Assert.assertEquals(expected.size(), count);
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	@Test
	public void testExpressionSelectColumn4() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select score-amount as b from test where name in ('leo2', 'leo0') order by b desc limit 10");
			stmt.execute();
			List<Integer> expected = Arrays.asList(new Integer[] { 2, 2, 0, 0, 0});
			ResultSet rs = stmt.getResultSet();
			int count = 0;
			while (rs.next()) {
				Assert.assertEquals(expected.get(count).intValue(), rs.getInt(1));
				count++;
			}
			Assert.assertEquals(expected.size(), count);
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	@Test
	public void testExpressionSelectColumn5() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select score from test where name in ('leoxx', 'leo8') order by score desc limit 10");
			stmt.execute();
			List<Integer> expected = Arrays.asList(new Integer[] { 8});
			ResultSet rs = stmt.getResultSet();
			int count = 0;
			while (rs.next()) {
				Assert.assertEquals(expected.get(count).intValue(), rs.getInt(1));
				count++;
			}
			Assert.assertEquals(expected.size(), count);
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
}
