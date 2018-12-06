package com.dianping.zebra.shard.router.dimension;

import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.router.rule.dimension.DefaultDimensionRule;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wxl on 17/6/10.
 */

public class DefaultDimensionRuleTest {

    private Set<String> buildSkSet(String... cols) {
        Set<String> colSet = new HashSet<String>();
        for(String colName : cols) {
            colSet.add(colName);
        }
        return colSet;
    }

    @Test
    public void testInitShardColumn() {
        TableShardDimensionConfig tsdc = new TableShardDimensionConfig();
        tsdc.setDbRule("");         tsdc.setTbRule("");
        tsdc.setDbIndexes("db");    tsdc.setTbSuffix("db:[$]");
        DefaultDimensionRule rule = new DefaultDimensionRule(tsdc);

        rule.initShardColumn("#uid#%2", "#tid#%4");
        Assert.assertEquals(rule.getShardColumns(), buildSkSet("uid", "tid"));

        rule.getShardColumns().clear();
        rule.initShardColumn("#UpdateTime# == null ? SKIP : 0", "#UpdateTime# == null ? SKIP : 0");
        Assert.assertEquals(rule.getShardColumns(), buildSkSet("UpdateTime"));

        rule.getShardColumns().clear();
        rule.initShardColumn("#UserType#==0 ? ((#UnifiedCouponID#[13..16]).toInteger())%10000%4 : ((#UnifiedCouponID#[1..2]).toInteger())%100%4+4",
                "#UserType#==0 ? ((#UnifiedCouponID#[13..16]).toInteger()%10000).intdiv(4)%8 : ((#UnifiedCouponID#[1..2]).toInteger()%100).intdiv(4)%8");
        Assert.assertEquals(rule.getShardColumns(), buildSkSet("UnifiedCouponID", "UserType"));
    }
}
