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

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.group.config.datasource.entity.*;
import com.dianping.zebra.group.config.datasource.transform.BaseVisitor;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.util.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Map.Entry;

public class DefaultDataSourceConfigManager extends AbstractConfigManager implements DataSourceConfigManager {

	private final char keyValueSeparator = '=';

	private final char pairSeparator = '&';

	private GroupDataSourceConfigBuilder builder;

	private String jdbcRef;

	public DefaultDataSourceConfigManager(String jdbcRef, ConfigService configService) {
		super(configService);
		this.jdbcRef = jdbcRef;
	}

	@Override
	public void addListerner(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public synchronized void init() {
		try {
			this.builder = new GroupDataSourceConfigBuilder();
		} catch (Exception e) {
			throw new ZebraConfigException(
			      String.format("Fail to initialize DefaultDataSourceConfigManager with config key[%s].", this.jdbcRef),
			      e);
		}
	}

	@Override
	public GroupDataSourceConfig getGroupDataSourceConfig() {
		return initGroupDataSourceConfig();
	}

	private GroupDataSourceConfig initGroupDataSourceConfig() {
		GroupDataSourceConfig config = new GroupDataSourceConfig();
		this.builder.visitGroupDataSourceConfig(config);
		return config;
	}

	@Override
	protected synchronized void onPropertyUpdated(PropertyChangeEvent evt) {
	}

	private void validateConfig(Map<String, DataSourceConfig> dataSourceConfigs) {
		int readNum = 0, writeNum = 0;
		for (Entry<String, DataSourceConfig> entry : dataSourceConfigs.entrySet()) {
			if (entry.getValue().getCanRead()) {
				readNum += 1;
			}
			if (entry.getValue().getCanWrite()) {
				writeNum += 1;
			}
		}
		if (readNum < 1 && writeNum < 1) {
			throw new ZebraConfigException(
			      String.format("Not enough read or write dataSources[read:%s, write:%s].", readNum, writeNum));
		}
	}

	class GroupDataSourceConfigBuilder extends BaseVisitor {

		private String getGroupDataSourceKey() {
			return String.format("%s.%s", Constants.DEFAULT_GROUP_DATASOURCE_PRFIX, jdbcRef);
		}

		private String getGroupDataSourceKeyForApp() {
			return String.format("%s.%s", getGroupDataSourceKey(), AppPropertiesUtils.getAppName());
		}

		private String getSingleDataSourceKey(String dsId) {
			return String.format("%s.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsId);
		}

		@Override
		public void visitDataSourceConfig(DataSourceConfig dsConfig) {
			ReadOrWriteRole role = new ReadOrWriteRole(dsConfig.getId(), dsConfig.getCanRead(), dsConfig.getCanWrite());
			String dsId = role.getRealDsName();
			String dsXml = getProperty(getSingleDataSourceKey(dsId), null);

			if (dsXml != null) {
				final SingleDataSourceConfig dsProperties = JaxbUtils.fromXml(dsXml, SingleDataSourceConfig.class);

				if (dsProperties != null) {
					dsConfig.setActive(
					      StringUtils.isNotBlank(dsProperties.getActive()) ? Boolean.parseBoolean(dsProperties.getActive())
					            : dsConfig.getActive());
					dsConfig.setJdbcUrl(
					      StringUtils.isNotBlank(dsProperties.getUrl()) ? dsProperties.getUrl() : dsConfig.getJdbcUrl());
					String urlDriver = JdbcDriverClassHelper.getDriverClassNameByJdbcUrl(dsConfig.getJdbcUrl());
					dsConfig.setDriverClass(
					      StringUtils.isNotBlank(dsProperties.getDriverClass()) ? dsProperties.getDriverClass() : urlDriver);
					dsConfig.setUsername(StringUtils.isNotBlank(dsProperties.getUsername()) ? dsProperties.getUsername()
					      : dsConfig.getUsername());
					dsConfig.setPassword(StringUtils.isNotBlank(dsProperties.getPassword()) ? dsProperties.getPassword()
					      : dsConfig.getPassword());

					if (StringUtils.isNotBlank(dsProperties.getProperties())) {
						Map<String, String> sysMap = Splitters.by(pairSeparator, keyValueSeparator).trim()
						      .split(dsProperties.getProperties());

						for (Entry<String, String> property : sysMap.entrySet()) {
							Any any = new Any();
							any.setName(property.getKey());
							any.setValue(property.getValue());

							dsConfig.getProperties().add(any);
						}

						// hack for maxStatementsPerConnection, since the lion key
						// ${ds.$.properties} does not set this value.
						if (!sysMap.containsKey("maxStatementsPerConnection")) {
							Any any = new Any();
							any.setName("maxStatementsPerConnection");
							any.setValue("100");

							dsConfig.getProperties().add(any);
						}
					}
				}
			}
		}

		@Override
		public void visitGroupDataSourceConfig(GroupDataSourceConfig groupDsConfig) {
			String config = configService.getProperty(getGroupDataSourceKeyForApp());

			if (StringUtils.isBlank(config)) {
				config = configService.getProperty(getGroupDataSourceKey());
			}

			if (StringUtils.isNotBlank(config)) {
				GroupConfig groupConfig = JaxbUtils.fromXml(config, GroupConfig.class);

				if (groupConfig != null && groupConfig.getSingleConfigs() != null) {
					for (SingleConfig singleConfig : groupConfig.getSingleConfigs()) {
						singleConfig.checkConfig();

						if (singleConfig.getReadWeight() >= 0) {
							ReadOrWriteRole role = new ReadOrWriteRole(singleConfig.getName(), true, false);
							DataSourceConfig dataSource = groupDsConfig
							      .findOrCreateDataSourceConfig(role);
							dataSource.setCanRead(true);
							dataSource.setWeight(singleConfig.getReadWeight());
							visitDataSourceConfig(dataSource);
						}

						if (singleConfig.getWriteWeight() >= 0) {
							ReadOrWriteRole role = new ReadOrWriteRole(singleConfig.getName(), false, true);
							DataSourceConfig dataSource = groupDsConfig
							      .findOrCreateDataSourceConfig(role);
							dataSource.setCanWrite(true);
							dataSource.setWeight(singleConfig.getWriteWeight());
							visitDataSourceConfig(dataSource);
						}
					}
				}

				validateConfig(groupDsConfig.getDataSourceConfigs());
			}

			groupDsConfig.setFilters(
			      getProperty(String.format("%s.default.filters", Constants.DEFAULT_DATASOURCE_ZEBRA_PRFIX), null));

			// 初始化路由策略, 默认为CenterAwareRouter
			groupDsConfig.setRouterStrategy(getProperty(String.format(Constants.ROUTER_STRATEGY_LION_KEY_PATTERN, jdbcRef),
			      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER));
		}
	}
}
