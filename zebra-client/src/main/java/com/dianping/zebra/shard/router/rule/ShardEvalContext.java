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
package com.dianping.zebra.shard.router.rule;

import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.dianping.zebra.shard.parser.SQLParsedResult;

public class ShardEvalContext {
	private final SQLParsedResult parseResult;

	private final List<Object> params;

	private String tableName;

	private List<ColumnValue> columnValues;

	private boolean optimizeShardKeyInSql;

	private boolean needOptimizeShardKeyInSql;

	public ShardEvalContext(SQLParsedResult parseResult, List<Object> params) {
		this.parseResult = parseResult;
		this.params = params;
	}

	public ShardEvalContext(SQLParsedResult parseResult, List<Object> params, boolean optimizeShardKeyInSql) {
		this.parseResult = parseResult;
		this.params = params;
		this.optimizeShardKeyInSql = optimizeShardKeyInSql;
	}

	public SQLParsedResult getParseResult() {
		return parseResult;
	}

	public List<Object> getParams() {
		return params;
	}

	public List<ColumnValue> getColumnValues() {
		return columnValues;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setColumnValues(List<ColumnValue> columnValues) {
		this.columnValues = columnValues;
	}

	public boolean isOptimizeShardKeyInSql() {
		return optimizeShardKeyInSql;
	}

	public void setOptimizeShardKeyInSql(boolean optimizeShardKeyInSql) {
		this.optimizeShardKeyInSql = optimizeShardKeyInSql;
	}

	public boolean isNeedOptimizeShardKeyInSql() {
		return needOptimizeShardKeyInSql;
	}

	public void setNeedOptimizeShardKeyInSql(boolean needOptimizeShardKeyInSql) {
		this.needOptimizeShardKeyInSql = needOptimizeShardKeyInSql;
	}

	public static class ColumnValue {
		private boolean used = false;

		private Map<String, Object> value;

		private SQLExpr inSqlExpr;

		public ColumnValue(Map<String, Object> value, SQLExpr inSqlExpr) {
			this.value = value;
			this.inSqlExpr = inSqlExpr;
		}

		public boolean isUsed() {
			return used;
		}

		public void setUsed(boolean used) {
			this.used = used;
		}

		public Map<String, Object> getValue() {
			return value;
		}

		public SQLExpr getInSqlExpr() {
			return inSqlExpr;
		}

		public void setInSqlExpr(SQLExpr inSqlExpr) {
			this.inSqlExpr = inSqlExpr;
		}
	}
}
