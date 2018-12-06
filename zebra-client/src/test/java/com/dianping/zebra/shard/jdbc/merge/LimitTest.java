package com.dianping.zebra.shard.jdbc.merge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.SingleDBBaseTestCase;

import junit.framework.Assert;

public class LimitTest extends SingleDBBaseTestCase {
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
	public void testLimit1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select name, type, score from test order by type desc, score asc limit ?");
			stmt.setInt(1, 1);
			stmt.execute();
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
	public void testLimit1_1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select name, type, score from test order by type desc, score asc limit 1");
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
	public void testLimit2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test limit ?,?");
			stmt.setInt(1, 10);
			stmt.setInt(2, 5);
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
	public void testLimit2_1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test limit ?,5");
			stmt.setInt(1, 10);
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
	public void testLimit2_2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test limit 10,?");
			stmt.setInt(1, 5);
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
	public void testLimit2_3() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select name from test limit 10,5");
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
	public void testLimit3() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id = 0 limit ?,?");
			stmt.setInt(1, 0);
			stmt.setInt(2, 2);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
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
	public void testLimit3_1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id = 0 limit ? offset ?");
			stmt.setInt(1, 2);
			stmt.setInt(2, 0);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
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
	public void testLimit3_2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id = 0 limit ? offset 0");
			stmt.setInt(1, 2);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
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
	public void testLimit3_3() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id = 0 limit 2 offset ?");
			stmt.setInt(1, 0);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
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
	public void testLimit4() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id = 0 limit ?,?");
			stmt.setInt(1, 1);
			stmt.setInt(2, 2);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(1, rows.size());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testLimit4_1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id = 0 limit ? offset ?");
			stmt.setInt(1, 2);
			stmt.setInt(2, 1);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(1, rows.size());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testLimit4_2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id = 0 limit ? offset 1");
			stmt.setInt(1, 2);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(1, rows.size());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testLimit4_3() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id = 0 limit 2 offset ?");
			stmt.setInt(1, 1);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(1, rows.size());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testLimit4_4() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select name from test where id = 0 limit 2 offset 1");
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(1, rows.size());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testLimit5() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id in (0,1) limit ?,?");
			stmt.setInt(1, 0);
			stmt.setInt(2, 5);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(5, rows.size());
			for(String name : rows){
				System.out.println(name);
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
	public void testLimit6() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id in (0,1) limit ?,?");
			stmt.setInt(1, 3);
			stmt.setInt(2, 5);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(2, rows.size());
			for(String name : rows){
				System.out.println(name);
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
	public void testLimit6_1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id in (0,1) limit ? offset ?");
			stmt.setInt(1, 5);
			stmt.setInt(2, 3);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(2, rows.size());
			for(String name : rows){
				System.out.println(name);
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
	public void testLimit6_2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			Statement stmt  = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select name from test where id in (0,1) limit 5 offset 3");
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(2, rows.size());
			for(String name : rows){
				System.out.println(name);
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
	public void testLimit7() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name from test where id in (0,1) limit ?,?");
			stmt.setInt(1, 0);
			stmt.setInt(2, 3);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			List<String> rows = new ArrayList<String>();
			while (rs.next()) {
				rows.add(rs.getString("name"));
			}
			Assert.assertEquals(3, rows.size());
			for(String name : rows){
				System.out.println(name);
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
	public void testLimitOffset() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt1 = conn.prepareStatement("select name from test where id in (0,1) limit ?, ?");
			stmt1.setInt(1, 0);
			stmt1.setInt(2, 3);
			stmt1.execute();
			ResultSet rs1 = stmt1.getResultSet();

			List<String> rows1 = new ArrayList<String>();
			while (rs1.next()) {
				rows1.add(rs1.getString("name"));
			}

			PreparedStatement stmt2 = conn.prepareStatement("select name from test where id in (0,1) limit ? offset ?");
			stmt2.setInt(1, 3);
			stmt2.setInt(2, 0);
			stmt2.execute();
			ResultSet rs2 = stmt2.getResultSet();

			List<String> rows2 = new ArrayList<String>();
			while (rs2.next()) {
				rows2.add(rs2.getString("name"));
			}

			Assert.assertEquals(rows1.size(), rows2.size());

			stmt1.close();
			stmt2.close();
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testLimitOffset1() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt1 = conn.prepareStatement("select name from test where id in (0,1) limit 0, ?");
			stmt1.setInt(1, 3);
			stmt1.execute();
			ResultSet rs1 = stmt1.getResultSet();

			List<String> rows1 = new ArrayList<String>();
			while (rs1.next()) {
				rows1.add(rs1.getString("name"));
			}

			PreparedStatement stmt2 = conn.prepareStatement("select name from test where id in (0,1) limit ? offset 0");
			stmt2.setInt(1, 3);
			stmt2.execute();
			ResultSet rs2 = stmt2.getResultSet();

			List<String> rows2 = new ArrayList<String>();
			while (rs2.next()) {
				rows2.add(rs2.getString("name"));
			}

			Assert.assertEquals(rows1.size(), rows2.size());

			stmt1.close();
			stmt2.close();
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void testLimitOffset2() throws Exception {
		DataSource ds = (DataSource) context.getBean("zebraDS");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement stmt1 = conn.prepareStatement("select name from test where id in (0,1) limit ?, 3");
			stmt1.setInt(1, 0);
			stmt1.execute();
			ResultSet rs1 = stmt1.getResultSet();

			List<String> rows1 = new ArrayList<String>();
			while (rs1.next()) {
				rows1.add(rs1.getString("name"));
			}

			PreparedStatement stmt2 = conn.prepareStatement("select name from test where id in (0,1) limit 3 offset ?");
			stmt2.setInt(1, 0);
			stmt2.execute();
			ResultSet rs2 = stmt2.getResultSet();

			List<String> rows2 = new ArrayList<String>();
			while (rs2.next()) {
				rows2.add(rs2.getString("name"));
			}

			Assert.assertEquals(rows1.size(), rows2.size());

			stmt1.close();
			stmt2.close();
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
}
