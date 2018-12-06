package com.dianping.zebra.group.router;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ServiceConfigBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;
import com.dianping.zebra.group.router.region.LocalRegionManager;

public class RegionAwareRouterTest {

	private DataSourceConfigManager dataSourceConfigManager;

	private DataSourceRouter dataSourceRouter;

	@Before
	public void init() {
		String dataSourceResourceId = "sample.ds.router";
		Map<String, Object> confgis = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, dataSourceResourceId).getConfigs();
		this.dataSourceConfigManager = DataSourceConfigManagerFactory
		      .getConfigManager(Constants.CONFIG_MANAGER_TYPE_LOCAL, confgis);
	}

	@Test
	public void testInOneRegion() {
		((LocalRegionManager) LocalRegionManager.getInstance()).setLocalAddress("192.3.1.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(),
		      Constants.CONFIG_MANAGER_TYPE_LOCAL, Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);

		Set<String> sets = new HashSet<String>();

		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(3, sets.size());
		Assert.assertEquals(true, sets.contains("db-n3-read"));
		Assert.assertEquals(true, sets.contains("db-n4-read"));
		Assert.assertEquals(true, sets.contains("db-n2-read"));
	}

	@Test
	public void testInOneRegion2() {
		((LocalRegionManager) LocalRegionManager.getInstance()).setLocalAddress("192.11.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);

		Set<String> sets = new HashSet<String>();

		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(3, sets.size());
		Assert.assertEquals(true, sets.contains("db-n6-read"));
		Assert.assertEquals(true, sets.contains("db-n7-read"));
		Assert.assertEquals(true, sets.contains("db-n8-read"));
	}

	@Test
	public void testCenter1() {
		LocalRegionManager manager = (LocalRegionManager) LocalRegionManager.getInstance();
		Set<String> sh = new HashSet<String>();
		sh.add("db-n2-read");
		sh.add("db-n3-read");
		sh.add("db-n4-read");
		Set<String> sets = new HashSet<String>();

		manager.setLocalAddress("192.2.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);

		manager.setLocalAddress("192.1.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);
		manager.setLocalAddress("192.3.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);

		manager.setLocalAddress("192.4.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);
		manager.setLocalAddress("192.4.5.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);
	}

	@Test
	public void testRegion1IdcAware() {
		LocalRegionManager manager = (LocalRegionManager) LocalRegionManager.getInstance();
		Set<String> sh = new HashSet<String>();
		sh.add("db-n2-read");
		Set<String> sets = new HashSet<String>();

		manager.setLocalAddress("192.1.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);

		manager.setLocalAddress("192.2.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);
		manager.setLocalAddress("192.2.35.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);

		manager.setLocalAddress("192.4.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		for (int i = 0; i < 10000; i++) {
			Assert.assertEquals("db-n3-read", this.dataSourceRouter.select(new RouterContext()).getId());
		}
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		for (int i = 0; i < 10000; i++) {
			Assert.assertEquals("db-n3-read", this.dataSourceRouter.select(new RouterContext()).getId());
		}
	}

	@Test
	public void testRegion1() {
		LocalRegionManager manager = (LocalRegionManager) LocalRegionManager.getInstance();
		Set<String> sh = new HashSet<String>();
		sh.add("db-n2-read");
		sh.add("db-n3-read");
		sh.add("db-n4-read");
		sh.add("db-n5-read");
		Set<String> sets = new HashSet<String>();

		manager.setLocalAddress("192.6.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);

		manager.setLocalAddress("192.1.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);
		manager.setLocalAddress("192.2.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);

		manager.setLocalAddress("192.3.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);
		manager.setLocalAddress("192.6.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(sh, sets);
	}

	@Test
	public void testCenter3() {
		LocalRegionManager manager = (LocalRegionManager) LocalRegionManager.getInstance();
		Set<String> c1 = new HashSet<String>();
		c1.add("db-n7-read");
		c1.add("db-n6-read");
		c1.add("db-n8-read");
		Set<String> sets = new HashSet<String>();

		// RZ
		manager.setLocalAddress("192.11.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);

		manager.setLocalAddress("192.21.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);
		manager.setLocalAddress("192.21.45.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);

		manager.setLocalAddress("192.20.67.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_CENTER_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);

	}

	@Test
	public void testRegion2IdcAware() {
		LocalRegionManager manager = (LocalRegionManager) LocalRegionManager.getInstance();
		Set<String> c1 = new HashSet<String>();
		c1.add("db-n6-read");
		Set<String> sets = new HashSet<String>();

		Set<String> center2 = new LinkedHashSet<>();
		center2.add("db-n7-read");
		center2.add("db-n8-read");

		manager.setLocalAddress("192.11.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);

		manager.setLocalAddress("192.10.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			Assert.assertEquals("db-n6-read", this.dataSourceRouter.select(new RouterContext()).getId());
		}
		manager.setLocalAddress("192.11.35.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			Assert.assertEquals("db-n6-read", this.dataSourceRouter.select(new RouterContext()).getId());
		}

		manager.setLocalAddress("192.20.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		for (int i = 0; i < 10000; i++) {
			Assert.assertTrue(center2.contains(this.dataSourceRouter.select(new RouterContext()).getId()));
		}
		manager.setLocalAddress("192.20.5.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		for (int i = 0; i < 10000; i++) {
			Assert.assertTrue(center2.contains(this.dataSourceRouter.select(new RouterContext()).getId()));
		}

		manager.setLocalAddress("192.21.3.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		for (int i = 0; i < 10000; i++) {
			Assert.assertTrue(center2.contains(this.dataSourceRouter.select(new RouterContext()).getId()));
		}
		manager.setLocalAddress("192.21.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_IDC_AWARE_ROUTER);
		for (int i = 0; i < 10000; i++) {
			Assert.assertTrue(center2.contains(this.dataSourceRouter.select(new RouterContext()).getId()));
		}
	}

	@Test
	public void testRegion2() {
		LocalRegionManager manager = (LocalRegionManager) LocalRegionManager.getInstance();
		Set<String> c1 = new HashSet<String>();
		c1.add("db-n6-read");
		c1.add("db-n7-read");
		c1.add("db-n8-read");
		Set<String> sets = new HashSet<String>();

		manager.setLocalAddress("192.11.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);

		manager.setLocalAddress("192.10.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);

		manager.setLocalAddress("192.20.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);

		manager.setLocalAddress("192.21.4.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);
		manager.setLocalAddress("192.10.6.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);

		manager.setLocalAddress("192.20.6.1");
		this.dataSourceRouter = new RegionAwareRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local",
		      Constants.ROUTER_STRATEGY_REGION_AWARE_ROUTER);
		sets.clear();
		for (int i = 0; i < 10000; i++) {
			RouterTarget routerTarget = this.dataSourceRouter.select(new RouterContext());
			sets.add(routerTarget.getId());
		}
		Assert.assertEquals(c1, sets);
	}

}
