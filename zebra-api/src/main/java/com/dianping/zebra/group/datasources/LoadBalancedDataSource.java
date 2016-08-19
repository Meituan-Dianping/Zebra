package com.dianping.zebra.group.datasources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.system.entity.SystemConfig;
import com.dianping.zebra.group.exception.SlaveDsDisConnectedException;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.group.router.DataSourceRouter;
import com.dianping.zebra.group.router.RetryConnectDataSourceRouter;
import com.dianping.zebra.group.router.RouterContext;
import com.dianping.zebra.group.router.RouterTarget;
import com.dianping.zebra.group.util.SqlAliasManager;
import com.dianping.zebra.single.jdbc.AbstractDataSource;
import com.dianping.zebra.single.jdbc.SingleDataSource;
import com.dianping.zebra.single.manager.SingleDataSourceManager;
import com.dianping.zebra.single.manager.SingleDataSourceManagerFactory;
import com.dianping.zebra.util.JDBCUtils;
import com.dianping.zebra.util.JdbcDriverClassHelper;

public class LoadBalancedDataSource extends AbstractDataSource {

	private SingleDataSourceManager dataSourceManager;

	private Map<String, SingleDataSource> dataSources;

	private Map<String, DataSourceConfig> loadBalancedConfigMap;

	private DataSourceRouter router;

	private SystemConfig systemConfig;

	public LoadBalancedDataSource(Map<String, DataSourceConfig> loadBalancedConfigMap, List<JdbcFilter> filters,
			SystemConfig systemConfig) {
		this.dataSources = new HashMap<String, SingleDataSource>();
		this.loadBalancedConfigMap = loadBalancedConfigMap;
		this.filters = filters;
		this.systemConfig = systemConfig;
	}

	public void close() throws SQLException {
		for (SingleDataSource ds : dataSources.values()) {
			dataSourceManager.destoryDataSource(ds);
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		RouterContext context = new RouterContext();

		for (SingleDataSource dataSource : this.dataSources.values()) {
			if (dataSource.isDown() || dataSource.isClosed()) {
				context.addExcludeTarget(dataSource.getId());
			}
		}

		RouterTarget target = this.router.select(context);

		if (target != null) {
			int tmpRetryTimes = -1;
			Set<RouterTarget> excludeTargets = new HashSet<RouterTarget>();
			List<SQLException> exceptions = new ArrayList<SQLException>();

			while (tmpRetryTimes++ < this.systemConfig.getRetryTimes()) {
				try {
					if (tmpRetryTimes > 0) {
						SqlAliasManager.setRetrySqlAlias();
					}

					return this.dataSources.get(target.getId()).getConnection();
				} catch (SQLException e) {
					exceptions.add(e);
					excludeTargets.add(target);
					context = new RouterContext(excludeTargets);
					target = router.select(context);
					if (target == null) {
						break;
					}
				}
			}

			if (!exceptions.isEmpty()) {
				JDBCUtils.throwSQLExceptionIfNeeded(exceptions);
			}
		} else {
			throw new SQLException("No available dataSource");
		}

		throw new SQLException("Can not aquire connection");
	}

	public Map<String, SingleDataSourceMBean> getCurrentDataSourceMBean() {
		Map<String, SingleDataSourceMBean> beans = new HashMap<String, SingleDataSourceMBean>();
		beans.putAll(dataSources);

		return beans;
	}

	public void init() {
		this.dataSourceManager = SingleDataSourceManagerFactory.getDataSourceManager();

		for (DataSourceConfig config : loadBalancedConfigMap.values()) {
			try {
				JdbcDriverClassHelper.loadDriverClass(config.getDriverClass(), config.getJdbcUrl());
				Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(),
						config.getPassword());

				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				throw new SlaveDsDisConnectedException(
						"Cannot connect slave datasource(" + config.getJdbcUrl() + ":" + config.getUsername() + ").",
						e);
			}
		}

		for (DataSourceConfig config : loadBalancedConfigMap.values()) {
			SingleDataSource dataSource = dataSourceManager.createDataSource(config, this.filters);
			this.dataSources.put(config.getId(), dataSource);
		}

		this.router = new RetryConnectDataSourceRouter(loadBalancedConfigMap, systemConfig.getDataCenters());
	}
}
