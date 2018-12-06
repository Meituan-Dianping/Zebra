package com.dianping.zebra.group;

import com.dianping.zebra.config.LocalConfigServiceTest;
import com.dianping.zebra.filter.DefaultFilterManagerTest;
import com.dianping.zebra.filter.FilterChainTest;
import com.dianping.zebra.filter.mock.FinalSqlTest;
import com.dianping.zebra.filter.wall.SqlFlowIdGenerator;
import com.dianping.zebra.filter.wall.WallFilterTest;
import com.dianping.zebra.group.config.DataSourceConfigManagerTest;
import com.dianping.zebra.group.config.HidePasswordVisitorTest;
import com.dianping.zebra.group.config.SystemConfigManagerTest;
import com.dianping.zebra.group.datasources.FailoverDataSourceTest;
import com.dianping.zebra.group.datasources.LoadBalancedDataSourceTest;
import com.dianping.zebra.group.jdbc.*;
import com.dianping.zebra.group.router.*;
import com.dianping.zebra.group.router.region.LocalRegionManagerTest;
import com.dianping.zebra.group.util.AppPropertiesUtilsTest;
import com.dianping.zebra.group.util.SmoothReloadTest;
import com.dianping.zebra.group.util.SqlUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({

		//config
		DataSourceConfigManagerTest.class,
		LocalConfigServiceTest.class,
		SystemConfigManagerTest.class,
		HidePasswordVisitorTest.class,

		//datasources
		FailoverDataSourceTest.class,
		LoadBalancedDataSourceTest.class,

		//filter
		DefaultFilterManagerTest.class,
		FilterChainTest.class,
		WallFilterTest.class,
		SqlFlowIdGenerator.class,
		FinalSqlTest.class,

		//jdbc
		GroupDataSourceC3P0FieldTest.class,
		DPGroupConnectionTestCase.class,
		DPGroupPreparedStatementTest.class,
		DPGroupStatementTest.class,
		GroupDataSourceTest.class,

		//router
		CustomizedReadWriteStrategyWrapperTest.class,
		LocalContextReadWriteStrategyTest.class,
		GroupDataSourceRouterTest.class,
		ReadWriteStrategyServiceLoaderTest.class,
		IdcAwareRouterTest.class,
		LocalRegionManagerTest.class,
		RegionAwareRouterTest.class,
		CenterAwareRouterTest.class,

		//util
		AppPropertiesUtilsTest.class,
		SmoothReloadTest.class,
		SqlUtilsTest.class
})
public class AllTests {

}
