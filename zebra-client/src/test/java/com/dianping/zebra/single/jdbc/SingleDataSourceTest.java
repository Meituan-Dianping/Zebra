package com.dianping.zebra.single.jdbc;

import com.dianping.zebra.Constants;
import com.google.common.collect.Lists;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SingleDataSourceTest extends AbstractSingleDataSourceTest {

    @Test
    public void testDataSourceResult() throws SQLException {
        testDataSourceResult("c3p0");
        testDataSourceResult("druid");
        testDataSourceResult("tomcat-jdbc");
        if (System.getProperty("java.version").startsWith("1.7.")) {
            testDataSourceResult("dbcp2");
        }
        testDataSourceResult("dbcp");
    }

    public void testDataSourceResult(String poolType) throws SQLException {
        SingleDataSource ds = new SingleDataSource();

        ds.setPoolType(poolType);

        ds.setJdbcUrl("jdbc:h2:mem:test;MVCC=TRUE");
        ds.setUser("sa");
        ds.setPassword("");
        ds.setDriverClass("org.h2.Driver");

        ds.setConfigManagerType(Constants.CONFIG_MANAGER_TYPE_LOCAL);
        ds.setInitialPoolSize(3);
        ds.setMaxPoolSize(30);
        ds.setMinPoolSize(4);
        ds.setMaxIdleTime(600);
        ds.setCheckoutTimeout(1000);
        ds.setIdleConnectionTestPeriod(60);
        ds.setAcquireRetryAttempts(3);
        ds.setAcquireRetryDelay(300);
        ds.setMaxStatements(0);
        ds.setMaxStatementsPerConnection(100);
        ds.setNumHelperThreads(10);
        ds.setMaxAdministrativeTaskTime(5);
        ds.setPreferredTestQuery("SELECT 1");

        ds.init();

        assertResult(ds, "select 'single' as `single`", "single");

        ds.close();
    }

    @Test
    public void test() {
        assertField(SingleDataSource.class, ComboPooledDataSource.class, new ArrayList<String>());
    }

    @Override
    protected List<String> getNotSupportedMethod() {
        return Lists.newArrayList("setIdentityToken");
    }
}
