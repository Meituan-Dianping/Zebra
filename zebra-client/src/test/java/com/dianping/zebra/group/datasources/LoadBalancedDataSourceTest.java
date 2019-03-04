package com.dianping.zebra.group.datasources;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.config.ServiceConfigBuilder;
import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;
import com.dianping.zebra.group.config.SystemConfigManager;
import com.dianping.zebra.group.config.SystemConfigManagerFactory;
import com.dianping.zebra.group.exception.SlaveDsDisConnectedException;
import org.junit.Test;

import java.util.Map;

public class LoadBalancedDataSourceTest {

	@Test(expected = SlaveDsDisConnectedException.class)
	public void test_init_fail_when_wrong_config() {
		String resourceName = "sample.ds.error";
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, resourceName).build();
		ConfigService configService = ConfigServiceFactory.getConfigService(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);

		DataSourceConfigManager dataSourceConfigManager = DataSourceConfigManagerFactory
		      .getConfigManager(resourceName, configService);
		SystemConfigManager systemConfigManager = SystemConfigManagerFactory.getConfigManger(Constants.CONFIG_MANAGER_TYPE_LOCAL, configService);

		LoadBalancedDataSource ds = new LoadBalancedDataSource(
		      dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), null,
		      systemConfigManager.getSystemConfig(), Constants.CONFIG_MANAGER_TYPE_LOCAL, configService, "WeightRouter");

		ds.init();
	}
}
