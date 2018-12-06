package com.dianping.zebra.single.jdbc;

import com.google.common.collect.Lists;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dbcp2SingleDataSourceFieldTest extends AbstractSingleDataSourceTest {

    @Test
    public void testDataSourceResult() throws Exception {
        if (!System.getProperty("java.version").startsWith("1.7.")) {
            return;
        }
        Dbcp2SingleDataSource ds = new Dbcp2SingleDataSource();
        @SuppressWarnings("serial")
        HashMap<String, Object> propertyValueToCheck = new HashMap<String, Object>() {
            {
                put("url", "jdbc:h2:mem:test;MVCC=TRUE");
                put("username", "sa");
                put("driverClassName", "org.h2.Driver");
                put("initialSize", 8);
                put("maxTotal", 48);
                put("maxIdle", 20);
                put("minIdle", 4);
                put("maxWaitMillis", (long) 1000);
                put("testWhileIdle", true);
                put("testOnBorrow", false);
                put("validationQuery", "SELECT 1");
                put("validationQueryTimeout", 1);
                put("numTestsPerEvictionRun", 30);
                put("minEvictableIdleTimeMillis", (long) 1800000);
                put("removeAbandonedOnBorrow", true);
                put("defaultAutoCommit", true);
            }
        };
        ds.setUrl("jdbc:h2:mem:test;MVCC=TRUE");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setDriverClassName("org.h2.Driver");

        ds.setInitialSize(8);
        ds.setMaxTotal(48);
        ds.setMaxIdle(20);
        ds.setMinIdle(4);
        ds.setMaxWaitMillis(1000);
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);
        ds.setValidationQuery("SELECT 1");
        ds.setValidationQueryTimeout(1);
        ds.setNumTestsPerEvictionRun(30);
        ds.setMinEvictableIdleTimeMillis(1800000);
        ds.setRemoveAbandonedOnBorrow(true);
        ds.setRemoveAbandonedOnMaintenance(true);
        ds.setRemoveAbandonedTimeout(300);
        ds.setConnectionProperties("connectTimeout=1000;socketTimeout=1000");
        ds.setDefaultAutoCommit(true);
        ds.setConnectionInitSqls("select 1");

        ds.init();

        assertResult(ds, "select 'dbcp2' as `dbcp2`", "dbcp2");
        DataSource dataSource = ds.dataSourcePool.getInnerDataSourcePool();
        for (Map.Entry<String, Object> entry : propertyValueToCheck.entrySet()) {
            assertParameterValue(BasicDataSource.class, dataSource, entry.getKey(), entry.getValue());
        }

        ds.close();
    }

    @Test
    public void test() {
        if (!System.getProperty("java.version").startsWith("1.7.")) {
            return;
        }
        assertField(Dbcp2SingleDataSource.class, BasicDataSource.class, new ArrayList<String>());
    }

    @Override
    protected List<String> getNotSupportedMethod() {
        return Lists.newArrayList();
    }
}
