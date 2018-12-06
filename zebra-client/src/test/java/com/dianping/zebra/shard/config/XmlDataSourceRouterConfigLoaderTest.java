package com.dianping.zebra.shard.config;

import com.dianping.zebra.shard.router.builder.XmlDataSourceRouterConfigLoader;
import org.junit.Test;

public class XmlDataSourceRouterConfigLoaderTest {

    @Test
    public void testLoadConfig() throws Exception {
        XmlDataSourceRouterConfigLoader loader = new XmlDataSourceRouterConfigLoader();
        RouterRuleConfig config = loader.loadConfig("router-rule-multidb-lifecycle.xml");

        System.out.println(config);
    }
}