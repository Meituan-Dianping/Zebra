package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by wxl on 17/3/16.
 */

public class ShardDataSourceTest extends MultiDBBaseTestCase {

    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "db-datafiles/createtable-multidb-shardtest.xml";
    }

    @Override
    protected String getDataFile() {
        return "db-datafiles/data-multidb-shardtest.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] { "ctx-multidb-shardtest.xml" };
    }

    public DataSource getDataSource() {
        return (DataSource) context.getBean("zebraDS");
    }


    @Test
    public void testShardSelect() throws SQLException {
        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();

        String sql = "SELECT * FROM `table` WHERE `id` = %d";
        for(int i = 1; i <= 8; ++i) {
            ResultSet rs = stmt.executeQuery(String.format(sql, i));

            if(rs.next()) {
                Assert.assertEquals(i, rs.getInt(2));
                Assert.assertEquals("man_"+i, rs.getString(3));
            }
        }
        stmt.close();
        conn.close();
    }

    @Test
    public void testShardInsert() throws SQLException {
        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();

        String sql = "insert into `table`(id, name, age, address) values (12, 'man_12', 32, 'shanghai_12,china')";
        stmt.execute(sql);
        sql = "SELECT * FROM `table` WHERE `id` = 12";
        ResultSet rs = stmt.executeQuery(sql);
        Assert.assertEquals(true, rs.next());

        Assert.assertEquals(12, rs.getInt(2));
        Assert.assertEquals("man_12", rs.getString(3));
        Assert.assertEquals(32, rs.getInt(4));
        Assert.assertEquals("shanghai_12,china", rs.getString(5));


        stmt.close();
        conn.close();
    }


    //    @Test
    public void testSelectForUpdate() throws SQLException {
        final DataSource ds = getDataSource();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = ds.getConnection();
                    conn.setAutoCommit(false);
                    Statement stmt = conn.createStatement();
                    stmt.execute("SELECT `name` FROM `table` WHERE `id` = 1 FOR UPDATE");
                    ResultSet rs = stmt.getResultSet();
                    while (rs.next()) {
                        Assert.assertEquals("man_1", rs.getString(1));
                    }
                    Thread.sleep(2000);
                    rs.close();
                    conn.commit();
                    stmt.close();
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = ds.getConnection();
                    Statement stmt = conn.createStatement();

                    Thread.sleep(1000);
                    System.out.println("Thread 2 ----------- before");
                    try {
                        stmt.execute("UPDATE `table` SET NAME = 'man_update_1' WHERE `id` = 1");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        Assert.assertEquals("rg.h2.jdbc.JdbcSQLException: Timeout trying to lock table ; SQL statement:\n" +
                                "UPDATE `table0`\n" +
                                "SET NAME = 'main_update_1'\n" +
                                "WHERE `id` = 1 [50200-157]", e.getMessage());
                    }
                    System.out.println("Thread 2 ----------- end");
                    Thread.sleep(2000);
                    ResultSet rs = stmt.executeQuery("SELECT `name` FROM `table` WHERE `id` = 1");
                    while (rs.next()) {
                        Assert.assertEquals("man_update_1", rs.getString(1));
                    }
                    rs.close();
                    stmt.close();
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
    }


    //连真实的mysql数据库可测，这里是连beta环境的xintongshardtest
    //@Test
    @SuppressWarnings("unused")
    public void testShardRange() throws SQLException {
        Map<String, String> keyMap = new HashMap<String, String>();


        ShardDataSource ds = new ShardDataSource();
        ds.setRuleName("xintongsharttest");
        ds.init();

        Connection conn = ds.getConnection();
        Statement st = conn.createStatement();
        Random random = new Random();

        try {
            for (int i = 0; i < 5; ++i) {
                int t = random.nextInt(4) + 1;
                ResultSet r = st.executeQuery("select * from AuditLog WHERE `id` BETWEEN 90 AND 100");
                System.out.println("ID\tPID\tMSG");
                while (r.next()) {
                    int id = r.getInt(1);
                    System.out.println(r.getInt(1) + "\t" + r.getInt(2) + "\t" + r.getString(3));
                    System.out.println("DB = " + id % 2 + ", TB = " + (id / 2) % 2);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //    @Test
    public void testRealDb() throws SQLException {
        try {
            ShardDataSource ds = new ShardDataSource();
            ds.setParallelExecuteTimeOut(10000);
            ds.setRuleName("zebratestservice");
            ds.setForbidNoShardKeyWrite(true);
            ds.init();

            Connection conn = ds.getConnection();
            Statement st = conn.createStatement();

            try {
                System.out.println("------------ s -------------");

                ResultSet rs = st.executeQuery("SELECT * FROM TestSync WHERE Name = 'abc'");
                while (rs.next()) {
                    System.out.println("ID="+rs.getInt(1)+" Uid="+rs.getInt(2)+" Name="+rs.getString(3));
                }
                rs.close();

                int r = st.executeUpdate("UPDATE TestSync SET NAME = 'abcd' WHERE Name = 'abc'");


                System.out.println("------------ e -------------");
            } catch (Exception e) {
                e.printStackTrace();
            }
            st.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
