package com.dianping.zebra.filter.wall;

/**
 * Created by Dozer on 9/24/14.
 */

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ServiceConfigBuilder;
import com.dianping.zebra.filter.DefaultJdbcFilterChain;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.filter.SQLProcessContext;
import com.dianping.zebra.group.config.DefaultSystemConfigManager;
import org.junit.Test;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.util.DaoContextHolder;

import junit.framework.Assert;

public class WallFilterTest {

	@Test
	public void test_addId_to_Sql() throws SQLException {
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, DefaultSystemConfigManager.DEFAULT_LOCAL_CONFIG).build();
		WallFilter filter = new MockWallFilter(0);
		filter.setConfigManager(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
		filter.init();

		List<JdbcFilter> filters = new ArrayList<JdbcFilter>();
		filters.add(filter);
		DataSourceConfig config = new DataSourceConfig();
		config.setId("test-write-1");
		DaoContextHolder.setSqlName("test1");
		JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
			@Override
			public String processSQL(DataSourceConfig dsConfig, SQLProcessContext ctx, JdbcFilter chain)
			      throws SQLException {
				if (index < filters.size()) {
					return filters.get(index++).processSQL(dsConfig, ctx, chain);
				} else {
					return "select * from test";
				}
			}
		};

		Assert.assertEquals("/*id:a2f07094*/select * from test",
		      chain.processSQL(config, new SQLProcessContext(true), chain));
	}

	@Test(expected = SQLException.class)
	public void test_sql_rejected_by_flow_control() throws SQLException {
		WallFilter filter = new MockWallFilter(11);
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, DefaultSystemConfigManager.DEFAULT_LOCAL_CONFIG).build();
		filter.setConfigManager(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
		filter.init();

		List<JdbcFilter> filters = new ArrayList<JdbcFilter>();
		filters.add(filter);
		DataSourceConfig config = new DataSourceConfig();
		config.setId("test-write-1");
		DaoContextHolder.setSqlName("test");

		JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
			@Override
			public String processSQL(DataSourceConfig dsConfig, SQLProcessContext ctx, JdbcFilter chain)
			      throws SQLException {
				if (index < filters.size()) {
					return filters.get(index++).processSQL(dsConfig, ctx, chain);
				} else {
					return "select * from test";
				}
			}
		};

		filter.processSQL(config, new SQLProcessContext(true), chain);
	}

	@Test
	public void test_sql_not_reject_by_flow_control() throws SQLException {
		WallFilter filter = new MockWallFilter(9);
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
				.putValue(Constants.CONFIG_SERVICE_NAME_KEY, DefaultSystemConfigManager.DEFAULT_LOCAL_CONFIG).build();
		filter.setConfigManager(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
		filter.init();

		List<JdbcFilter> filters = new ArrayList<JdbcFilter>();
		filters.add(filter);
		DataSourceConfig config = new DataSourceConfig();
		config.setId("test-write-1");
		DaoContextHolder.setSqlName("test1");

		JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
			@Override
			public String processSQL(DataSourceConfig dsConfig, SQLProcessContext ctx, JdbcFilter chain)
			      throws SQLException {
				if (index < filters.size()) {
					return filters.get(index++).processSQL(dsConfig, ctx, chain);
				} else {
					return "select * from test";
				}
			}
		};

		filter.processSQL(config, new SQLProcessContext(true), chain);
	}

	@Test
	public void test_load_flow_control_from_config() throws SQLException {
		WallFilter filter = new MockWallFilter(0);
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, DefaultSystemConfigManager.DEFAULT_LOCAL_CONFIG).build();
		filter.setConfigManager(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
		filter.init();

		Assert.assertEquals(filter.getFlowControl().size(), 5);
		Assert.assertTrue(filter.getFlowControl().containsKey("a2f07094"));
		Assert.assertTrue(filter.getFlowControl().containsKey("a2f3e453"));
		Assert.assertTrue(filter.getFlowControl().containsKey("12345678"));
		Assert.assertTrue(filter.getFlowControl().containsKey("1234abcd"));
		Assert.assertTrue(filter.getFlowControl().containsKey("12344321"));
	}

	public class MockWallFilter extends WallFilter {
		private int flowPercent;

		public MockWallFilter(int flowPercent) {
			this.flowPercent = flowPercent;
		}

		protected int generateFlowPercent() {
			return flowPercent;
		}
	}
}
