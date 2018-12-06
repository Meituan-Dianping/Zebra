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
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Dbcp2DataSourcePool extends AbstractDataSourcePool implements DataSourcePool {

	protected static final Logger LOGGER = LoggerFactory.getLogger(Dbcp2DataSourcePool.class);

	private BasicDataSource pool = null;

	@Override
	public DataSource build(DataSourceConfig config, boolean withDefaultValue) {
		BasicDataSource dbcp2DataSource = new BasicDataSource();

		dbcp2DataSource.setUrl(config.getJdbcUrl());
		dbcp2DataSource.setUsername(config.getUsername());
		dbcp2DataSource.setPassword(config.getPassword());
		dbcp2DataSource.setDriverClassName(StringUtils.isNotBlank(config.getDriverClass()) ? config.getDriverClass()
		      : JdbcDriverClassHelper.getDriverClassNameByJdbcUrl(config.getJdbcUrl()));

		if (withDefaultValue) {
			dbcp2DataSource.setInitialSize(getIntProperty(config, "initialPoolSize", 5));
			dbcp2DataSource.setMaxTotal(getIntProperty(config, "maxPoolSize", 30));
			dbcp2DataSource.setMinIdle(getIntProperty(config, "minPoolSize", 5));
			dbcp2DataSource.setMaxIdle(getIntProperty(config, "maxPoolSize", 20));
			dbcp2DataSource.setMaxWaitMillis(getIntProperty(config, "checkoutTimeout", 1000));
			dbcp2DataSource.setValidationQuery(getStringProperty(config, "preferredTestQuery", "SELECT 1"));
			dbcp2DataSource.setMinEvictableIdleTimeMillis(getIntProperty(config, "minEvictableIdleTimeMillis", 1800000));// 30min
			dbcp2DataSource
			      .setTimeBetweenEvictionRunsMillis(getIntProperty(config, "timeBetweenEvictionRunsMillis", 30000)); // 30s
			dbcp2DataSource.setRemoveAbandonedTimeout(getIntProperty(config, "removeAbandonedTimeout", 300)); // 30s
			dbcp2DataSource.setNumTestsPerEvictionRun(getIntProperty(config, "numTestsPerEvictionRun", 6)); // 30s
			dbcp2DataSource.setValidationQueryTimeout(getIntProperty(config, "validationQueryTimeout", 0));
			if (StringUtils.isNotBlank(getStringProperty(config, "connectionInitSql", null))) {
				List<String> initSqls = new ArrayList<String>();
				initSqls.add(getStringProperty(config, "connectionInitSql", null));
				dbcp2DataSource.setConnectionInitSqls(initSqls);
			}

			dbcp2DataSource.setTestWhileIdle(true);
			dbcp2DataSource.setTestOnBorrow(false);
			dbcp2DataSource.setTestOnReturn(false);
			dbcp2DataSource.setRemoveAbandonedOnBorrow(true);
			dbcp2DataSource.setRemoveAbandonedOnMaintenance(true);
		} else {
			try {
				PropertiesInit<BasicDataSource> propertiesInit = new PropertiesInit<BasicDataSource>(dbcp2DataSource);
				propertiesInit.initPoolProperties(config);
			} catch (Exception e) {
				throw new ZebraConfigException(String.format("dbcp2 dataSource [%s] created error : ", config.getId()), e);
			}
		}

		this.pool = dbcp2DataSource;
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
