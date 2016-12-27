/**
 * Project: zebra-client
 *
 * File Created at Feb 17, 2014
 *
 */
package com.dianping.zebra;

import com.dianping.zebra.util.AppPropertiesUtils;

public final class Constants {
	public static final String ZEBRA_VERSION = "2.8.5";

	public static final String SQL_FORCE_WRITE_HINT = "/*+zebra:w*/";
	
	public static final String SQL_HINT_FORMAT = "/\\*\\+zebra:(?)+\\*/";

	public static final String CONNECTION_POOL_TYPE_C3P0 = "c3p0";

	public static final String CONNECTION_POOL_TYPE_TOMCAT_JDBC = "tomcat-jdbc";
	
	public static final String CONNECTION_POOL_TYPE_DRUID = "druid";

	public static final String CONFIG_MANAGER_TYPE_REMOTE = "remote";

	public static final String CONFIG_MANAGER_TYPE_LOCAL = "local";

	public static final String DEFAULT_DATASOURCE_GROUP_PRFIX = "groupds";

	public static final String DEFAULT_DATASOURCE_ZEBRA_PRFIX = "zebra";

	public static final String DEFAULT_DATASOURCE_ZEBRA_SQL_BLACKLIST_PRFIX = "zebra-sql-blacklist";

	public static final String DEFAULT_SHARDING_PRFIX = "shardds";

	public static final String ZEBRA_FILTER_PRFIX = "zebra.filter.";

	public static final String DEFAULT_DATASOURCE_SINGLE_PRFIX = "ds";

	public static final String DEFAULT_DATASOURCE_LION_PREFIX = "data-source.";

	// GroupDataSource
	public final static String SPRING_PROPERTY_FILTER = "filter";

	public static final String SPRING_PROPERTY_FORCE_WRITE_ON_LONGIN = "forceWriteOnLogin";

	public static final String SPRING_PROPERTY_EXTRA_JDBC_URL_PARAMS = "extraJdbcUrlParams";
	
	// DataSource
	public static final String ELEMENT_APP_REFRESH_FLAG = "appkey.refresh.time";

	public static final String ELEMENT_ACTIVE = "active";

	public static final String ELEMENT_TEST_READONLY_SQL = "testReadOnlySql";

	public static final String ELEMENT_PUNISH_LIMIT = "punishLimit";

	public static final String ELEMENT_TIME_WINDOW = "timeWindow";

	public static final String ELEMENT_DRIVER_CLASS = "driverClass";

	public static final String ELEMENT_JDBC_URL = "url";

	public static final String ELEMENT_PASSWORD = "password";

	public static final String ELEMENT_WARMUP_TIME = "warmupTime";

	public static final String ELEMENT_USER = "username";

	public static final String ELEMENT_PROPERTIES = "properties";

	public static final String ELEMENT_TAG = "tag";
	
	public static final String ELEMENT_POOL_TYPE = AppPropertiesUtils.getAppName() + ".zebra.pool.type";

	// System
	public static final String ELEMENT_RETRY_TIMES = "retryTimes";

	public static final String ELEMENT_FLOW_CONTROL = "flowControl";
	
	public static final String ELEMENT_DATA_CENTER = "dataCenter";

	// router
	public static final String ROUTER_STRATEGY_ROUNDROBIN = "roundrobin";

	// phoenix
	public static final String PHOENIX_APP_NO_NAME = "noname";
}
