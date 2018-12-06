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

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.shard.jdbc.ShardStatement;

public abstract class UnsupportedShardPreparedStatement extends ShardStatement implements PreparedStatement {

	protected UnsupportedShardPreparedStatement(List<JdbcFilter> filters) {
		super(filters);
	}

	@Override
	public void addBatch() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport addBatch");
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getMetaData");
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getParameterMetaData");
	}
}
