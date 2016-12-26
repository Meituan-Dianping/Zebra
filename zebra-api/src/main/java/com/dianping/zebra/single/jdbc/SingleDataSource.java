package com.dianping.zebra.single.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.filter.DefaultJdbcFilterChain;
import com.dianping.zebra.filter.FilterManagerFactory;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.group.util.DataSourceState;
import com.dianping.zebra.log.LoggerLoader;
import com.dianping.zebra.single.pool.ZebraPoolManager;
import com.dianping.zebra.util.StringUtils;
import com.mchange.v2.c3p0.PoolBackedDataSource;

/**
 * 
 * @author hao.zhu
 *
 */
public class SingleDataSource extends C3P0StyleDataSource implements DataSourceLifeCycle, SingleDataSourceMBean {
	
	static{
		LoggerLoader.init();
	}

	private static final Logger logger = LoggerLoader.getLogger(SingleDataSource.class);

	public static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:mysql://[^:]+:\\d+/(\\w+)");

	private String dsId;

	private DataSourceConfig config;

	private DataSource dataSource;

	private CountPunisher punisher;

	private volatile DataSourceState state = DataSourceState.INITIAL;

	private AtomicInteger closeAttmpet = new AtomicInteger(1);

	private String poolType = "c3p0";

	public SingleDataSource() {
		this.config = new DataSourceConfig();
		this.config.setCanRead(true);
		this.config.setCanWrite(true);
		this.config.setActive(true);
		this.punisher = new CountPunisher(this, config.getTimeWindow(), config.getPunishLimit());
	}

	// internal use only
	public SingleDataSource(DataSourceConfig config, List<JdbcFilter> filters) {
		this.dsId = config.getId();
		this.config = config;
		this.punisher = new CountPunisher(this, config.getTimeWindow(), config.getPunishLimit());
		this.filters = filters;
		this.dataSource = initDataSource(config);
	}

	@Override
	public void init() {
		mergeDataSourceConfig();

		this.dataSource = initDataSource(this.config);
		this.filters = FilterManagerFactory.getFilterManager().loadFilters("cat");
	}

	private void mergeDataSourceConfig() {
		if (config.getDriverClass() == null || config.getDriverClass().length() <= 0) {
			// in case that DBA has not give default value to driverClass.
			config.setDriverClass(c3p0Config.getDriverClass());
		}

		this.config.setType(this.poolType);
		this.config.setProperties(c3p0Config.getProperties());
	}

	public synchronized void setJdbcUrl(String jdbcUrl) {
		checkNull("jdbcUrl", jdbcUrl);
		this.config.setJdbcUrl(jdbcUrl);

		Matcher matcher = JDBC_URL_PATTERN.matcher(jdbcUrl);
		if (matcher.find()) {
			this.config.setId(matcher.group(1));
		}
	}

	public synchronized void setUser(String user) {
		checkNull("user", user);
		this.config.setUsername(user);
	}

	public synchronized void setPassword(String password) {
		this.config.setPassword(password);
	}

	private void checkNull(String key, String value) {
		if (StringUtils.isBlank(value)) {
			throw new ZebraConfigException(key + " cannot be null!");
		}
	}

	private void checkState() throws SQLException {
		if (state == DataSourceState.CLOSED || state == DataSourceState.DOWN) {
			throw new SQLException(String.format("dataSource is not avaiable, current state is [%s]", state));
		}
	}

