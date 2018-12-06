package com.dianping.zebra.single.jdbc;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TomcatJdbcSingleDataSourceFieldTest extends AbstractSingleDataSourceTest {

    @Test
    public void testDataSourceResult() throws SQLException {
        TomcatJdbcSingleDataSource ds = new TomcatJdbcSingleDataSource();
        ds.setUrl("jdbc:h2:mem:test;MVCC=TRUE");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setDriverClassName("org.h2.Driver");

        ds.setJmxEnabled(true);
        ds.setTestWhileIdle(false);
        ds.setTestOnBorrow(true);
        ds.setValidationQuery("SELECT 1");
        ds.setTestOnReturn(false);
        ds.setValidationInterval(30000);
        ds.setTimeBetweenEvictionRunsMillis(30000);
        ds.setMaxActive(100);
        ds.setInitialSize(10);
        ds.setMaxWait(10000);
        ds.setRemoveAbandonedTimeout(60);
        ds.setMinEvictableIdleTimeMillis(30000);
        ds.setMinIdle(10);
        ds.setLogAbandoned(true);
        ds.setRemoveAbandoned(true);
        ds.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

        ds.init();

        assertResult(ds, "select 'tomcat-jdbc' as `tomcat-jdbc`", "tomcat-jdbc");

        ds.close();
    }

    @Test
    public void test() {
        assertField(TomcatJdbcSingleDataSource.class, org.apache.tomcat.jdbc.pool.DataSource.class,
                new ArrayList<String>());
    }

    @Override
    protected List<String> getNotSupportedMethod() {
        return Lists.newArrayList();
    }
}
