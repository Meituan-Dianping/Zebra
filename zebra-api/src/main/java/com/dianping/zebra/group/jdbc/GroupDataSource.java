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

import org.apache.logging.log4j.Logger;
import com.dianping.zebra.Constants;
import com.dianping.zebra.annotation.Internal;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.config.LionKey;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.filter.DefaultJdbcFilterChain;
import com.dianping.zebra.filter.FilterManagerFactory;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;
import com.dianping.zebra.group.config.SystemConfigManager;
import com.dianping.zebra.group.config.SystemConfigManagerFactory;
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
import com.dianping.zebra.log.LoggerLoader;
import com.dianping.zebra.single.jdbc.C3P0StyleDataSource;
import com.dianping.zebra.single.manager.SingleDataSourceManagerFactory;
import com.dianping.zebra.util.AppPropertiesUtils;
import com.dianping.zebra.util.JDBCUtils;
import com.dianping.zebra.util.StringUtils;

/**
 * 
 * @author hao.zhu
 *
 */
public class GroupDataSource extends C3P0StyleDataSource implements GroupDataSourceMBean {

	static {
		LoggerLoader.init();
	}

	protected static final Logger logger = LoggerLoader.getLogger(GroupDataSource.class);

	// config
	protected String jdbcRef;

	protected String poolType;// support three type : "c3p0" , "tomcat-jdbc" or
								// "druid"

	protected RouterType routerType = RouterType.MASTER_SLAVE;

	protected Map<String, Object> springProperties = new HashMap<String, Object>();

	protected GroupDataSourceConfig groupConfig = new GroupDataSourceConfig();

	protected SystemConfigManager systemConfigManager;

	protected DataSourceConfigManager dataSourceConfigManager;

	// other
	protected volatile boolean init = false;

	protected ReadWriteStrategy readWriteStrategy;

	protected LoadBalancedDataSource readDataSource;

	protected FailOverDataSource writeDataSource;

	public GroupDataSource() {
	}

	public GroupDataSource(String jdbcRef) {
		this.jdbcRef = jdbcRef.trim();
	}

