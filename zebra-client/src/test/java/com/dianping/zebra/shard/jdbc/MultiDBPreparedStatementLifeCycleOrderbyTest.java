package com.dianping.zebra.shard.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;

import junit.framework.Assert;

public class MultiDBPreparedStatementLifeCycleOrderbyTest extends MultiDBBaseTestCase {

    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "db-datafiles/createtable-multidb-lifecycle.xml";
    }

    @Override
    protected String getDataFile() {
        return "db-datafiles/data-limitdb-lifecycle.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] { "ctx-multidb-lifecycle.xml" };
    }

    @Test
    public void testOrderbyUsingColumnNameOrColumnLabel() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt1 = conn.prepareStatement("select test.name as n,  test.score as s from test order by test.score");
            ResultSet rs1 = stmt1.executeQuery();

            PreparedStatement stmt2 = conn.prepareStatement("select test.name as n,  test.score as s from test order by s");
            ResultSet rs2 = stmt2.executeQuery();

            while(rs1.next() & rs2.next()){
                Assert.assertEquals(rs1.getInt(2), rs2.getInt(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

}