	@Override
	public void close() throws SQLException {
		if (filters != null && filters.size() > 0) {
			JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
				@Override
				public void closeSingleDataSource(SingleDataSource source, JdbcFilter chain) throws SQLException {
					if (index < filters.size()) {
						filters.get(index++).closeSingleDataSource(source, chain);
					} else {
						source.closeOrigin();
					}
				}
			};
			chain.closeSingleDataSource(this, chain);
		} else {
			closeOrigin();
		}
	}

	public void closeOrigin() throws SQLException {
		ZebraPoolManager.close(this);
	}

	public synchronized DataSourceConfig getConfig() {
		return this.config;
	}

	@Override
	public Connection getConnection() throws SQLException {
		checkState();
		return getConnection(null, null);
	}

	@Override
	public Connection getConnection(final String username, final String password) throws SQLException {
		if (filters != null && filters.size() > 0) {
			JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
				@Override
				public SingleConnection getSingleConnection(SingleDataSource source, JdbcFilter chain)
						throws SQLException {
					if (index < filters.size()) {
						return filters.get(index++).getSingleConnection(source, chain);
					} else {
						return source.getConnectionOrigin(username, password);
					}
				}
			};
			return chain.getSingleConnection(this, chain);
		} else {
			return getConnectionOrigin(username, password);
		}
	}

	private SingleConnection getConnectionOrigin(String username, String password) throws SQLException {
		checkState();
		Connection conn;
		try {
			conn = dataSource.getConnection();
		} catch (SQLException e) {
			punisher.countAndPunish(e);
			throw e;
		}

		if (state == DataSourceState.INITIAL) {
			state = DataSourceState.UP;
		}

		return new SingleConnection(this, this.config, conn, this.filters);
	}

	@Override
	public String getCurrentState() {
		return state.toString();
	}

	public String getId() {
		return this.dsId;
	}

	@Override
	public int getNumBusyConnection() {
		if (dataSource != null) {
			try {
				if (dataSource instanceof PoolBackedDataSource) {
					return ((PoolBackedDataSource) dataSource).getNumBusyConnections();
				} else if (dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
					return ((org.apache.tomcat.jdbc.pool.DataSource) dataSource).getActive();
				}
			} catch (Exception e) {
			}
		}

		return 0;
	}

	@Override
	public int getNumConnections() {
		if (dataSource != null) {
			try {
				if (dataSource instanceof PoolBackedDataSource) {
					return ((PoolBackedDataSource) dataSource).getNumConnections();
				} else if (dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
					return ((org.apache.tomcat.jdbc.pool.DataSource) dataSource).getSize();
				}
			} catch (Exception e) {
			}
		}

		return 0;
	}

	@Override
	public int getNumIdleConnection() {
		if (dataSource != null) {
			try {
				if (dataSource instanceof PoolBackedDataSource) {
					return ((PoolBackedDataSource) dataSource).getNumIdleConnections();
				} else if (dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
					return ((org.apache.tomcat.jdbc.pool.DataSource) dataSource).getIdle();
				}
			} catch (Exception e) {
			}
		}

		return 0;
	}

	public CountPunisher getPunisher() {
		return this.punisher;
	}

	@Override
	public DataSourceState getState() {
		return this.state;
	}

	private DataSource initDataSource(final DataSourceConfig value) {
		if (filters != null && filters.size() > 0) {
			JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
				@Override
				public DataSource initSingleDataSource(SingleDataSource source, JdbcFilter chain) {
					if (index < filters.size()) {
						return filters.get(index++).initSingleDataSource(source, chain);
					} else {
						return source.initDataSourceOrigin(value);
					}
				}
			};
			return chain.initSingleDataSource(this, chain);
		} else {
			return initDataSourceOrigin(value);
		}
	}

	private DataSource initDataSourceOrigin(DataSourceConfig value) {
		return ZebraPoolManager.buildDataSource(value);
	}

	public boolean isAvailable() {
		return this.state == DataSourceState.INITIAL || this.state == DataSourceState.UP;
	}

	public boolean isClosed() {
		return this.state == DataSourceState.CLOSED;
	}

	public boolean isDown() {
		return this.state == DataSourceState.DOWN;
	}

	@Override
	public void markClosed() {
		this.state = DataSourceState.CLOSED;
	}

	@Override
	public void markDown() {
		this.state = DataSourceState.DOWN;
	}

	@Override
	public void markUp() {
		this.state = DataSourceState.INITIAL;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected void refresh(String propertyName) {
	}

	public synchronized void setPoolType(String poolType) {
		this.poolType = poolType;
	}

	public DataSource getInnerDataSource() {
		return this.dataSource;
	}

	public int getAndIncrementCloseAttempt() {
		return this.closeAttmpet.getAndIncrement();
	}

	public int getCloseAttempt() {
		return this.closeAttmpet.get();
	}

	public synchronized void setState(DataSourceState state) {
		this.state = state;
	}
}
