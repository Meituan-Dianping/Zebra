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
package com.dianping.zebra.shard.config;

import com.dianping.zebra.Constants;

import java.util.Properties;

public class ShardDataSourceCustomConfig {

	private String poolType = Constants.CONNECTION_POOL_TYPE_HIKARICP;

	private Properties dsConfigProperties;

	private boolean lazyInit = true;

	private String extraJdbcUrlParams;

	private String routerStrategy;

	private String filter;

	private String routerType;

	public String getPoolType() {
		return poolType;
	}

	public void setPoolType(String poolType) {
		this.poolType = poolType;
	}

	public String getExtraJdbcUrlParams() {
		return extraJdbcUrlParams;
	}

	public void setExtraJdbcUrlParams(String extraJdbcUrlParams) {
		this.extraJdbcUrlParams = extraJdbcUrlParams;
	}

	public boolean isLazyInit() {
		return lazyInit;
	}

	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	public Properties getDsConfigProperties() {
		return dsConfigProperties;
	}

	public void setDsConfigProperties(Properties dsConfigProperties) {
		this.dsConfigProperties = dsConfigProperties;
	}

	public String getRouterStrategy() {
		return routerStrategy;
	}

	public void setRouterStrategy(String routerStrategy) {
		this.routerStrategy = routerStrategy;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getRouterType() {
		return routerType;
	}

	public void setRouterType(String routerType) {
		this.routerType = routerType;
	}
}
