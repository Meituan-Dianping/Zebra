package com.dianping.zebra.group.config;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.LionConfigService;
import com.dianping.zebra.config.PropertyConfigService;
import com.dianping.zebra.exception.ZebraConfigException;

public final class SystemConfigManagerFactory {

	/**
	 * SystemConfigManagerFactory has only one instance of SystemConfigManager <br>
	 * which differs from DataSourceConfigManagerFactory who has its own
	 * DataSourceConfigManager for each GroupDataSource
	 */
	private static SystemConfigManager systemConfigManager;

	private SystemConfigManagerFactory() {
	}

	public static SystemConfigManager getConfigManger(String configManagerType) {
		if (systemConfigManager == null) {
			synchronized (SystemConfigManagerFactory.class) {
				if (systemConfigManager == null) {
					if (Constants.CONFIG_MANAGER_TYPE_LOCAL.equalsIgnoreCase(configManagerType)) {
						PropertyConfigService configService = new PropertyConfigService(
								DefaultSystemConfigManager.DEFAULT_LOCAL_CONFIG);
						configService.init();

						systemConfigManager = new DefaultSystemConfigManager(configService);
					} else if (Constants.CONFIG_MANAGER_TYPE_REMOTE.equalsIgnoreCase(configManagerType)) {
						systemConfigManager = new DefaultSystemConfigManager(LionConfigService.getInstance());
					} else {
						throw new ZebraConfigException(
								String.format("Illegal systemConfigManagerType[%s]", configManagerType));
					}
					systemConfigManager.init();
				}

			}
		}

		return systemConfigManager;
	}
}
