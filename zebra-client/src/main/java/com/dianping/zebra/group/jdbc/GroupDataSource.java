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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra.group.jdbc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.zebra.config.*;
import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.group.config.*;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.Constants;
import com.dianping.zebra.annotation.Internal;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.config.LionKey;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.filter.DefaultJdbcFilterChain;
import com.dianping.zebra.filter.FilterManagerFactory;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.group.datasources.FailOverDataSource;
import com.dianping.zebra.group.datasources.LoadBalancedDataSource;
import com.dianping.zebra.group.monitor.GroupDataSourceMBean;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.group.router.ReadWriteStrategy;
import com.dianping.zebra.group.router.ReadWriteStrategyWrapper;
import com.dianping.zebra.group.router.RouterType;
import com.dianping.zebra.group.util.SmoothReload;
import com.dianping.zebra.single.jdbc.C3p0DataSourceAdapter;
import com.dianping.zebra.single.manager.SingleDataSourceManagerFactory;
import com.dianping.zebra.util.AppPropertiesUtils;
import com.dianping.zebra.util.JDBCUtils;
import com.dianping.zebra.util.StringUtils;

public class GroupDataSource extends C3p0DataSourceAdapter implements GroupDataSourceMBean {

	protected static final Logger LOGGER = LoggerFactory.getLogger(GroupDataSource.class);

	// key
	protected String jdbcRef;

	// support six type : "c3p0" , "tomcat-jdbc" , "druid" , "dbcp" , "dbcp2" or "hikaricp"
	protected String poolType = "hikaricp";

	protected boolean lazyInit = true;

	protected Map<String, Object> springProperties = new HashMap<String, Object>();

	protected GroupDataSourceConfig groupConfig = new GroupDataSourceConfig();

	protected SystemConfigManager systemConfigManager;

	protected DataSourceConfigManager dataSourceConfigManager;

	// router
	protected boolean useCustomRouterConfig = false;

	protected String routerStrategy = Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER;

	protected RouterType routerType = RouterType.MASTER_SLAVE;

	// other
	protected ReadWriteStrategy readWriteStrategy;

	protected LoadBalancedDataSource readDataSource;

	protected FailOverDataSource writeDataSource;

	private int jdbcRefMaxInitialCount = 30;

	private static Map<String, Integer> jdbcRefMaxInitialCountMap = new ConcurrentHashMap<String, Integer>(4);

	protected volatile boolean init = false;

	public GroupDataSource() {
	}

	public GroupDataSource(String jdbcRef) {
		this.jdbcRef = jdbcRef.trim();
		serviceConfigs.put(Constants.CONFIG_SERVICE_NAME_KEY, jdbcRef);
	}

	@Internal
	protected void buildExtraJdbcUrlParams(GroupDataSourceConfig newGroupConfig) {
		Object extraJdbcUrlParamsObject = this.springProperties.get(Constants.SPRING_PROPERTY_EXTRA_JDBC_URL_PARAMS);

		if (extraJdbcUrlParamsObject instanceof String) {
			String extraJdbcUrlParams = (String) extraJdbcUrlParamsObject;

			if (!StringUtils.isBlank(extraJdbcUrlParams)) {
				for (DataSourceConfig cfg : newGroupConfig.getDataSourceConfigs().values()) {
					String[] urlInfo = cfg.getJdbcUrl().split("\\?");
					String url = urlInfo[0];
					String param = urlInfo.length > 1 ? urlInfo[1] : null;

					if (StringUtils.isBlank(param) && StringUtils.isBlank(extraJdbcUrlParams)) {
						continue;
					}

					Map<String, String> map = new HashMap<String, String>();
					StringUtils.splitStringToMap(map, param);
					StringUtils.splitStringToMap(map, extraJdbcUrlParams);

					cfg.setJdbcUrl(String.format("%s?%s", url, StringUtils.joinMapToString(map)));
				}
			}
		}
	}

