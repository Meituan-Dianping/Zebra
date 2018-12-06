/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.single.jdbc.AbstractDataSource;

import java.util.Properties;

public abstract class ShardDataSourceConfigAdapter extends AbstractDataSource {
	protected Properties dsConfigProperties = new Properties();

	public void setConnectionInitSql(String connectionInitSql) {
		dsConfigProperties.setProperty("connectionInitSql", connectionInitSql);
	}

	public void setDriverClass(String driverClass) {
		dsConfigProperties.setProperty("driverClass", driverClass);
	}

	// 当连接池中连接耗尽时一次同时获取的连接数
	public void setAcquireIncrement(int acquireIncrement) {
		dsConfigProperties.setProperty("acquireIncrement", String.valueOf(acquireIncrement));
	}

	// 从数据库获取新连接失败后重复尝试的次数
	public void setAcquireRetryAttempts(int acquireRetryAttempts) {
		dsConfigProperties.setProperty("acquireRetryAttempts", String.valueOf(acquireRetryAttempts));
	}

	// 重新尝试的时间间隔(ms)
	public void setAcquireRetryDelay(int acquireRetryDelay) {
		dsConfigProperties.setProperty("acquireRetryDelay", String.valueOf(acquireRetryDelay));
	}

	// 关闭连接时是否提交未提交事务
	public void setAutoCommitOnClose(boolean autoCommitOnClose) {
		dsConfigProperties.setProperty("autoCommitOnClose", String.valueOf(autoCommitOnClose));
	}

	// c3p0将建一张名为Test的空表, 并使用其自带的查询语句进行测试, 如果定义了这个参数那么属性preferredTestQuery将被忽略。
	// 用户不能在这张Test表上进行任何操作, 它将只供c3p0测试使用
	public void setAutomaticTestTable(String automaticTestTable) {
		dsConfigProperties.setProperty("automaticTestTable", automaticTestTable);
	}

	// 则获取连接失败后是否关闭连接池
	public void setBreakAfterAcquireFailure(boolean breakAfterAcquireFailure) {
		dsConfigProperties.setProperty("breakAfterAcquireFailure", String.valueOf(breakAfterAcquireFailure));
	}

	// 指定连接池的获取连接的超时时间
	public void setCheckoutTimeout(int checkoutTimeout) {
		dsConfigProperties.setProperty("checkoutTimeout", String.valueOf(checkoutTimeout));
	}

	// 定制Connection的管理
	public void setConnectionCustomizerClassName(String connectionCustomizerClassName) {
		dsConfigProperties.setProperty("connectionCustomizerClassName", connectionCustomizerClassName);
	}

	// 连接测试类
	public void setConnectionTesterClassName(String connectionTesterClassName) {
		dsConfigProperties.setProperty("connectionTesterClassName", connectionTesterClassName);
	}

	public void setDataSourceName(String dataSourceName) {
		dsConfigProperties.setProperty("dataSourceName", dataSourceName);
	}

	// 连接超时未归还关闭连接打印堆栈信息
	public void setDebugUnreturnedConnectionStackTraces(boolean debugUnreturnedConnectionStackTraces) {
		dsConfigProperties.setProperty("debugUnreturnedConnectionStackTraces",
		      String.valueOf(debugUnreturnedConnectionStackTraces));
	}

	public void setDescription(String description) {
		dsConfigProperties.setProperty("description", description);
	}

	public void setFactoryClassLocation(String factoryClassLocation) {
		dsConfigProperties.setProperty("factoryClassLocation", factoryClassLocation);
	}

	public void setForceIgnoreUnresolvedTransactions(boolean forceIgnoreUnresolvedTransactions) {
		dsConfigProperties.setProperty("forceIgnoreUnresolvedTransactions",
		      String.valueOf(forceIgnoreUnresolvedTransactions));
	}

	// 每隔一定时间检查连接池内空闲连接
	public void setIdleConnectionTestPeriod(int idleConnectionTestPeriod) {
		dsConfigProperties.setProperty("idleConnectionTestPeriod", String.valueOf(idleConnectionTestPeriod));
	}

	// 连接池的初始化连接数
	public void setInitialPoolSize(int initialPoolSize) {
		dsConfigProperties.setProperty("initialPoolSize", String.valueOf(initialPoolSize));
	}

	public void setMaxAdministrativeTaskTime(int maxAdministrativeTaskTime) {
		dsConfigProperties.setProperty("maxAdministrativeTaskTime", String.valueOf(maxAdministrativeTaskTime));
	}

