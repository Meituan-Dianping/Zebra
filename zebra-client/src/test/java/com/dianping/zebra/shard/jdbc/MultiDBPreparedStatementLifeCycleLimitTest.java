package com.dianping.zebra.shard.jdbc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.alibaba.druid.sql.ast.SQLObjectImpl;
import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;
import com.dianping.zebra.shard.parser.SQLParsedResult;
import com.dianping.zebra.shard.parser.SQLParser;
import com.dianping.zebra.shard.parser.ShardLimitSqlSplitRewrite;
import com.dianping.zebra.shard.parser.SqlToCountSqlRewrite;

import junit.framework.Assert;

public class MultiDBPreparedStatementLifeCycleLimitTest extends MultiDBBaseTestCase {

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
    public void testPopResult() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select * from test limit 1");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<TestEntity> popResult = popResult(rs);
            Assert.assertEquals(1, popResult.size());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    // 测试改写后offset为0
    public void testMultiRouterLimitResult0() throws Exception {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select score from test order by score limit 5,4");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<Integer> rows = new ArrayList<Integer>();
            while (rs.next()) {
                rows.add(rs.getInt("score"));
            }
            Assert.assertEquals(4, rows.size());
            Assert.assertEquals(3, rows.get(0).intValue());
            Assert.assertEquals(3, rows.get(1).intValue());
            Assert.assertEquals(3, rows.get(2).intValue());
            Assert.assertEquals(3, rows.get(3).intValue());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    // 测试倒序排改写后offset为0
    public void testMultiRouterLimitResult1() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select score from test order by score desc limit 7,3");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<Integer> rows = new ArrayList<Integer>();
            while (rs.next()) {
                rows.add(rs.getInt("score"));
            }
            Assert.assertEquals(3, rows.size());
            Assert.assertEquals(9, rows.get(0).intValue());
            Assert.assertEquals(9, rows.get(1).intValue());
            Assert.assertEquals(9, rows.get(2).intValue());
        } catch (Exception e) {
            System.err.println(e);
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    // 测试改写后offset不为0
    public void testMultiRouterLimitResult2() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM test ORDER BY score LIMIT 10,3");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<Integer> rows = new ArrayList<Integer>();
            while (rs.next()) {
                rows.add(rs.getInt("score"));
            }
            Assert.assertEquals(3, rows.size());
            Assert.assertEquals(3, rows.get(0).intValue());
            Assert.assertEquals(4, rows.get(1).intValue());
            Assert.assertEquals(5, rows.get(2).intValue());
        } catch (Exception e) {
            System.err.println(e);
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    // 测试正排,倒排且部分表第一次结果为0的case
    public void testMultiRouterLimitResult3() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM test WHERE score > 6 ORDER BY score DESC, NAME ASC LIMIT 5,3");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<TestEntity> popResult = popResult(rs);
            Assert.assertEquals(3, popResult.size());
            Assert.assertEquals("conan2", popResult.get(0).getName());
            Assert.assertEquals(10, popResult.get(0).getScore());
            Assert.assertEquals("conan4", popResult.get(1).getName());
            Assert.assertEquals(10, popResult.get(1).getScore());
            Assert.assertEquals("conan1", popResult.get(2).getName());
            Assert.assertEquals(9, popResult.get(2).getScore());
        } catch (Exception e) {
            System.err.println(e);
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    // 测试正排,倒排
    public void testMultiRouterLimitResult4() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM test WHERE score <10 ORDER BY score DESC, NAME ASC LIMIT 5,3");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<TestEntity> popResult = popResult(rs);
            Assert.assertEquals(3, popResult.size());
            Assert.assertEquals("conan2", popResult.get(0).getName());
            Assert.assertEquals(8, popResult.get(0).getScore());
            Assert.assertEquals("conan3", popResult.get(1).getName());
            Assert.assertEquals(8, popResult.get(1).getScore());
            Assert.assertEquals("conan0", popResult.get(2).getName());
            Assert.assertEquals(7, popResult.get(2).getScore());
        } catch (Exception e) {
            System.err.println(e);
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testSmallLimit() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select score from test order by score asc limit 3,4");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<TestEntity> popResult = popResult(rs);
            Assert.assertEquals(4, popResult.size());
            Assert.assertEquals(2, popResult.get(0).getScore());
            Assert.assertEquals(2, popResult.get(1).getScore());
            Assert.assertEquals(3, popResult.get(2).getScore());
            Assert.assertEquals(3, popResult.get(2).getScore());
        } catch (Exception e) {
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testLimitSqlWithParams() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn
                    .prepareStatement("select score from test where score > ? order by score asc limit ?,?");
            stmt.setInt(1, 8);
            stmt.setInt(2, 1);
            stmt.setInt(3, 3);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<TestEntity> popResult = popResult(rs);
            Assert.assertEquals(3, popResult.size());
            Assert.assertEquals(9, popResult.get(0).getScore());
            Assert.assertEquals(9, popResult.get(1).getScore());
            Assert.assertEquals(10, popResult.get(2).getScore());
        } catch (Exception e) {
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testLimitSqlWithLargeParams() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select id, score from test order by score asc limit ?,?");
            stmt.setInt(1, 10);
            stmt.setInt(2, 3);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();

            List<TestEntity> popResult = popResult(rs);
            Assert.assertEquals(3, popResult.size());
            Assert.assertEquals(3, popResult.get(0).getScore());
            Assert.assertEquals(4, popResult.get(1).getScore());
            Assert.assertEquals(5, popResult.get(2).getScore());
        } catch (Exception e) {
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testSingleRuleLimit() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select * from test where id = 0 order by id limit ?, ?");
            stmt.setInt(1, 1);
            stmt.setInt(2, 3);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<TestEntity> popResult = popResult(rs);
            Assert.assertEquals(0, popResult.size());
        } catch (Exception e) {
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testSplitLimitSqlReWriteFunction() {
        String sql = "select * from test limit 10,3";
        SQLParsedResult parseWithoutCache = SQLParser.parseWithoutCache(sql);
        String rewriteSql = new ShardLimitSqlSplitRewrite().rewrite(parseWithoutCache, 8, null);
        Assert.assertEquals("SELECT *\nFROM test\nLIMIT 1, 3", rewriteSql);
    }

    @Test
    public void testSplitLimitSqlReWriteFunction2() {
        String sql = "select * from test limit 3 offset 10";
        SQLParsedResult parseWithoutCache = SQLParser.parseWithoutCache(sql);
        String rewriteSql = new ShardLimitSqlSplitRewrite().rewrite(parseWithoutCache, 8, null);
        Assert.assertEquals("SELECT *\nFROM test\nLIMIT 1, 3", rewriteSql);
    }

    @Test
    public void testCount() {
        String sql = "select count(test.score) from test order by test.score";
        SQLParsedResult parseWithoutCache = SQLParser.parseWithoutCache(sql);

        Map<String, SQLObjectImpl> selectItemMap = parseWithoutCache.getMergeContext().getSelectItemMap();
        Map<String, String> columnNameAliasMapping = parseWithoutCache.getMergeContext().getColumnNameAliasMapping();
        Assert.assertEquals(true, selectItemMap.containsKey("COUNT(test.score)"));
        Assert.assertEquals(true, columnNameAliasMapping.isEmpty());
    }

    @Test
    public void testSelectAll() {
        String sql = "select test.* from test order by test.score";
        SQLParsedResult parseWithoutCache = SQLParser.parseWithoutCache(sql);

        Map<String, SQLObjectImpl> selectItemMap = parseWithoutCache.getMergeContext().getSelectItemMap();
        Map<String, String> columnNameAliasMapping = parseWithoutCache.getMergeContext().getColumnNameAliasMapping();
        Assert.assertEquals("test.*", selectItemMap.get("*").toString());
        Assert.assertEquals(true, columnNameAliasMapping.isEmpty());
    }


    @Test
    public void testOrderbyFullName() {
        String sql = "select test.name as n,  test.score as s from test order by test.score";
        SQLParsedResult parseWithoutCache = SQLParser.parseWithoutCache(sql);

        Map<String, SQLObjectImpl> selectItemMap = parseWithoutCache.getMergeContext().getSelectItemMap();
        Map<String, String> columnNameAliasMapping = parseWithoutCache.getMergeContext().getColumnNameAliasMapping();
        Assert.assertEquals(true, selectItemMap.containsKey("n"));
        Assert.assertEquals(true, selectItemMap.containsKey("s"));
        Assert.assertEquals("n", columnNameAliasMapping.get("name"));
        Assert.assertEquals("s", columnNameAliasMapping.get("score"));
    }

    @Test
    public void testOrderbyAlias() {
        String sql = "select test.name as n,  test.score as s from test order by s";
        SQLParsedResult parseWithoutCache = SQLParser.parseWithoutCache(sql);

        Map<String, SQLObjectImpl> selectItemMap = parseWithoutCache.getMergeContext().getSelectItemMap();
        Map<String, String> columnNameAliasMapping = parseWithoutCache.getMergeContext().getColumnNameAliasMapping();
        Assert.assertEquals(true, selectItemMap.containsKey("n"));
        Assert.assertEquals(true, selectItemMap.containsKey("s"));
        Assert.assertEquals("n", columnNameAliasMapping.get("name"));
        Assert.assertEquals("s", columnNameAliasMapping.get("score"));
    }

    @Test
    public void testReWriteFunction() {
        String sql = "select * from test";
        String rewrite = new SqlToCountSqlRewrite().rewrite(sql, null);
        Assert.assertEquals(rewrite, "SELECT COUNT(*) AS zebra_count\nFROM test");
    }

    @SuppressWarnings("unused")
    private class TestEntity {
        private int id;
        private String name;
        private int score;
        private String type;
        private int classid;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getClassid() {
            return classid;
        }

        public void setClassid(int classid) {
            this.classid = classid;
        }

    }

    public List<TestEntity> popResult(ResultSet rs) throws SQLException, IOException {
        ArrayList<TestEntity> result = new ArrayList<TestEntity>();
        while (rs.next()) {
            TestEntity entity = new TestEntity();
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = metaData.getColumnCount(); i > 0; i--) {
                String columnName = metaData.getColumnName(i).toLowerCase();
                Class<?>[] params = new Class<?>[1];
                try {
                    params[0] = entity.getClass().getDeclaredField(columnName).getType();
                } catch (NoSuchFieldException e1) {
                    e1.printStackTrace();
                } catch (SecurityException e1) {
                    e1.printStackTrace();
                }
                String methodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                Method method = null;
                try {
                    method = entity.getClass().getMethod(methodName, params);
                } catch (NoSuchMethodException e) {
                    continue;
                }
                try {
                    method.invoke(entity, rs.getObject(i));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            result.add(entity);
        }

        return result;
    }

}
