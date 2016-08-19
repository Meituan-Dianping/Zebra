package com.dianping.zebra.single.jdbc;

import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.util.StringUtils;

import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractDataSource implements DataSource {

	public static final String LOCAL = Constants.CONFIG_MANAGER_TYPE_LOCAL;

	public static final String REMOTE = Constants.CONFIG_MANAGER_TYPE_REMOTE;

	protected String configManagerType = REMOTE;

	protected volatile List<JdbcFilter> filters;

	private int loginTimeout = 0;

	private PrintWriter out = null;

	protected void close() throws SQLException {
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return out;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.out = out;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return this.loginTimeout;
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		this.loginTimeout = seconds;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new UnsupportedOperationException("getParentLogger");
	}

	protected void init() {
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.getClass().isAssignableFrom(iface);
	}

	public void setConfigManagerType(String configManagerType) {
		if (StringUtils.isBlank(configManagerType)) {
			throw new ZebraException("configManagerType must not be blank");
		}

		this.configManagerType = configManagerType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		try {
			return (T) this;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}
}
