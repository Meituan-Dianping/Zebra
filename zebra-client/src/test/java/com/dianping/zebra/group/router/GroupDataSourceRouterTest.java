package com.dianping.zebra.group.router;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.config.ServiceConfigBuilder;
import org.junit.Before;
import org.junit.Test;

import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;

import junit.framework.Assert;

public class GroupDataSourceRouterTest {

	private Map<String, Integer> counter = new HashMap<String, Integer>();

	private DataSourceConfigManager dataSourceConfigManager;

	private DataSourceRouter dataSourceRouter;

	private ConfigService configService;

	@Before
	public void init() {
		String dataSourceResourceId = "sample.ds.v2";
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, dataSourceResourceId).build();
		this.configService = ConfigServiceFactory.getConfigService(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
		this.dataSourceConfigManager = DataSourceConfigManagerFactory
		      .getConfigManager(dataSourceResourceId, configService);
		this.dataSourceRouter = new BackupDataSourceRouter(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), "local", configService,
		      "WeightRouter");
	}

	@Test
	public void testPerformance() {
		long now = System.currentTimeMillis();

		for (int i = 0; i < 10000000; i++) {
			String readSql = "select * from a";
			RouterContext routerInfo = new RouterContext(readSql);
			dataSourceRouter.select(routerInfo);
		}

		System.out.println(System.currentTimeMillis() - now);

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

		dataSourceConfigs.get("db-n3-read").setWeight(0);

		this.dataSourceRouter = new BackupDataSourceRouter(dataSourceConfigs, "local", configService, "WeightRouter");
		RouterContext routerContext = new RouterContext();
		routerContext.addExcludeTarget("db-n2-read");

		RouterTarget target = dataSourceRouter.select(routerContext);
		Assert.assertEquals("db-n3-read", target.getId());
	}
}
