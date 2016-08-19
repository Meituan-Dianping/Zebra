package com.dianping.zebra.group.datasources;

import org.junit.Test;

import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;
import com.dianping.zebra.group.config.SystemConfigManager;
import com.dianping.zebra.group.config.SystemConfigManagerFactory;
import com.dianping.zebra.group.exception.SlaveDsDisConnectedException;

public class LoadBalancedDataSourceTest {
	
	@Test(expected=SlaveDsDisConnectedException.class)
	public void test_init_fail_when_wrong_config(){
		
		DataSourceConfigManager dataSourceConfigManager = DataSourceConfigManagerFactory.getConfigManager("local",
		      "sample.ds.error");
		SystemConfigManager systemConfigManager = SystemConfigManagerFactory.getConfigManger("local");

		LoadBalancedDataSource ds = new LoadBalancedDataSource(dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs(), null, systemConfigManager.getSystemConfig());

		ds.init();
	}
}
