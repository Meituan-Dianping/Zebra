package com.dianping.zebra.group.router;


import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ServiceConfigBuilder;
import com.dianping.zebra.group.router.region.LocalRegionManager;
import org.junit.Before;
import org.junit.Test;

import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;

import junit.framework.Assert;

import java.util.Map;

public class IdcAwareRouterTest {

    private DataSourceConfigManager dataSourceConfigManager;

    private DataSourceRouter dataSourceRouter;

    @Before
    public void init() {
        String dataSourceResourceId = "sample.ds.router";
        Map<String, Object> configs = ServiceConfigBuilder.newInstance().putValue(Constants.CONFIG_SERVICE_NAME_KEY, dataSourceResourceId).getConfigs();
        this.dataSourceConfigManager = DataSourceConfigManagerFactory.getConfigManager(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
    }

    @Test
    public void testInOneDataCenter() {
        ((LocalRegionManager)LocalRegionManager.getInstance()).setLocalAddress("192.3.1.1");
        this.dataSourceRouter = new IdcAwareRouter(
                dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), null,"local");

        RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
        Assert.assertEquals("db-n3-read", routerTarget.getId());
    }

    @Test
    public void testInOneDataCenter1() {
        ((LocalRegionManager)LocalRegionManager.getInstance()).setLocalAddress("192.6.1.1");
        this.dataSourceRouter = new IdcAwareRouter(
                dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), null,"local");

        RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
        Assert.assertEquals("db-n5-read", routerTarget.getId());
    }

    @Test
    public void testInOneDataCenter2() {
        ((LocalRegionManager)LocalRegionManager.getInstance()).setLocalAddress("192.10.1.131");
        this.dataSourceRouter = new IdcAwareRouter(
                dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), null,"local");

        RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
        Assert.assertEquals("db-n6-read", routerTarget.getId());
    }

    @Test
    public void testInOneDataCenter5() {
        ((LocalRegionManager)LocalRegionManager.getInstance()).setLocalAddress("192.2.1.1");
        this.dataSourceRouter = new IdcAwareRouter(
                dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), null,"local");

        for (int i = 0; i < 100; i++) {
            RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
            Assert.assertTrue((("db-n2-read".equalsIgnoreCase(routerTarget.getId())) ||
                    ("db-n1-read".equalsIgnoreCase(routerTarget.getId()))));
        }
    }
}
