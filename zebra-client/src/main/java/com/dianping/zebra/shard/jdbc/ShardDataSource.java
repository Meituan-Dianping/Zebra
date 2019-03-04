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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ServiceConfigBuilder;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.filter.DefaultJdbcFilterChain;
import com.dianping.zebra.filter.FilterManagerFactory;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;

import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.shard.config.ShardDataSourceCustomConfig;
import com.dianping.zebra.shard.jdbc.parallel.SQLThreadPoolExecutor;
import com.dianping.zebra.shard.parser.SQLParser;
import com.dianping.zebra.shard.router.DefaultShardRouter;
import com.dianping.zebra.shard.router.RouterBuilder;
import com.dianping.zebra.shard.router.ShardRouter;
import com.dianping.zebra.shard.router.builder.RemoteRouterBuilder;
import com.dianping.zebra.util.StringUtils;

/**
 * @author Leo Liang
 * @author hao.zhu
 */
public class ShardDataSource extends ShardDataSourceConfigAdapter {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ShardDataSource.class);

	private String ruleName;

	private Map<String, DataSource> dataSourcePool;

	private RouterBuilder routerFactory;

	private DataSourceRepository dataSourceRepository;

	private ShardRouter router;

	private ConfigService configService;

	private volatile boolean closed = false;

	private String defaultDatasource; // 用于不分库且部分分表的库中不分表的表的路由

	private int concurrencyLevel = 1; // 单库并发度

	private boolean forbidNoShardKeyWrite; // 禁止不带分表键全表扫的update与delete

	private boolean optimizeShardKeyInSql; // 优化shard key的in条件语句

	private ShardDataSourceCustomConfig shardDataSourceCustomConfig = new ShardDataSourceCustomConfig();

	public void init() {
		if (StringUtils.isNotBlank(ruleName)) {
			if (configService == null) {
				configService = ConfigServiceFactory.getConfigService(configManagerType, serviceConfigs);
			}

			if (routerFactory == null) {
				routerFactory = new RemoteRouterBuilder(ruleName, defaultDatasource, forbidNoShardKeyWrite, configService);
			}
		} else {
			if (dataSourcePool == null || dataSourcePool.isEmpty()) {
				throw new IllegalArgumentException("dataSourcePool is required.");
			}

			if (routerFactory == null) {
				throw new IllegalArgumentException("routerRuleFile must be set.");
			}
		}

		this.initFilters();

		if (filters != null && filters.size() > 0) {
			JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
				@Override
				public void initShardDataSource(ShardDataSource source, JdbcFilter chain) {
					if (index < filters.size()) {
						filters.get(index++).initShardDataSource(source, chain);
					} else {
						source.initInternal();
					}
				}
			};
			chain.initShardDataSource(this, chain);
		} else {
			initInternal();
		}
	}

	private void initFilters() {
		this.filters = FilterManagerFactory.getFilterManager().loadFilters("cat", configManagerType, serviceConfigs);
	}

	private void initInternal() {
		this.router = routerFactory.build();
		if (this.router instanceof DefaultShardRouter) {
			((DefaultShardRouter) this.router).setOptimizeShardKeyInSql(this.optimizeShardKeyInSql);
		}

		if (dataSourceRepository == null) {
			dataSourceRepository = DataSourceRepository.getInstance();
		}

		if (dataSourcePool != null) {
			dataSourceRepository.init(dataSourcePool);
		} else {
			this.shardDataSourceCustomConfig.setConfigManagerType(this.configManagerType);
			this.shardDataSourceCustomConfig.setDsConfigProperties(this.dsConfigProperties);
			dataSourceRepository.init(this.router.getRouterRule(), this.shardDataSourceCustomConfig);
		}

		// init thread pool
		SQLThreadPoolExecutor.getInstance(false);
		if (SQLThreadPoolExecutor.readWriteSplitPool) {
			SQLThreadPoolExecutor.getInstance(true);
		}

		// init SQL Parser
		SQLParser.init();

		if (ruleName != null) {
			LOGGER.info(String.format("ShardDataSource(%s) successfully initialized.", ruleName));
		} else {
			LOGGER.info("ShardDataSource successfully initialized.");
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		checkInit();
		if (closed) {
			throw new SQLException("Datasource has been closed!");
		}

		ShardConnection connection = new ShardConnection(username, password, filters, concurrencyLevel);
		connection.setRouter(router);
		connection.setDataSourceRepository(dataSourceRepository);

		return connection;
	}

	private void checkInit() throws SQLException {
		if (router == null) {
			throw new SQLException(String.format("ShardDataSource [%s] is not initialize", ruleName));
		}
	}

	public ShardRouter getRouter() {
		return router;
	}

	@Override
	public void close() throws SQLException {
		if (dataSourceRepository != null) {
			dataSourceRepository.close();
		}

		closed = true;

		LOGGER.info(String.format("ShardDataSource(%s) successfully closed.", ruleName));
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setConfigType(String configType) {
		this.configManagerType = configType;
	}

	public void setDataSourcePool(Map<String, DataSource> dataSourcePool) {
		this.dataSourcePool = dataSourcePool;
	}

	public void setDataSourceRepository(DataSourceRepository dataSourceRepository) {
		this.dataSourceRepository = dataSourceRepository;
	}

	public void setRouterFactory(RouterBuilder routerFactory) {
		this.routerFactory = routerFactory;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
		this.serviceConfigs.put(Constants.CONFIG_SERVICE_NAME_KEY, ruleName);
	}

	// thread pool read write splitting
	public void setReadWriteSplitPool(boolean readWriteSplitPool) {
		SQLThreadPoolExecutor.readWriteSplitPool = readWriteSplitPool;
	}

	// write pool
	public void setParallelCorePoolSize(int parallelCorePoolSize) {
		SQLThreadPoolExecutor.writeCorePoolSize = parallelCorePoolSize;
	}

	public void setParallelMaxPoolSize(int parallelMaxPoolSize) {
		SQLThreadPoolExecutor.writeMaxPoolSize = parallelMaxPoolSize;
	}

	public void setParallelWorkQueueSize(int parallelWorkQueueSize) {
		SQLThreadPoolExecutor.writeWorkQueueSize = parallelWorkQueueSize;
	}

	public void setParallelExecuteTimeOut(int parallelExecuteTimeOut) {
		SQLThreadPoolExecutor.writeExecuteTimeOut = parallelExecuteTimeOut;
	}

	// read pool
	public void setReadParallelCorePoolSize(int parallelCorePoolSize) {
		SQLThreadPoolExecutor.readCorePoolSize = parallelCorePoolSize;
	}

	public void setReadParallelMaxPoolSize(int parallelMaxPoolSize) {
		SQLThreadPoolExecutor.readMaxPoolSize = parallelMaxPoolSize;
	}

	public void setReadParallelWorkQueueSize(int parallelWorkQueueSize) {
		SQLThreadPoolExecutor.readWorkQueueSize = parallelWorkQueueSize;
	}

	public void setReadParallelExecuteTimeOut(int parallelExecuteTimeOut) {
		SQLThreadPoolExecutor.readExecuteTimeOut = parallelExecuteTimeOut;
	}

	public void setDefaultDatasource(String defaultDatasource) {
		this.defaultDatasource = defaultDatasource;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setPoolType(String poolType) {
		this.shardDataSourceCustomConfig.setPoolType(poolType);
	}

	public void setLazyInit(boolean lazyInit) {
		this.shardDataSourceCustomConfig.setLazyInit(lazyInit);
	}

	// it's invalid when use custom datasource
	public void setExtraJdbcUrlParams(String extraJdbcUrlParams) {
		this.shardDataSourceCustomConfig.setExtraJdbcUrlParams(extraJdbcUrlParams);
	}

	public void setRouterStrategy(String routerStrategy) {
		this.shardDataSourceCustomConfig.setRouterStrategy(routerStrategy);
	}

	public void setFilter(String filter) {
		this.shardDataSourceCustomConfig.setFilter(filter);
	}

	public void setRouterType(String routerType) {
		this.shardDataSourceCustomConfig.setRouterType(routerType);
	}

	public void setConcurrencyLevel(int concurrencyLevel) {
		if (concurrencyLevel >= 1) {
			this.concurrencyLevel = concurrencyLevel;
		}
	}

	public void setForbidNoShardKeyWrite(boolean forbidNoShardKeyWrite) {
		this.forbidNoShardKeyWrite = forbidNoShardKeyWrite;
	}

	public void setOptimizeShardKeyInSql(boolean optimizeShardKeyInSql) {
		this.optimizeShardKeyInSql = optimizeShardKeyInSql;
	}
}
