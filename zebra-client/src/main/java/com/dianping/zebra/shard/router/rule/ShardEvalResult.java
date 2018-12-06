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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.dianping.zebra.shard.parser.SQLInExprWrapper;

import java.util.*;

public class ShardEvalResult {
	private String logicalTable;

	private boolean batchInsert;

	private boolean optimizeShardKeyInSql;

	private Set<String> shardColumns;

	// 必须是Set类型，防止一个库上的同一个表被路由多次
	private Map<String, Set<String>> dbAndTables;

	private Map<String, Map<String, Set<Integer>>> insertClauseIndexMap = new HashMap<String, Map<String, Set<Integer>>>();

	private Map<String, Map<String, Set<SQLInExprWrapper>>> skInExprWrapperMap = new HashMap<String, Map<String, Set<SQLInExprWrapper>>>();


	public ShardEvalResult(String logicalTable) {
		this(logicalTable, new HashMap<String, Set<String>>());
	}

	public ShardEvalResult(String logicalTable, boolean batchInsert) {
		this(logicalTable, new HashMap<String, Set<String>>());
		this.batchInsert = batchInsert;
	}

	public ShardEvalResult(String logicalTable, Map<String, Set<String>> dbAndTables) {
		this.logicalTable = logicalTable;
		this.dbAndTables = dbAndTables;
	}

	public void recordInsertClauseIndexMap(String db, String tb, int index) {
		if (this.insertClauseIndexMap == null) {
			this.insertClauseIndexMap = new HashMap<String, Map<String, Set<Integer>>>();
		}

		Map<String, Set<Integer>> tbIndexMap = this.insertClauseIndexMap.get(db);
		if (tbIndexMap == null) {
			tbIndexMap = new HashMap<String, Set<Integer>>();
			this.insertClauseIndexMap.put(db, tbIndexMap);
		}
		Set<Integer> indexList = tbIndexMap.get(tb);
		if (indexList == null) {
			indexList = new LinkedHashSet<Integer>();
			tbIndexMap.put(tb, indexList);
		}
		indexList.add(index);
	}

	public void recordShardKeyInSqlExpr(String db, String tb, Set<String> shardColumns, SQLExpr sqlExpr) {
		Map<String, Set<SQLInExprWrapper>> tbInSetMap = this.skInExprWrapperMap.get(db);
		if (tbInSetMap == null) {
			tbInSetMap = new HashMap<String, Set<SQLInExprWrapper>>();
			this.skInExprWrapperMap.put(db, tbInSetMap);
		}
		Set<SQLInExprWrapper> inSet = tbInSetMap.get(tb);
		if (inSet == null) {
			inSet = new LinkedHashSet<SQLInExprWrapper>();
			tbInSetMap.put(tb, inSet);
		}
		inSet.add(new SQLInExprWrapper(shardColumns, sqlExpr));
	}

	public Map<String, Set<String>> getDbAndTables() {
		return dbAndTables;
	}

	public void setDbAndTables(Map<String, Set<String>> dbAndTables) {
		this.dbAndTables = dbAndTables;
	}

	public void addDbAndTable(String dataSource, String table) {
		if (!dbAndTables.containsKey(dataSource)) {
			dbAndTables.put(dataSource, new LinkedHashSet<String>());
		}
		dbAndTables.get(dataSource).add(table);
	}

	public String getLogicalTable() {
		return logicalTable;
	}

	public boolean isBatchInsert() {
		return batchInsert;
	}

	public void setBatchInsert(boolean batchInsert) {
		this.batchInsert = batchInsert;
	}

	public Map<String, Map<String, Set<Integer>>> getInsertClauseIndexMap() {
		return insertClauseIndexMap;
	}

	public void setInsertClauseIndexMap(Map<String, Map<String, Set<Integer>>> insertClauseIndexMap) {
		this.insertClauseIndexMap = insertClauseIndexMap;
	}

	public boolean isOptimizeShardKeyInSql() {
		return optimizeShardKeyInSql;
	}

	public void setOptimizeShardKeyInSql(boolean optimizeShardKeyInSql) {
		this.optimizeShardKeyInSql = optimizeShardKeyInSql;
	}

	public Set<String> getShardColumns() {
		return shardColumns;
	}

	public void setShardColumns(Set<String> shardColumns) {
		this.shardColumns = shardColumns;
	}

	public Map<String, Map<String, Set<SQLInExprWrapper>>> getSkInExprWrapperMap() {
		return skInExprWrapperMap;
	}
}
