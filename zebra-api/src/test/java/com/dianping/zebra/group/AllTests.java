package com.dianping.zebra.group;

import com.dianping.zebra.group.exception.DalStatusExtensionTest;
import com.dianping.zebra.group.jdbc.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.zebra.config.LocalConfigServiceTest;
import com.dianping.zebra.group.config.DataSourceConfigManagerTest;
import com.dianping.zebra.group.config.HidePasswordVisitorTest;
import com.dianping.zebra.group.config.SystemConfigManagerTest;
import com.dianping.zebra.group.datasources.FailoverDataSourceTest;
import com.dianping.zebra.group.datasources.LoadBalancedDataSourceTest;
import com.dianping.zebra.group.filter.DefaultFilterManagerTest;
import com.dianping.zebra.group.filter.FilterChainTest;
import com.dianping.zebra.group.filter.wall.WallFilterTest;
import com.dianping.zebra.group.router.CustomizedReadWriteStrategyWrapperTest;
import com.dianping.zebra.group.router.DataCenterAwareRouterTest;
import com.dianping.zebra.group.router.GroupDataSourceRouterTest;
import com.dianping.zebra.group.router.LocalContextReadWriteStrategyTest;
import com.dianping.zebra.group.util.AppPropertiesUtilsTest;
import com.dianping.zebra.group.util.SmoothReloadTest;
import com.dianping.zebra.group.util.SqlUtilsTest;

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

	  //exception
	  DalStatusExtensionTest.class,

	  //filter
	  DefaultFilterManagerTest.class,
	  FilterChainTest.class,
	  WallFilterTest.class,

	  //jdbc
	  DPGroupConnectionTestCase.class,
	  DPGroupPreparedStatementTest.class,
	  DPGroupStatementTest.class,
	  GroupDataSourceTest.class,
	  SingleAndGroupC3P0FieldTest.class,

	  //router
	  CustomizedReadWriteStrategyWrapperTest.class,
	  LocalContextReadWriteStrategyTest.class,
	  GroupDataSourceRouterTest.class,
	  DataCenterAwareRouterTest.class,

	  //util
	  AppPropertiesUtilsTest.class,
	  SmoothReloadTest.class,
	  SqlUtilsTest.class
})
public class AllTests {

}
