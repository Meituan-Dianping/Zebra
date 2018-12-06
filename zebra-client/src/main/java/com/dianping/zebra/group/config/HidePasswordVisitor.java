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
package com.dianping.zebra.group.config;

import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.group.config.datasource.transform.BaseVisitor;
import com.dianping.zebra.util.StringUtils;

public class HidePasswordVisitor extends BaseVisitor {

	private GroupDataSourceConfig config;

	private DataSourceConfig newConfig;

	public HidePasswordVisitor(GroupDataSourceConfig config) {
		this.config = config;
	}

	@Override
	public void visitDataSourceConfig(DataSourceConfig dataSourceConfig) {
		newConfig = config.findOrCreateDataSourceConfig(dataSourceConfig.getId());
		newConfig.mergeAttributes(dataSourceConfig);
		newConfig.setJdbcUrl(dataSourceConfig.getJdbcUrl());
		newConfig.setUsername(dataSourceConfig.getUsername());
		newConfig.setPassword(StringUtils.repeat("*", dataSourceConfig.getPassword() == null ? 0 : dataSourceConfig
		      .getPassword().length()));
		newConfig.setDriverClass(dataSourceConfig.getDriverClass());
		newConfig.setTimeWindow(dataSourceConfig.getTimeWindow());
		newConfig.setPunishLimit(dataSourceConfig.getPunishLimit());
		newConfig.setWarmupTime(dataSourceConfig.getWarmupTime());

		for (Any any : dataSourceConfig.getProperties()) {
			newConfig.getProperties().add(any);
		}
	}

	@Override
	public void visitGroupDataSourceConfig(GroupDataSourceConfig groupDataSourceConfig) {
		config.mergeAttributes(groupDataSourceConfig);

		for (DataSourceConfig dataSourceConfig : groupDataSourceConfig.getDataSourceConfigs().values()) {
			visitDataSourceConfig(dataSourceConfig);
		}
	}
}