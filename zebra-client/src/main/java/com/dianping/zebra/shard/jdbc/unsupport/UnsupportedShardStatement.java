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
package com.dianping.zebra.shard.jdbc.unsupport;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public abstract class UnsupportedShardStatement implements Statement {

	@Override
	public void addBatch(String sql) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport addBatch");
	}

	@Override
	public void cancel() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport cancel");
	}

	@Override
	public void clearBatch() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport clearBatch");
	}

	@Override
	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport clearWarnings");
	}

	public void closeOnCompletion() throws SQLException {
		throw new UnsupportedOperationException("closeOnCompletion");
	}

	@Override
	public int[] executeBatch() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport executeBatch");
	}

	@Override
	public int getFetchDirection() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getFetchDirection");
	}

	@Override
	public int getFetchSize() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getFetchSize");
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getMaxFieldSize");
	}

	@Override
	public int getMaxRows() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getMaxRows");
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getMoreResults");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getWarnings");
	}

	public boolean isCloseOnCompletion() throws SQLException {
		throw new UnsupportedOperationException("isCloseOnCompletion");
	}

	@Override
	public boolean isPoolable() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport isPoolable");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport isWrapperFor");
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setCursorName");
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setEscapeProcessing");
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setFetchDirection");
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setFetchSize");
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setMaxFieldSize");
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setMaxRows");
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setPoolable");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport unwrap");
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		return 0;
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		// throw new UnsupportedOperationException("Zebra unsupport
		// setQueryTimeout");
	}
}
