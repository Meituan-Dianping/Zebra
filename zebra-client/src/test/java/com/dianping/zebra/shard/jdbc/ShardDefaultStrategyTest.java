package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by wxl on 17/7/4.
 */

public class ShardDefaultStrategyTest extends MultiDBBaseTestCase {
    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "mockdb-config/testDefaultstrategy/createtable-multidb-defaultstrategy.xml";
    }

    @Override
    protected String getDataFile() {
        return "mockdb-config/testDefaultstrategy/data-multidb-defaultstrategy.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] {"mockdb-config/testDefaultstrategy/ctx-multidb-defaultstrategy.xml"};
    }

    public DataSource getDataSource() {
        return (DataSource) context.getBean("zebraDS");
    }



    @Test
    public void test() throws SQLException {
        ShardDataSource ds = (ShardDataSource)getDataSource();
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();

        Set<String> expected = new HashSet<String>();
        expected.add("shard-1");    expected.add("shard-2");    expected.add("shard-3");    expected.add("shard-4");
        Set<String> actual = new HashSet<String>();
        ResultSet rs = stmt.executeQuery("SELECT * FROM ShardTable");
        while (rs.next()) {
            actual.add(rs.getString(3));
        }
        Assert.assertEquals(expected, actual);

        expected.clear();
        expected.add("default-1"); expected.add("default-2");
        actual.clear();
        rs = stmt.executeQuery("SELECT * FROM DefaultTable");
        while (rs.next()) {
            actual.add(rs.getString(3));
        }
        Assert.assertEquals(expected, actual);

        PreparedStatement pst = conn.prepareStatement("SELECT uid FROM DefaultTable WHERE id = ?");
        pst.setInt(1, 2);
        rs = pst.executeQuery();
        rs.next();
        Assert.assertEquals(2, rs.getInt(1));

        stmt.close();
        conn.close();
    }

    @Test
    public void testTransaction() throws SQLException {
        ShardDataSource ds = (ShardDataSource)getDataSource();
        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement();
        Set<String> expected = new HashSet<String>();
        Set<String> actual = new HashSet<String>();

        // initialize data
        expected.add("default-1"); expected.add("default-2");
        ResultSet rs = stmt.executeQuery("SELECT * FROM DefaultTable");
        while (rs.next()) {
            actual.add(rs.getString(3));
        }
        rs.close();
        Assert.assertEquals(expected, actual);

        // update rollback
        String name = null;
        stmt.execute("UPDATE DefaultTable SET `name` = 'default-update-2' WHERE id = 2");
        rs = stmt.executeQuery("SELECT `name` FROM DefaultTable WHERE id = 2");
        conn.rollback();
        if(rs.next()) {
            name = rs.getString(1);
        }
        rs.close();
        Assert.assertEquals("default-update-2", name);

        rs = stmt.executeQuery("SELECT `name` FROM DefaultTable WHERE id = 2");
        if(rs.next()) {
            name = rs.getString(1);
        }
        rs.close();
        Assert.assertEquals("default-2", name);

        // update commit
        stmt.execute("UPDATE DefaultTable SET `name` = 'default-commit-1' WHERE id = 1");
        rs = stmt.executeQuery("SELECT `name` FROM DefaultTable WHERE id = 1");
        conn.commit();
        if(rs.next()) {
            name = rs.getString(1);
        }
        rs.close();
        Assert.assertEquals("default-commit-1", name);

        rs = stmt.executeQuery("SELECT `name` FROM DefaultTable WHERE id = 1");
        if(rs.next()) {
            name = rs.getString(1);
        }
        rs.close();
        Assert.assertEquals("default-commit-1", name);


        stmt.close();
        conn.close();
    }


}
