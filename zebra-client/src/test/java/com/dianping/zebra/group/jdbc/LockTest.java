package com.dianping.zebra.group.jdbc;

import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class LockTest {

    @Test
    public void test() throws SQLException, IOException {
        ArrayList<Thread> threadList = new ArrayList<Thread>();
        for (int j = 0; j < 3; j++) {
            GroupDataSource ds = new GroupDataSource("zebra");
            ds.init();
            for (int i = 0; i < 20; i++) {
                CountDownLatch latch = new CountDownLatch(20);
                Thread t = new Thread(new Task(ds, latch));
                threadList.add(t);
            }
        }
        for (Thread t : threadList) {
            t.start();
        }

        System.in.read();
    }

    private static void test1(GroupDataSource ds) throws SQLException {
        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        PreparedStatement stmt = conn.prepareStatement("Update AuditLog Set ProcessID = ProcessID + 1 where id = ?");

        stmt.setString(1, String.valueOf(1));

        stmt.executeUpdate();

        System.out.println(1);
        conn.commit();
        stmt.close();
        conn.close();
        for (int i = 1; i <= 50; i++) {
            conn = ds.getConnection();
            stmt = conn.prepareStatement("select * from  AuditLog where id = ?");

            stmt.setString(1, String.valueOf(1));

            stmt.executeQuery();
//			stmt = conn.prepareStatement("INSERT INTO `AuditLog` (`ProcessID`, `Message`, `Executor`, `method`)"
//					+ "VALUES(0, '0', 'keren.chen', 'addWhitelist')");
//			stmt.execute();
            stmt = conn.prepareStatement("Update AuditLog Set ProcessID = ProcessID + 1 where id = ?");

            stmt.setString(1, String.valueOf(2));

            stmt.executeUpdate();

            System.out.println(2);

            stmt.close();
            conn.close();
        }
    }

    public static class Task implements Runnable {

        private GroupDataSource ds = null;
        private CountDownLatch latch = null;

        public Task(GroupDataSource ds, CountDownLatch latch) {
            this.ds = ds;
            this.latch = latch;
        }

        @Override
        public void run() {
            latch.countDown();

            try {
                test1(ds);
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
            }

            try {
                System.in.read();
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
        }
    }
}