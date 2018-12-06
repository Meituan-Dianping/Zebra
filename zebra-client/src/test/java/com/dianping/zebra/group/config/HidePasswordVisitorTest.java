package com.dianping.zebra.group.config;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

public class HidePasswordVisitorTest {
	@Test
	public void testHidePassword() {
		GroupDataSourceConfig groupDataSourceConfig = new GroupDataSourceConfig();

		DataSourceConfig ds = new DataSourceConfig();
		ds.setPassword("1234567");
		ds.setActive(false);
		ds.setCanRead(false);
		ds.setCanWrite(false);
		ds.setDriverClass("jdbc");
		ds.setId("ds1");
		ds.setJdbcUrl("url");
		ds.setPunishLimit(100);
		ds.setTimeWindow(100);
		ds.setUsername("username");
		ds.setWarmupTime(10);
		ds.setWeight(10);

		groupDataSourceConfig.addDataSourceConfig(ds);

		HidePasswordVisitor visitor = new HidePasswordVisitor(groupDataSourceConfig);
		groupDataSourceConfig.accept(visitor);

		Assert.assertEquals("*******", ds.getPassword());
		Assert.assertEquals(false, ds.getActive());
		Assert.assertEquals(false, ds.getCanRead());
		Assert.assertEquals(false, ds.getCanWrite());
		Assert.assertEquals("jdbc", ds.getDriverClass());
		Assert.assertEquals("ds1", ds.getId());
		Assert.assertEquals("url", ds.getJdbcUrl());
		Assert.assertEquals(100, ds.getPunishLimit());
		Assert.assertEquals(100, ds.getTimeWindow());
		Assert.assertEquals("username", ds.getUsername());
		Assert.assertEquals(10, ds.getWarmupTime());
		Assert.assertEquals(10, ds.getWeight());
	}

}
