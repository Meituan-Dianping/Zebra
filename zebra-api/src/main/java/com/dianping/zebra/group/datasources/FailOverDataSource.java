package com.dianping.zebra.group.datasources;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.filter.DefaultJdbcFilterChain;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.exception.MasterDsNotFoundException;
import com.dianping.zebra.group.exception.WeakReferenceGCException;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.log.LoggerLoader;
import com.dianping.zebra.single.jdbc.AbstractDataSource;
import com.dianping.zebra.single.jdbc.SingleDataSource;
import com.dianping.zebra.single.manager.SingleDataSourceManagerFactory;
import com.dianping.zebra.util.JdbcDriverClassHelper;
import com.dianping.zebra.util.StringUtils;

/**
 * features: 1. auto-detect master database by select @@read_only</br>
 * 2. auto check the master database.</br>
 * 3. if cannot find any master database in the initial phase, fail fast.</br>
 */
public class FailOverDataSource extends AbstractDataSource {
	private static final Logger logger = LoggerLoader.getLogger(FailOverDataSource.class);

	private Map<String, DataSourceConfig> configs;

	private volatile SingleDataSource master;

	public FailOverDataSource(Map<String, DataSourceConfig> configs, List<JdbcFilter> filters) {
		this.configs = configs;
		this.filters = filters;
	}

	@Override
	public void close() throws SQLException {
		if (master != null) {
			SingleDataSourceManagerFactory.getDataSourceManager().destoryDataSource(master);
		}
	}

	private String getConfigSummary() {
		StringBuilder sb = new StringBuilder(100);

		for (Map.Entry<String, DataSourceConfig> config : configs.entrySet()) {
			sb.append(String.format("[datasource=%s,url=%s,username=%s,password=%s,driverClass=%s,properties=%s]",
					config.getValue().getId(), config.getValue().getJdbcUrl(), config.getValue().getUsername(),
					StringUtils.repeat("*",
							config.getValue().getPassword() == null ? 0 : config.getValue().getPassword().length()),
					config.getValue().getDriverClass(), config.getValue().getProperties()));
		}

		return sb.toString();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		// 因为MHA主库都配成了虚IP，一旦发生切换或者主库挂了，都会导致获取不到连接
		return master.getConnection();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	public SingleDataSourceMBean getCurrentDataSourceMBean() {
		return master;
	}

	private SingleDataSource getDataSource(DataSourceConfig config) {
		if (master != null) {
			SingleDataSourceManagerFactory.getDataSourceManager().destoryDataSource(master);
		}

		return SingleDataSourceManagerFactory.getDataSourceManager().createDataSource(config, this.filters);
	}

	@Override
	public void init() {
		init(true);
	}

	public void init(boolean forceCheckMaster) {
		MasterDataSourceMonitor monitor = new MasterDataSourceMonitor(this);

		try {
			FindMasterDataSourceResult result = monitor.findMasterDataSource();
			if (!result.isMasterExist()) {
				String configSummary = getConfigSummary();
				String error_message = null;

				if (StringUtils.isBlank(configSummary)) {
					error_message = "DataSource config is empty, please contact DBA to properly config it.";
				} else {
					error_message = String.format(
							"Cannot find any master dataSource, this is probably due to the 'readOnly' property of each mysql instance is true. For your convenient, the dal configs = %s",
							configSummary);
				}

				if (forceCheckMaster) {
					MasterDsNotFoundException exp = new MasterDsNotFoundException(error_message, result.getException());
					throw exp;
				} else {
					logger.warn(error_message, result.getException());
				}
			}
		} catch (WeakReferenceGCException e) {
			logger.error("should never be here!", e);
		}
	}

	private boolean setMasterDb(DataSourceConfig config) {
		if (master == null || !master.getId().equals(config.getId())) {
			master = getDataSource(config);
			return true;
		}
		return false;
	}

	static enum CheckMasterDataSourceResult {
		READ_WRITE(1), READ_ONLY(2), ERROR(3);

		private int value;

		private Exception exception;

		private CheckMasterDataSourceResult(int value) {
			this.value = value;
		}

		public Exception getException() {
			return exception;
		}

		public void setException(Exception exception) {
			this.exception = exception;
		}

		public int getValue() {
			return value;
		}
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
		private WeakReference<FailOverDataSource> ref;

		public MasterDataSourceMonitor(FailOverDataSource dsRef) {
			this.ref = new WeakReference<FailOverDataSource>(dsRef);
		}

		private FindMasterDataSourceResult findMasterDataSourceOrigin() throws WeakReferenceGCException {
			FindMasterDataSourceResult result = new FindMasterDataSourceResult();

			if (getWeakFailOverDataSource().configs.values().size() == 0) {
				Exception exp = new ZebraConfigException("zero writer data source in config!");
				logger.warn(exp.getMessage(), exp);
			}

			for (DataSourceConfig config : getWeakFailOverDataSource().configs.values()) {
				CheckMasterDataSourceResult checkResult = isMasterDataSource(config);
				if (checkResult == CheckMasterDataSourceResult.READ_WRITE) {
					result.setChangedMaster(getWeakFailOverDataSource().setMasterDb(config));
					result.setMasterExist(true);
					result.setDsId(config.getId());
					break;
				} else if (checkResult == CheckMasterDataSourceResult.ERROR) {
					result.setException(checkResult.getException());
				}
			}

			if (result.isMasterExist()) {
				// reset the exception if has any
				result.setException(null);
			}

			return result;
		}

		public FindMasterDataSourceResult findMasterDataSource() throws WeakReferenceGCException {
			List<JdbcFilter> filters = getWeakFailOverDataSource().filters;

			if (filters != null && filters.size() > 0) {
				JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
					@Override
					public FindMasterDataSourceResult findMasterFailOverDataSource(MasterDataSourceMonitor source,
							JdbcFilter chain) {
						if (index < filters.size()) {
							return filters.get(index++).findMasterFailOverDataSource(source, chain);
						} else {
							return source.findMasterDataSourceOrigin();
						}
					}
				};
				return chain.findMasterFailOverDataSource(this, chain);
			} else {
				return findMasterDataSourceOrigin();
			}
		}

		protected Connection getConnection(DataSourceConfig config) throws SQLException {
			JdbcDriverClassHelper.loadDriverClass(config.getDriverClass(), config.getJdbcUrl());

			return DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());
		}

		private FailOverDataSource getWeakFailOverDataSource() throws WeakReferenceGCException {
			FailOverDataSource weak = ref.get();
			if (weak == null) {
				throw new WeakReferenceGCException();
			}
			return weak;
		}

		private boolean isMaster(ResultSet rs) throws SQLException {
			if (rs.next()) {
				return rs.getInt(1) == 0;
			} else {
				return false;
			}
		}

		protected CheckMasterDataSourceResult isMasterDataSource(DataSourceConfig config) {
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;

			try {
				conn = getConnection(config);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(config.getTestReadOnlySql());

				if (isMaster(rs)) {
					return CheckMasterDataSourceResult.READ_WRITE;
				} else {
					return CheckMasterDataSourceResult.READ_ONLY;
				}
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);

				CheckMasterDataSourceResult result = CheckMasterDataSourceResult.ERROR;
				result.setException(e);

				return result;
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException ignore) {
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException ignore) {
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException ignore) {
					}
				}
			}
		}
	}
}