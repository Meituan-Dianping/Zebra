package com.dianping.zebra.group.router;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;
import com.dianping.zebra.group.config.SystemConfigManagerFactory;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

import junit.framework.Assert;

public class GroupDataSourceRouterTest {

	private Map<String, Integer> counter = new HashMap<String, Integer>();

	private DataSourceConfigManager dataSourceConfigManager;

	private DataSourceRouter dataSourceRouter;

	private SystemConfig sytemConfig;

	@Before
	public void init() {
		String dataSourceResourceId = "sample.ds.v2";
		String configManagerType = "local";
		this.dataSourceConfigManager = DataSourceConfigManagerFactory.getConfigManager(configManagerType,
				dataSourceResourceId);
		this.sytemConfig = SystemConfigManagerFactory.getConfigManger(configManagerType).getSystemConfig();
		this.dataSourceRouter = new RetryConnectDataSourceRouter(
				dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(),
				sytemConfig.getDataCenters());
	}

	@Test
	public void testReadSelect() {
		for (int i = 0; i < 1000; i++) {
			String readSql = "select * from a";
			RouterContext routerInfo = new RouterContext(readSql);
			RouterTarget target = dataSourceRouter.select(routerInfo);

			Integer integer = counter.get(target.getId());
			if (integer == null) {
				integer = 1;
			} else {
				integer++;
			}

			counter.put(target.getId(), integer);
		}

		for (Entry<String, Integer> entry : counter.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
	}

	@Test
	public void testSelectWeight0() {
		Map<String, DataSourceConfig> dataSourceConfigs = dataSourceConfigManager.getGroupDataSourceConfig()
				.getDataSourceConfigs();

		dataSourceConfigs.get("db3").setWeight(0);

		this.dataSourceRouter = new RetryConnectDataSourceRouter(dataSourceConfigs, sytemConfig.getDataCenters());
		RouterContext routerContext = new RouterContext();
		routerContext.addExcludeTarget("db2");

		RouterTarget target = dataSourceRouter.select(routerContext);
		Assert.assertEquals("db3", target.getId());
	}
}
