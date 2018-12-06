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
package com.dianping.zebra.single.pool;

import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.util.StringUtils;
import com.dianping.zebra.util.json.JsonArray;

import java.lang.reflect.Method;
import java.util.Collection;

public abstract class AbstractDataSourcePool implements DataSourcePool {

	protected static final int MAX_CLOSE_ATTEMPT = 600;

	protected void throwException(String dsId) {
		throw new ZebraException(String.format("Cannot close dataSource[%s] since there are busy connections.", dsId));
	}

	protected int getIntProperty(DataSourceConfig config, String name, int defaultValue) {
		for (Any any : config.getProperties()) {
			if (any.getName().equalsIgnoreCase(name)) {
				if (any.getValue().startsWith("@I_")) {
					return Integer.valueOf(any.getValue().substring(3));
				} else {
					return Integer.parseInt(any.getValue());
				}
			}
		}

		return defaultValue;
	}

	protected long getLongProperty(DataSourceConfig config, String name, long defaultValue) {
		for (Any any : config.getProperties()) {
			if (any.getName().equalsIgnoreCase(name)) {
				if (any.getValue().startsWith("@L_")) {
					return Long.valueOf(any.getValue().substring(3));
				} else {
					return Long.parseLong(any.getValue());
				}
			}
		}

		return defaultValue;
	}

	protected String getStringProperty(DataSourceConfig config, String name, String defaultValue) {
		for (Any any : config.getProperties()) {
			if (any.getName().equalsIgnoreCase(name)) {
				return any.getValue();
			}
		}

		return defaultValue;
	}

	protected class PropertiesInit<T> {

		private T obj;

		public PropertiesInit(T obj) {
			this.obj = obj;
		}

		public T initPoolProperties(DataSourceConfig config) throws Exception {
			for (Any any : config.getProperties()) {
				Class<?>[] parameters = new Class[1];
				Object[] args = new Object[1];
				if ("true".equalsIgnoreCase(any.getValue()) || "false".equalsIgnoreCase(any.getValue())) {
					parameters[0] = boolean.class;
					args[0] = Boolean.valueOf(any.getValue());
				} else {
					String value = any.getValue();
					if (value.startsWith("@I_")) {
						parameters[0] = int.class;
						args[0] = Integer.valueOf(value.substring(3));
					} else if (value.startsWith("@L_")) {
						parameters[0] = long.class;
						args[0] = Integer.valueOf(value.substring(3));
					} else if (value.startsWith("@C_")) {
						parameters[0] = Collection.class;
						JsonArray jsonArray = new JsonArray(value.substring(3));
						args[0] = jsonArray.getArrayList();
					} else {
						parameters[0] = String.class;
						args[0] = any.getValue();
					}
				}

				try {
					Method method;
					try {
						// 尝试先用上面的内置类型找方法，如果找不到，用args的参数类型再尝试找是否存在该方法
						method = obj.getClass().getMethod("set" + StringUtils.upperFirstChar(any.getName()), parameters);
						method.invoke((T) obj, args);
					} catch (NoSuchMethodException e) {
						parameters[0] = args[0].getClass();
						method = obj.getClass().getMethod("set" + StringUtils.upperFirstChar(any.getName()), parameters);
						method.invoke((T) obj, args);
					}
				} catch (Exception e) {
					throw e;
				}
			}

			return obj;
		}
	}
}
