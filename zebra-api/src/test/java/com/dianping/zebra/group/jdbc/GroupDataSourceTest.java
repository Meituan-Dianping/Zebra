package com.dianping.zebra.group.jdbc;

import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.util.StringUtils;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dozer on 8/18/14.
 */
public class GroupDataSourceTest extends MultiDatabaseTestCase {
	private void assert_params(String expected, String actual) {
		expected = expected.split("\\?")[1];
		actual = actual.split("\\?")[1];

		Map<String, String> expectedMap = new HashMap<String, String>();
		Map<String, String> actualMap = new HashMap<String, String>();
		StringUtils.splitStringToMap(expectedMap, expected);
		StringUtils.splitStringToMap(actualMap, actual);

		Assert.assertEquals(expectedMap.size(), actualMap.size());

		for (Map.Entry<String, String> expectedEntity : expectedMap.entrySet()) {
			Assert.assertTrue(expectedEntity.getKey(), actualMap.containsKey(expectedEntity.getKey()));
			Assert.assertEquals(expectedEntity.getKey(), expectedEntity.getValue(),
				actualMap.get(expectedEntity.getKey()));
		}
	}

	private GroupDataSourceConfig initGroupDataSourceConfig(String url) {
		GroupDataSourceConfig groupConfig = new GroupDataSourceConfig();
		DataSourceConfig config = new DataSourceConfig();
		config.setJdbcUrl(url);
		config.setId("1");
		groupConfig.addDataSourceConfig(config);
		return groupConfig;
	}

	@Test
	public void test_group_datasource_security_check_success() {
		getDataSource();
	}
	
	@Test(expected=ZebraException.class)
	public void test_group_datasource_security_check_fail() {
		GroupDataSource ds = new GroupDataSource("sample.ds.security");
		ds.setConfigManagerType(getConfigManagerType());
		ds.init();
	}

	
	@Test
	public void test_build_group_datasource_config_filter_with_same() {
		GroupDataSourceConfig config = new GroupDataSourceConfig();
		GroupDataSource ds = new GroupDataSource();
		ds.setFilter("stat");
		config.setFilters("stat");
		ds.buildFilter(config);
		Assert.assertEquals("stat", config.getFilters());
	}

	@Test
	public void test_build_group_datasource_config_filter_from_config() {
		GroupDataSourceConfig config = new GroupDataSourceConfig();
		GroupDataSource ds = new GroupDataSource();
		config.setFilters("stat");
		ds.buildFilter(config);
		Assert.assertEquals("stat", config.getFilters());
	}

	@Test
	public void test_build_group_datasource_config_filter_with_bean() {
		GroupDataSourceConfig config = new GroupDataSourceConfig();
		GroupDataSource ds = new GroupDataSource();
		ds.setFilter("stat");
		ds.buildFilter(config);
		Assert.assertEquals("stat", config.getFilters());
	}

	@Test
	public void test_build_group_datasource_config_filter_with_combine() {
		GroupDataSourceConfig config = new GroupDataSourceConfig();
		GroupDataSource ds = new GroupDataSource();
		ds.setFilter("stat");
		config.setFilters("cat");
		ds.buildFilter(config);

		List<String> configs = Lists.newArrayList(config.getFilters().split(","));
		Assert.assertTrue(configs.size() == 2);
		Assert.assertTrue(configs.contains("cat"));
		Assert.assertTrue(configs.contains("stat"));
	}

	@Test
	public void test_build_group_datasource_config_filter_with_ignore() {
		GroupDataSourceConfig config = new GroupDataSourceConfig();
		GroupDataSource ds = new GroupDataSource();
		ds.setFilter("stat,!cat");
		config.setFilters("stat,cat");
		ds.buildFilter(config);
		Assert.assertEquals("stat", config.getFilters());
	}

