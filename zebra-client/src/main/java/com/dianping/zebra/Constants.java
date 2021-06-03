/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra;

public final class Constants {
	public static final String ZEBRA_VERSION = "2.9.2-SNAPSHOT";

	public static final String SQL_FORCE_WRITE_HINT = "/*+zebra:w*/";

	public static final String SQL_HINT_FORMAT = "/\\*\\+zebra:(?)+\\*/";

	public static final String CONNECTION_POOL_TYPE_C3P0 = "c3p0";

	public static final String CONNECTION_POOL_TYPE_TOMCAT_JDBC = "tomcat-jdbc";

	public static final String CONNECTION_POOL_TYPE_DRUID = "druid";

	public static final String CONNECTION_POOL_TYPE_DBCP2 = "dbcp2";

	public static final String CONNECTION_POOL_TYPE_DBCP = "dbcp";

	public static final String CONNECTION_POOL_TYPE_HIKARICP = "hikaricp";

	public static final String CONFIG_MANAGER_TYPE_REMOTE = "demo";

	public static final String CONFIG_MANAGER_TYPE_LOCAL = "local";

	public static final String DEFAULT_DATASOURCE_ZEBRA_PRFIX = "zebra";

	public static final String DEFAULT_GROUP_DATASOURCE_PRFIX = "zebra.group";

	public static final String DEFAULT_GROUP_SECURITY_PRFIX = "zebra.group-security";

	public static final String DEFAULT_DATASOURCE_ZEBRA_SQL_BLACKLIST_PRFIX = "zebra.sql-blacklist";

	public static final String DEFAULT_SHARDING_PRFIX = "zebra.shard";

	public static final String DEFAULT_ZEBRA_FILTER_PRFIX = "zebra.filter.";

	public static final String DEFAULT_DATASOURCE_SINGLE_PRFIX = "zebra.ds";

	public static final String DEFAULT_CHARSET = "UTF-8";

	// GroupDataSource
	public static final String SPRING_PROPERTY_FILTER = "filter";

	public static final String SPRING_PROPERTY_EXTRA_JDBC_URL_PARAMS = "extraJdbcUrlParams";

	// System
	public static final String ELEMENT_RETRY_TIMES = "retryTimes";

	public static final String ELEMENT_FLOW_CONTROL = "flowControl";

	//config service
	public static final String CONFIG_SERVICE_NAME_KEY = "name";

	// router
	public static final String ROUTER_STRATEGY_ROUNDROBIN = "roundrobin";

	public static final String ROUTER_STRATEGY_PREFIX = "zebra-router";

	public static final String ROUTER_CENTER_CONFIG_LION_KEY = "zebra-router.center.config";

	public static final String ROUTER_REGION_CONFIG_LION_KEY = "zebra-router.region.config";

	public static final String ROUTER_STRATEGY_LION_KEY_PATTERN = "zebra-router.%s.router";

	public static final String ROUTER_STRATEGY_IDC_AWARE_ROUTER = "IdcAwareRouter"; // 机房优先路由

	public static final String ROUTER_STRATEGY_CENTER_AWARE_ROUTER = "WeightRouter"; // 同中心权重路由

	public static final String ROUTER_STRATEGY_REGION_AWARE_ROUTER = "RegionAwareRouter"; // 同区域权重路由

	// app
	public static final String APP_NO_NAME = "noname";

	//zookeeper
	public static final String DEFAULT_ZK_FILENAME = "zookeeper";

	public static final String ZK_ADDR_KEY = "zkserver";

	public static final String DEFAULT_PATH = "/";

	public static final String DEFAULT_PATH_SEPARATOR = "/";

}
