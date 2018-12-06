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
package com.dianping.zebra.config;

import com.dianping.zebra.Constants;

public final class LionKey {
	private LionKey() {
	}

	public static String getJdbcRefConfigKey(String jdbcRef) {
		return String.format("%s.%s", Constants.DEFAULT_GROUP_DATASOURCE_PRFIX, jdbcRef);
	}

	public static String getGlobalDatabaseSecurityConfigKey() {
		return String.format("%s.global.%s", Constants.DEFAULT_GROUP_SECURITY_PRFIX, "security");
	}

	public static String getGlobalDatabaseSecuritySwitchConfigKey() {
		return String.format("%s.global.%s", Constants.DEFAULT_GROUP_SECURITY_PRFIX, "switch");
	}

	public static String getDatabaseSecurityConfigKey(String database) {
		return String.format("%s.%s.%s", Constants.DEFAULT_GROUP_SECURITY_PRFIX, database.toLowerCase(), "security");
	}

	public static String getDatabaseSecuritySwitchKey(String database) {
		return String.format("%s.%s.%s", Constants.DEFAULT_GROUP_SECURITY_PRFIX, database.toLowerCase(), "switch");
	}

	public static String getShardConfigKey(String ruleName) {
		return String.format("%s.%s", Constants.DEFAULT_SHARDING_PRFIX, ruleName);
	}
}
