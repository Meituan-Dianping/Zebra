/**
 * Project: ${zebra-client.aid}
 *
 * File Created at 2011-7-6
 * $Id$
 *
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Test;

import com.dianping.zebra.group.router.ZebraForceMasterHelper;
import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;

import junit.framework.Assert;

/**
 *
 * @author Leo Liang
 *
 */
public class MultiDBPreparedStatementLifeCycleGroupTest extends MultiDBBaseTestCase {

    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "db-datafiles/createtable-multidb-lifecycle-group.xml";
    }

    @Override
    protected String getDataFile() {
        return "db-datafiles/data-multidb-lifecycle-group.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] { "ctx-multidb-lifecycle-group.xml" };
    }

    @Test
    public void testSelectForceSingleMaster() throws Exception {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        ZebraForceMasterHelper.forceMasterInLocalContext();
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select name from test where id = 1");
            ResultSet rs = stmt.executeQuery();
            Set<String> rows = new HashSet<String>();
            while (rs.next()) {
                rows.add(rs.getString("name"));
            }

            Assert.assertEquals(1, rows.size());
            Assert.assertTrue(rows.contains("test_1_m"));
        } catch (Exception e) {
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }

            ZebraForceMasterHelper.forceMasterInLocalContext();
        }
    }

    @Test
    public void testSelectAll() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select test.* from test");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            List<TestEntity> popResult = popResult(rs);
            Assert.assertEquals(4, popResult.size());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
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
