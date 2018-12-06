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
package com.dianping.zebra.single.pool;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.util.DataSourceState;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.single.jdbc.SingleDataSource;
import com.dianping.zebra.util.JdbcDriverClassHelper;
import com.dianping.zebra.util.StringUtils;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbcpDataSourcePool extends AbstractDataSourcePool implements DataSourcePool {

	protected static final Logger LOGGER = LoggerFactory.getLogger(DbcpDataSourcePool.class);

	private BasicDataSource pool = null;

	@Override
	public DataSource build(DataSourceConfig config, boolean withDefaultValue) {
		BasicDataSource dbcpDataSource = new BasicDataSource();

		dbcpDataSource.setUrl(config.getJdbcUrl());
		dbcpDataSource.setUsername(config.getUsername());
		dbcpDataSource.setPassword(config.getPassword());
		dbcpDataSource.setDriverClassName(StringUtils.isNotBlank(config.getDriverClass()) ? config.getDriverClass() : JdbcDriverClassHelper.getDriverClassNameByJdbcUrl(config.getJdbcUrl()));

		if (withDefaultValue) {
			dbcpDataSource.setInitialSize(getIntProperty(config, "initialPoolSize", 5));
			dbcpDataSource.setMaxActive(getIntProperty(config, "maxPoolSize", 30));
			dbcpDataSource.setMinIdle(getIntProperty(config, "minPoolSize", 5));
			dbcpDataSource.setMaxIdle(getIntProperty(config, "maxPoolSize", 20));
			dbcpDataSource.setMaxWait(getIntProperty(config, "checkoutTimeout", 1000));
			dbcpDataSource.setValidationQuery(getStringProperty(config, "preferredTestQuery", "SELECT 1"));
			dbcpDataSource.setMinEvictableIdleTimeMillis(getIntProperty(config, "minEvictableIdleTimeMillis", 1800000));// 30min
			dbcpDataSource.setTimeBetweenEvictionRunsMillis(getIntProperty(config, "timeBetweenEvictionRunsMillis", 30000)); // 30s
			dbcpDataSource.setRemoveAbandonedTimeout(getIntProperty(config, "removeAbandonedTimeout", 300)); // 30s
			dbcpDataSource.setNumTestsPerEvictionRun(getIntProperty(config, "numTestsPerEvictionRun", 6)); // 30s
			dbcpDataSource.setValidationQueryTimeout(getIntProperty(config, "validationQueryTimeout", 0));
			if (StringUtils.isNotBlank(getStringProperty(config, "connectionInitSql", null))) {
				List<String> initSqls = new ArrayList<String>();
				initSqls.add(getStringProperty(config, "connectionInitSql", null));
				dbcpDataSource.setConnectionInitSqls(initSqls);
			}

			dbcpDataSource.setTestWhileIdle(true);
			dbcpDataSource.setTestOnBorrow(false);
			dbcpDataSource.setTestOnReturn(false);
			dbcpDataSource.setRemoveAbandoned(true);

		} else {
			try {
				PropertiesInit<BasicDataSource> propertiesInit = new PropertiesInit<BasicDataSource>(dbcpDataSource);
				propertiesInit.initPoolProperties(config);
			} catch (Exception e) {
				throw new ZebraConfigException(String.format("dbcp2 dataSource [%s] created error : ", config.getId()),
						e);
			}
		}

		this.pool = dbcpDataSource;
		LOGGER.info(String.format("New dataSource [%s] created.", config.getId()));

		return this.pool;
	}

	@Override
	public void close(SingleDataSource singleDataSource, boolean forceClose) throws SQLException {
		String dsId = singleDataSource.getId();
		LOGGER.info(singleDataSource.getAndIncrementCloseAttempt() + " attempt to close datasource [" + dsId + "]");

		if (forceClose) {
			LOGGER.info("closing old datasource [" + dsId + "]");

			pool.close();

			LOGGER.info("old datasource [" + dsId + "] closed");
			singleDataSource.setState(DataSourceState.CLOSED);
		} else {
			if (pool.getNumActive() == 0 || singleDataSource.getCloseAttempt() >= MAX_CLOSE_ATTEMPT) {
				LOGGER.info("closing old datasource [" + dsId + "]");

				pool.close();

				LOGGER.info("old datasource [" + dsId + "] closed");
				singleDataSource.setState(DataSourceState.CLOSED);
			} else {
				throwException(dsId);
			}
		}
	}

	@Override
	public int getNumBusyConnection() {
		if (pool != null) {
			try {
				return pool.getNumActive();
			} catch (Exception e) {
			}
		}

		return 0;
	}

	@Override
	public int getNumConnections() {
		if (pool != null) {
			try {
				return (pool.getNumActive() + pool.getNumIdle());
			} catch (Exception e) {
			}
		}

		return 0;
	}

	@Override
	public int getNumIdleConnection() {
		if (pool != null) {
			try {
				return pool.getNumIdle();
			} catch (Exception e) {
			}
		}

		return 0;
	}

	@Override
	public DataSource getInnerDataSourcePool() {
		return this.pool;
	}
}
