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

public class HiKariCPSingleDataSource extends SingleDataSource {
	protected static final Logger LOGGER = LoggerFactory.getLogger(HiKariCPSingleDataSource.class);

	public HiKariCPSingleDataSource() {
		super();
		this.poolType = Constants.CONNECTION_POOL_TYPE_HIKARICP;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
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
		throw new UnsupportedOperationException("HiKariCPSingleDataSource does not need to set up pool type !");
	}

	// ////////////////////////////////////////////////////////////
	// set HiKariCP properties
	// ////////////////////////////////////////////////////////////

	public void setDataSourceClassName(String dataSourceClassName) {
		setProperty("dataSourceClassName", dataSourceClassName);
	}

	public void setPoolName(String poolName) {
		setProperty("poolName", poolName);
	}

	public void setAutoCommit(Boolean autoCommit) {
		setProperty("autoCommit", autoCommit);
	}

	public void setConnectionTimeout(long connectionTimeout) {
		setProperty("connectionTimeout", 1000L);
	}

	public void setIdleTimeout(long idleTimeout) {
		setProperty("idleTimeout", idleTimeout);
	}

	public void setMaxLifetime(long maxLifetime) {
		setProperty("maxLifetime", maxLifetime);
	}

	public void setConnectionTestQuery(String connectionTestQuery) {
		setProperty("connectionTestQuery", connectionTestQuery);
	}

	public void setMinimumIdle(int minimumIdle) {
		setProperty("minimumIdle", minimumIdle);
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		setProperty("maximumPoolSize", maximumPoolSize);
	}

	public void setInitializationFailTimeout(long initializationFailTimeout) {
		setProperty("initializationFailTimeout", initializationFailTimeout);
	}

	public void setIsolateInternalQueries(boolean isolateInternalQueries) {
		setProperty("isolateInternalQueries", isolateInternalQueries);
	}

	public void setAllowPoolSuspension(boolean allowPoolSuspension) {
		setProperty("allowPoolSuspension", allowPoolSuspension);
	}

	public void setReadOnly(boolean readOnly) {
		setProperty("readOnly", readOnly);
	}

	public void setRegisterMbeans(boolean registerMbeans) {
		setProperty("registerMbeans", registerMbeans);
	}

	public void setCatalog(String catalog) {
		setProperty("catalog", catalog);
	}

	public void setConnectionInitSql(String connectionInitSql) {
		setProperty("connectionInitSql", connectionInitSql);
	}

	public void setTransactionIsolation(String transactionIsolation) {
		setProperty("transactionIsolation", transactionIsolation);
	}

	public void setValidationTimeout(long validationTimeout) {
		setProperty("validationTimeout", validationTimeout);
	}

	public void setLeakDetectionThreshold(long leakDetectionThreshold) {
		setProperty("leakDetectionThreshold", leakDetectionThreshold);
	}
}
