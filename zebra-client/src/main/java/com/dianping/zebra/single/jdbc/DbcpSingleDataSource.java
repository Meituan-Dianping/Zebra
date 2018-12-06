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

public class DbcpSingleDataSource extends SingleDataSource {

	protected static final Logger LOGGER = LoggerFactory.getLogger(DbcpSingleDataSource.class);

	public DbcpSingleDataSource() {
		super();
		this.poolType = Constants.CONNECTION_POOL_TYPE_DBCP;
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
		throw new UnsupportedOperationException("DBCPSingleDataSource does not need to set up pool type !");
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	// ////////////////////////////////////////////////////////////
	// set dbcp 1.4 properties
	// ////////////////////////////////////////////////////////////

	public void setDefaultAutoCommit(boolean defaultAutoCommit) {
		setProperty("defaultAutoCommit", defaultAutoCommit);
	}

	public void setDefaultReadOnly(boolean defaultReadOnly) {
		setProperty("defaultReadOnly", defaultReadOnly);
	}

	public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
		setProperty("defaultTransactionIsolation", defaultTransactionIsolation);
	}

	public void setDefaultCatalog(String defaultCatalog) {
		setProperty("defaultCatalog", defaultCatalog);
	}

	public void setDriverClassLoader(ClassLoader classLoader) {
		throw new UnsupportedOperationException("zebra does not support classloader in dbcp 1.4");
	}

	public void setMaxActive(int maxActive) {
		setProperty("maxActive", maxActive);
	}

	public void setInitialSize(int initialSize) {
		setProperty("initialSize", initialSize);
	}

	public void setMaxIdle(int maxIdle) {
		setProperty("maxIdle", maxIdle);
	}

	public void setMinIdle(int minIdle) {
		setProperty("minIdle", minIdle);
	}

	public void setMaxWait(long maxWait) {
		setProperty("maxWait", maxWait);
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

	public void setPoolPreparedStatements(boolean poolPreparedStatements) {
		setProperty("poolPreparedStatements", poolPreparedStatements);
	}

	public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
		setProperty("maxOpenPreparedStatements", maxOpenPreparedStatements);
	}

	public void setAccessToUnderlyingConnectionAllowed(boolean accessToUnderlyingConnectionAllowed) {
		setProperty("accessToUnderlyingConnectionAllowed", accessToUnderlyingConnectionAllowed);
	}

	public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		setProperty("removeAbandonedTimeout", removeAbandonedTimeout);
	}

	public void setLogAbandoned(boolean logAbandoned) {
		setProperty("logAbandoned", logAbandoned);
	}

	public void setDisconnectionSqlCodes(String disconnectionSqlCodes) {
		StringTokenizer tokenizer = new StringTokenizer(disconnectionSqlCodes, ",");
		setProperty("disconnectionSqlCodes", Collections.list(tokenizer));
	}

	public void setConnectionProperties(String connectionProperties) {
		setProperty("connectionProperties", connectionProperties);
	}

	public void setRemoveAbandoned(boolean removeAbandoned) {
		setProperty("removeAbandoned", removeAbandoned);
	}

}
