package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;

/**
 * Created by wxl on 17/6/13.
 */

public class MultiShardKeyRepeatTest extends MultiDBBaseTestCase {
    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "mockdb-config/testMultiShardKey/createtable-multidb-multisk.xml";
    }

    @Override
    protected String getDataFile() {
        return "mockdb-config/testMultiShardKey/data-multidb-multisk.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] { "mockdb-config/testMultiShardKey/ctx-multidb-multisk_repeat.xml" };
    }

    public DataSource getDataSource() {
        return (DataSource) context.getBean("zebraDS");
    }


    @Test
    public void test() {
        try {
            ShardDataSource ds = (ShardDataSource)getDataSource();
            ds.init();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ZebraConfigException);
            Assert.assertEquals("Tbut contains two dimension with same shard key set!", e.getMessage());
        }
    }

}
