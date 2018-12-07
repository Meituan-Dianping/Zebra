package com.dianping.zebra.sample.jdbc;

import com.dianping.zebra.group.jdbc.GroupDataSource;
import org.junit.Test;

import java.sql.*;

public class GroupDataSourceSample {

	@Test
	public void groupTest() {
		GroupDataSource ds = buildDs();

		insertValue(ds);
		selectValue(ds);
	}

	private void selectValue(GroupDataSource ds) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * From `user`");

			while (rs.next()) {
				System.out.println("mis : " + rs.getString(2));
			}
		} catch (SQLException e) {
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	private void insertValue(GroupDataSource ds) {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ds.getConnection();
			stmt = conn.prepareStatement(
			      "insert int `user` (`Name`,`Mis`,`Email`,`UpdateTime`,`CreateTime`) values (?, ?, ?, ?, ?)");
			stmt.setString(1, "test_abc");
			stmt.setString(2, "test_abc");
			stmt.setString(3, "test_abc@123.com");
			stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

			stmt.executeUpdate();
		} catch (SQLException e) {
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	private GroupDataSource buildDs() {
		GroupDataSource ds = new GroupDataSource();
		ds.setJdbcRef("zebra_sample");
		ds.setPoolType("hikaricp");
		ds.setConfigManagerType("zookeeper");
		ds.setExtraJdbcUrlParams("useSSL=true");
		ds.setMinPoolSize(1);
		ds.setMaxPoolSize(1);
		ds.setInitialPoolSize(1);
		ds.setCheckoutTimeout(1000);
		ds.setPreferredTestQuery("select 1");
		ds.init();

		return ds;
	}
}
