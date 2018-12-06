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
package com.dianping.zebra.single.jdbc;

import com.alibaba.druid.filter.Filter;
import com.dianping.zebra.Constants;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class DruidSingleDataSource extends SingleDataSource {

	protected static final Logger LOGGER = LoggerFactory.getLogger(DruidSingleDataSource.class);

	public DruidSingleDataSource() {
		super();
		this.poolType = Constants.CONNECTION_POOL_TYPE_DRUID;
	}

	public void setUrl(String url) {
		super.setJdbcUrl(url);
	}

	public void setUsername(String username) {
		super.setUser(username);
	}

	public void setPassword(String password) {
		super.setPassword(password);
	}

	public void setDriverClassName(String driverClassName) {
		setProperty("driverClassName", driverClassName);
	}

	public void setPoolType(String poolType) {
		throw new UnsupportedOperationException("DruidSingleDataSource does not need to set up pool type !");
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	// ////////////////////////////////////////////////////////////
	// set druid properties
	// ////////////////////////////////////////////////////////////

	public void setName(String name) {
		setProperty("name", name);
	}

	public void setInitialSize(int initialSize) {
		setProperty("initialSize", initialSize);
	}

	public void setMaxActive(int maxActive) {
		setProperty("maxActive", maxActive);
	}

	public void setMaxIdle(int maxIdle) {
	}

	public void setMinIdle(int minIdle) {
		setProperty("minIdle", minIdle);
	}

	public void setMaxWait(long maxWait) {
		setProperty("maxWait", maxWait);
	}

	public void setPoolPreparedStatements(boolean poolPreparedStatements) {
		setProperty("poolPreparedStatements", poolPreparedStatements);
	}

	public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
		setProperty("maxOpenPreparedStatements", maxOpenPreparedStatements);
	}

	public void setValidationQuery(String validationQuery) {
		setProperty("validationQuery", validationQuery);
	}

	public void setValidationQueryTimeout(int validationQueryTimeout) {
		setProperty("validationQueryTimeout", validationQueryTimeout);
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		setProperty("testOnBorrow", testOnBorrow);
	}

	public void setTestOnReturn(boolean testOnReturn) {
		setProperty("testOnReturn", testOnReturn);
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		setProperty("testWhileIdle", testWhileIdle);
	}

	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		setProperty("timeBetweenEvictionRunsMillis", timeBetweenEvictionRunsMillis);
	}

	@Deprecated
	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		setProperty("numTestsPerEvictionRun", numTestsPerEvictionRun);
	}

	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		setProperty("minEvictableIdleTimeMillis", minEvictableIdleTimeMillis);
	}

	public void setConnectionInitSqls(String connectionInitSqls) {
		StringTokenizer tokenizer = new StringTokenizer(connectionInitSqls, ";");
		setProperty("connectionInitSqls", Collections.list(tokenizer));
	}

	public void setExceptionSorter(String exceptionSoter) {
		setProperty("exceptionSoter", exceptionSoter);
	}

	public void setFilters(String filters) {
		setProperty("filters", filters);
	}

	public void setUseUnfairLock(boolean unfairlock) {
		setProperty("useUnfairLock", unfairlock);
	}

	public void setProxyFilters(List<Filter> filters) {
		throw new UnsupportedOperationException("zerba does not support setProxyFilters in druid.");
	}

	// add @20170116
	public void setResetStatEnable(boolean resetStatEnable) {
		setProperty("resetStatEnable", resetStatEnable);
	}

	public void setLogDifferentThread(boolean logDifferentThread) {
		setProperty("logDifferentThread", logDifferentThread);
	}

	public void setUseGlobalDataSourceStat(boolean useGlobalDataSourceStat) {
		setProperty("useGlobalDataSourceStat", useGlobalDataSourceStat);
	}

	public void setEnable(boolean enable) {
		setProperty("enable", enable);
	}

	public void setRemoveAbandonedTimeoutMillis(long removeAbandonedTimeoutMillis) {
		setProperty("removeAbandonedTimeoutMillis", removeAbandonedTimeoutMillis);
	}

	public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		setProperty("removeAbandonedTimeout", removeAbandonedTimeout);
	}

	public void setSharePreparedStatements(boolean sharePreparedStatements) {
		setProperty("sharePreparedStatements", sharePreparedStatements);
	}

	public void setTimeBetweenConnectErrorMillis(long timeBetweenConnectErrorMillis) {
		setProperty("timeBetweenConnectErrorMillis", timeBetweenConnectErrorMillis);
	}

	public void setMaxCreateTaskCount(int maxCreateTaskCount) {
		setProperty("maxCreateTaskCount", maxCreateTaskCount);
	}

	public void setTransactionQueryTimeout(int transactionQueryTimeout) {
		setProperty("transactionQueryTimeout", transactionQueryTimeout);
	}

	public void setConnectionErrorRetryAttempts(int connectionErrorRetryAttempts) {
		setProperty("connectionErrorRetryAttempts", connectionErrorRetryAttempts);
	}

	public void setValidConnectionCheckerClassName(String validConnectionCheckerClassName) {
		setProperty("validConnectionCheckerClassName", validConnectionCheckerClassName);
	}

	public void setPhyTimeoutMillis(long phyTimeoutMillis) {
		setProperty("phyTimeoutMillis", phyTimeoutMillis);
	}

	public void setExceptionSorterClassName(String exceptionSorterClassName) {
		setProperty("exceptionSorterClassName", exceptionSorterClassName);
	}

	public void setFailFast(boolean failFast) {
		setProperty("failFast", failFast);
	}

	public void setRemoveAbandoned(boolean removeAbandoned) {
		setProperty("removeAbandoned", removeAbandoned);
	}

	public void setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
		setProperty("maxPoolPreparedStatementPerConnectionSize", maxPoolPreparedStatementPerConnectionSize);
	}

	public void setAsyncCloseConnectionEnable(boolean asyncCloseConnectionEnable) {
		setProperty("asyncCloseConnectionEnable", asyncCloseConnectionEnable);
	}

	public void setUseLocalSessionState(boolean useLocalSessionState) {
		setProperty("useLocalSessionState", useLocalSessionState);
	}

	public void setQueryTimeout(int queryTimeout) {
		setProperty("queryTimeout", queryTimeout);
	}

	public void setDefaultCatalog(String defaultCatalog) {
		setProperty("defaultCatalog", defaultCatalog);
	}

	public void setNotFullTimeoutRetryCount(int notFullTimeoutRetryCount) {
		setProperty("notFullTimeoutRetryCount", notFullTimeoutRetryCount);
	}

	public void setMaxWaitThreadCount(int maxWaitThreadCount) {
		setProperty("maxWaitThreadCount", maxWaitThreadCount);
	}

	public void setAccessToUnderlyingConnectionAllowed(boolean accessToUnderlyingConnectionAllowed) {
		setProperty("accessToUnderlyingConnectionAllowed", accessToUnderlyingConnectionAllowed);
	}

	public void setDupCloseLogEnable(boolean dupCloseLogEnable) {
		setProperty("dupCloseLogEnable", dupCloseLogEnable);
	}

	public void setLogAbandoned(boolean logAbandoned) {
		setProperty("logAbandoned", logAbandoned);
	}

	public void setOracle(boolean oracle) {
		setProperty("oracle", oracle);
	}

	public void setPasswordCallbackClassName(String passwordCallbackClassName) {
		setProperty("passwordCallbackClassName", passwordCallbackClassName);
	}

	public void setConnectionProperties(String connectionProperties) {
		setProperty("connectionProperties", connectionProperties);
	}

	public void setUseOracleImplicitCache(boolean useOracleImplicitCache) {
		setProperty("useOracleImplicitCache", useOracleImplicitCache);
	}

	public void setStatLoggerClassName(String statLoggerClassName) {
		setProperty("statLoggerClassName", statLoggerClassName);
	}

	public void setTransactionThresholdMillis(long transactionThresholdMillis) {
		setProperty("transactionThresholdMillis", transactionThresholdMillis);
	}

	public void setDefaultAutoCommit(boolean defaultAutoCommit) {
		setProperty("defaultAutoCommit", defaultAutoCommit);
	}

	public void setDefaultTransactionIsolation(Integer defaultTransactionIsolation) {
		setProperty("defaultTransactionIsolation", defaultTransactionIsolation);
	}

	public void setTimeBetweenLogStatsMillis(long timeBetweenLogStatsMillis) {
		setProperty("timeBetweenLogStatsMillis", timeBetweenLogStatsMillis);
	}

	public void setDefaultReadOnly(Boolean defaultReadOnly) {
		setProperty("defaultReadOnly", defaultReadOnly);
	}

}
