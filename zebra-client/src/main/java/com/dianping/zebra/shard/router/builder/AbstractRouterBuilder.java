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

import java.util.*;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.shard.config.ExceptionalDimensionConfig;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.dianping.zebra.shard.router.RouterBuilder;
import com.dianping.zebra.shard.router.rule.TableShardRule;
import com.dianping.zebra.shard.router.rule.dimension.DefaultDimensionRule;
import com.dianping.zebra.shard.router.rule.dimension.DimensionRule;
import com.dianping.zebra.shard.router.rule.dimension.ExceptionalDimensionRule;
import com.dianping.zebra.shard.router.rule.RouterRule;

public abstract class AbstractRouterBuilder implements RouterBuilder {

	protected RouterRule build(RouterRuleConfig routerConfig, boolean forbidNoShardKeyWrite) {
		RouterRule routerRule = new RouterRule();

		Map<String, TableShardRule> tableShardRules = new HashMap<String, TableShardRule>();
		for (TableShardRuleConfig ruleConfig : routerConfig.getTableShardConfigs()) {
			List<TableShardDimensionConfig> dimensionConfigs = ruleConfig.getDimensionConfigs();
			if (dimensionConfigs != null && !dimensionConfigs.isEmpty()) {
				TableShardRule shardRule = new TableShardRule(ruleConfig.getTableName(), forbidNoShardKeyWrite);
				arrangeDimensionConfigs(dimensionConfigs);
				for (TableShardDimensionConfig dimensionConfig : dimensionConfigs) {
					shardRule.addDimensionRule(buildDimensionRule(dimensionConfig));
				}
				checkDimensionShardKey(shardRule);
				tableShardRules.put(ruleConfig.getTableName(), shardRule);
			}
		}
		routerRule.setTableShardRules(tableShardRules);

		return routerRule;
	}

	protected RouterRule build(RouterRuleConfig routerConfig) {
		return build(routerConfig, false);
	}

	/**
	 * 检查table shard rule, 查看是否存在包含相同shard key set的dimension rule
	 */
	private void checkDimensionShardKey(TableShardRule tableShardRule) {
		List<DimensionRule> dimensionRules = tableShardRule.getDimensionRules();
		if(dimensionRules != null) {
			Set<Set<String>> shardKeySets = new HashSet<Set<String>>();
			for(DimensionRule dimensionRule : dimensionRules) {
				Set<String> shardKey = dimensionRule.getShardColumns();
				if(shardKeySets.contains(shardKey)) {
					throw new ZebraConfigException(tableShardRule.getTableName()+" contains two dimension with same shard key set!");
				} else {
					shardKeySets.add(shardKey);
				}
			}
		}
	}

	private void arrangeDimensionConfigs(List<TableShardDimensionConfig> dimensionConfigs) {
		if (dimensionConfigs.size() == 1) {
			dimensionConfigs.get(0).setMaster(true);
		}

		Collections.sort(dimensionConfigs, new Comparator<TableShardDimensionConfig>() {
			@Override
			public int compare(TableShardDimensionConfig o1, TableShardDimensionConfig o2) {
				return Boolean.valueOf(o2.isMaster()).compareTo(Boolean.valueOf(o1.isMaster()));
			}
		});
	}

	private DimensionRule buildDimensionRule(TableShardDimensionConfig dimensionConfig) {
		DefaultDimensionRule rule = new DefaultDimensionRule(dimensionConfig);
		rule.setExceptionalRules(
				buildExceptionalRules(dimensionConfig.getExceptionalDimensionConfig(), dimensionConfig.isMaster()));

		return rule;
	}

	private List<DimensionRule> buildExceptionalRules(List<ExceptionalDimensionConfig> exceptions, boolean isMaster) {
		if (exceptions == null || exceptions.isEmpty()) {
			return Collections.emptyList();
		}
		List<DimensionRule> rules = new ArrayList<DimensionRule>();
		for (ExceptionalDimensionConfig exception : exceptions) {
			ExceptionalDimensionRule rule = new ExceptionalDimensionRule();
			rule.init(exception);
			rule.setMaster(isMaster);
			rules.add(rule);
		}

		return rules;
	}
}
