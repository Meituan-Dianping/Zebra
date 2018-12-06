package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by wxl on 17/4/21.
 */

public class ShardRangeDefaultTest extends MultiDBBaseTestCase {

    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "mockdb-config/testRangeDefault/createtable-multidb-rangetest.xml";
    }

    @Override
    protected String getDataFile() {
        return "mockdb-config/testRangeDefault/data-multidb-rangetest.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] {"mockdb-config/testRangeDefault/ctx-multidb-rangetest.xml"};
    }

    public DataSource getDataSource() {
        return (DataSource) context.getBean("zebraDS");
    }


    /**
     *           DATE          | DB | TB
     * ------------------------+----+------
     *            - 2010-01-09 |  0 |  4
     * ------------------------+----+------
     * 2010-01-10 — 2010-06-30 |  0 |  0
     * ------------------------+----+------
     * 2010-07-01 — 2010-12-31 |  0 |  1
     * ------------------------+----+------
     * 2011-01-01 — 2011-06-30 |  0 |  2
     * ------------------------+----+------
     * 2011-07-01 — 2011-12-31 |  0 |  3
     * ------------------------+----+------
     * 2012-01-01 — 2012-06-30 |  1 |  0
     * ------------------------+----+------
     * 2012-07-01 — 2012-12-31 |  1 |  1
     * ------------------------+----+------
     * 2013-01-01 — 2013-06-30 |  1 |  2
     * ------------------------+----+------
     * 2013-07-01 — 2013-12-20 |  1 |  3
     * ------------------------+----+------
     * 2013-12-21 -            |  0 |  4
     *
     */
    @Test
    public void testCross() throws SQLException {

        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM `TestTable` WHERE `createtime` = ?");
        String sql;
        ResultSet rs;

        pst.setDate(1, new Date(1264953600000L));
        rs = pst.executeQuery();
        while (rs.next()) {
            Assert.assertEquals(1, rs.getInt(1));
        }

        checkAllCount(stmt, 8);
        checkCountLess(stmt, "2010-01-10", 0, false);
        checkCountGreater(stmt, "2013-12-20", 0, false);

        checkCountBetween(stmt, "2010-01-10", "2011-12-20", 4);
        checkCountBetween(stmt, "2010-01-10", "2011-07-31", 3);
        checkCountBetween(stmt, "2012-01-10", "2013-12-20", 4);
        checkCountBetween(stmt, "2012-01-10", "2013-07-31", 3);

        checkCountBetween(stmt, "2010-06-01", "2010-06-30", 0);
        innerInsert(stmt, 1, "t-0-1_a", 1, "2010-06-30");
        checkAllCount(stmt, 9);
        checkCountBetween(stmt, "2010-06-01", "2010-06-30", 1);

        innerInsert(stmt, 1, "t-0-4", 1, "2010-01-09");
        checkAllCount(stmt, 10);
        checkCountLess(stmt, "2010-01-10", 1, false);
        checkCountGreater(stmt, "2010-01-10", 9, true);

        innerInsert(stmt, 1, "t-1-2_a", 1, "2012-12-31");
        checkAllCount(stmt, 11);
        checkCountBetween(stmt, "2012-07-01", "2012-12-31", 2);
        checkCountEqual(stmt, "2012-08-01", 1);

        innerDelete(stmt, "2012-08-01");
        checkAllCount(stmt, 10);
        checkCountEqual(stmt, "2012-08-01", 0);

        sql = "SELECT * FROM `TestTable` WHERE `createtime` = '2010-06-30'";
        rs = stmt.executeQuery(sql);
        if (rs.next()) {
            Assert.assertEquals("t-0-1_a", rs.getString(3));
        } else {
            Assert.assertTrue(false);
        }
        innerUpdate(stmt, "2010-06-30", "Name", "'t-0-1_b'");
        sql = "SELECT * FROM `TestTable` WHERE `createtime` = '2010-06-30'";
        rs = stmt.executeQuery(sql);
        if (rs.next()) {
            Assert.assertEquals("t-0-1_b", rs.getString(3));
        } else {
            Assert.assertTrue(false);
        }


        stmt.close();
        conn.close();
    }

    private void innerUpdate(Statement stmt, String createTime, String column, String value) throws SQLException {
        String sql = "UPDATE `TestTable` SET `"+column+"` = "+value+" WHERE `CreateTime` = '"+createTime+"'";
        stmt.execute(sql);
    }

    private void innerDelete(Statement stmt, String time) throws SQLException{
        String sql = "delete from `TestTable` WHERE `CreateTime` ='"+time+"'";
        stmt.execute(sql);
    }

    private void innerInsert(Statement stmt, int id, String name, int age, String time) throws SQLException{
        String sql = "insert into `TestTable`(id, name, age, createtime) " +
                "values ("+id+", '"+name+"', "+age+", '"+time+"')";
        stmt.execute(sql);
    }

    private void checkCountGreater(Statement stmt, String start, int count, boolean isInclusive) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `TestTable` WHERE `createtime` > '"+start+"'";
        if(isInclusive) {
            sql = "SELECT COUNT(*) FROM `TestTable` WHERE `createtime` >= '"+start+"'";
        }
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Assert.assertEquals(count, rs.getInt(1));
        }
    }

    private void checkCountLess(Statement stmt, String start, int count, boolean isInclusive) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `TestTable` WHERE `createtime` < '"+start+"'";
        if(isInclusive) {
            sql = "SELECT COUNT(*) FROM `TestTable` WHERE `createtime` <= '"+start+"'";
        }
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Assert.assertEquals(count, rs.getInt(1));
        }
    }

    @SuppressWarnings("unused")
    private void checkCountByRange(Statement stmt, String start, String end, int count) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `TestTable` WHERE `createtime` >= '"+start+"' AND `createtime` <= '"+end+"'";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Assert.assertEquals(count, rs.getInt(1));
        }
    }

    private void checkCountBetween(Statement stmt, String start, String end, int count) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `TestTable` WHERE `createtime` BETWEEN '"+start+"' AND '"+end+"'";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Assert.assertEquals(count, rs.getInt(1));
        }
    }

    @SuppressWarnings("unused")
    private void checkCountByRange(Statement stmt, String start, boolean startInclusive, String end, boolean endInclusive, int count) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `TestTable` WHERE `createtime` > '"+start+"'";
        if(startInclusive) {
            sql = "SELECT COUNT(*) FROM `TestTable` WHERE `createtime` >= '"+start+"'";
        }
        if(endInclusive) {
            sql += " AND `createtime` <= '"+end+"'";
        } else {
            sql += " AND `createtime` < '"+end+"'";
        }
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Assert.assertEquals(count, rs.getInt(1));
        }
    }

    private void checkCountEqual(Statement stmt,  String start, int count) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `TestTable` WHERE `CreateTime` = '"+start+"'";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Assert.assertEquals(count, rs.getInt(1));
        }
    }

    private void checkAllCount(Statement stmt, int count) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `TestTable`";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Assert.assertEquals(count, rs.getInt(1));
        }
    }




}
