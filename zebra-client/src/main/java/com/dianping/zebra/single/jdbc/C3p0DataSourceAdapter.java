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

import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.util.json.JsonArray;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public abstract class C3p0DataSourceAdapter extends AbstractDataSource {
	protected DataSourceConfig dsProperties = new DataSourceConfig();

	public void setConnectionInitSql(String connectionInitSql) {
		setProperty("connectionInitSql", connectionInitSql);
	}

	public void setDriverClass(String driverClass) {
		setProperty("driverClass", driverClass);
	}

	public void setAcquireIncrement(int acquireIncrement) {
		setProperty("acquireIncrement", String.valueOf(acquireIncrement));
	}

	public void setAcquireRetryAttempts(int acquireRetryAttempts) {
		setProperty("acquireRetryAttempts", String.valueOf(acquireRetryAttempts));
	}

	public void setAcquireRetryDelay(int acquireRetryDelay) {
		setProperty("acquireRetryDelay", String.valueOf(acquireRetryDelay));
	}

	public void setAutoCommitOnClose(boolean autoCommitOnClose) {
		setProperty("autoCommitOnClose", String.valueOf(autoCommitOnClose));
	}

	public void setAutomaticTestTable(String automaticTestTable) {
		setProperty("automaticTestTable", automaticTestTable);
	}

	public void setBreakAfterAcquireFailure(boolean breakAfterAcquireFailure) {
		setProperty("breakAfterAcquireFailure", String.valueOf(breakAfterAcquireFailure));
	}

	public void setCheckoutTimeout(int checkoutTimeout) {
		setProperty("checkoutTimeout", String.valueOf(checkoutTimeout));
	}

	public void setConnectionCustomizerClassName(String connectionCustomizerClassName) {
		setProperty("connectionCustomizerClassName", connectionCustomizerClassName);
	}

	public void setConnectionTesterClassName(String connectionTesterClassName) {
		setProperty("connectionTesterClassName", connectionTesterClassName);
	}

	public void setDataSourceName(String dataSourceName) {
		setProperty("dataSourceName", dataSourceName);
	}

	public void setDebugUnreturnedConnectionStackTraces(boolean debugUnreturnedConnectionStackTraces) {
		setProperty("debugUnreturnedConnectionStackTraces", String.valueOf(debugUnreturnedConnectionStackTraces));
	}

	public void setDescription(String description) {
		setProperty("description", description);
	}

	public void setFactoryClassLocation(String factoryClassLocation) {
		setProperty("factoryClassLocation", factoryClassLocation);
	}

	public void setForceIgnoreUnresolvedTransactions(boolean forceIgnoreUnresolvedTransactions) {
		setProperty("forceIgnoreUnresolvedTransactions", String.valueOf(forceIgnoreUnresolvedTransactions));
	}

	public void setIdleConnectionTestPeriod(int idleConnectionTestPeriod) {
		setProperty("idleConnectionTestPeriod", String.valueOf(idleConnectionTestPeriod));
	}

	public void setInitialPoolSize(int initialPoolSize) {
		setProperty("initialPoolSize", String.valueOf(initialPoolSize));
	}

	public void setMaxAdministrativeTaskTime(int maxAdministrativeTaskTime) {
		setProperty("maxAdministrativeTaskTime", String.valueOf(maxAdministrativeTaskTime));
	}

	public void setMaxConnectionAge(int maxConnectionAge) {
		setProperty("maxConnectionAge", String.valueOf(maxConnectionAge));
	}

	public void setMaxIdleTime(int maxIdleTime) {
		setProperty("maxIdleTime", String.valueOf(maxIdleTime));
	}

	public void setMaxIdleTimeExcessConnections(int maxIdleTimeExcessConnections) {
		setProperty("maxIdleTimeExcessConnections", String.valueOf(maxIdleTimeExcessConnections));
	}

	public void setMaxPoolSize(int maxPoolSize) {
		setProperty("maxPoolSize", String.valueOf(maxPoolSize));
	}

	public void setMaxStatements(int maxStatements) {
		setProperty("maxStatements", String.valueOf(maxStatements));
	}

	public void setMaxStatementsPerConnection(int maxStatementsPerConnection) {
		setProperty("maxStatementsPerConnection", String.valueOf(maxStatementsPerConnection));
	}

	public void setMinPoolSize(int minPoolSize) {
		setProperty("minPoolSize", String.valueOf(minPoolSize));
	}

	public void setNumHelperThreads(int numHelperThreads) {
		setProperty("numHelperThreads", String.valueOf(numHelperThreads));
	}

	public void setOverrideDefaultPassword(String overrideDefaultPassword) {
		setProperty("overrideDefaultPassword", overrideDefaultPassword);
	}

	public void setOverrideDefaultUser(String overrideDefaultUser) {
		setProperty("overrideDefaultUser", overrideDefaultUser);
	}

	public void setPreferredTestQuery(String preferredTestQuery) {
		setProperty("preferredTestQuery", preferredTestQuery);
	}

	public void setPropertyCycle(int propertyCycle) {
		setProperty("propertyCycle", String.valueOf(propertyCycle));
	}

	public void setTestConnectionOnCheckin(boolean testConnectionOnCheckin) {
		setProperty("testConnectionOnCheckin", String.valueOf(testConnectionOnCheckin));
	}

	public void setTestConnectionOnCheckout(boolean testConnectionOnCheckout) {
		setProperty("testConnectionOnCheckout", String.valueOf(testConnectionOnCheckout));
	}

	public void setUnreturnedConnectionTimeout(int unreturnedConnectionTimeout) {
		setProperty("unreturnedConnectionTimeout", String.valueOf(unreturnedConnectionTimeout));
	}

	public void setUserOverridesAsString(String userOverridesAsString) {
		setProperty("userOverridesAsString", userOverridesAsString);
	}

	public void setUsesTraditionalReflectiveProxies(boolean usesTraditionalReflectiveProxies) {
		setProperty("usesTraditionalReflectiveProxies", String.valueOf(usesTraditionalReflectiveProxies));
	}

	public void setProperties(Properties properties) {
		for (Map.Entry<Object, Object> item : properties.entrySet()) {
			setProperty(String.valueOf(item.getKey().toString()), String.valueOf(item.getValue()));
		}
	}

	protected void setProperty(String name, String value) {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(String.format("set property[%s : %s].", name, value));
		}

		setProperty0(name, value);
	}

	protected void setProperty(String name, int value) {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(String.format("set property[%s : %s].", name, value));
		}

		setProperty0(name, "@I_" + String.valueOf(value));
	}

	protected void setProperty(String name, long value) {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(String.format("set property[%s : %s].", name, value));
		}

		setProperty0(name, "@L_" + String.valueOf(value));
	}

	protected void setProperty(String name, Collection<Object> collection) {
		JsonArray jsonArray = new JsonArray(collection);

		if (getLogger().isDebugEnabled()) {
			getLogger().debug(String.format("set property[%s : %s].", name, jsonArray.toString()));
		}
		setProperty0(name, "@C_" + jsonArray.toString());
	}

	protected void setProperty(String name, boolean value) {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(String.format("set property[%s : %s].", name, value));
		}

		setProperty0(name, String.valueOf(value));
	}

	private void setProperty0(String name, String value) {
		Any any = null;
		for (Any a : dsProperties.getProperties()) {
			if (a.getName().equals(name)) {
				any = a;
				break;
			}
		}

		if (any == null) {
			any = new Any();
			dsProperties.getProperties().add(any);
		}

		any.setName(name);
		any.setValue(value);

		refresh(name);
	}

	protected abstract Logger getLogger();

	protected abstract void refresh(String propertyName);
}
