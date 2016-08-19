package com.dianping.zebra.shard.router.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	protected RouterRule build(RouterRuleConfig routerConfig) {
		RouterRule routerRule = new RouterRule();

		Map<String, TableShardRule> tableShardRules = new HashMap<String, TableShardRule>();
		for (TableShardRuleConfig ruleConfig : routerConfig.getTableShardConfigs()) {
			List<TableShardDimensionConfig> dimensionConfigs = ruleConfig.getDimensionConfigs();
			if (dimensionConfigs != null && !dimensionConfigs.isEmpty()) {
				TableShardRule shardRule = new TableShardRule(ruleConfig.getTableName());
				shardRule.setGeneratedPk(ruleConfig.getGeneratedPK());
				arrangeDimensionConfigs(dimensionConfigs);
				for (TableShardDimensionConfig dimensionConfig : dimensionConfigs) {
					shardRule.addDimensionRule(buildDimensionRule(dimensionConfig));
				}
				tableShardRules.put(ruleConfig.getTableName(), shardRule);
			}
		}
		routerRule.setTableShardRules(tableShardRules);

		return routerRule;
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
