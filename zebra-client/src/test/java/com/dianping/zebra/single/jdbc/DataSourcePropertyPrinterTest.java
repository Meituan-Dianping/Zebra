package com.dianping.zebra.single.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DataSourcePropertyPrinterTest {
    @Test
    public void druidPropertyPrinter() {
        List<String> methods = Lists.newArrayList("setRemoveAbandonedTimeoutMillis","setRemoveAbandonedTimeout", "setSharePreparedStatements",
                "setTimeBetweenConnectErrorMillis", "setMaxCreateTaskCount", "setTransactionQueryTimeout",
                "setConnectionErrorRetryAttempts", "setValidConnectionCheckerClassName", "setPhyTimeoutMillis",
                "setExceptionSorterClassName", "setFailFast", "setResetStatEnable", "setLogDifferentThread",
                "setRemoveAbandoned", "setMaxPoolPreparedStatementPerConnectionSize", "setAsyncCloseConnectionEnable",
                "setUseLocalSessionState", "setQueryTimeout", "setDefaultCatalog", "setNotFullTimeoutRetryCount",
                "setUseGlobalDataSourceStat", "setMaxWaitThreadCount", "setAccessToUnderlyingConnectionAllowed",
                "setDupCloseLogEnable", "setLogAbandoned", "setOracle", "setPasswordCallbackClassName",
                "setConnectionProperties", "setUseOracleImplicitCache", "setStatLoggerClassName",
                "setTransactionThresholdMillis", "setDefaultAutoCommit", "setEnable", "setDefaultTransactionIsolation",
                "setTimeBetweenLogStatsMillis", "setDefaultReadOnly");
        propertyPrinter(DruidDataSource.class, methods);
    }

    @Test
    public void tomcatJdbcPropertyPrinter() {
        List<String> methods = Lists.newArrayList("setUseLock", "setName");
        propertyPrinter(org.apache.tomcat.jdbc.pool.DataSource.class, methods);
    }

    @Test
    public void dbcpPropertyPrinter(){
        List<String> methods = Lists.newArrayList("setRemoveAbandoned");
        propertyPrinter(BasicDataSource.class, methods);
    }

    @Test
    public void dbcp2PropertyPrinter(){
        List<String> methods = Lists.newArrayList("setJmxName","setEnableAutoCommitOnReturn","setEvictionPolicyClassName");
        propertyPrinter(org.apache.commons.dbcp2.BasicDataSource.class, methods);
    }

    private void propertyPrinter(Class<?> dataSourceClass, List<String> methods) {
        ArrayList<Method> checkMethods = new ArrayList<Method>();
        for (Method method : dataSourceClass.getMethods()) {
            if (methods.contains(method.getName())) {
                checkMethods.add(method);
            }
        }

        for (Method method : checkMethods) {
            String paramName = String.valueOf(method.getName().charAt("set".length())).toLowerCase()
                    + method.getName().substring("set".length() + 1);
            String typeName = method.getParameterTypes()[0].getName().indexOf(".") == -1
                    ? method.getParameterTypes()[0].getName()
                    : method.getParameterTypes()[0].getName()
                    .substring(method.getParameterTypes()[0].getName().lastIndexOf(".") + 1);
            System.out.println(String.format("public void %s(%s %s){\nsetProperty(\"%s\", %s);}", method.getName(),
                    typeName, paramName, paramName, paramName));
        }
    }
}
