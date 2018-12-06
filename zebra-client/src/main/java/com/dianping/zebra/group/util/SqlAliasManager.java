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
package com.dianping.zebra.group.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.zebra.util.StringUtils;

public class SqlAliasManager {

	private static final String SQL_RETRY_SUFFIX = " (retry-by-zebra)";

	private static final String MORE_SQL_NAME = "other";

	private static final ThreadLocal<String> sqlAlias = new ThreadLocal<String>();

	private static final int MAX_ALLOWED_SQL_CHAR = 1024;

	private static final int MAX_ALLOWED_TRUNCATED_SQL_NUM = 1000;

	private static final Map<Integer, Object> cachedTruncatedSqls = new ConcurrentHashMap<Integer, Object>(
			MAX_ALLOWED_TRUNCATED_SQL_NUM * 2);

	private static volatile boolean cachedTruncatedSqlsIsFull = false;

	private static final Object PRESENT = new Object();

	private static String getCachedTruncatedSql(String sql) {
		if (StringUtils.isEmpty(sql)) {
			return null;
		}

		if (cachedTruncatedSqlsIsFull) {
			return MORE_SQL_NAME;
		} else {
			cachedTruncatedSqlsIsFull = cachedTruncatedSqls.size() >= MAX_ALLOWED_TRUNCATED_SQL_NUM;
		}

		if (sql.length() > MAX_ALLOWED_SQL_CHAR) {
			sql = sql.substring(0, MAX_ALLOWED_SQL_CHAR);
		}
		int sqlHash = sql.hashCode();

		if (!cachedTruncatedSqls.containsKey(sqlHash)) {
			if (cachedTruncatedSqlsIsFull) {
				return MORE_SQL_NAME;
			}
			cachedTruncatedSqls.put(sqlHash, PRESENT);
		}
		return sql;
	}

	public static String getSqlAlias() {
		return sqlAlias.get();
	}

	public static void setRetrySqlAlias() {
		String alias = sqlAlias.get();

		if (alias != null) {
			sqlAlias.set(alias + SQL_RETRY_SUFFIX);
		}
	}

	public static void setSqlAlias(String sql) {
		String sqlName = DaoContextHolder.getSqlName();

		if (StringUtils.isNotBlank(sqlName)) {
			sqlAlias.set(sqlName);
		} else {
			sqlAlias.set(getCachedTruncatedSql(sql));
		}
	}
}