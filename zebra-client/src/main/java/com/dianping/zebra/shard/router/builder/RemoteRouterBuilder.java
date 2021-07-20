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
package com.dianping.zebra.shard.router.builder;

import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.LionKey;
import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.router.DefaultShardRouter;
import com.dianping.zebra.shard.router.RouterBuilder;
import com.dianping.zebra.shard.router.ShardRouter;
import com.dianping.zebra.shard.router.rule.RouterRule;
import com.dianping.zebra.shard.util.ShardRuleParser;
import com.dianping.zebra.util.JaxbUtils;
import com.dianping.zebra.util.StringUtils;
import com.dianping.zebra.util.json.JsonObject;

public class RemoteRouterBuilder extends AbstractRouterBuilder implements RouterBuilder {
	private final RouterRuleConfig routerConfig;

	private String defaultDatasource;

	private boolean forbidNoShardKeyWrite;

	public RemoteRouterBuilder(String ruleName, String defaultDatasource, boolean forbidNoShardKeyWrite,
	      ConfigService configService) {
		this.defaultDatasource = defaultDatasource;
		this.forbidNoShardKeyWrite = forbidNoShardKeyWrite;
		String property = configService.getProperty(LionKey.getShardConfigKey(ruleName));

		if (StringUtils.isNotBlank(property)) {
			routerConfig = JaxbUtils.fromXml(property, RouterRuleConfig.class);
		} else {
			throw new ZebraConfigException("the shardrule of [" + ruleName + "] is empty!");
		}
	}

	public RemoteRouterBuilder(String ruleName, String defaultDatasource, ConfigService configService) {
		this(ruleName, defaultDatasource, false, configService);
	}

	@Override
	public ShardRouter build() {
		RouterRule routerRule = build(routerConfig, forbidNoShardKeyWrite);
		return new DefaultShardRouter(routerRule, defaultDatasource);
	}
}
