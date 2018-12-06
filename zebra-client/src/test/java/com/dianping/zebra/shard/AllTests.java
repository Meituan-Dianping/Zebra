package com.dianping.zebra.shard;

import com.dianping.zebra.shard.jdbc.*;
import com.dianping.zebra.shard.router.dimension.DefaultDimensionRuleTest;
import com.dianping.zebra.shard.router.rule.DefaultTableSetsManagerTest;
import com.dianping.zebra.shard.util.ShardDateParseUtilTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.zebra.shard.config.XmlDataSourceRouterConfigLoaderTest;
import com.dianping.zebra.shard.jdbc.merge.DistinctTest;
import com.dianping.zebra.shard.jdbc.merge.LimitTest;
import com.dianping.zebra.shard.jdbc.parallel.SQLThreadPoolExecutorTest;
import com.dianping.zebra.shard.jdbc.specification.ConnectionTest;
import com.dianping.zebra.shard.jdbc.specification.DataSourceTest;
import com.dianping.zebra.shard.jdbc.specification.StatementTest;
import com.dianping.zebra.shard.parser.SQLHintTest;
import com.dianping.zebra.shard.parser.SQLParserResultTest;
import com.dianping.zebra.shard.parser.SQLRewriteTest;
import com.dianping.zebra.shard.router.DataSourceRouterImplTest;
import com.dianping.zebra.shard.router.XmlDataSourceRouterFactoryTest;
import com.dianping.zebra.shard.router.rule.GroovyRuleEngineTest;
import com.dianping.zebra.shard.router.rule.SimpleDataSourceProviderTest;
import com.dianping.zebra.shard.router.rule.engine.RuleEngineBaseTest;
import com.dianping.zebra.shard.util.ShardColumnValueUtilTest;

@RunWith(Suite.class)
@SuiteClasses({
        //config
        //LionRouterTest.class,
        XmlDataSourceRouterConfigLoaderTest.class,

        //jdbc.specification
        DataSourceTest.class,
        ConnectionTest.class,
        StatementTest.class,

        //jdbc
        MultiDBLifeCycleTest.class,
        MultiDBPreparedStatementLifeCycleTest.class,
        MultiDBPreparedStatementLifeCycleGroupTest.class,
        MultiDBPreparedStatementLifeCycleLimitTest.class,
        MultiDBPreparedStatementLifeCycleOrderbyTest.class,
        ResultSetTest.class,
        SingleDBLifeCycleTest.class,
        SingleDBPreparedStatementGroupFollowNoteIntegrationTest.class,
        SingleDBPreparedStatementLifeCycleTest.class,
        SpecialSQLTest.class,
        ShardConnectionTest.class,
        ShardStatementTest.class,
        ShardPreparedStatementTest.class,
        ShardPreparedStatementMultiKeyTest.class,
        ShardSupportedCaseTest.class,
        ShardDataSourceTest.class,
        ShardRangeCrossTest.class,
        ShardRangeDefaultTest.class,
        MultiShardKeyTest.class,
        MultiShardKeyRepeatTest.class,
        ShardDefaultStrategyTest.class,
        ShardConcurrencyTest.class,

        //jdbc.parallel
        SQLThreadPoolExecutorTest.class,

        //merge
        LimitTest.class,
        DistinctTest.class,

        //parser
        SQLParserResultTest.class,
        SQLRewriteTest.class,
        SQLHintTest.class,

        //router
        DataSourceRouterImplTest.class,
        GroovyRuleEngineTest.class,
        SimpleDataSourceProviderTest.class,
        XmlDataSourceRouterFactoryTest.class,
        RuleEngineBaseTest.class,
        DefaultDimensionRuleTest.class,
        DefaultTableSetsManagerTest.class,


        //util
        ShardColumnValueUtilTest.class,
        ShardDateParseUtilTest.class
})
public class AllTests {

}