	@Test
	public void test_build_group_datasource_config_filter_from_configManager() {
		GroupDataSource ds = (GroupDataSource) getDataSource();

		Assert.assertTrue(ds.getConfig().getFilters().contains("mock"));
		Assert.assertTrue(ds.getConfig().getFilters().contains("stat"));
		Assert.assertEquals(2, ds.getConfig().getFilters().split(",").length);
	}

	@Test
	public void test_build_group_datasource_config_filter_from_configManager2() {
		GroupDataSource ds = new GroupDataSource(getResourceId());
		ds.setConfigManagerType(getConfigManagerType());
		ds.setFilter("cat,!mock");
		ds.init();

		Assert.assertTrue(ds.getConfig().getFilters().contains("cat"));
		Assert.assertTrue(ds.getConfig().getFilters().contains("stat"));
		Assert.assertEquals(2, ds.getConfig().getFilters().split(",").length);
	}

	@Test
	public void test_build_group_datasource_config_with_refresh() {
		GroupDataSource ds = new GroupDataSource(getResourceId());
		ds.setConfigManagerType(getConfigManagerType());
		ds.setFilter("cat,!mock");
		ds.init();

		Assert.assertTrue(ds.getConfig().getFilters().contains("cat"));
		Assert.assertTrue(ds.getConfig().getFilters().contains("stat"));
		Assert.assertEquals(2, ds.getConfig().getFilters().split(",").length);

		// refresh
		ds.setInitialPoolSize(2);
		Assert.assertTrue(ds.getConfig().getFilters().contains("cat"));
		Assert.assertTrue(ds.getConfig().getFilters().contains("stat"));
		Assert.assertEquals(2, ds.getConfig().getFilters().split(",").length);
		Assert.assertEquals(true,
			checkConfig(ds.getConfig().getDataSourceConfigs().get("db1").getProperties(), "initialPoolSize", "2"));
	}

	@Test
	public void test_build_group_datasource_config_with_forceWriteOnLogin_with_refresh() {
		GroupDataSource ds = new GroupDataSource(getResourceId());
		ds.setConfigManagerType(getConfigManagerType());
		ds.setForceWriteOnLogin(false);
		ds.init();

		Assert.assertEquals(false, ds.getConfig().getForceWriteOnLogin());

		// refresh
		ds.setInitialPoolSize(2);
		Assert.assertTrue(ds.getConfig().getFilters().contains("mock"));
		Assert.assertTrue(ds.getConfig().getFilters().contains("stat"));
		Assert.assertEquals(2, ds.getConfig().getFilters().split(",").length);
		Assert.assertEquals(true,
			checkConfig(ds.getConfig().getDataSourceConfigs().get("db1").getProperties(), "initialPoolSize", "2"));
	}

	private boolean checkConfig(List<Any> anys, String key, String value) {
		for (Any any : anys) {
			if (any.getName().equalsIgnoreCase(key)) {
				Assert.assertEquals(value, any.getValue());
				return true;
			}
		}

		return false;
	}

