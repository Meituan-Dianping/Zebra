package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;
import com.dianping.zebra.util.SqlExecuteHelper;
import com.google.common.collect.Lists;

import junit.framework.Assert;

import org.junit.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 * <p/>
 * ===== data-multidb-lifecycle 分库分表规则 =====
 * dbRule="(#id#.intValue() % 8).intdiv(2)"
 * dbIndexes="id0,id1,id2,id3"
 * tbRule="#id#.intValue() % 2"
 * tbSuffix="alldb:[_0,_7]"
 * isMaster="true"
 * <p/>
 * dbRule="(#classid#.intValue() % 8).intdiv(2)"
 * dbIndexes="class0,class1,class2,class3"
 * tbRule="#classid#.intValue() % 2"
 * tbSuffix="alldb:[_class0,_class7]"
 * isMaster="false"
 * ==============================================
 */
public class ShardSupportedCaseTest extends MultiDBBaseTestCase {
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
        return "db-datafiles/data-multidb-lifecycle.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[]{"ctx-multidb-lifecycle.xml"};
    }

    @Test(expected = Exception.class)
    public void test_insert_without_key() throws Exception {
        SqlExecuteHelper.executeInsert(getZebraDs().getConnection(), "insert into test ( name, score, type, classid) values ('xxx', 1, 'a', 0)");
    }


    @Test
    public void test_insert_with_key() throws Exception {
        Assert.assertTrue(SqlExecuteHelper.executeUpdate(getZebraDs().getConnection(), "insert into test (id, name, score, type, classid) values (100, 'xxx', 1, 'a', 0)") == 1);

        @SuppressWarnings("unchecked")
        List<List<Object>> expectData = Lists.<List<Object>>newArrayList(Lists.<Object>newArrayList(100, "xxx", 1, "a", 0));
        assertData(getZebraDs().getConnection(), "select id,name,score,type,classid from test where id = 100", expectData);
        assertData(getInnerDs("id2").getConnection(), "select id,name,score,type,classid from test_4 where id = 100", expectData);
        assertData(getInnerDs("id0").getConnection(), "select id,name,score,type,classid from test_1 where id = 100", Lists.<List<Object>>newArrayList());
    }

    @Test
    public void test_select() throws SQLException {
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test").size() > 0;
    }

    @Test
    public void test_select_with_in() throws SQLException {
        String sql = "select classid from test where id in (1,2,3)";
        List<List<Object>> data = SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), sql);

        System.out.println(data);
    }


    @Test
    public void test_select_with_where_and_order_by() throws SQLException {
        String[] whereCondition = new String[]{
                "",
                "where id = 3",
                "where id > 3"
        };

        for (String it : whereCondition) {
            String sql = "select classid from test " + it + " order by classid";
            List<List<Object>> data = SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), sql);
            Integer lastClassid = null;

            for (List<Object> row : data) {
                if (lastClassid != null) {
                    assert (Integer) row.get(0) >= lastClassid;
                }
                lastClassid = (Integer) row.get(0);
            }
        }
    }

    @Test
    public void test_select_with_id() throws SQLException {
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where  id = 3").size() > 0;
    }

    @Test
    public void test_select_with_not_equals() throws SQLException {
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where  id <> 3").size() > 0;
    }

    @Test
    public void test_select_with_limit() throws SQLException {
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where  id <> 3 limit 1").size() == 1;
    }


    @Test
    public void test_select_with_id_and_name() throws SQLException {
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id = 3 and name = 'leox'").size() == 0;
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id = 3 and name = 'leo3'").size() > 0;
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id = 3 and name like '%dozer%'").size() == 0;
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id = 3 and name like '%leo%'").size() > 0;
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id > 3 and name like '%dozer%'").size() == 0;
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id > 3 and name like '%leo%'").size() > 0;
    }

    @Test
    public void test_select_with_sub_query() throws SQLException {
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id in (select id from test where id= 5)").size() > 0;
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id in (select id from test where id > 2 and id < 10)").size() > 0;
    }

    @Test
    public void test_select_with_count() throws SQLException {
        //todo: 支持无字段别名的查询
        Long count = (Long) SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select count(id) as id_count from test").get(0).get(0);
        Integer max = (Integer) SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select max(id) as id_max from test").get(0).get(0);
        Integer min = (Integer) SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select min(id) as id_min from test").get(0).get(0);
        Long sum = (Long) SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select sum(id) as id_sum from test").get(0).get(0);
//        def avg = SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select avg(id) as id_avg from test")[0][0];//todo: not support!!

        assert min <= sum / count;
        assert sum / count <= max;
    }

    @Test
    public void test_select_with_group() throws SQLException {
        SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select type,count(id) as id_count from test group by type");
    }

    @Test
    public void test_select_with_multi_partition() throws SQLException {
        //删光 class 维度的数据
        assert SqlExecuteHelper.executeUpdate(getInnerDs("class0").getConnection(), "delete from test_class0") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("class0").getConnection(), "delete from test_class1") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("class1").getConnection(), "delete from test_class2") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("class1").getConnection(), "delete from test_class3") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("class2").getConnection(), "delete from test_class4") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("class2").getConnection(), "delete from test_class5") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("class3").getConnection(), "delete from test_class6") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("class3").getConnection(), "delete from test_class7") > 0;

        //分区成功
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id = 3 and classid = 3").size() > 0;
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id = 3 and classid > 2").size() > 0;
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id = 3 and classid <> 2").size() > 0;

        //分区失败，直接到主维度全表扫描
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where id = 3 or classid = 3").size() == 2;
    }

    @Test
    public void test_select_with_class_id() throws SQLException {
        //删光 id 维度的数据
        assert SqlExecuteHelper.executeUpdate(getInnerDs("id0").getConnection(), "delete from test_0") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("id0").getConnection(), "delete from test_1") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("id1").getConnection(), "delete from test_2") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("id1").getConnection(), "delete from test_3") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("id2").getConnection(), "delete from test_4") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("id2").getConnection(), "delete from test_5") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("id3").getConnection(), "delete from test_6") > 0;
        assert SqlExecuteHelper.executeUpdate(getInnerDs("id3").getConnection(), "delete from test_7") > 0;

        //分区成功
        assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), "select * from test where classid = 3").size() > 0;
    }

    @Test
    public void test_update() throws SQLException {
        String baseUpdate = "update test set name = 'newName' ";
        String baseQuery = "select name from test ";
        String[] whereCondiction = new String[]{
                "where id = 3",
                "where id in (1,2,3)",
                "where id <> 5",
                "",
                //"where classid = 3", //不支持！
                //"where id in (select id from test where id = 3)", //不支持！
        };

        for (String it : whereCondiction) {
            assert SqlExecuteHelper.executeUpdate(getZebraDs().getConnection(), baseUpdate + it) > 0;
            for (List<Object> row : SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), baseQuery + it)) {
                assert row.get(0).equals("newName");
            }
        }

    }

    @Test
    public void test_delete() throws SQLException {
        String baseUpdate = "delete from test ";
        String baseQuery = "select * from test ";
        String[] whereCondiction = new String[]{
                "where id = 3",
                "where id in (1,2,3)",
                "where id <> 5",
                ""
                //"where classid = 3", //不支持
                //"where id in (select id from test where id = 3)", //不支持
        };

        for (String it : whereCondiction) {
            assert SqlExecuteHelper.executeUpdate(getZebraDs().getConnection(), baseUpdate + it) > 0;
            assert SqlExecuteHelper.executeQuery(getZebraDs().getConnection(), baseQuery + it).size() == 0;
        }
    }

    protected DataSource getZebraDs() {
        return (DataSource) context.getBean("zebraDS");
    }

    protected DataSource getInnerDs(String name) {
        return (DataSource) context.getBean(name);
    }

    protected void assertData(Connection conn, String sql, List<List<Object>> expect) throws SQLException {
        List<List<Object>> actual = SqlExecuteHelper.executeQuery(conn, sql);
        Assert.assertEquals(actual.size(), expect.size());
        for (int k = 0; k < actual.size(); k++) {
            List<Object> act = actual.get(k);
            List<Object> exp = expect.get(k);

            Assert.assertEquals(act.size(), exp.size());
            for (int j = 0; j < act.size(); j++) {
                Assert.assertTrue(act.get(j).equals(exp.get(j)));
            }
        }

        assert expect.equals(actual);
    }
}
