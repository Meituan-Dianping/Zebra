package com.dianping.zebra.group.performance;

import com.dianping.zebra.Constants;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dozer on 9/24/14.
 */
public class PerformanceTest {
	GroupDataSource ds;

	private void createTable() throws SQLException {
		execute("CREATE TABLE IF NOT EXISTS app ( name varchar(100) )");
	}

	void execute(String sql) throws SQLException {
		Connection conn = null;
		Statement stat = null;
		try {
			conn = ds.getConnection();
			stat = conn.createStatement();
			stat.execute(sql);
		} finally {
			if (stat != null) {
				stat.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Test
	public void test_not_use_filter() throws SQLException, InterruptedException {
		test_performance(false);
	}

	public void test_performance(boolean useFilter) throws SQLException, InterruptedException {
		ds = new GroupDataSource();
		ds.setJdbcRef("sample.ds.v2");
		if (useFilter) {
			ds.setFilter("mock,stat,wall");
		}
		ds.setConfigManagerType(Constants.CONFIG_MANAGER_TYPE_LOCAL);
		ds.init();
		createTable();
		ds.getConfig();

		List<Thread> threads = new ArrayList<Thread>();
		for (int k = 0; k < 100; k++) {
			threads.add(new Thread(new Executer()));
		}

		long startTime = System.currentTimeMillis();
		for (Thread t : threads) {
			t.start();
		}
		while (Iterables.all(threads, new Predicate<Thread>() {
			@Override public boolean apply(Thread thread) {
				return !thread.isAlive();
			}
		})) {
			Thread.yield();
		}
		System.out.println(System.currentTimeMillis() - startTime);
	}

	@Test
	public void test_use_filter() throws SQLException, InterruptedException {
		test_performance(true);
	}

	class Executer implements Runnable {
		@Override public void run() {
			try {
				for (int k = 1; k <= 1000; k++) {
					execute("insert into `app` (`name`) values ('test')");
					execute("update `app` set `name` = 'test2'");
					execute("delete from `app`");
					execute("select 1 from `app`");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