	protected void mergeC3P0Properties(GroupDataSourceConfig newGroupConfig) {
		for (Entry<String, DataSourceConfig> entry : newGroupConfig.getDataSourceConfigs().entrySet()) {
			DataSourceConfig config = entry.getValue();

			if (config.getDriverClass() == null || config.getDriverClass().length() <= 0) {
				// in case that DBA has not give default value to driverClass.
				config.setDriverClass(c3p0Config.getDriverClass());
			}

			for (Any property : c3p0Config.getProperties()) {
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

	/**
	 * Append extra jdbcUrl parameters like "zeroDateTimeBehavior=convertToNull"
	 * after default jdbcUrl.This is used to auto-replace dataSource bean to
	 * avoid the case that the default jdbcUrl is not same as its original
	 * jdbcUrl. In normal case, this is not used.
	 */
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
		mergeC3P0Properties(newGroupConfig);
		mergeSpringPropertyConfig(newGroupConfig);
		mergePoolType(newGroupConfig);

		return newGroupConfig;
	}

	private void mergePoolType(GroupDataSourceConfig newGroupConfig) {
		if (StringUtils.isNotBlank(this.poolType)) {
			for (DataSourceConfig config : newGroupConfig.getDataSourceConfigs().values()) {
				config.setType(this.poolType);
			}
		}
	}

	protected void mergeSpringPropertyConfig(GroupDataSourceConfig newGroupConfig) {
		buildExtraJdbcUrlParams(newGroupConfig);
		buildFilter(newGroupConfig);

		Object forceWriteOnLogin = springProperties.get(Constants.SPRING_PROPERTY_FORCE_WRITE_ON_LONGIN);
		if (forceWriteOnLogin instanceof Boolean) {
			newGroupConfig.setForceWriteOnLogin(((Boolean) forceWriteOnLogin).booleanValue());
		}
	}

	public void close() throws SQLException {
		// In case GroupDataSource is not properly initialized.
		if (dataSourceConfigManager != null) {
			dataSourceConfigManager.close();
		}

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
				public GroupConnection getGroupConnection(GroupDataSource source, JdbcFilter chain)
						throws SQLException {
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

	private GroupConnection getConnectionInternal(String username, String password) {
		return new GroupConnection(readDataSource, writeDataSource, readWriteStrategy, routerType, filters);
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

	private long getMaxWarmupTime() {
		long max = 0l;
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
		} else {
			logger.info("initialize a new GroupDataSource by using jdbcRef[" + jdbcRef + "].");
		}

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
	}

	/**
	 * check whether your app has enough authority to access the database.
	 */
	protected void securityCheck() {
		ConfigService configService = ConfigServiceFactory.getConfigService(configManagerType, jdbcRef);
		String on = configService.getProperty(LionKey.getDatabaseSecuritySwitchKey());

		if ("true".equalsIgnoreCase(on)) {
			String database = jdbcRef;

			int pos = jdbcRef.indexOf('.');
			if (pos > 0) {
				database = jdbcRef.substring(0, pos);
			}

			String property = configService.getProperty(LionKey.getDatabaseSecurityConfigKey(database));

			if (StringUtils.isNotBlank(property)) {
				String appName = AppPropertiesUtils.getAppName();

				if (!property.contains(appName)) {
					throw new ZebraException(
							"Access deny ! Your app is not allowed to access this database, please register your app on http://zebra.dp/");
				}
			}
		}
	}

	protected void initConfig() {
		this.dataSourceConfigManager = DataSourceConfigManagerFactory.getConfigManager(configManagerType, jdbcRef);
		this.dataSourceConfigManager.addListerner(new GroupDataSourceConfigChangedListener());
		this.systemConfigManager = SystemConfigManagerFactory.getConfigManger(configManagerType);
		this.groupConfig = buildGroupConfig();
	}

	private void initDataSources() {
		try {
			this.readDataSource = new LoadBalancedDataSource(getLoadBalancedConfig(groupConfig.getDataSourceConfigs()),
					this.filters, systemConfigManager.getSystemConfig());
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

	private void initFilters() {
		this.filters = FilterManagerFactory.getFilterManager().loadFilters(this.groupConfig.getFilters());
	}

	private void initInternal() {
		SingleDataSourceManagerFactory.getDataSourceManager().init();
		initDataSources();
		initReadWriteStrategy();
		this.init = true;
		logger.info(String.format("GroupDataSource(%s) successfully initialized.", jdbcRef));
	}

	private void initReadWriteStrategy() {
		ServiceLoader<ReadWriteStrategy> strategies = ServiceLoader.load(ReadWriteStrategy.class);
		ReadWriteStrategyWrapper wraper = new ReadWriteStrategyWrapper();

		if (strategies != null) {
			for (ReadWriteStrategy strategy : strategies) {
				if (strategy != null) {
					wraper.addStrategy(strategy);
				}
			}
		}

		readWriteStrategy = wraper;

		refreshReadWriteStrategyConfig();
	}

	@Override
	protected void refresh(String propertyToChange) {
		if (!this.init) {
			return;
		}

		final GroupDataSourceConfig newGroupConfig = buildGroupConfig();

		if (groupConfig.toString().equals(newGroupConfig.toString())) {
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
						source.refreshIntenal(newGroupConfig);
					}
				}
			};
			chain.refreshGroupDataSource(this, propertyToChange, chain);
		} else {
			refreshIntenal(newGroupConfig);
		}
	}

	private void refreshIntenal(GroupDataSourceConfig groupDataSourceConfig) {
		logger.info(String.format("start to refresh the dataSources(%s)...", jdbcRef));

		LoadBalancedDataSource newReadDataSource = null;
		FailOverDataSource newWriteDataSource = null;
		boolean preparedSwitch = false;
		try {
			newReadDataSource = new LoadBalancedDataSource(
					getLoadBalancedConfig(groupDataSourceConfig.getDataSourceConfigs()), this.filters,
					systemConfigManager.getSystemConfig());
			newReadDataSource.init();
			newWriteDataSource = new FailOverDataSource(getFailoverConfig(groupDataSourceConfig.getDataSourceConfigs()),
					this.filters);
			newWriteDataSource.init();

			preparedSwitch = true;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			try {
				close(newReadDataSource, newWriteDataSource);
			} catch (Exception ignore) {
			}
		}

		if (preparedSwitch) {
			LoadBalancedDataSource tmpReadDataSource = this.readDataSource;
			FailOverDataSource tmpWriteDataSource = this.writeDataSource;

			synchronized (this) {
				// switch
				this.readDataSource = newReadDataSource;
				this.writeDataSource = newWriteDataSource;
			}

			// destroy old dataSources
			try {
				this.close(tmpReadDataSource, tmpWriteDataSource);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			// switch config
			groupConfig = groupDataSourceConfig;

			initFilters();
			refreshReadWriteStrategyConfig();

			logger.info(String.format("refresh the dataSources(%s) successfully!", jdbcRef));
		} else {
			logger.warn(String.format("fail to refresh the dataSource(%s)", jdbcRef));
		}

	}

	private void refreshReadWriteStrategyConfig() {
		if (readWriteStrategy != null) {
			readWriteStrategy.setGroupDataSourceConfig(this.groupConfig);
		}
	}

	public void setExtraJdbcUrlParams(String extraJdbcUrlParams) {
		this.springProperties.put(Constants.SPRING_PROPERTY_EXTRA_JDBC_URL_PARAMS, extraJdbcUrlParams);
	}

	public void setSocketTimeout(long socketTimeout) {
		this.springProperties.put(Constants.SPRING_PROPERTY_EXTRA_JDBC_URL_PARAMS, "socketTimeout=" + socketTimeout);
	}

	public synchronized void setFilter(String filter) {
		this.springProperties.put(Constants.SPRING_PROPERTY_FILTER, filter);
		refresh(Constants.SPRING_PROPERTY_FILTER);
	}

	public synchronized void setJdbcRef(String jdbcRef) {
		this.jdbcRef = jdbcRef;
	}

	// Compatible old GroupDataSource < 2.4.8
	@Deprecated
	public void setName(String name) {
		this.jdbcRef = name;
	}

	public synchronized void setPoolType(String poolType) {
		this.poolType = poolType;
	}

	// hack for set only use slave or master datasource
	public void setRouterType(String routerType) {
		this.routerType = RouterType.getRouterType(routerType);
	}

	@Override
	public synchronized void setCheckoutTimeout(int checkoutTimeout) {
		// do nothing
		// 如果这个属性配置成了0，在数据源挂掉，并启动切换成可用的数据源后，可能会有线程无限等待，导致老的数据源无法关闭。
	}

	public synchronized void setForceWriteOnLogin(boolean turnOn) {
		this.springProperties.put(Constants.SPRING_PROPERTY_FORCE_WRITE_ON_LONGIN, turnOn);
		refresh(Constants.SPRING_PROPERTY_FORCE_WRITE_ON_LONGIN);
	}

	public class GroupDataSourceConfigChangedListener implements PropertyChangeListener {
		@Override
		public synchronized void propertyChange(PropertyChangeEvent evt) {
			refresh(evt.getPropertyName());
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
