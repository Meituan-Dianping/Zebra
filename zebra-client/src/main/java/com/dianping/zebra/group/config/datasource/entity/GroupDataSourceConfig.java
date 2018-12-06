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
package com.dianping.zebra.group.config.datasource.entity;

import com.dianping.zebra.group.config.ReadOrWriteRole;
import com.dianping.zebra.group.config.datasource.BaseEntity;
import com.dianping.zebra.group.config.datasource.IVisitor;

import java.util.LinkedHashMap;
import java.util.Map;

public class GroupDataSourceConfig extends BaseEntity<GroupDataSourceConfig> {
	private String m_filters = "";

	private String m_routerStrategy = "WeightRouter";

	private Map<String, DataSourceConfig> m_dataSourceConfigs = new LinkedHashMap();

	public GroupDataSourceConfig() {
	}

	public void accept(IVisitor visitor) {
		visitor.visitGroupDataSourceConfig(this);
	}

	public GroupDataSourceConfig addDataSourceConfig(DataSourceConfig dataSourceConfig) {
		this.m_dataSourceConfigs.put(dataSourceConfig.getId(), dataSourceConfig);
		return this;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof GroupDataSourceConfig)) {
			return false;
		} else {
			GroupDataSourceConfig _o = (GroupDataSourceConfig) obj;
			String filters = _o.getFilters();
			String routerStrategy = _o.getRouterStrategy();
			Map<String, DataSourceConfig> dataSourceConfigs = _o.getDataSourceConfigs();
			boolean result = true;
			result &= this.m_filters == filters || this.m_filters != null && this.m_filters.equals(filters);
			result &= this.m_routerStrategy == routerStrategy
			      || this.m_routerStrategy != null && this.m_routerStrategy.equals(routerStrategy);
			result &= this.m_dataSourceConfigs == dataSourceConfigs
			      || this.m_dataSourceConfigs != null && this.m_dataSourceConfigs.equals(dataSourceConfigs);
			return result;
		}
	}

	public DataSourceConfig findDataSourceConfig(String id) {
		return (DataSourceConfig) this.m_dataSourceConfigs.get(id);
	}

	public DataSourceConfig findOrCreateDataSourceConfig(String id) {
		DataSourceConfig dataSourceConfig = (DataSourceConfig) this.m_dataSourceConfigs.get(id);
		if (dataSourceConfig == null) {
			synchronized (this.m_dataSourceConfigs) {
				dataSourceConfig = (DataSourceConfig) this.m_dataSourceConfigs.get(id);
				if (dataSourceConfig == null) {
					dataSourceConfig = new DataSourceConfig(id);
					this.m_dataSourceConfigs.put(id, dataSourceConfig);
				}
			}
		}

		return dataSourceConfig;
	}

	public DataSourceConfig findOrCreateDataSourceConfig(ReadOrWriteRole role) {
		return findOrCreateDataSourceConfig(role.getReadOrWriteDsName());
	}

	public Map<String, DataSourceConfig> getDataSourceConfigs() {
		return this.m_dataSourceConfigs;
	}

	public String getFilters() {
		return this.m_filters;
	}

	public String getRouterStrategy() {
		return this.m_routerStrategy;
	}

	public int hashCode() {
		int hash = 0;
		hash = hash * 31 + (this.m_filters == null ? 0 : this.m_filters.hashCode());
		hash = hash * 31 + (this.m_routerStrategy == null ? 0 : this.m_routerStrategy.hashCode());
		hash = hash * 31 + (this.m_dataSourceConfigs == null ? 0 : this.m_dataSourceConfigs.hashCode());
		return hash;
	}

	public void mergeAttributes(GroupDataSourceConfig other) {
		if (other.getFilters() != null) {
			this.m_filters = other.getFilters();
		}

		if (other.getRouterStrategy() != null) {
			this.m_routerStrategy = other.getRouterStrategy();
		}
	}

	public boolean removeDataSourceConfig(String id) {
		if (this.m_dataSourceConfigs.containsKey(id)) {
			this.m_dataSourceConfigs.remove(id);
			return true;
		} else {
			return false;
		}
	}

	public GroupDataSourceConfig setFilters(String filters) {
		this.m_filters = filters;
		return this;
	}

	public GroupDataSourceConfig setRouterStrategy(String routerStrategy) {
		this.m_routerStrategy = routerStrategy;
		return this;
	}
}