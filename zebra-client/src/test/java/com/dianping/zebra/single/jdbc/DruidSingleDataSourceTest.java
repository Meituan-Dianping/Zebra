package com.dianping.zebra.single.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DruidSingleDataSourceTest extends AbstractSingleDataSourceTest {

    @Test
    public void testDataSourceResult() throws SQLException {
        DruidSingleDataSource ds = new DruidSingleDataSource();
        ds.setUrl("jdbc:h2:mem:test;MVCC=TRUE");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setDriverClassName("org.h2.Driver");

        ds.setFilters("stat");
        ds.setMaxActive(10);
        ds.setInitialSize(1);
        ds.setMaxWait(1000);
        ds.setMinIdle(1);

        ds.setTimeBetweenEvictionRunsMillis(60000);
        ds.setMinEvictableIdleTimeMillis(300000);
        ds.setValidationQuery("SELECT 1");
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);

        ds.setPoolPreparedStatements(true);
        ds.setMaxOpenPreparedStatements(20);
        ds.setConnectionInitSqls("select 1");

        ds.init();

        assertResult(ds, "select 'druid' as `druid`", "druid");

        ds.close();
    }

    @Test
    public void testField() {
        assertField(DruidSingleDataSource.class, DruidDataSource.class, new ArrayList<String>());
    }

    @Override
    protected List<String> getNotSupportedMethod() {
        return Lists.newArrayList("setClearFiltersEnable","setPasswordCallback","setDbType","setDestroyScheduler",
                "setDriver","setConnectProperties","setDriverClassLoader","setObjectName","setValidConnectionChecker");
    }
}