	public void setMaxConnectionAge(int maxConnectionAge) {
		dsConfigProperties.setProperty("maxConnectionAge", String.valueOf(maxConnectionAge));
	}

	// 连接池内连接最大空闲时间. 若为0则永不丢弃
	public void setMaxIdleTime(int maxIdleTime) {
		dsConfigProperties.setProperty("maxIdleTime", String.valueOf(maxIdleTime));
	}

	public void setMaxIdleTimeExcessConnections(int maxIdleTimeExcessConnections) {
		dsConfigProperties.setProperty("maxIdleTimeExcessConnections", String.valueOf(maxIdleTimeExcessConnections));
	}

	// 连接池的最大连接数
	public void setMaxPoolSize(int maxPoolSize) {
		dsConfigProperties.setProperty("maxPoolSize", String.valueOf(maxPoolSize));
	}

	// c3p0全局的PreparedStatements缓存的大小
	public void setMaxStatements(int maxStatements) {
		dsConfigProperties.setProperty("maxStatements", String.valueOf(maxStatements));
	}

	// 连接池内单个连接拥有的最大缓存statements数
	public void setMaxStatementsPerConnection(int maxStatementsPerConnection) {
		dsConfigProperties.setProperty("maxStatementsPerConnection", String.valueOf(maxStatementsPerConnection));
	}

	// 连接池中保留的最小连接数
	public void setMinPoolSize(int minPoolSize) {
		dsConfigProperties.setProperty("minPoolSize", String.valueOf(minPoolSize));
	}

	// 辅助线程数量, c3p0是异步操作的, 缓慢的JDBC操作通过辅助线程完成。
	public void setNumHelperThreads(int numHelperThreads) {
		dsConfigProperties.setProperty("numHelperThreads", String.valueOf(numHelperThreads));
	}

	public void setOverrideDefaultPassword(String overrideDefaultPassword) {
		dsConfigProperties.setProperty("overrideDefaultPassword", overrideDefaultPassword);
	}

	public void setOverrideDefaultUser(String overrideDefaultUser) {
		dsConfigProperties.setProperty("overrideDefaultUser", overrideDefaultUser);
	}

	// 定义检测连接有效性所执行的测试语句
	public void setPreferredTestQuery(String preferredTestQuery) {
		dsConfigProperties.setProperty("preferredTestQuery", preferredTestQuery);
	}

	public void setPropertyCycle(int propertyCycle) {
		dsConfigProperties.setProperty("propertyCycle", String.valueOf(propertyCycle));
	}

	// 连接checkin到连接池时是否检查有效性
	public void setTestConnectionOnCheckin(boolean testConnectionOnCheckin) {
		dsConfigProperties.setProperty("testConnectionOnCheckin", String.valueOf(testConnectionOnCheckin));
	}

	// 从连接池checkout连接时是否检查有效性
	public void setTestConnectionOnCheckout(boolean testConnectionOnCheckout) {
		dsConfigProperties.setProperty("testConnectionOnCheckout", String.valueOf(testConnectionOnCheckout));
	}

	public void setUnreturnedConnectionTimeout(int unreturnedConnectionTimeout) {
		dsConfigProperties.setProperty("unreturnedConnectionTimeout", String.valueOf(unreturnedConnectionTimeout));
	}

	public void setUserOverridesAsString(String userOverridesAsString) {
		dsConfigProperties.setProperty("userOverridesAsString", userOverridesAsString);
	}

	public void setUsesTraditionalReflectiveProxies(boolean usesTraditionalReflectiveProxies) {
		dsConfigProperties.setProperty("usesTraditionalReflectiveProxies",
		      String.valueOf(usesTraditionalReflectiveProxies));
	}

	public synchronized void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
		dsConfigProperties.setProperty("timeBetweenEvictionRunsMillis", String.valueOf(timeBetweenEvictionRunsMillis));
	}

	public synchronized void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
		dsConfigProperties.setProperty("minEvictableIdleTimeMillis", String.valueOf(minEvictableIdleTimeMillis));
	}

	public synchronized void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		dsConfigProperties.setProperty("numTestsPerEvictionRun", String.valueOf(numTestsPerEvictionRun));
	}

	public synchronized void setValidationQueryTimeout(int validationQueryTimeout) {
		dsConfigProperties.setProperty("validationQueryTimeout", String.valueOf(validationQueryTimeout));
	}

	public synchronized void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		dsConfigProperties.setProperty("removeAbandonedTimeout", String.valueOf(removeAbandonedTimeout));
	}
}
