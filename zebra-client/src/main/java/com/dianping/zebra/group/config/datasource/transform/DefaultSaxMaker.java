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
package com.dianping.zebra.group.config.datasource.transform;

import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import org.xml.sax.Attributes;

public class DefaultSaxMaker implements IMaker<Attributes> {
	public DefaultSaxMaker() {
	}

	public Any buildAny(Attributes attributes) {
		throw new UnsupportedOperationException("Not needed!");
	}

	public DataSourceConfig buildDataSourceConfig(Attributes attributes) {
		String id = attributes.getValue("id");
		String weight = attributes.getValue("weight");
		String canRead = attributes.getValue("canRead");
		String canWrite = attributes.getValue("canWrite");
		String active = attributes.getValue("active");
		String type = attributes.getValue("type");
		String tag = attributes.getValue("tag");
		String jdbcref = attributes.getValue("jdbcref");
		String lazyInit = attributes.getValue("lazyInit");
		DataSourceConfig dataSourceConfig = new DataSourceConfig(id);
		if (weight != null) {
			dataSourceConfig.setWeight((Integer) this.convert(Integer.class, weight, 0));
		}

		if (canRead != null) {
			dataSourceConfig.setCanRead((Boolean) this.convert(Boolean.class, canRead, false));
		}

		if (canWrite != null) {
			dataSourceConfig.setCanWrite((Boolean) this.convert(Boolean.class, canWrite, false));
		}

		if (active != null) {
			dataSourceConfig.setActive((Boolean) this.convert(Boolean.class, active, false));
		}

		if (type != null) {
			dataSourceConfig.setType(type);
		}

		if (tag != null) {
			dataSourceConfig.setTag(tag);
		}

		if (jdbcref != null) {
			dataSourceConfig.setJdbcref(jdbcref);
		}

		if (lazyInit != null) {
			dataSourceConfig.setLazyInit((Boolean) this.convert(Boolean.class, lazyInit, false));
		}

		return dataSourceConfig;
	}

	public GroupDataSourceConfig buildGroupDataSourceConfig(Attributes attributes) {
		String filters = attributes.getValue("filters");
		String routerStrategy = attributes.getValue("router-strategy");
		GroupDataSourceConfig groupDataSourceConfig = new GroupDataSourceConfig();
		if (filters != null) {
			groupDataSourceConfig.setFilters(filters);
		}

		if (routerStrategy != null) {
			groupDataSourceConfig.setRouterStrategy(routerStrategy);
		}

		return groupDataSourceConfig;
	}

	@SuppressWarnings("unchecked")
	protected <T> T convert(Class<T> type, String value, T defaultValue) {
		if (value == null) {
			return defaultValue;
		} else if (type == Boolean.class) {
			return (T) Boolean.valueOf(value);
		} else if (type == Integer.class) {
			return (T) Integer.valueOf(value);
		} else if (type == Long.class) {
			return (T) Long.valueOf(value);
		} else if (type == Short.class) {
			return (T) Short.valueOf(value);
		} else if (type == Float.class) {
			return (T) Float.valueOf(value);
		} else if (type == Double.class) {
			return (T) Double.valueOf(value);
		} else if (type == Byte.class) {
			return (T) Byte.valueOf(value);
		} else {
			return (T) (type == Character.class ? value.charAt(0) : value);
		}
	}
}