	protected void buildFilter(GroupDataSourceConfig newGroupConfig) {
		String remoteConfig = newGroupConfig.getFilters();
		Object beanConfigObject = this.springProperties.get(Constants.SPRING_PROPERTY_FILTER);
		String beanConfig = beanConfigObject instanceof String ? (String) beanConfigObject : null;
		Set<String> result = new HashSet<String>();

		if (!StringUtils.isBlank(remoteConfig)) {
			String[] remoteFilters = remoteConfig.split(",");
			result.addAll(Arrays.asList(remoteFilters));
		}

		if (!StringUtils.isBlank(beanConfig)) {
			String[] beanFilters = beanConfig.split(",");
			for (String beanFilter : beanFilters) {
				if (beanFilter.startsWith("!") && beanFilter.length() > 1) {
					result.remove(beanFilter.substring(1));
				} else {
					result.add(beanFilter);
				}
			}
		}

		newGroupConfig.setFilters(StringUtils.joinCollectionToString(result, ","));
	}

	protected GroupDataSourceConfig buildGroupConfig() {
		GroupDataSourceConfig newGroupConfig = this.dataSourceConfigManager.getGroupDataSourceConfig();

		return buildGroupConfig(newGroupConfig);
	}

	protected GroupDataSourceConfig buildGroupConfig(GroupDataSourceConfig newGroupConfig) {
		mergeJdbcRef(newGroupConfig);
		mergeC3P0Properties(newGroupConfig);
		mergeSpringPropertyConfig(newGroupConfig);
		mergePoolType(newGroupConfig);
		mergeLazyInit(newGroupConfig);

		return newGroupConfig;
	}

	protected void mergeC3P0Properties(GroupDataSourceConfig newGroupConfig) {
		for (Entry<String, DataSourceConfig> entry : newGroupConfig.getDataSourceConfigs().entrySet()) {
			DataSourceConfig config = entry.getValue();

			if (config.getDriverClass() == null || config.getDriverClass().length() <= 0) {
				// in case that DBA has not give default value to driverClass.
				config.setDriverClass(dsProperties.getDriverClass());
			}

			for (Any property : dsProperties.getProperties()) {
				String key = property.getName();
				String value = property.getValue();
				Any any = findAny(config.getProperties(), key);

				if (any != null) {
					any.setValue(value);
				} else {
					Any any1 = new Any();
					any1.setName(key);
					any1.setValue(value);
					config.getProperties().add(any1);
				}
			}
		}
	}

	private void mergeJdbcRef(GroupDataSourceConfig newGroupConfig) {
		for (DataSourceConfig config : newGroupConfig.getDataSourceConfigs().values()) {
			config.setJdbcref(jdbcRef);
		}
	}

	private void mergePoolType(GroupDataSourceConfig newGroupConfig) {
		if (StringUtils.isNotBlank(this.poolType)) {
			for (DataSourceConfig config : newGroupConfig.getDataSourceConfigs().values()) {
				config.setType(this.poolType);
			}
		}
	}

	private void mergeLazyInit(GroupDataSourceConfig newGroupConfig) {
		if (!this.lazyInit) {
			for (DataSourceConfig config : newGroupConfig.getDataSourceConfigs().values()) {
				config.setLazyInit(this.lazyInit);
			}
		}
	}

	protected void mergeSpringPropertyConfig(GroupDataSourceConfig newGroupConfig) {
		buildExtraJdbcUrlParams(newGroupConfig);
		buildFilter(newGroupConfig);
	}

	public void close() throws SQLException {
		// In case GroupDataSource is not properly initialized.
		if (dataSourceConfigManager != null) {
			dataSourceConfigManager.close();
		}

		LOGGER.info(String.format("start to close the GroupDataSource(%s)...", jdbcRef));

		DataSourceConfigRefresh.getInstance().unregister(this);

		this.close(this.readDataSource, this.writeDataSource);
	}

