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
import java.util.Date;

/**
 * 
 * @author Leo Liang
 * 
 */
public class SingleDBPreparedStatementGroupFollowNoteIntegrationTest extends SingleDBBaseTestCase {

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
		return "db-datafiles/data-singledb-group-follownote-integrationtest.xml";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.zebra.jdbc.ZebraDBTestCase#getCreateTableScriptPath()
	 */
	@Override
	protected String getCreateTableScriptPath() {
		return "db-datafiles/createtable-singledb-group-follownote-integrationtest.xml";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.zebra.jdbc.ZebraDBTestCase#getSpringConfigLocations()
	 */
	@Override
	protected String[] getSpringConfigLocations() {
		return new String[] { "ctx-singledb-group-follownote-integrationtest.xml" };
	}

	@Test
	public void test1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT FN.FollowNoteID AS NoteID, N.GroupID, FN.UserID, CONCAT('RE:', N.NoteTitle) AS NoteTitle, '' AS NoteBody, FN.AddTime, FN.LastIP, G.GroupName, G.GroupPermaLink, FN.NoteID AS GroupNoteID FROM DP_GroupFollowNote FN JOIN DP_GroupNote N ON N.NoteID = FN.NoteID JOIN DP_Group G ON N.GroupID = G.GroupID WHERE FN.NoteClass = 3 ORDER BY FN.AddTime LIMIT 0, 10");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			int count = 0;
			System.out.println("Test 1 ------------------------------------------------------");
			while (rs.next()) {
				count++;
				System.out
						.println(" NoteID: " + rs.getObject(1) + " GroupID: " + rs.getObject(2) + " UserID: "
								+ rs.getObject(3) + " NoteTitle: " + rs.getString(4) + " NoteBody: " + rs.getString(5)
								+ " AddTime: " + rs.getObject(6) + " LastIP: " + rs.getString(7) + " GroupName: "
								+ rs.getString(8) + " GroupPermaLink: " + rs.getString(9) + " GroupNoteID: "
								+ rs.getObject(10));
			}
			System.out.println("Test 1 ------------------------------------------------------");
			Assert.assertEquals(8, count);
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
	public void test2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("UPDATE DP_GroupFollowNote SET NoteClass = ? WHERE UserID = ?");
			stmt.setInt(1, 4);
			stmt.setInt(2, 1);
			stmt.execute();

			Statement stmt2 = conn.createStatement();
			stmt2.execute("select count(*) total from DP_GroupFollowNote where NoteClass=4");
			ResultSet rs = stmt2.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(3, rs.getLong(1));
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
	public void test3() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT N.GroupID, F.FollowNoteID, F.UserID, F.NoteId FROM DP_GroupFollowNote F INNER JOIN DP_GroupNote N ON N.NoteID = F.NoteID WHERE F.UserID = ? AND F.NoteClass <> 3");
			stmt.setInt(1, 0);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			System.out.println("Test 3 ------------------------------------------------------");
			int count = 0;
			while (rs.next()) {
				count++;
				System.out.println(" GroupID: " + rs.getObject(1) + " FollowNoteID: " + rs.getObject(2) + " UserID: "
						+ rs.getObject(3) + " NoteId: " + rs.getObject(4));
			}
			System.out.println("Test 3 ------------------------------------------------------");
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
	public void test4() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT * FROM DP_GroupFollowNote WHERE (NoteClass = 1 OR (NoteClass = 4 AND UserID = ?)) AND NoteID = ? LIMIT ?, ?");
			stmt.setInt(1, 0);
			stmt.setInt(2, 7);
			stmt.setInt(3, 1);
			stmt.setInt(4, 10);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			System.out.println("Test 4 ------------------------------------------------------");
			int count = 0;
			while (rs.next()) {
				count++;
				System.out.println(" FollowNoteID: " + rs.getObject(1));
			}
			System.out.println("Test 4 ------------------------------------------------------");
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
	public void test5() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT COUNT(FollowNoteID) FROM DP_GroupFollowNote WHERE (NoteClass = 1 OR (NoteClass = 4 AND UserID = ?)) AND NoteID = ?");
			stmt.setInt(1, 0);
			stmt.setInt(2, 7);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(2, rs.getLong(1));
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
	public void test6() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT * FROM DP_GroupFollowNote WHERE (NoteClass = 1 OR (NoteClass = 4 AND UserID = ?)) AND NoteID = ? AND UserID = ? LIMIT ?, ?");
			stmt.setInt(1, 0);
			stmt.setInt(2, 7);
			stmt.setInt(3, 0);
			stmt.setInt(4, 0);
			stmt.setInt(5, 10);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			int count = 0;
			System.out.println("Test 6 ------------------------------------------------------");
			while (rs.next()) {
				count++;
				System.out.println("FollowNoteID: " + rs.getObject("FollowNoteID"));
			}
			System.out.println("Test 6 ------------------------------------------------------");
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
	public void test7() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT COUNT(FollowNoteID) FROM DP_GroupFollowNote WHERE (NoteClass = 1 OR (NoteClass = 4 AND UserID = ?)) AND NoteID = ? AND UserID = ?");
			stmt.setInt(1, 0);
			stmt.setInt(2, 7);
			stmt.setInt(3, 0);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(2, rs.getLong(1));
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
	public void test8() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("INSERT INTO DP_GroupFollowNote (NoteID, UserID, NoteClass, ADDTIME, UpdateTime, LastIP, DCashNumber) values(?,?,?,?,?,?,?)");
			stmt.setInt(1, 8);
			stmt.setInt(2, 8);
			stmt.setInt(3, 1);
			stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
			stmt.setTimestamp(5, new Timestamp(new Date().getTime()));
			stmt.setString(6, "0.0.0.0");
			stmt.setInt(7, 10);
			stmt.execute();
			Class.forName(getDriverName());
			Connection conn2 = DriverManager.getConnection(getDBUrl());
			Statement stmt2 = conn2.createStatement();
			stmt2.execute("select count(*) from DP_GroupFollowNote_noteid0 where UserID = 8");
			ResultSet rs = stmt2.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(1, rs.getLong(1));
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
	public void test9() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM DP_GroupFollowNote WHERE FollowNoteID = ?");
			stmt.setInt(1, 140);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			int count = 0;
			while (rs.next()) {
				count++;
				Assert.assertEquals(140, rs.getInt("FollowNoteID"));
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
	public void test10() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT COUNT(FollowNoteID) FROM DP_GroupFollowNote WHERE (NoteClass = 1 OR (NoteClass = 4 AND UserID = ?)) AND NoteID = ? AND FollowNoteID <= ?");
			stmt.setInt(1, 0);
			stmt.setInt(2, 7);
			stmt.setInt(3, 141);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(1, rs.getLong(1));
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
	public void test11() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT COUNT(DISTINCT(UserID)) FROM DP_GroupFollowNote WHERE NoteID = ?");
			stmt.setInt(1, 7);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(1, rs.getLong(1));
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
	public void test12() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT COUNT(FollowNoteID) FROM DP_GroupFollowNote WHERE NoteID = ? AND UserID = ?");
			stmt.setInt(1, 7);
			stmt.setInt(2, 0);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(3, rs.getLong(1));
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
	public void test13() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT * FROM DP_GroupFollowNote WHERE NoteID = ? AND NoteClass = 1 ORDER BY FollowNoteID DESC LIMIT 1");
			stmt.setInt(1, 7);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(141, rs.getInt("FollowNoteID"));
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
	public void test14() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT COUNT(FollowNoteID) F FROM DP_GroupFollowNote F INNER JOIN DP_GroupNote N ON F.NoteID = N.NoteID AND N.GroupID=? AND N.Status = 1 WHERE F.UserID = ? AND F.NoteClass = 1");
			stmt.setInt(1, 100);
			stmt.setInt(2, 0);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(1, rs.getLong(1));
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
	public void test15() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("UPDATE DP_GroupFollowNote SET DCashNumber = DCashNumber + ? WHERE FollowNoteID = ?");
			stmt.setInt(1, 10);
			stmt.setInt(2, 142);
			stmt.execute();
			Statement stmt2 = conn.createStatement();
			stmt2.execute("select DCashNumber from DP_GroupFollowNote where FollowNoteID=142");
			ResultSet rs = stmt2.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(21, rs.getShort(1));
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
	public void test16() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT DISTINCT GN.NoteID F FROM DP_GroupNote GN INNER JOIN DP_Group G ON GN.GroupID = G.GroupID AND G.Status = 0 INNER JOIN DP_GroupFollowNote GFN ON GN.NoteID = GFN.NoteID WHERE (GN.Status = 1 OR (GN.Status = 3 AND GN.UserID = ?)) AND GN.UserID <> ? AND GFN.UserID = ? AND GFN.NoteClass = 1");
			stmt.setInt(1, 7);
			stmt.setInt(2, 7);
			stmt.setInt(3, 7);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(0, rs.getLong(1));
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
	public void test17() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("UPDATE DP_GroupFollowNote SET LastIP = ? WHERE FollowNoteID = ?");
			stmt.setString(1, "5.1.1.1");
			stmt.setInt(2, 142);
			stmt.execute();
			Statement stmt2 = conn.createStatement();
			stmt2.execute("select LastIP from DP_GroupFollowNote where FollowNoteID=142");
			ResultSet rs = stmt2.getResultSet();
			while (rs.next()) {
				Assert.assertEquals("5.1.1.1", rs.getString(1));
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
	public void test18() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("UPDATE DP_GroupFollowNote SET NoteClass = ? WHERE FollowNoteID = ?");
			stmt.setInt(1, 10);
			stmt.setInt(2, 142);
			stmt.execute();
			Statement stmt2 = conn.createStatement();
			stmt2.execute("select NoteClass from DP_GroupFollowNote where FollowNoteID=142");
			ResultSet rs = stmt2.getResultSet();
			while (rs.next()) {
				Assert.assertEquals(10, rs.getInt(1));
			}
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

}
