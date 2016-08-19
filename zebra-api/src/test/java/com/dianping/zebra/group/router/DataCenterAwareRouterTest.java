package com.dianping.zebra.group.router;

import org.junit.Before;
import org.junit.Test;

import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;
import com.dianping.zebra.group.config.SystemConfigManagerFactory;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

import junit.framework.Assert;

public class DataCenterAwareRouterTest {

	private DataSourceConfigManager dataSourceConfigManager;

	private DataSourceRouter dataSourceRouter;

	private SystemConfig sytemConfig;

	@Before
	public void init() {
		String dataSourceResourceId = "sample.ds.router";
		String configManagerType = "local";
		this.dataSourceConfigManager = DataSourceConfigManagerFactory.getConfigManager(configManagerType,
				dataSourceResourceId);
		this.sytemConfig = SystemConfigManagerFactory.getConfigManger(configManagerType).getSystemConfig();
	}

	@Test
	public void testInOneDataCenter() {
		this.dataSourceRouter = new DataCenterAwareRouter(
				dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), sytemConfig.getDataCenters(),
				"192.168.1.1");

		RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
		Assert.assertEquals("db3", routerTarget.getId());
	}

	@Test
	public void testInAnotherDataCenter() {
		this.dataSourceRouter = new DataCenterAwareRouter(
				dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), sytemConfig.getDataCenters(),
				"10.2.1.1");

		for (int i = 0; i < 100; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			System.out.println(routerTarget.getId());
			Assert.assertTrue(
					("db2".equalsIgnoreCase(routerTarget.getId())));
		}
	}

	@Test
	public void testInNoneDataCenter() {
		this.dataSourceRouter = new DataCenterAwareRouter(
				dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), sytemConfig.getDataCenters(),
				"10.3.1.1");

		for (int i = 0; i < 100; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			System.out.println(routerTarget.getId());
			Assert.assertTrue(("db2".equalsIgnoreCase(routerTarget.getId()) || "db3".equalsIgnoreCase(routerTarget.getId())));
		}
	}
}
