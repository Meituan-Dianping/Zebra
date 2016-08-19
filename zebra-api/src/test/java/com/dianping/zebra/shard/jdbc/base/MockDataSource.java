/**
 * Project: com.dianping.zebra.zebra-client-0.1.0
 * 
 * File Created at 2011-6-22
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.jdbc.base;

import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @author danson.liu
 *
 */
public class MockDataSource implements DataSource {
	
	private final String identity;

	public MockDataSource(String identity) {
		this.identity = identity;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return null;
	}

	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		return null;
	}

	public String getIdentity() {
		return identity;
	}

   public Logger getParentLogger() throws SQLFeatureNotSupportedException {
	   return null;
   }
}
