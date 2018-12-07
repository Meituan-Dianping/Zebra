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
package com.dianping.zebra.filter.wall;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.filter.DefaultJdbcFilter;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.filter.SQLProcessContext;
import com.dianping.zebra.group.config.SystemConfigManager;
import com.dianping.zebra.group.config.SystemConfigManagerFactory;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.util.DaoContextHolder;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.util.StringUtils;

public class WallFilter extends DefaultJdbcFilter {

	protected static final Logger logger = LoggerFactory.getLogger(WallFilter.class);

	private static final int MAX_ID_LENGTH = 8;

	private static final int MAX_CACHE_SQL_ID_LENGTH = 10000;

	private Map<String, String> sqlIDCache = new ConcurrentHashMap<String, String>(1024);

	private Map<String, SqlFlowControl> flowControl;

	private SystemConfigManager systemConfigManager;

	private Random random;

	protected void checkFlowControl(String id) throws SQLException {
		if (StringUtils.isNotBlank(id) && flowControl.containsKey(id)) {
			if (generateFlowPercent() >= flowControl.get(id).getAllowPercent()) {
				throw new SQLException("The SQL is in the blacklist. Please contact with dba!", "SQL.Blacklist");
			}
		}
	}

	protected int generateFlowPercent() {
		return random.nextInt(100);
	}

	protected String generateId(String dsId, String sqlAlias) throws NoSuchAlgorithmException {
		String token = String.format("/*%s*/%s", dsId, sqlAlias);
		String resultId = sqlIDCache.get(token);

		if (resultId != null) {
			return resultId;
		} else {
			resultId = StringUtils.md5(token).substring(0, MAX_ID_LENGTH);

			if (sqlIDCache.size() <= MAX_CACHE_SQL_ID_LENGTH) {
				sqlIDCache.put(token, resultId);
			}

			return resultId;
		}
	}

	public int getOrder() {
		return MIN_ORDER;
	}

	@Override
	public void init() {
		super.init();
		this.random = new Random();
		this.initFlowControl();
	}

	private void initFlowControl() {
		ConfigService configService = ConfigServiceFactory.getConfigService(configManagerType, configs);
		this.systemConfigManager = SystemConfigManagerFactory.getConfigManger(configManagerType, configService);
		this.flowControl = this.systemConfigManager.getSqlFlowControlMap();

		this.systemConfigManager.addListerner(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
				synchronized (flowControl) {
					flowControl = systemConfigManager.getSqlFlowControlMap();
				}
			}
		});
	}

	@Override
	public String processSQL(DataSourceConfig dsConfig, SQLProcessContext ctx, JdbcFilter chain) throws SQLException {
		String sql = chain.processSQL(dsConfig, ctx, chain);
		String sqlAlias = DaoContextHolder.getSqlName();

		if (ctx.isPreparedStmt() && dsConfig.getId() != null && StringUtils.isNotBlank(sqlAlias)) {
			try {
				String id = generateId(dsConfig.getId(), sqlAlias);

				ctx.putProperty(SQLProcessContext.PROPERTY_SQL_ID, id);
				checkFlowControl(id);

				return String.format("/*id:%s*/%s", id, sql);
			} catch (NoSuchAlgorithmException e) {
				return sql;
			}
		} else {
			return sql;
		}
	}

	public Map<String, SqlFlowControl> getFlowControl() {
		return flowControl;
	}
}