	private void test_build_group_datasource_config_url_extra(GroupDataSourceConfig config, String extra)
		throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		GroupDataSource target = new GroupDataSource();
		target.setExtraJdbcUrlParams(extra);
		Method method = GroupDataSource.class.getDeclaredMethod("buildExtraJdbcUrlParams", GroupDataSourceConfig.class);
		method.setAccessible(true);
		method.invoke(target, config);
	}

	@Test
	public void test_build_group_datasource_config_url_extra_with_all()
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		GroupDataSourceConfig config = initGroupDataSourceConfig(
			"jdbc:mysql://192.168.8.44:3306/localhost-m1-write?autoReconnect=true&cc=2");
		test_build_group_datasource_config_url_extra(config, "encode=utf8&bb=1");
		assert_params("jdbc:mysql://192.168.8.44:3306/localhost-m1-write?autoReconnect=true&cc=2&encode=utf8&bb=1",
			config.getDataSourceConfigs().get("1").getJdbcUrl());
	}

	@Test
	public void test_build_group_datasource_config_url_extra_with_empty()
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		GroupDataSourceConfig config = initGroupDataSourceConfig("jdbc:mysql://192.168.8.44:3306/localhost-m1-write?");
		test_build_group_datasource_config_url_extra(config, null);
		Assert.assertEquals("jdbc:mysql://192.168.8.44:3306/localhost-m1-write?",
			config.getDataSourceConfigs().get("1").getJdbcUrl());
	}

	@Test
	public void test_build_group_datasource_config_url_extra_with_empty_extra()
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		GroupDataSourceConfig config = initGroupDataSourceConfig(
			"jdbc:mysql://192.168.8.44:3306/localhost-m1-write?autoReconnect=true&cc=2");
		test_build_group_datasource_config_url_extra(config, null);
		assert_params("jdbc:mysql://192.168.8.44:3306/localhost-m1-write?autoReconnect=true&cc=2",
			config.getDataSourceConfigs().get("1").getJdbcUrl());
	}

	@Test
	public void test_build_group_datasource_config_url_extra_with_empty_url()
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		GroupDataSourceConfig config = initGroupDataSourceConfig("jdbc:mysql://192.168.8.44:3306/localhost-m1-write");
		test_build_group_datasource_config_url_extra(config, "autoReconnect=true&cc=2");
		assert_params("jdbc:mysql://192.168.8.44:3306/localhost-m1-write?autoReconnect=true&cc=2",
			config.getDataSourceConfigs().get("1").getJdbcUrl());
	}

	@Test
	public void test_build_group_datasource_config_url_extra_with_same_key()
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		GroupDataSourceConfig config = initGroupDataSourceConfig(
			"jdbc:mysql://192.168.8.44:3306/localhost-m1-write?autoReconnect=true&cc=1");
		test_build_group_datasource_config_url_extra(config, "autoReconnect=false");
		assert_params("jdbc:mysql://192.168.8.44:3306/localhost-m1-write?autoReconnect=false&cc=1",
			config.getDataSourceConfigs().get("1").getJdbcUrl());
	}

	@Test
	public void test_override_properties_by_srping_bean()
		throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		GroupDataSourceConfig groupConfig = new GroupDataSourceConfig();
		DataSourceConfig config = new DataSourceConfig();
		config.setId("1");
		Any any = new Any();
		any.setName("maxPoolSize");
		any.setValue("1");
		config.setProperties(Lists.newArrayList(any));
		groupConfig.addDataSourceConfig(config);

		GroupDataSource target = new GroupDataSource();
		target.setMaxPoolSize(2);
		target.setMinPoolSize(1);

		Method method = GroupDataSource.class.getDeclaredMethod("mergeC3P0Properties", GroupDataSourceConfig.class);
		method.setAccessible(true);
		method.invoke(target, groupConfig);

		Assert.assertEquals("2", groupConfig.getDataSourceConfigs().get("1").getProperties().get(0).getValue());
	}

	@Override
	protected String getConfigManagerType() {
		return Constants.CONFIG_MANAGER_TYPE_LOCAL;
	}

	@Override
	protected String getResourceId() {
		return "sample.ds.v2";
	}

	@Override
	protected String getSchema() {
		return getClass().getResource("/schema.sql").getPath();
	}

	@Override
	protected DataSourceEntry[] getDataSourceEntryArray() {
		DataSourceEntry[] entries = new DataSourceEntry[3];

		DataSourceEntry entry1 = new DataSourceEntry("jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets.xml",
			true);
		DataSourceEntry entry2 = new DataSourceEntry("jdbc:h2:mem:test1;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets1.xml",
			false);
		DataSourceEntry entry3 = new DataSourceEntry("jdbc:h2:mem:test2;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets2.xml",
			false);

		entries[0] = entry1;
		entries[1] = entry2;
		entries[2] = entry3;

		return entries;
	}
}
