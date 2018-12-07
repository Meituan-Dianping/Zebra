package com.dianping.zebra.group.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.config.ServiceConfigBuilder;
import org.junit.Test;

public class ConfigManagerTest {

	@Test
	public void testManager() throws IOException, InterruptedException {
		String resourceName = "sample.ds.v2";
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, resourceName).getConfigs();
		ConfigService configService = ConfigServiceFactory.getConfigService(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
		SystemConfigManager systemConfigManager = SystemConfigManagerFactory.getConfigManger(Constants.CONFIG_MANAGER_TYPE_LOCAL, configService);

		System.out.println(systemConfigManager.getSystemConfig());

		systemConfigManager.addListerner(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println(String.format("Property %s changed from [%s] to [%s]", evt.getPropertyName(),
				      evt.getOldValue(), evt.getNewValue()));
			}
		});

		DataSourceConfigManager dataSourceConfigManager = DataSourceConfigManagerFactory
		      .getConfigManager(resourceName, configService);

		System.out.println(dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs());
		dataSourceConfigManager.addListerner(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println(String.format("Property %s changed from [%s] to [%s]", evt.getPropertyName(),
				      evt.getOldValue(), evt.getNewValue()));
			}
		});

		System.in.read();
	}
}
