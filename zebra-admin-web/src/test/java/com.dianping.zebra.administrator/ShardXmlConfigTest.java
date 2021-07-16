package com.dianping.zebra.administrator;

import com.dianping.zebra.administrator.util.JaxbUtils;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.dianping.zebra.shard.router.builder.XmlDataSourceRouterConfigLoader;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * @author tong.xin on 2021/7/16.
 */
public class ShardXmlConfigTest {

    @Test
    public void testSaveShardRule() {
        List<TableShardDimensionConfig> tableDimensions = new ArrayList<>();
        TableShardDimensionConfig tableShardDimensionConfig = new TableShardDimensionConfig();
        tableShardDimensionConfig.setTableName("test_table");
        tableShardDimensionConfig.setTbSuffixZeroPadding(false);
        tableShardDimensionConfig.setTbSuffix("alldb:[_0,_7]");
        tableShardDimensionConfig.setMaster(true);
        tableShardDimensionConfig.setTbRule("#id#.intValue() % 2");
        tableShardDimensionConfig.setDbRule("(#id#.intValue() % 8).intdiv(2)");
        tableShardDimensionConfig.setDbIndexes("id0,id1,id2,id3");
        tableDimensions.add(tableShardDimensionConfig);


        List<TableShardRuleConfig> tableShardRuleConfigs = new ArrayList<>();
        TableShardRuleConfig tableShardRuleConfig = new TableShardRuleConfig();
        tableShardRuleConfig.setTableName("test_table");
        tableShardRuleConfig.setDimensionConfigs(tableDimensions);
        tableShardRuleConfigs.add(tableShardRuleConfig);

        RouterRuleConfig config = new RouterRuleConfig();
        config.setTableShardConfigs(tableShardRuleConfigs);

        byte[] bytes = JaxbUtils.jaxbWriteXml(RouterRuleConfig.class, config);

        String res = new String(bytes);
        System.out.println(res);
    }
}
