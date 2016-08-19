package com.dianping.zebra.monitor.cat;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Ignore;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.group.util.DaoContextHolder;
import com.dianping.zebra.shard.jdbc.ShardDataSource;
import com.dianping.zebra.single.jdbc.SingleDataSource;

@Ignore
public class CatLogTest {

	@Test
	public void testGroup() throws SQLException, IOException {
		GroupDataSource ds = new GroupDataSource("zebra");
		ds.init();

		for (int i = 0; i < 100; i++) {
			com.dianping.cat.message.Transaction t = Cat.newTransaction("test", "test");
			Connection conn = ds.getConnection();

			DaoContextHolder.setSqlName("TestPreparead1");
			PreparedStatement stmt = conn.prepareStatement("select * from Cluster where Name = ?");

			stmt.setString(1, "wed");

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				System.out.println(rs.getString(1));
			}

			System.out.println("-----");
			rs.close();
			stmt.close();
			conn.close();
			t.complete();
		}
		System.in.read();
	}

	// @Test
	public void testShard() throws SQLException, IOException {
		ShardDataSource ds = new ShardDataSource();
		ds.setRuleName("rsreceipt");
		ds.init();

		Connection conn = ds.getConnection();
		DaoContextHolder.setSqlName("TestShardPreparead");

		PreparedStatement stmt = conn.prepareStatement("select * from RS_Receipt where UserID in (? ,?)");

		stmt.setInt(1, 1797130);
		stmt.setInt(2, 1797131);

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			System.out.println(rs.getInt(2));
		}
		System.in.read();
	}

	@Test
	public void testSingleDataSource() throws SQLException, IOException {
		SingleDataSource ds = new SingleDataSource();

		ds.setPoolType("c3p0");

		ds.setJdbcUrl("jdbc:mysql://10.1.77.20:3306/zebra?characterEncoding=UTF8&socketTimeout=60000");
		ds.setUser("zebra_a");
		ds.setPassword("dp!@aFDceborN");
		ds.setDriverClass("com.mysql.jdbc.Driver");

		ds.setInitialPoolSize(5);
		ds.setMaxPoolSize(20);
		ds.setMinPoolSize(5);
		ds.setIdleConnectionTestPeriod(60);
		ds.setAcquireRetryAttempts(50);
		ds.setAcquireRetryDelay(300);
		ds.setMaxStatements(0);
		ds.setNumHelperThreads(6);
		ds.setMaxAdministrativeTaskTime(5);
		ds.setPreferredTestQuery("SELECT 1");
		ds.setCheckoutTimeout(1000);

		ds.init();

		Connection conn = ds.getConnection();

		DaoContextHolder.setSqlName("Test");
		PreparedStatement stmt = conn.prepareStatement("select * from Cluster where id = ?");

		stmt.setInt(1, 20);

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			String name = rs.getString(2);

			System.out.println(name);
		}

		System.in.read();
	}
}
