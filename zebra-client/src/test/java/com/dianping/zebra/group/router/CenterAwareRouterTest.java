package com.dianping.zebra.group.router;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.config.ServiceConfigBuilder;
import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;
import com.dianping.zebra.group.router.region.LocalRegionManager;
import com.dianping.zebra.group.router.region.ZebraRegionManager;
import com.dianping.zebra.group.router.region.ZebraRegionManagerLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by wxl on 17/6/19.
 */

public class CenterAwareRouterTest {
	private DataSourceConfigManager dataSourceConfigManager;

	private Set<String> center1, others;

	private ZebraRegionManager zebraRegionManager;

	@Before
	public void init() {
		String dataSourceResourceId = "sample.ds.router";
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, dataSourceResourceId).build();
		ConfigService configService = ConfigServiceFactory.getConfigService(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
		this.dataSourceConfigManager = DataSourceConfigManagerFactory
		      .getConfigManager(dataSourceResourceId, configService);
		center1 = new HashSet<String>();
		center1.add("db-n2-read");
		center1.add("db-n3-read");
		center1.add("db-n4-read");
		others = new HashSet<String>();
		others.add("db-n5-read");
		others.add("db-n6-read");
		others.add("db-n7-read");
		others.add("db-n8-read");

		zebraRegionManager = ZebraRegionManagerLoader.getRegionManager(Constants.CONFIG_MANAGER_TYPE_LOCAL,
		      configService);
		zebraRegionManager.init();
	}

	@Test
	public void testCenterRouter() {
		Set<String> sets = new HashSet<String>();
		LocalRegionManager manager = (LocalRegionManager) zebraRegionManager;

		manager.setLocalAddress("192.3.123.21");
		DataSourceRouter dataSourceRouter = new CenterAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local", null, false);
		for (int i = 0; i < 1000; ++i) {
			String target = dataSourceRouter.select(new RouterContext()).getId();
			sets.add(target);
		}
		Assert.assertEquals(center1, sets);

		manager.setLocalAddress("192.1.123.21");
		dataSourceRouter = new CenterAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local", null, false);
		sets.clear();
		for (int i = 0; i < 1000; ++i) {
			String target = dataSourceRouter.select(new RouterContext()).getId();
			sets.add(target);
		}
		Assert.assertEquals(center1, sets);

		manager.setLocalAddress("192.6.77.20");
		sets.clear();
		dataSourceRouter = new CenterAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local", null, true);
		for (int i = 0; i < 1000; ++i) {
			String target = dataSourceRouter.select(new RouterContext()).getId();
			Assert.assertEquals("db-n5-read", target);
		}

		manager.setLocalAddress("192.20.123.21");
		sets.clear();
		dataSourceRouter = new CenterAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local", null, true);
		for (int i = 0; i < 100; ++i) {
			String target = dataSourceRouter.select(new RouterContext()).getId();
			Assert.assertTrue(others.contains(target));
		}
	}
}
