package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.shard.api.ShardDataSourceHelper;
import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by wxl on 17/6/10.
 */

public class MultiShardKeyTest extends MultiDBBaseTestCase {
    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "mockdb-config/testMultiShardKey/createtable-multidb-multisk.xml";
    }

    @Override
    protected String getDataFile() {
        return "mockdb-config/testMultiShardKey/data-multidb-multisk.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] { "mockdb-config/testMultiShardKey/ctx-multidb-multisk.xml" };
    }

    public DataSource getDataSource() {
        return (DataSource) context.getBean("zebraDS");
    }

    private Set<TbEntity> parseAndCloseResultSet(ResultSet rs) throws SQLException {
        Set<TbEntity> s = new HashSet<TbEntity>();
        if (rs != null) {
            while (rs.next()) {
                s.add(new TbEntity(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4)));
            }
            rs.close();
        }
        return s;
    }

    private Set<TbEntity> generateEntities(TbEntity... entities) {
        Set<TbEntity> s = new HashSet<TbEntity>();
        if (entities != null) {
            for(TbEntity entity : entities) {
                s.add(entity);
            }
        }
        return s;
    }

    @Test
    public void testMultiSk() throws SQLException {

        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = null;
        Set<TbEntity> expected = null;

        // table = Tb, sk = uid
        rs = st.executeQuery("SELECT * FROM `Tb` WHERE `uid` IN (0,1,2,3)");
        Set<TbEntity> actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(1, 0, 4, "uid-0"), new TbEntity(1, 1, 4, "uid-1"), new TbEntity(1, 2, 4, "uid-2"), new TbEntity(1, 3, 4, "uid-3"));
        Assert.assertEquals(expected, actual);

        // table = Tb, sk = tid
        rs = st.executeQuery("SELECT * FROM `Tb` WHERE `tid` IN (0,1,2,3)");
        actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(2, 4, 0, "tid-0"), new TbEntity(2, 4, 1, "tid-1"), new TbEntity(2, 4, 2, "tid-2"), new TbEntity(2, 4, 3, "tid-3"));
        Assert.assertEquals(expected, actual);

        // table = Tb, sk = uid + tid
        actual = new HashSet<TbEntity>();
        for (int i = 0; i <= 4; ++i) {
            rs = st.executeQuery("/*+zebra:sk=uid+tid*/SELECT * FROM `Tb` WHERE `uid` = " + i + " AND `tid` = 1");
            actual.addAll(parseAndCloseResultSet(rs));
        }
        expected = generateEntities(new TbEntity(1, 1, 1, "ut-11"), new TbEntity(1, 2, 1, "ut-21"), new TbEntity(1, 3, 1, "ut-31"), new TbEntity(1, 4, 1, "ut-41"));
        Assert.assertEquals(expected, actual);

        // table = Tbut, sk = uid
        rs = st.executeQuery("SELECT * FROM `Tbut` WHERE `uid` IN (0,1,2,3)");
        actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(1, 0, 4, "ut-uid-0"), new TbEntity(1, 1, 4, "ut-uid-1"), new TbEntity(1, 2, 4, "ut-uid-2"), new TbEntity(1, 3, 4, "ut-uid-3"));
        Assert.assertEquals(expected, actual);

        // table = Tbut, sk = uid + tid
        int[][] ut = new int[][] { { 4, 4, 5, 5 }, { 0, 1, 0, 1 } };
        actual = new HashSet<TbEntity>();
        for (int i = 0; i < 4; ++i) {
            rs = st.executeQuery("/*+zebra:sk=uid+tid*/SELECT * FROM `Tbut` WHERE `uid` = " + ut[0][i] + " AND `tid` = " + ut[1][i]);
            actual.addAll(parseAndCloseResultSet(rs));
        }
        expected = generateEntities(new TbEntity(2, 4, 0, "u_t-00"), new TbEntity(2, 4, 1, "u_t-01"), new TbEntity(2, 5, 0, "u_t-10"), new TbEntity(2, 5, 1, "u_t-11"));
        Assert.assertEquals(expected, actual);

        st.close();
        conn.close();
    }

    @Test
    public void testMultiSkApi() throws SQLException {
        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = null;
        Set<TbEntity> expected = null;
        HashSet<String> hintShardColumns = new HashSet<String>();
        HashSet<String> hintShardColumns2 = new HashSet<String>();
        hintShardColumns.add("uid");
        hintShardColumns.add("tid");
        hintShardColumns2.add("tid");

        // table = Tb, sk = uid
        rs = st.executeQuery("SELECT * FROM `Tb` WHERE `uid` IN (0,1,2,3)");
        Set<TbEntity> actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(1, 0, 4, "uid-0"), new TbEntity(1, 1, 4, "uid-1"), new TbEntity(1, 2, 4, "uid-2"), new TbEntity(1, 3, 4, "uid-3"));
        Assert.assertEquals(expected, actual);

        // table = Tb, update tid(auxiliary)
        ShardDataSourceHelper.setHintShardColumn(hintShardColumns2);
        st.execute("UPDATE `Tb` SET `Name` = 'tid-update-2' WHERE `tid` = 2");
        rs = st.executeQuery("SELECT * FROM  `Tb` WHERE `tid` = 2");
        actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(2, 4, 2, "tid-update-2"));
        Assert.assertEquals(expected, actual);


        // table = Tb, sk = uid + tid
        actual = new HashSet<TbEntity>();
        for (int i = 0; i <= 4; ++i) {
            ShardDataSourceHelper.setHintShardColumn(hintShardColumns);
            rs = st.executeQuery("SELECT * FROM `Tb` WHERE `uid` = " + i + " AND `tid` = 1");
            actual.addAll(parseAndCloseResultSet(rs));
        }
        expected = generateEntities(new TbEntity(1, 1, 1, "ut-11"), new TbEntity(1, 2, 1, "ut-21"), new TbEntity(1, 3, 1, "ut-31"), new TbEntity(1, 4, 1, "ut-41"));
        Assert.assertEquals(expected, actual);

        // table = Tbut, sk = uid + tid
        int[][] ut = new int[][] { { 4, 4, 5, 5 }, { 0, 1, 0, 1 } };
        actual = new HashSet<TbEntity>();
        for (int i = 0; i < 4; ++i) {
            ShardDataSourceHelper.setHintShardColumn(hintShardColumns);
            rs = st.executeQuery("/*+zebra:sk=uid*/SELECT * FROM `Tbut` WHERE `uid` = " + ut[0][i] + " AND `tid` = " + ut[1][i]);
            actual.addAll(parseAndCloseResultSet(rs));
        }
        expected = generateEntities(new TbEntity(2, 4, 0, "u_t-00"), new TbEntity(2, 4, 1, "u_t-01"), new TbEntity(2, 5, 0, "u_t-10"), new TbEntity(2, 5, 1, "u_t-11"));
        Assert.assertEquals(expected, actual);

        st.close();
        conn.close();
    }

    @Test
    public void testMultiSkUpdate() throws SQLException {
        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();
        Statement st = conn.createStatement();
        PreparedStatement pst = null;
        ResultSet rs = null;
        Set<TbEntity> expected = null;

        // table = Tb, update uid(master)
        st.execute("UPDATE `Tb` SET `Name` = 'uid-update-1' WHERE `uid` = 1");
        pst = conn.prepareStatement("SELECT * FROM  `Tb` WHERE `uid` = ?");
        pst.setInt(1, 1);
        rs = pst.executeQuery();
        Set<TbEntity> actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(1, 1, 4, "uid-update-1"));
        Assert.assertEquals(expected, actual);

        // table = Tb, update tid(auxiliary)
        st.execute("UPDATE `Tb` SET `Name` = 'tid-update-2' WHERE `tid` = 2");
        pst = conn.prepareStatement("SELECT * FROM  `Tb` WHERE `tid` = ?");
        pst.setInt(1, 2);
        rs = pst.executeQuery();
        actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(2, 4, 2, "tid-update-2"));
        Assert.assertEquals(expected, actual);

        st.execute("/*+zebra:sk=tid*/UPDATE `Tb` SET `Name` = 'tid-update-3' WHERE `tid` = 3");
        pst = conn.prepareStatement("SELECT * FROM  `Tb` WHERE `tid` = ?");
        pst.setInt(1, 3);
        rs = pst.executeQuery();
        actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(2, 4, 3, "tid-update-3"));
        Assert.assertEquals(expected, actual);

        // table = Tb, update uid+tid(auxiliary)
        st.execute("UPDATE `Tb` SET `Name` = 'ut-update-31' WHERE `uid`= 3 AND `tid` = 4");
        rs = st.executeQuery("SELECT * FROM `Tb` WHERE `uid` = 3");
        actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(1, 3, 4, "ut-update-31"));
        Assert.assertEquals(expected, actual);

        st.execute("/*+zebra:sk=uid+tid*/UPDATE `Tb` SET `Name` = 'ut-update-31' WHERE `uid`= 3 AND `tid` = 1");
        pst = conn.prepareStatement("/*+zebra:sk=uid+tid*/SELECT * FROM  `Tb` WHERE `uid` = ? AND `tid` = ?");
        pst.setInt(1, 3);
        pst.setInt(2, 1);
        rs = pst.executeQuery();
        actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(1, 3, 1, "ut-update-31"));
        Assert.assertEquals(expected, actual);

        // table = Tb, update uid+tid(auxiliary) hit uid
        st.execute("/*+zebra:sk=uid*/UPDATE `Tb` SET `Name` = 'uid-ut-24' WHERE `uid`= 2 AND `tid` = 4");
        pst = conn.prepareStatement("SELECT * FROM  `Tb` WHERE `uid` = ?");
        pst.setInt(1, 2);
        rs = pst.executeQuery();
        actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(1, 2, 4, "uid-ut-24"));
        Assert.assertEquals(expected, actual);


        // table = Tbut, update uid(master)
        st.execute("UPDATE `Tbut` SET `Name` = 'ut_uid-update-2' WHERE `uid`= 2");
        pst = conn.prepareStatement("SELECT * FROM  `Tbut` WHERE `uid` = ?");
        pst.setInt(1, 2);
        rs = pst.executeQuery();
        actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(1, 2, 4, "ut_uid-update-2"));
        Assert.assertEquals(expected, actual);

        // table = Tbut, update uid+tid(auxiliary)
        st.execute("/*+zebra:sk=uid+tid*/UPDATE `Tbut` SET `Name` = 'u_t-update-51' WHERE `uid`= 5 AND `tid` = 1");
        pst = conn.prepareStatement("/*+zebra:sk=uid+tid*/SELECT * FROM  `Tbut` WHERE `uid` = ? AND `tid` = ?");
        pst.setInt(1, 5);
        pst.setInt(2, 1);
        rs = pst.executeQuery();
        actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(2, 5, 1, "u_t-update-51"));
        Assert.assertEquals(expected, actual);


        st.close();
        conn.close();
    }


    @Test
    public void testMultiSkInsert() throws SQLException {

        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();
        Statement st = conn.createStatement();
        PreparedStatement pst = null;
        ResultSet rs = null;
        Set<TbEntity> expected = null;

        // table = Tb, update uid(master)
        st.execute("INSERT INTO `Tb` (`uid`, `name`) VALUES(8, 'uid-insert-8')");
        pst = conn.prepareStatement("SELECT * FROM  `Tb` WHERE `uid` = ?");
        pst.setInt(1, 8);
        rs = pst.executeQuery();
        Set<TbEntity> actual = parseAndCloseResultSet(rs);
        expected = generateEntities(new TbEntity(3, 8, 0, "uid-insert-8"));
        Assert.assertEquals(expected, actual);

        st.close();
        conn.close();
    }

    @Test
    public void testAuxiliaryDimension() throws SQLException {

        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = null;

        // table = Tb, update uid(master)
        st.execute("UPDATE `Tb` SET `Name` = 'uid-update-1' WHERE `uid` = 1");

        // table = Tb, update tid(auxiliary)
        try {
            st.execute("UPDATE `Tb` SET `Name` = 'tid-update-2' WHERE `tid` = 2");
        } catch (Exception e) {
            Assert.assertEquals("com.dianping.zebra.exception.ZebraException: Cannot find a master dimension to write, " +
                    "sql type = UPDATE", e.getMessage());
        }
        st.execute("/*+zebra:sk=tid*/UPDATE `Tb` SET `Name` = 'tid-update-2' WHERE `tid` = 2");

        // table = Tb, insert tid(auxiliary)
        try {
            st.execute("INSERT INTO `Tb`(`tid`, `Name`) VALUES (8, 'tid-8')");
        } catch (Exception e) {
            Assert.assertEquals("com.dianping.zebra.shard.exception.ShardRouterException: Router column[[uid]] not found in the sql!", e.getMessage());
        }
        st.execute("/*+zebra:sk=tid*/INSERT INTO `Tb`(`tid`, `Name`) VALUES (8, 'tid-8')");
        rs = st.executeQuery("/*+zebra:sk=tid*/SELECT * FROM  `Tb` WHERE `tid` = 8");
        Set<TbEntity>actual = parseAndCloseResultSet(rs);
        Set<TbEntity>expected = generateEntities(new TbEntity(3, 0, 8, "tid-8"));
        Assert.assertEquals(expected, actual);

        st.close();
        conn.close();
    }

    private static class TbEntity {
        private int id;

        private int uid;

        private int tid;

        private String name;

        public TbEntity(int id, int uid, int tid, String name) {
            this.id = id;
            this.uid = uid;
            this.tid = tid;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TbEntity tbEntity = (TbEntity) o;

            if (id != tbEntity.id) return false;
            if (uid != tbEntity.uid) return false;
            if (tid != tbEntity.tid) return false;
            return name != null ? name.equals(tbEntity.name) : tbEntity.name == null;

        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + uid;
            result = 31 * result + tid;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "TBE{" +
                    "id=" + id +
                    ", uid=" + uid +
                    ", tid=" + tid +
                    ", name='" + name + "'}\n";
        }
    }
}
