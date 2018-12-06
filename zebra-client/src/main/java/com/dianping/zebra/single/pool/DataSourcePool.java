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
package com.dianping.zebra.single.pool;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.single.jdbc.SingleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public interface DataSourcePool {
	public DataSource build(DataSourceConfig config, boolean withDefaultValue);

	public void close(SingleDataSource singleDataSource, boolean forceClose) throws SQLException;

	public DataSource getInnerDataSourcePool();

	public int getNumBusyConnection();

	public int getNumConnections();

	public int getNumIdleConnection();
}
