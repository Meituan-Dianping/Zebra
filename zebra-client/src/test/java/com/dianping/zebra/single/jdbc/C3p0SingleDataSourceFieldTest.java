package com.dianping.zebra.single.jdbc;

import com.google.common.collect.Lists;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class C3p0SingleDataSourceFieldTest extends AbstractSingleDataSourceTest {

    @Test
    public void testDataSourceResult() throws Exception {
        C3p0SingleDataSource ds = new C3p0SingleDataSource();

        ds.setJdbcUrl("jdbc:h2:mem:test;MVCC=TRUE");
        ds.setUser("sa");
        ds.setPassword("");
        ds.setDriverClass("org.h2.Driver");

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

        assertResult(ds, "select 'c3p0' as `c3p0`", "c3p0");

        ds.close();
    }

    @Test
    public void test() {
        assertField(C3p0SingleDataSource.class, ComboPooledDataSource.class, new ArrayList<String>());
    }

    @Override
    protected List<String> getNotSupportedMethod() {
        return Lists.newArrayList("setIdentityToken");
    }
}

