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
package com.dianping.zebra.shard.router.rule.dimension;

import java.util.*;
import java.util.Map.Entry;

import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.router.rule.ShardEvalContext;
import com.dianping.zebra.shard.router.rule.ShardEvalResult;
import com.dianping.zebra.shard.router.rule.ShardEvalContext.ColumnValue;
import com.dianping.zebra.shard.router.rule.engine.GroovyRuleEngine;
import com.dianping.zebra.shard.router.rule.engine.RuleEngine;
import com.dianping.zebra.shard.router.rule.tableset.TableSets;
import com.dianping.zebra.shard.router.rule.tableset.TableSetsManager;
import com.dianping.zebra.shard.router.rule.tableset.DefaultTableSetsManager;

/**
 * 
 * @author hao.zhu
 *
 */
public class DefaultDimensionRule extends AbstractDimensionRule {

	private static final String shardByMonthPrefix = "shardByMonth";

	private static final String shardByLongPrefix = "shardByLong";

	private RuleEngine dbRuleEngine;

	private RuleEngine tableRuleEngine;

	private List<DimensionRule> whiteListRules;

	private TableSetsManager tablesMappingManager;

	private Map<String, Set<String>> allDBAndTables = new HashMap<String, Set<String>>();

	public DefaultDimensionRule(TableShardDimensionConfig dimensionConfig) {
		this.isMaster = dimensionConfig.isMaster();

		this.tablesMappingManager = new DefaultTableSetsManager(dimensionConfig.getTableName(),
				dimensionConfig.getDbIndexes(), dimensionConfig.getTbSuffix(), dimensionConfig.isTbSuffixZeroPadding());
		this.allDBAndTables.putAll(this.tablesMappingManager.getAllTableSets());

		// for new unified rule and range
		String dbRuleStr = dimensionConfig.getDbRule();
		String tbRuleStr = dimensionConfig.getTbRule();
		if(dbRuleStr != null && (dbRuleStr.contains(shardByMonthPrefix) || dbRuleStr.contains(shardByLongPrefix))) {
			this.isRange = true;
		}
		this.dbRuleEngine = new GroovyRuleEngine(dbRuleStr);
		if(tbRuleStr != null && tbRuleStr.length() > 0) {
			this.tableRuleEngine = new GroovyRuleEngine(dimensionConfig.getTbRule());
		}

		this.initShardColumn(dimensionConfig.getDbRule(), dimensionConfig.getTbRule());
	}

	@Override
	public Map<String, Set<String>> getAllDBAndTables() {
		return allDBAndTables;
	}

	@Override
	public ShardEvalResult eval(ShardEvalContext evalContext) {
		return eval(evalContext, false);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ShardEvalResult eval(ShardEvalContext matchContext, boolean isBatchInsert) {
		ShardEvalResult result = new ShardEvalResult(matchContext.getTableName(), isBatchInsert);
		result.setOptimizeShardKeyInSql(matchContext.isNeedOptimizeShardKeyInSql());
		result.setShardColumns(getShardColumns());

		// eval whitelist first
		for (DimensionRule dimensionRule : this.whiteListRules) {
			ShardEvalResult eval = dimensionRule.eval(matchContext);

			for (Entry<String, Set<String>> entry : eval.getDbAndTables().entrySet()) {
				String db = entry.getKey();
				for (String table : entry.getValue()) {
					result.addDbAndTable(db, table);
				}
			}
		}

		int clauseIndex = 0;
		for (ColumnValue evalContext : matchContext.getColumnValues()) {
			if (!evalContext.isUsed()) {
				evalContext.setUsed(true);

				Object dbObj = dbRuleEngine.eval(evalContext.getValue());
				if(dbObj instanceof Map) {
					Map<Integer, List<Integer>> dbTbIndexMap = (Map<Integer, List<Integer>>) dbObj;
					for(Map.Entry<Integer, List<Integer>> entry : dbTbIndexMap.entrySet()) {
						int dbIndex = entry.getKey();
						TableSets tableSet = tablesMappingManager.getTableSetsByPos(dbIndex);
						List<Integer> tbIndexList = entry.getValue();
						for(int tbIndex : tbIndexList) {
							result.addDbAndTable(tableSet.getDbIndex(), tableSet.getTableSets().get(tbIndex));
							if (isBatchInsert) {
								result.recordInsertClauseIndexMap(tableSet.getDbIndex(), tableSet.getTableSets().get(tbIndex), clauseIndex);
							}
						}
					}
					result.setOptimizeShardKeyInSql(false);
				} else {
					Number dbPos = (Number)dbObj;
					TableSets tableSet = tablesMappingManager.getTableSetsByPos(dbPos.intValue());
					Number tablePos = (Number) tableRuleEngine.eval(evalContext.getValue());
					String table = tableSet.getTableSets().get(tablePos.intValue());
					String dbIndex = tableSet.getDbIndex();
					result.addDbAndTable(dbIndex, table);

					if (isBatchInsert) {
						result.recordInsertClauseIndexMap(dbIndex, table, clauseIndex);
					} else if (evalContext.getInSqlExpr() != null) {
						result.recordShardKeyInSqlExpr(dbIndex, table, shardColumns, evalContext.getInSqlExpr());
					}
				}
				clauseIndex++;
			}
		}

		return result;
	}

	public void setExceptionalRules(List<DimensionRule> whiteListRules) {
		this.whiteListRules = whiteListRules;

		for (DimensionRule whiteListRule : this.whiteListRules) {
			Map<String, Set<String>> whiteListDBAndTables = whiteListRule.getAllDBAndTables();
			for (Entry<String, Set<String>> allDBAndTable : whiteListDBAndTables.entrySet()) {
				String db = allDBAndTable.getKey();
				if (!allDBAndTables.containsKey(db)) {
					allDBAndTables.put(db, new LinkedHashSet<String>());
				}
				allDBAndTables.get(db).addAll(allDBAndTable.getValue());
			}
		}
	}

	@Override
	public Set<String> getShardColumns() {
		return this.shardColumns;
	}
}
