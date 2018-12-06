package com.dianping.zebra.group.performance;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ZebraBaseCase implements Runnable {

	protected DataSource dataSource;

	private AtomicInteger i = new AtomicInteger(1);
	
	public ZebraBaseCase(String path) throws Exception {
		ApplicationContext appContext = new ClassPathXmlApplicationContext(path);
		dataSource = (DataSource) appContext.getBean("dataSource");
	}
	
	@Override
	public void run() {
		Connection conn = null;
		try {
			int id = i.getAndIncrement();

			conn = dataSource.getConnection();
			conn.setAutoCommit(true);
			insert(conn, id);
			update(conn, id);
			select(conn);
			delete(conn, id);

			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void delete(Connection conn, int id) {
		String sql = "delete from PERSON where id='" + id + "'";

		_execute(conn, sql);
	}

	private void select(Connection conn) {
		String sql = "select * from PERSON where id='2'";

		_execute(conn, sql);
	}

	private void update(Connection conn, int id) {
		String sql = "update PERSON set name='test33' where id='" + id + "'";

		_execute(conn, sql);
	}

	private void insert(Connection conn, int id) {
		String sql = "insert into PERSON values (" + id + ",'dasd','fasfa',231)";

		_execute(conn, sql);
	}

	private void _execute(Connection conn, String sql) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
		}
	}

}
