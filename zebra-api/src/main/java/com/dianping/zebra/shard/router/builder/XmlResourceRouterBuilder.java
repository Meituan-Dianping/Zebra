/**
 * Project: com.dianping.zebra.zebra-client-0.1.0
 *
 * File Created at 2011-6-13
 * $Id$
 *
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
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

    private XmlDataSourceRouterConfigLoader routerConfigLoader = new XmlDataSourceRouterConfigLoader();

    public XmlResourceRouterBuilder(String routerRuleFile) {
        this.routerRuleFile = routerRuleFile;
    }

    /**
     * @param routerConfigLoader the routerConfigLoader to set
     */
    public void setRouterConfigLoader(XmlDataSourceRouterConfigLoader routerConfigLoader) {
        this.routerConfigLoader = routerConfigLoader;
    }

    @Override
    public ShardRouter build() {
    	RouterRuleConfig routerConfig = routerConfigLoader.loadConfig(routerRuleFile);
    	RouterRule routerRule = build(routerConfig);

    	return new DefaultShardRouter(routerRule);
    }
}
