package com.dianping.zebra.shard.router.builder;

import com.dianping.zebra.config.LionConfigService;
import com.dianping.zebra.config.LionKey;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.dianping.zebra.shard.router.DefaultShardRouter;
import com.dianping.zebra.shard.router.RouterBuilder;
import com.dianping.zebra.shard.router.ShardRouter;
import com.dianping.zebra.shard.router.rule.RouterRule;
import com.google.gson.Gson;

public class LionRouterBuilder extends AbstractRouterBuilder implements RouterBuilder {
	private final RouterRuleConfig routerConfig;

	public LionRouterBuilder(String ruleName) {
		LionConfigService configService = LionConfigService.getInstance();

		this.routerConfig = new Gson().fromJson(configService.getProperty(LionKey.getShardConfigKey(ruleName)),
		      RouterRuleConfig.class);

		if (routerConfig.getTableShardConfigs() != null) {
			for (TableShardRuleConfig tableConfig : routerConfig.getTableShardConfigs()) {
				if (tableConfig.getDimensionConfigs() != null) {
					for (TableShardDimensionConfig dimensionConfig : tableConfig.getDimensionConfigs()) {
						dimensionConfig.setTableName(tableConfig.getTableName());
					}
				}
			}
		}
	}

	@Override
	public ShardRouter build() {
		RouterRule routerRule = build(routerConfig);
		return new DefaultShardRouter(routerRule);
	}
}
