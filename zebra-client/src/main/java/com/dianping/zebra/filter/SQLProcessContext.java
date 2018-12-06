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
package com.dianping.zebra.filter;

import java.util.HashMap;
import java.util.Map;

public class SQLProcessContext {
	public static final String PROPERTY_SQL_ID = "ID";

	private boolean isPreparedStmt;

	private Map<String, Object> properties = new HashMap<String, Object>();

	public SQLProcessContext(boolean isPreparedStmt) {
		super();
		this.isPreparedStmt = isPreparedStmt;
	}

	public void putProperty(String key, Object prop) {
		properties.put(key, prop);
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	public boolean isPreparedStmt() {
		return isPreparedStmt;
	}

	public void setPreparedStmt(boolean isPreparedStmt) {
		this.isPreparedStmt = isPreparedStmt;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
}
