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

import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.router.DefaultShardRouter;
import com.dianping.zebra.shard.router.RouterBuilder;
import com.dianping.zebra.shard.router.ShardRouter;
import com.dianping.zebra.shard.router.rule.RouterRule;

/**
 * @author danson.liu
 */
public class XmlResourceRouterBuilder extends AbstractRouterBuilder implements RouterBuilder {

	private final String routerRuleFile;

	private String defaultDatasource;

	private boolean forbidNoShardKeyWrite;

	private XmlDataSourceRouterConfigLoader routerConfigLoader = new XmlDataSourceRouterConfigLoader();

	public XmlResourceRouterBuilder(String routerRuleFile) {
		this.routerRuleFile = routerRuleFile;
	}

	public XmlResourceRouterBuilder(String routerRuleFile, String defaultDatasource) {
		this.routerRuleFile = routerRuleFile;
		this.defaultDatasource = defaultDatasource;
	}

	public XmlResourceRouterBuilder(String routerRuleFile, String defaultDatasource, boolean forbidNoShardKeyWrite) {
		this.routerRuleFile = routerRuleFile;
		this.defaultDatasource = defaultDatasource;
		this.forbidNoShardKeyWrite = forbidNoShardKeyWrite;
	}

	/**
	 * @param routerConfigLoader
	 *           the routerConfigLoader to set
	 */
	public void setRouterConfigLoader(XmlDataSourceRouterConfigLoader routerConfigLoader) {
		this.routerConfigLoader = routerConfigLoader;
	}

	@Override
	public ShardRouter build() {
		RouterRuleConfig routerConfig = routerConfigLoader.loadConfig(routerRuleFile);
		RouterRule routerRule = build(routerConfig, forbidNoShardKeyWrite);

		return new DefaultShardRouter(routerRule, defaultDatasource);
	}
}
