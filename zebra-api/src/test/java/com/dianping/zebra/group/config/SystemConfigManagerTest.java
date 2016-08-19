package com.dianping.zebra.group.config;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.zebra.group.config.system.entity.SystemConfig;

public class SystemConfigManagerTest {
	
	@Test
	public void testConfig(){
		SystemConfigManager systemConfigManager = SystemConfigManagerFactory.getConfigManger("local");
		
		SystemConfig config = systemConfigManager.getSystemConfig();
		Assert.assertEquals(2, config.getRetryTimes());
		Assert.assertEquals(2, config.getSqlFlowControls().size());
	}
}
