package com.dianping.zebra.group.config;

import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;

public final class DataSourceConfigManagerFactory {

	private DataSourceConfigManagerFactory() {
	}

	public static DataSourceConfigManager getConfigManager(String configManagerType, String name) {
		ConfigService configService = ConfigServiceFactory.getConfigService(configManagerType, name);
		DataSourceConfigManager dataSourceConfigManager =  new DefaultDataSourceConfigManager(name, configService);

		dataSourceConfigManager.init();

		return dataSourceConfigManager;
	}
}
