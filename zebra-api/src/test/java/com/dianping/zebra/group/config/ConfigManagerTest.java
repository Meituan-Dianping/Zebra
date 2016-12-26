package com.dianping.zebra.group.config;

import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class ConfigManagerTest {

	@Test
	public void testManager() throws IOException, InterruptedException {
		SystemConfigManager systemConfigManager = SystemConfigManagerFactory.getConfigManger("local");

		System.out.println(systemConfigManager.getSystemConfig());

		systemConfigManager.addListerner(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println(String.format("Property %s changed from [%s] to [%s]", evt.getPropertyName(),
				      evt.getOldValue(), evt.getNewValue()));
			}
		});

		DataSourceConfigManager dataSourceConfigManager = DataSourceConfigManagerFactory.getConfigManager("local",
		      "sample.ds.v2");

		System.out.println(dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs());
		dataSourceConfigManager.addListerner(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println(String.format("Property %s changed from [%s] to [%s]", evt.getPropertyName(),
				      evt.getOldValue(), evt.getNewValue()));
			}
		});

//		System.in.read();
	}
}