	private void close(final LoadBalancedDataSource read, final FailOverDataSource write) throws SQLException {
		if (filters != null && filters.size() > 0) {
			JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
				@Override
				public void closeGroupDataSource(GroupDataSource source, JdbcFilter chain) throws SQLException {
					if (index < filters.size()) {
						filters.get(index++).closeGroupDataSource(source, chain);
					} else {
						source.closeInternal(read, write);
					}
				}
			};
			chain.closeGroupDataSource(this, chain);
		} else {
			closeInternal(read, write);
		}
	}

	private void closeInternal(final LoadBalancedDataSource read, final FailOverDataSource write) throws SQLException {
		List<SQLException> exps = new ArrayList<SQLException>();

		try {
			if (read != null) {
				read.close();
			}
		} catch (SQLException e) {
			exps.add(e);
		}

		try {
			if (write != null) {
				write.close();
			}
		} catch (SQLException e) {
			exps.add(e);
		}

		JDBCUtils.throwSQLExceptionIfNeeded(exps);
	}

	private Any findAny(List<Any> all, String name) {
		for (Any any : all) {
			if (any.getName().equals(name)) {
				return any;
			}
		}

		return null;
	}

	// todo
	@Override
	public GroupDataSourceConfig getConfig() {
		return groupConfig;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	@Override
	public Connection getConnection(final String username, final String password) throws SQLException {
		if (filters != null && filters.size() > 0) {
			JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
				@Override
				public GroupConnection getGroupConnection(GroupDataSource source, JdbcFilter chain) throws SQLException {
					if (index < filters.size()) {
						return filters.get(index++).getGroupConnection(source, chain);
					} else {
						return source.getConnectionInternal(username, password);
					}
				}
			};
			return chain.getGroupConnection(this, chain);
		} else {
			return getConnectionInternal(username, password);
		}
	}

	private GroupConnection getConnectionInternal(String username, String password) throws SQLException {
		checkInit();
		return new GroupConnection(readDataSource, writeDataSource, readWriteStrategy, routerType, filters);
	}

	private void checkInit() throws SQLException {
		if (!this.init) {
			throw new SQLException(String.format("GroupDataSource [%s] is not initialized", jdbcRef));
		}
	}

	private Map<String, DataSourceConfig> getFailoverConfig(Map<String, DataSourceConfig> configs) {
		Map<String, DataSourceConfig> failoverConfigMap = new HashMap<String, DataSourceConfig>();

		for (Entry<String, DataSourceConfig> entry : configs.entrySet()) {
			String key = entry.getKey();
			DataSourceConfig config = entry.getValue();

			if (config.isActive() && config.isCanWrite()) {
				failoverConfigMap.put(key, config);
			}
		}

		return failoverConfigMap;
	}

	public String getJdbcRef() {
		return jdbcRef;
	}

	private Map<String, DataSourceConfig> getLoadBalancedConfig(Map<String, DataSourceConfig> configs) {
		Map<String, DataSourceConfig> loadBalancedConfigMap = new HashMap<String, DataSourceConfig>();

		for (Entry<String, DataSourceConfig> entry : configs.entrySet()) {
			String key = entry.getKey();
			DataSourceConfig config = entry.getValue();

			if (config.isActive() && config.isCanRead()) {
				loadBalancedConfigMap.put(key, config);
			}
		}

		return loadBalancedConfigMap;
	}

	private int getMaxWarmupTime() {
		int max = 0;
		for (DataSourceConfig config : groupConfig.getDataSourceConfigs().values()) {
			if (config.getWarmupTime() > max) {
				max = config.getWarmupTime();
			}
		}

		return max;
	}

	@Override
	public synchronized Map<String, SingleDataSourceMBean> getReaderSingleDataSourceMBean() {
		return this.readDataSource.getCurrentDataSourceMBean();
	}

	@Override
	public synchronized SingleDataSourceMBean getWriteSingleDataSourceMBean() {
		return this.writeDataSource.getCurrentDataSourceMBean();
	}

	public synchronized void init() {
		if (StringUtils.isBlank(jdbcRef)) {
			throw new ZebraException("jdbcRef cannot be empty");
		}

		this.checkJdbcRefInitializationTimes();

		if (init) {
			throw new ZebraException(String.format("GroupDataSource [%s] is already initialized once.", jdbcRef));
		} else {
			this.init = true;
		}

		try {
			this.securityCheck();
			this.initConfig();
			this.initFilters();

			if (filters != null && filters.size() > 0) {
				JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
					@Override
					public void initGroupDataSource(GroupDataSource source, JdbcFilter chain) {
						if (index < filters.size()) {
							filters.get(index++).initGroupDataSource(source, chain);
						} else {
							source.initInternal();
						}
					}
				};
				chain.initGroupDataSource(this, chain);
			} else {
				initInternal();
			}

			this.recordJdbcRefInitializationTimes();
		} catch (Exception e) {
			String errorMsg = "init GroupDataSource[" + jdbcRef + "] error!";
			LOGGER.error(errorMsg, e);
			throw new ZebraException(errorMsg, e);
		}
	}

	/**
	 * check the number of initializations for same jdbcref
	 */
	private void checkJdbcRefInitializationTimes() {
		Integer count = jdbcRefMaxInitialCountMap.get(jdbcRef);
		if (count != null && count >= jdbcRefMaxInitialCount) {
			throw new ZebraException("jdbcRef [" + jdbcRef + "] count exceed limit[" + jdbcRefMaxInitialCount + "]");
		}

		LOGGER.info("initialize a new GroupDataSource by using jdbcRef[" + jdbcRef + "].");
	}

	private void recordJdbcRefInitializationTimes() {
		Integer count = jdbcRefMaxInitialCountMap.get(jdbcRef);

		if (count == null) {
			count = 0;
		}

		jdbcRefMaxInitialCountMap.put(jdbcRef, ++count);
	}

	/**
	 * check whether your app has enough authority to access the database.
	 */
	private boolean checkSecurityRule(String rules, String appName) {
		if (StringUtils.isNotBlank(rules)) {
			String[] ruleList = rules.split(",");
			for (String rule : ruleList) {
				if (appName.matches(rule.replaceAll("\\*", "[\\\\w|\\\\d]+"))) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * check whether your app has enough authority to access the database.
	 */
	private void securityCheck() {
		ConfigService configService = ConfigServiceFactory.getConfigService(configManagerType, serviceConfigs);
		String on = configService.getProperty(LionKey.getGlobalDatabaseSecuritySwitchConfigKey());

		if ("true".equalsIgnoreCase(on)) {
			String database = jdbcRef;

			int pos = jdbcRef.indexOf('.');
			if (pos > 0) {
				database = jdbcRef.substring(0, pos);
			}

			String globalListProperty = configService.getProperty(LionKey.getGlobalDatabaseSecurityConfigKey());
			String databaseSwitchProperty = configService.getProperty(LionKey.getDatabaseSecuritySwitchKey(database));
			String databaseListProperty = configService.getProperty(LionKey.getDatabaseSecurityConfigKey(database));

			String appName = AppPropertiesUtils.getAppName();
			if ("true".equalsIgnoreCase(databaseSwitchProperty)) {
				if (!checkSecurityRule(databaseListProperty, appName) && !checkSecurityRule(globalListProperty, appName)) {
					throw new ZebraException(
					      "Access deny ! Your app is not allowed to access this database, please register your app");
				}
			}
		}
	}

	protected void initConfig() {
		this.dataSourceConfigManager = DataSourceConfigManagerFactory.getConfigManager(configManagerType, serviceConfigs);
		this.dataSourceConfigManager.addListerner(new GroupDataSourceConfigChangedListener());
		this.systemConfigManager = SystemConfigManagerFactory.getConfigManger(configManagerType, serviceConfigs);
		this.groupConfig = buildGroupConfig();

		if (this.groupConfig != null && this.useCustomRouterConfig) {
			this.groupConfig.setRouterStrategy(this.routerStrategy);
		} else if (!this.useCustomRouterConfig) {
			this.routerStrategy = this.groupConfig.getRouterStrategy();
		}
	}

	private void initDataSources() {
		try {
			this.readDataSource = new LoadBalancedDataSource(getLoadBalancedConfig(groupConfig.getDataSourceConfigs()),
			      this.filters, systemConfigManager.getSystemConfig(), this.configManagerType,
			      groupConfig.getRouterStrategy());
			this.readDataSource.init();
			this.writeDataSource = new FailOverDataSource(getFailoverConfig(groupConfig.getDataSourceConfigs()),
			      this.filters);
			this.writeDataSource.init();
		} catch (RuntimeException e) {
			try {
				this.close(this.readDataSource, this.writeDataSource);
			} catch (SQLException ignore) {
			}

			throw new ZebraException("fail to initialize group dataSource [" + jdbcRef + "]", e);
		}
	}

	protected void initFilters() {
		this.filters = FilterManagerFactory.getFilterManager().loadFilters(this.groupConfig.getFilters(),
		      configManagerType, serviceConfigs);
	}

	protected void initInternal() {
		SingleDataSourceManagerFactory.getDataSourceManager().init();
		initDataSources();
		initReadWriteStrategy();
		DataSourceConfigRefresh.getInstance().register(this);
		LOGGER.info(String.format("GroupDataSource(%s) successfully initialized.", jdbcRef));
	}

	private void initReadWriteStrategy() {
		ServiceLoader<ReadWriteStrategy> strategies = ServiceLoader.load(ReadWriteStrategy.class);
		ReadWriteStrategyWrapper wrapper = new ReadWriteStrategyWrapper();

		if (strategies != null) {
			for (ReadWriteStrategy strategy : strategies) {
				if (strategy != null) {
					wrapper.addStrategy(strategy);
				}
			}
		}

		readWriteStrategy = wrapper;

		refreshReadWriteStrategyConfig();
	}

	@Override
	public synchronized void refresh(String propertyToChange) {
		if (!this.init) {
			return;
		}

		final GroupDataSourceConfig newGroupConfig = buildGroupConfig();

		if (this.groupConfig.toString().equals(newGroupConfig.toString())) {
			return;
		}

		SmoothReload sr = new SmoothReload(getMaxWarmupTime());
		sr.waitForReload();

		if (filters != null && filters.size() > 0) {
			JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
				@Override
				public void refreshGroupDataSource(GroupDataSource source, String propertyToChange, JdbcFilter chain) {
					if (index < filters.size()) {
						filters.get(index++).refreshGroupDataSource(source, propertyToChange, chain);
					} else {
						source.refreshInternal(newGroupConfig);
					}
				}
			};
			chain.refreshGroupDataSource(this, propertyToChange, chain);
		} else {
			refreshInternal(newGroupConfig);
		}
	}

	private void refreshInternal(GroupDataSourceConfig groupDataSourceConfig) {
		LOGGER.info(String.format("start to refresh the GroupDataSource(%s)...", jdbcRef));

		try {
			Map<String, DataSourceConfig> newFailoverConfig = getFailoverConfig(
			      groupDataSourceConfig.getDataSourceConfigs());
			this.writeDataSource.refresh(newFailoverConfig);

			Map<String, DataSourceConfig> newLoadBalancedConfig = getLoadBalancedConfig(
			      groupDataSourceConfig.getDataSourceConfigs());
			this.readDataSource.refresh(newLoadBalancedConfig);

			this.groupConfig = groupDataSourceConfig;

			initFilters();
			refreshReadWriteStrategyConfig();

			LOGGER.info(String.format("refresh the GroupDataSources(%s) successfully!", jdbcRef));
		} catch (Throwable e) {
			// never here
			LOGGER.error(e.getMessage(), e);
			LOGGER.warn(String.format("fail to refresh the GroupDataSource(%s)", jdbcRef));
			throw new ZebraConfigException("fail to refresh the GroupDataSource(" + jdbcRef + ")", e);
		}
	}

	private void refreshReadWriteStrategyConfig() {
		if (readWriteStrategy != null) {
			readWriteStrategy.setGroupDataSourceConfig(this.groupConfig);
		}
	}

	public synchronized void refreshRouter(Object routerStrategyObj) {
		if (useCustomRouterConfig || !this.init || LOCAL.equals(configManagerType)) {
			return;
		}

		if (routerStrategyObj != null && routerStrategyObj instanceof String) {
			String routerStrategy = (String) routerStrategyObj;
			try {
				if (routerStrategy.equals(this.routerStrategy)) {
					LOGGER.info("Rebuild router terminate, new router strategy is same to the original, routerStrategy = "
					      + this.routerStrategy);
					return;
				} else {
					this.routerStrategy = routerStrategy;
					if (this.groupConfig != null && !routerStrategy.equals(this.groupConfig.getRouterStrategy())) {
						this.groupConfig.setRouterStrategy(routerStrategy);
					}
				}
				readDataSource.buildRouter(this.routerStrategy);
				LOGGER.info("Rebuild router when router strategy changed, routerStrategy = " + this.routerStrategy);
			} catch (Exception e) {
				LOGGER.error("Refresh router strategy error!", e);
			}
		}
	}

	public void setExtraJdbcUrlParams(String extraJdbcUrlParams) {
		this.springProperties.put(Constants.SPRING_PROPERTY_EXTRA_JDBC_URL_PARAMS, extraJdbcUrlParams);
	}

	@Deprecated
	public void setSocketTimeout(long socketTimeout) {
		this.springProperties.put(Constants.SPRING_PROPERTY_EXTRA_JDBC_URL_PARAMS, "socketTimeout=" + socketTimeout);
	}

	public synchronized void setFilter(String filter) {
		this.springProperties.put(Constants.SPRING_PROPERTY_FILTER, filter);
		refresh(Constants.SPRING_PROPERTY_FILTER);
	}

	public synchronized void setJdbcRef(String jdbcRef) {
		if (jdbcRef != null) {
			this.jdbcRef = jdbcRef.trim();
			this.serviceConfigs.put(Constants.CONFIG_SERVICE_NAME_KEY, jdbcRef);
		}
	}

	// Compatible old GroupDataSource < 2.4.8
	@Deprecated
	public void setName(String name) {
		this.jdbcRef = name;
	}

	public synchronized void setPoolType(String poolType) {
		this.poolType = poolType;
	}

	public synchronized void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	public void setRouterStrategy(String routerStrategy) {
		this.routerStrategy = routerStrategy;
		this.useCustomRouterConfig = true;
	}

	// hack for set only use slave or master datasource
	public void setRouterType(String routerType) {
		this.routerType = RouterType.getRouterType(routerType);
	}

	public void setJdbcRefMaxInitialCount(int jdbcRefMaxInitialCount) {
		this.jdbcRefMaxInitialCount = jdbcRefMaxInitialCount;
	}

	@Override
	public synchronized void setCheckoutTimeout(int checkoutTimeout) {
		// do nothing
		// 如果这个属性配置成了0，在数据源挂掉，并启动切换成可用的数据源后，可能会有线程无限等待，导致老的数据源无法关闭。
	}

	public synchronized void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
		setProperty("timeBetweenEvictionRunsMillis", timeBetweenEvictionRunsMillis);
	}

	public synchronized void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		setProperty("numTestsPerEvictionRun", numTestsPerEvictionRun);
	}

	public synchronized void setValidationQueryTimeout(int validationQueryTimeout) {
		setProperty("validationQueryTimeout", validationQueryTimeout);
	}

	public synchronized void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		setProperty("removeAbandonedTimeout", removeAbandonedTimeout);
	}

	public synchronized void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
		setProperty("minEvictableIdleTimeMillis", minEvictableIdleTimeMillis);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	public class GroupDataSourceConfigChangedListener implements PropertyChangeListener {
		@Override
		public synchronized void propertyChange(PropertyChangeEvent evt) {
			refresh(evt.getPropertyName());
		}
	}

}
