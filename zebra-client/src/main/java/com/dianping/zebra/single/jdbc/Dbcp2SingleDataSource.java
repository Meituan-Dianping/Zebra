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

import com.dianping.zebra.Constants;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;

import java.util.Collections;
import java.util.StringTokenizer;

public class Dbcp2SingleDataSource extends SingleDataSource {

	protected static final Logger LOGGER = LoggerFactory.getLogger(Dbcp2SingleDataSource.class);

	public Dbcp2SingleDataSource() {
		super();
		this.poolType = Constants.CONNECTION_POOL_TYPE_DBCP2;
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
		throw new UnsupportedOperationException("DBCP2SingleDataSource does not need to set up pool type !");
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	// ////////////////////////////////////////////////////////////
	// set dbcp2 properties
	// ////////////////////////////////////////////////////////////

	public void setDefaultAutoCommit(Boolean defaultAutoCommit) {
		setProperty("defaultAutoCommit", defaultAutoCommit);
	}

	public void setDefaultReadOnly(Boolean defaultReadOnly) {
		setProperty("defaultReadOnly", defaultReadOnly);
	}

	public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
		setProperty("defaultTransactionIsolation", defaultTransactionIsolation);
	}

	public void setDefaultCatalog(String defaultCatalog) {
		setProperty("defaultCatalog", defaultCatalog);
	}

	public void setCacheState(boolean cacheState) {
		setProperty("cacheState", cacheState);
	}

	public void setDefaultQueryTimeout(Integer defaultQueryTimeout) {
		setProperty("defaultQueryTimeout", defaultQueryTimeout);
	}

	public void setEnableAutocommitOnReturn(boolean enableAutocommitOnReturn) {
		setProperty("enableAutocommitOnReturn", enableAutocommitOnReturn);
	}

	public void setRollbackOnReturn(boolean rollbackOnReturn) {
		setProperty("rollbackOnReturn", rollbackOnReturn);
	}

	public void setInitialSize(int initialSize) {
		setProperty("initialSize", initialSize);
	}

	public void setMaxTotal(int maxTotal) {
		setProperty("maxTotal", maxTotal);
	}

	public void setMaxIdle(int maxIdle) {
		setProperty("maxIdle", maxIdle);
	}

	public void setMinIdle(int minIdle) {
		setProperty("minIdle", minIdle);
	}

	public void setMaxWaitMillis(long maxWaitMillis) {
		setProperty("maxWaitMillis", maxWaitMillis);
	}

	public void setValidationQuery(String validationQuery) {
		setProperty("validationQuery", validationQuery);
	}

	public void setValidationQueryTimeout(int validationQueryTimeout) {
		setProperty("validationQueryTimeout", validationQueryTimeout);
	}

	public void setTestOnCreate(boolean testOnCreate) {
		setProperty("testOnCreate", testOnCreate);
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

	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		setProperty("numTestsPerEvictionRun", numTestsPerEvictionRun);
	}

	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		setProperty("minEvictableIdleTimeMillis", minEvictableIdleTimeMillis);
	}

	public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
		setProperty("softMinEvictableIdleTimeMillis", softMinEvictableIdleTimeMillis);
	}

	public void setMaxConnLifetimeMillis(long maxConnLifetimeMillis) {
		setProperty("maxConnLifetimeMillis", maxConnLifetimeMillis);
	}

	public void setLogExpiredConnections(boolean logExpiredConnections) {
		setProperty("logExpiredConnections", logExpiredConnections);
	}

	public void setConnectionInitSqls(String connectionInitSqls) {
		StringTokenizer tokenizer = new StringTokenizer(connectionInitSqls, ";");
		setProperty("connectionInitSqls", Collections.list(tokenizer));
	}

	public void setLifo(boolean lifo) {
		setProperty("lifo", lifo);
	}

	public void setPoolPreparedStatements(boolean poolPreparedStatements) {
		setProperty("poolPreparedStatements", poolPreparedStatements);
	}

	public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
		setProperty("maxOpenPreparedStatements", maxOpenPreparedStatements);
	}

	public void setAccessToUnderlyingConnectionAllowed(boolean accessToUnderlyingConnectionAllowed) {
		setProperty("accessToUnderlyingConnectionAllowed", accessToUnderlyingConnectionAllowed);
	}

	public void setRemoveAbandonedOnMaintenance(boolean removeAbandonedOnMaintenance) {
		setProperty("removeAbandonedOnMaintenance", removeAbandonedOnMaintenance);
	}

	public void setRemoveAbandonedOnBorrow(boolean removeAbandonedOnBorrow) {
		setProperty("removeAbandonedOnBorrow", removeAbandonedOnBorrow);
	}

	public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		setProperty("removeAbandonedTimeout", removeAbandonedTimeout);
	}

	public void setLogAbandoned(boolean logAbandoned) {
		setProperty("logAbandoned", logAbandoned);
	}

	public void setAbandonedUsageTracking(boolean abandonedUsageTracking) {
		setProperty("abandonedUsageTracking", abandonedUsageTracking);
	}

	public void setFastFailValidation(boolean fastFailValidation) {
		setProperty("fastFailValidation", fastFailValidation);
	}

	public void setDisconnectionSqlCodes(String disconnectionSqlCodes) {
		StringTokenizer tokenizer = new StringTokenizer(disconnectionSqlCodes, ",");
		setProperty("disconnectionSqlCodes", Collections.list(tokenizer));
	}

	public void setConnectionProperties(String connectionProperties) {
		setProperty("connectionProperties", connectionProperties);
	}

	// add @20170116
	public void setJmxName(String jmxName) {
		setProperty("jmxName", jmxName);
	}

	public void setEnableAutoCommitOnReturn(boolean enableAutoCommitOnReturn) {
		setProperty("enableAutoCommitOnReturn", enableAutoCommitOnReturn);
	}

	public void setEvictionPolicyClassName(String evictionPolicyClassName) {
		setProperty("evictionPolicyClassName", evictionPolicyClassName);
	}

}
