package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by wxl on 17/4/21.
 */

public class ShardRangeCrossTest extends MultiDBBaseTestCase {

    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "mockdb-config/testRangeCross/createtable-multidb-rangetest.xml";
    }

    @Override
    protected String getDataFile() {
        return "mockdb-config/testRangeCross/data-multidb-rangetest.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] {"mockdb-config/testRangeCross/ctx-multidb-rangetest.xml"};
    }

    public DataSource getDataSource() {
        return (DataSource) context.getBean("zebraDS");
    }


    /**
     *           DATE          | DB | TB
     * ------------------------+----+------
     *            - 2015-01-09 |  0 |  4
     * ------------------------+----+------
     * 2015-01-10 — 2015-03-31 |  0 |  0
     * ------------------------+----+------
     * 2015-04-01 — 2015-06-30 |  1 |  0
     * ------------------------+----+------
     * 2015-07-01 — 2015-09-30 |  0 |  1
     * ------------------------+----+------
     * 2015-10-01 — 2015-12-31 |  1 |  1
     * ------------------------+----+------
     * 2016-01-01 — 2016-03-31 |  0 |  2
     * ------------------------+----+------
     * 2016-04-01 — 2016-06-30 |  1 |  2
     * ------------------------+----+------
     * 2016-07-01 — 2016-09-30 |  0 |  3
     * ------------------------+----+------
     * 2016-10-15 — 2016-12-20 |  1 |  3
     * ------------------------+----+------
     * 2016-12-21 -            |  0 |  4
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

        pst.setDate(1, new Date(1420819200000L));
//        pst.setDate(1, new Date(1420950225000L));
//        pst.setDate(1, new Date(1420905600000L));
        rs = pst.executeQuery();
        while (rs.next()) {
            Assert.assertEquals(1, rs.getInt(1));
        }

        // select
        checkCountEqual(stmt, "2015-01-10 00:00:00", 1);
        checkCountGreater(stmt, "2014-01-01", 8, false);
        checkCountGreater(stmt, "2016-04-01", 2, false);
        checkCountGreater(stmt, "2016-04-01", 3, true);


        innerInsert(stmt, 1, "t-1-2", 1, "2016-04-02");
        checkCountByRange(stmt, "2016-04-01", "2016-04-02", 2);
        checkCountBetween(stmt, "2016-04-01", "2016-04-02", 2);
        checkCountGreater(stmt, "2016-04-01", 3, false);
        checkCountEqual(stmt, "2016-04-02", 1);
        checkCountEqual(stmt, "2016-04-03", 0);
        checkCountEqual(stmt, "2016-03-31", 0);
        checkCountLess(stmt, "2016-04-01", 5, false);
        checkCountByRange(stmt, "2016-04-01", true, "2016-05-01", false, 2);
        checkAllCount(stmt, 9);

        innerInsert(stmt, 1, "t-0-4", 1, "2014-07-07");
        checkAllCount(stmt, 10);
        checkCountLess(stmt, "2015-01-01", 1, false);
        checkCountLess(stmt, "2015-01-10", 2, true);

        innerInsert(stmt, 1, "t-0-4", 1, "2017-07-07");
        checkAllCount(stmt, 11);
        checkCountGreater(stmt, "2016-12-31", 1, false);
        checkCountGreater(stmt, "2016-10-01", 2, true);

        innerDelete(stmt, "2014-07-07");
        checkAllCount(stmt, 10);
        checkCountLess(stmt, "2015-01-01", 0, false);
        checkCountLess(stmt, "2015-01-10", 1, true);

        checkCountLess(stmt, "2015-10-01", 3,false);
        checkCountGreater(stmt, "2015-10-01", 6,false);
        checkCountByRange(stmt, "2015-10-01", true, "2015-10-01", true, 1);
        innerInsert(stmt, 1, "t-1-1", 1, "2015-10-01");
        checkAllCount(stmt, 11);
        checkCountLess(stmt, "2015-10-01", 3,false);
        checkCountEqual(stmt, "2015-10-01", 2);

        checkCountByRange(stmt, "2015-09-01", true, "2015-10-01", false, 0);
        innerInsert(stmt, 1, "t-1-1_a", 1, "2015-09-30");
        checkCountLess(stmt, "2015-10-01", 4,false);
        checkAllCount(stmt, 12);
        checkCountByRange(stmt, "2015-09-01", true, "2015-10-01", false, 1);


        sql = "SELECT * FROM `TestTable` WHERE `createtime` = '2015-09-30'";
        rs = stmt.executeQuery(sql);
        if (rs.next()) {
            Assert.assertEquals("t-1-1_a", rs.getString(3));
        } else {
            Assert.assertTrue(false);
        }
        innerUpdate(stmt, "2015-09-30", "Name", "'t-1-1_b'");
        sql = "SELECT * FROM `TestTable` WHERE `createtime` = '2015-09-30'";
        rs = stmt.executeQuery(sql);
        if (rs.next()) {
            Assert.assertEquals("t-1-1_b", rs.getString(3));
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
