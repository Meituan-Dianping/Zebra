package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by wxl on 17/12/12.
 */

public class ShardConcurrencyTest extends MultiDBBaseTestCase {
    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "mockdb-config/testConcurrency/createtable-multidb-concurrency.xml";
    }

    @Override
    protected String getDataFile() {
        return "mockdb-config/testConcurrency/data-multidb-concurrency.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] {"mockdb-config/testConcurrency/ctx-multidb-concurrency.xml"};
    }

    public DataSource getDataSource() {
        return (DataSource) context.getBean("zebraDS");
    }

    @Before
    public void init() throws SQLException {
        String sql = "INSERT INTO Tb (id, uid, name) VALUES (?,?,?),(?,?,?),(?,?,?),(?,?,?),(?,?,?),(?,?,?),(?,?,?),(?,?,?),(?,?,?),(?,?,?)";
        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();

        for (int i = 0; i < 8; ++i) {
            PreparedStatement pst = conn.prepareStatement(sql);
            for (int j = 1; j <= 10; ++j) {
                pst.setInt(3*(j-1)+1, j);
                pst.setInt(3*(j-1)+2, j*8+i);
                pst.setString(3*(j-1)+3, "Tb-"+i+"-"+j);
            }
            pst.executeUpdate();
            pst.close();
        }
        conn.close();
    }

    @Test
    public void test1() throws SQLException {
        DataSource ds = getDataSource();

        for (int i = 0; i < 4; ++i) {
            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("/*+zebra:cl="+i+"*/SELECT * FROM Tb WHERE uid < 21 AND uid > 7 ORDER BY uid");
            int count = 0;
            while (rs.next()) {
                int uid = rs.getInt(2);
                int b = uid / 8;
                int a = uid - 8 * b;
                String name = rs.getString(3);
                Assert.assertEquals("Tb-" + a + "-" + b, name);
                count++;
            }
            Assert.assertEquals(13, count);
            rs.close();
            stmt.close();
            conn.close();
        }

        for (int i = 0; i < 4; ++i) {
            Connection conn = ds.getConnection();
            PreparedStatement pst = conn.prepareStatement("/*+zebra:cl="+i+"*/SELECT * FROM Tb WHERE uid < ? AND uid > ? ORDER BY uid");
            pst.setInt(1, 21);
            pst.setInt(2, 7);
            ResultSet rs = pst.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                int uid = rs.getInt(2);
                int b = uid / 8;
                int a = uid - 8 * b;
                String name = rs.getString(3);
                Assert.assertEquals("Tb-" + a + "-" + b, name);
            }
            Assert.assertEquals(13, count);
            rs.close();
            pst.close();
            conn.close();
        }

        checkAllCount(ds, 80);

    }

    @Test
    public void test2() throws SQLException {
        DataSource ds = getDataSource();

        Connection conn = ds.getConnection();
        for (int i = 0; i < 4; ++i) {
            Statement stmt = conn.createStatement();
            int r = stmt.executeUpdate("/*+zebra:cl="+i+"*/UPDATE Tb SET NAME = 'Tb-"+i+"' WHERE uid > 7 AND uid < 21");
            Assert.assertEquals(13, r);


            ResultSet rs = stmt.executeQuery("/*+zebra:cl="+i+"*/SELECT * FROM Tb WHERE uid < 21 AND uid > 7 ORDER BY uid");
            int count = 0;
            while (rs.next()) {
                String name = rs.getString(3);
                Assert.assertEquals("Tb-"+i, name);
                count++;
            }
            Assert.assertEquals(13, count);
            rs.close(); stmt.close();
        }
        conn.close();


        checkAllCount(ds, 80);

    }


    private void checkAllCount(DataSource ds, int count) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `Tb`";
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Assert.assertEquals(count, rs.getInt(1));
        }
        rs.close();
        stmt.close();
        conn.close();
    }


}
