package com.dianping.zebra.group.config;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.config.ServiceConfigBuilder;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import junit.framework.Assert;

import org.junit.Test;

import com.dianping.zebra.group.config.system.entity.SystemConfig;

import java.util.Map;

public class SystemConfigManagerTest {

	@Test
	public void testConfig() {
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, DefaultSystemConfigManager.DEFAULT_LOCAL_CONFIG).getConfigs();
		ConfigService configService = ConfigServiceFactory.getConfigService(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
		SystemConfigManager systemConfigManager = SystemConfigManagerFactory.getConfigManger(Constants.CONFIG_MANAGER_TYPE_LOCAL, configService);

		SystemConfig config = systemConfigManager.getSystemConfig();
		Assert.assertEquals(3, config.getRetryTimes());
		Assert.assertEquals(5, config.getSqlFlowControls().size());

		SystemConfig newConfig = new SystemConfig();
		newConfig.setRetryTimes(3);
		SqlFlowControl flowControl = new SqlFlowControl();
		flowControl.setSqlId("a2f3e453");
		flowControl.setApp("_global_");
		flowControl.setAllowPercent(0);
		newConfig.addSqlFlowControl(flowControl);

		flowControl = new SqlFlowControl();
		flowControl.setSqlId("a2f07094");
		flowControl.setApp("_global_");
		flowControl.setAllowPercent(0);
		newConfig.addSqlFlowControl(flowControl);

		flowControl = new SqlFlowControl();
		flowControl.setSqlId("12345678");
		flowControl.setApp("zebra_ut");
		flowControl.setAllowPercent(0);
		newConfig.addSqlFlowControl(flowControl);

		flowControl = new SqlFlowControl();
		flowControl.setSqlId("1234abcd");
		flowControl.setApp("zebra_ut");
		flowControl.setAllowPercent(0);
		newConfig.addSqlFlowControl(flowControl);

		flowControl = new SqlFlowControl();
		flowControl.setSqlId("12344321");
		flowControl.setApp("zebra_ut");
		flowControl.setAllowPercent(0);
		newConfig.addSqlFlowControl(flowControl);

		Assert.assertEquals(systemConfigManager.getSystemConfig(), newConfig);
	}
}
