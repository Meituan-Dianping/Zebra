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
package com.dianping.zebra.group.datasources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.dianping.zebra.exception.ZebraException;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.single.jdbc.AbstractDataSource;
import com.dianping.zebra.single.jdbc.SingleDataSource;
import com.dianping.zebra.single.manager.SingleDataSourceManager;
import com.dianping.zebra.single.manager.SingleDataSourceManagerFactory;
import com.dianping.zebra.util.JdbcDriverClassHelper;

/**
 * features: 1. if cannot find any master database in the initial phase, failfast.</br>
 *           2. if cannot find any master database in the refresh phase, set master to null to help MHA switch.
 */
public class FailOverDataSource extends AbstractDataSource {
	private Map<String, DataSourceConfig> configs;

	private volatile SingleDataSource master;

	private SingleDataSourceManager dataSourceManager;

	public FailOverDataSource(Map<String, DataSourceConfig> configs, List<JdbcFilter> filters) {
		this.configs = configs;
		this.filters = filters;
	}

	@Override
	public void init() {
		if (configs == null || configs.isEmpty()) {
			throw new ZebraConfigException("empty master datasource in config!");
		} else {
			DataSourceConfig masterConfig = null;
			for (DataSourceConfig config : configs.values()) {
				if (config.isActive() && config.isCanWrite()) {
					masterConfig = config;
					break;
				}
			}

			checkConfig(masterConfig);

			if (masterConfig == null) {
				throw new ZebraConfigException("no active master datasource in config!");
			} else {
				this.dataSourceManager = SingleDataSourceManagerFactory.getDataSourceManager();

				this.master = this.dataSourceManager.createDataSource(masterConfig, this.filters);
			}
		}

	}

	private void checkConfig(DataSourceConfig config) {
		try {
			JdbcDriverClassHelper.loadDriverClass(config.getDriverClass(), config.getJdbcUrl());
			Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());

			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			throw new ZebraException("Cannot connect datasource(" + config.getJdbcUrl() + ":" + config.getUsername()
			      + ").", e);
		}
	}

	public synchronized void refresh(Map<String, DataSourceConfig> newFailoverConfig) {
		if (this.configs.toString().equals(newFailoverConfig.toString())) {
			return;
		}

		if (newFailoverConfig.isEmpty()) {
			this.dataSourceManager.destoryDataSource(this.master);
			this.master = null;
			this.configs = newFailoverConfig;
		} else {
			SingleDataSource newMaster = null;
			for (DataSourceConfig config : newFailoverConfig.values()) {
				newMaster = this.dataSourceManager.createDataSource(config, this.filters);

				// switch first
				SingleDataSource oldMaster = master;
				this.master = newMaster;
				this.configs = newFailoverConfig;

				// close after
				this.dataSourceManager.destoryDataSource(oldMaster);
				break;
			}
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		checkMaster();

		return master.getConnection();
	}

	private void checkMaster() throws SQLException {
		if (master == null) {
			throw new SQLException("master is not avaliable now, MHA failover may happened");
		}
	}

	public SingleDataSourceMBean getCurrentDataSourceMBean() {
		return this.master;
	}

	public static class FindMasterDataSourceResult {
		private String dsId;

		private boolean changedMaster;

		private boolean masterExist;

		private Exception exception;

		public String getDsId() {
			return dsId;
		}

		public void setDsId(String dsId) {
			this.dsId = dsId;
		}

		public Exception getException() {
			return exception;
		}

		public void setException(Exception exception) {
			this.exception = exception;
		}

		public boolean isChangedMaster() {
			return changedMaster;
		}

		public void setChangedMaster(boolean changedMaster) {
			this.changedMaster = changedMaster;
		}

		public boolean isMasterExist() {
			return masterExist;
		}

		public void setMasterExist(boolean masterExist) {
			this.masterExist = masterExist;
		}
	}

	public static class MasterDataSourceMonitor {
	}

	@Override
	public void close() throws SQLException {
		if (master != null) {
			SingleDataSourceManagerFactory.getDataSourceManager().destoryDataSource(master);
		}
	}
}