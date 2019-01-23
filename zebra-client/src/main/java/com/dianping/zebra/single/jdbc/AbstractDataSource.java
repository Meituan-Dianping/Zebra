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
package com.dianping.zebra.single.jdbc;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ServiceConfigBuilder;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.util.StringUtils;

import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class AbstractDataSource implements DataSource {

	public static final String LOCAL = Constants.CONFIG_MANAGER_TYPE_LOCAL;

	protected String configManagerType = LOCAL;

	protected Map<String, Object> serviceConfigs = ServiceConfigBuilder.newInstance().build();

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
		if(iface == null) {
			return false;
		}

		return iface.isAssignableFrom(this.getClass());
	}

	public void setConfigManagerType(String configManagerType) {
		if (StringUtils.isNotBlank(configManagerType)) {
			this.configManagerType = configManagerType;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if(iface == null) {
			return null;
		}

		try {
			if(iface.isAssignableFrom(this.getClass())) {
				return (T) this;
			} else {
				throw new SQLException(getClass().getName() + " Can not unwrap to" + iface.getName());
			}
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}
}
