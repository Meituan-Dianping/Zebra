package com.dianping.zebra.shard;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.zebra.shard.config.XmlDataSourceRouterConfigLoaderTest;
import com.dianping.zebra.shard.jdbc.MultiDBLifeCycleTest;
import com.dianping.zebra.shard.jdbc.MultiDBPreparedStatementLifeCycleTest;
import com.dianping.zebra.shard.jdbc.ResultSetTest;
import com.dianping.zebra.shard.jdbc.ShardConnectionTest;
import com.dianping.zebra.shard.jdbc.ShardPreparedStatementMultiKeyTest;
import com.dianping.zebra.shard.jdbc.ShardPreparedStatementTest;
import com.dianping.zebra.shard.jdbc.ShardStatementTest;
import com.dianping.zebra.shard.jdbc.ShardSupportedCaseTest;
import com.dianping.zebra.shard.jdbc.SingleDBLifeCycleTest;
import com.dianping.zebra.shard.jdbc.SingleDBPreparedStatementGroupFollowNoteIntegrationTest;
import com.dianping.zebra.shard.jdbc.SingleDBPreparedStatementLifeCycleTest;
import com.dianping.zebra.shard.jdbc.SpecialSQLTest;
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
import com.dianping.zebra.shard.util.ShardColumnValueUtilTest;

@RunWith(Suite.class)
@SuiteClasses({
        //config
        XmlDataSourceRouterConfigLoaderTest.class,

        //jdbc
        DataSourceTest.class,
        ConnectionTest.class,
        StatementTest.class,
        
        MultiDBLifeCycleTest.class,
        MultiDBPreparedStatementLifeCycleTest.class,
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
        
        //util
        ShardColumnValueUtilTest.class
})
public class AllTests {

}
