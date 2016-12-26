package com.dianping.zebra.config;

import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraConfigException;

public class ConfigServiceFactory {
	
	public static ConfigService getConfigService(String configManagerType, String name) {
		if (Constants.CONFIG_MANAGER_TYPE_LOCAL.equalsIgnoreCase(configManagerType)) {
			PropertyConfigService configService = new PropertyConfigService(name);
			configService.init();

			return configService;
		} else if (Constants.CONFIG_MANAGER_TYPE_REMOTE.equalsIgnoreCase(configManagerType)) {
			return LionConfigService.getInstance();
		} else {
			throw new ZebraConfigException(String.format("illegal configServiceType[%s]", configManagerType));
		}
	}
}
