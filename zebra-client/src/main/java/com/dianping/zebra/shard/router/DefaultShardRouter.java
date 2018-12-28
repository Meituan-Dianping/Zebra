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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra.shard.router;

import java.util.*;
import java.util.Map.Entry;

import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.dianping.zebra.shard.exception.ShardParseException;
import com.dianping.zebra.shard.exception.ShardRouterException;
import com.dianping.zebra.shard.merge.MergeContext;
import com.dianping.zebra.shard.parser.*;
import com.dianping.zebra.shard.router.RouterResult.RouterTarget;
import com.dianping.zebra.shard.router.rule.RouterRule;
import com.dianping.zebra.shard.router.rule.ShardEvalContext;
import com.dianping.zebra.shard.router.rule.ShardEvalResult;
import com.dianping.zebra.shard.router.rule.TableShardRule;

/**
 * @author hao.zhu
 */
public class DefaultShardRouter implements ShardRouter {

	private SQLRewrite sqlRewrite = new DefaultSQLRewrite();

	private RouterRule routerRule;

	private String defaultDatasource;	// 针对不分库不分表的表

	private boolean optimizeShardKeyInSql;

	public DefaultShardRouter(RouterRule routerRule, String defaultDatasource) {
		this(routerRule, new DefaultSQLRewrite(), defaultDatasource);
	}

	public DefaultShardRouter(RouterRule routerRule, SQLRewrite sqlRewrite, String defaultDatasource) {
		this.routerRule = routerRule;
		this.sqlRewrite = sqlRewrite;
		this.defaultDatasource = defaultDatasource;
	}

	@Override
	public RouterResult router(final String sql, List<Object> params) throws ShardRouterException, ShardParseException {
		SQLParsedResult parsedResult = SQLParser.parseWithCache(sql);

		boolean optimizeIn = false;
		RouterResult routerResult = new RouterResult();
		SQLHint sqlHint = ((parsedResult instanceof MultiSQLParsedResult) ? ((MultiSQLParsedResult) parsedResult).getSqlHint()
				: parsedResult.getRouterContext().getSqlhint());

		if (sqlHint != null) {
			routerResult.setConcurrencyLevel(sqlHint.getConcurrencyLevel());
			Boolean optimizeInObj = sqlHint.getOptimizeIn();
			optimizeIn = (optimizeInObj == null) ? this.optimizeShardKeyInSql : optimizeInObj;
		}

		// multi queries
		if (parsedResult instanceof MultiSQLParsedResult) {
			routerResult.setOptimizeShardKeyInSql(false);
			return multiQueriesRouter((MultiSQLParsedResult)parsedResult, params, routerResult);
		}

		List<TableShardRule> findShardRules = findShardRules(parsedResult.getRouterContext(), params);
		if (findShardRules.size() == 1) {
			return routerOneRule(parsedResult, params, routerResult, findShardRules.get(0), optimizeIn);
		} else if(findShardRules.size() > 1) {
			return routerMultiRules(parsedResult, params, routerResult, findShardRules);
		} else {
			return routerDefault(parsedResult, params, routerResult, sql);
		}
	}

	private RouterResult multiQueriesRouter(MultiSQLParsedResult multiSQLParsedResult, List<Object> params, RouterResult routerResult) {
		List<SQLParsedResult> sqlParsedResults = multiSQLParsedResult.getSqlParsedResults();
		if (sqlParsedResults != null) {
			List<RouterResult> routerResults = new ArrayList<RouterResult>();
			for (SQLParsedResult sqlParsedResult : sqlParsedResults) {
				List<TableShardRule> findShardRules = findShardRules(sqlParsedResult.getRouterContext(), params);
				if (findShardRules.size() != 1) {
					throw new ShardRouterException("Shard multi queries not support multi table rule or no table rule!");
				}
				RouterResult singleResult = routerOneRule(sqlParsedResult, params, new RouterResult(), findShardRules.get(0), false, true);
				routerResults.add(singleResult);
				params = singleResult.getParams();
			}

			Map<String, StringBuilder> mergeSqlMap = new HashMap<String, StringBuilder>();
			Map<String, Set<Integer>> variantRefIndexMap = new HashMap<String, Set<Integer>>();
			for (RouterResult singleResult : routerResults) {
				List<RouterTarget> routerTargets = singleResult.getSqls();
				if (routerResults != null) {
					for (RouterTarget target : routerTargets) {
						String db = target.getDatabaseName();
						StringBuilder builder = mergeSqlMap.get(db);
						if (builder == null) {
							builder = new StringBuilder(4096);
							mergeSqlMap.put(db, builder);
						}
						Set<Integer> variantRefIndexSet = variantRefIndexMap.get(db);
						if (variantRefIndexSet == null) {
							variantRefIndexSet = new HashSet<Integer>();
							variantRefIndexMap.put(db, variantRefIndexSet);
						}

						List<String> sqls = target.getSqls();
						List<Set<Integer>> variantRefIndexList = target.getAllVariantRefIndexList();
						if (sqls != null) {
							Iterator<Set<Integer>> it = (variantRefIndexList == null) ? null : variantRefIndexList.iterator();
							for (String sql : sqls) {
								builder.append(sql).append(';');
								if (it != null && it.hasNext()) {
									variantRefIndexSet.addAll(it.next());
								}
							}
						}
					}
				}
			}

			List<RouterTarget> routerTargets = new ArrayList<RouterTarget>(mergeSqlMap.size());
			for (Map.Entry<String, StringBuilder> entry : mergeSqlMap.entrySet()) {
				RouterTarget routerTarget = new RouterTarget(entry.getKey(), Arrays.asList(entry.getValue().toString()));
				routerTarget.setAllVariantRefIndexList(Arrays.asList(variantRefIndexMap.get(entry.getKey())));
				routerTargets.add(routerTarget);
			}
			routerResult.setSqls(routerTargets);
			routerResult.setParams(params);
			routerResult.setMultiQueries(true);
		}

		return routerResult;
	}

	// one table
	private RouterResult routerOneRule(SQLParsedResult parsedResult, List<Object> params, RouterResult routerResult,
			TableShardRule tableShardRule, boolean optimizeIn) {
		return routerOneRule(parsedResult, params, routerResult, tableShardRule, optimizeIn, false);
	}

	private RouterResult routerOneRule(SQLParsedResult parsedResult, List<Object> params, RouterResult routerResult,
			TableShardRule tableShardRule, boolean optimizeIn, boolean multiQueries) {

		ShardEvalResult shardResult = tableShardRule.eval(new ShardEvalContext(parsedResult, params, optimizeIn));

		routerResult.setMergeContext(new MergeContext(parsedResult.getMergeContext()));
		if (shardResult.isBatchInsert()) {
			routerResult.setBatchInsert(true);
			buildBatchInsertSqls(shardResult, parsedResult, tableShardRule.getTableName(), routerResult);
			routerResult.setParams(buildParams(params, routerResult));
		} else {
			if (optimizeIn && shardResult.isOptimizeShardKeyInSql()) {
				routerResult.setSqls(buildSqls(shardResult.getDbAndTables(), parsedResult, tableShardRule.getTableName(),
						shardResult.getSkInExprWrapperMap(), shardResult.getShardColumns()));
				routerResult.setOptimizeShardKeyInSql(true);
			} else if (multiQueries) {
				routerResult.setSqls(buildMultiQueriesSqls(shardResult.getDbAndTables(), parsedResult, tableShardRule.getTableName()));
			} else {
				routerResult.setSqls(buildSqls(shardResult.getDbAndTables(), parsedResult, tableShardRule.getTableName()));
			}
			routerResult.setParams(buildParams(params, routerResult));
		}

		return routerResult;
	}

	// multi table for binding table
	private RouterResult routerMultiRules(SQLParsedResult parsedResult, List<Object> params, RouterResult routerResult,
			List<TableShardRule> findShardRules) {
		List<ShardEvalResult> shardResults = new ArrayList<ShardEvalResult>();
		ShardEvalContext shardEvalContext = new ShardEvalContext(parsedResult, params);
		for (TableShardRule tableShardRule : findShardRules) {
			shardResults.add(tableShardRule.eval(shardEvalContext));
		}

		Map<String, List<Map<String, String>>> dbAndTables = new HashMap<String, List<Map<String, String>>>();
		for (ShardEvalResult shardResult : shardResults) {
			String logicalTable = shardResult.getLogicalTable();
			for (Entry<String, Set<String>> entry : shardResult.getDbAndTables().entrySet()) {
				String db = entry.getKey();

				List<Map<String, String>> tableMappingList = dbAndTables.get(db);
				if (tableMappingList == null) {
					int size = entry.getValue().size();
					tableMappingList = new ArrayList<Map<String, String>>(size);
					for (int i = 0; i < size; i++) {
						tableMappingList.add(new HashMap<String, String>());
					}
					dbAndTables.put(db, tableMappingList);
				}

				int index = 0;
				for (String physicalTable : entry.getValue()) {
					Map<String, String> tableMapping = tableMappingList.get(index++);
					tableMapping.put(logicalTable, physicalTable);
				}
			}
		}

		routerResult.setMergeContext(new MergeContext(parsedResult.getMergeContext()));
		routerResult.setSqls(buildSqls(dbAndTables, parsedResult));
		routerResult.setParams(buildParams(params, routerResult));

		return routerResult;
	}

	// single table default router
	private RouterResult routerDefault(SQLParsedResult parsedResult, List<Object> params, RouterResult routerResult, String sql) {
		// add for default strategy
		List<RouterTarget> routerSqls = new ArrayList<RouterTarget>();
		RouterTarget targetedSql = new RouterTarget(defaultDatasource);
		targetedSql.addSql(sql);
		routerSqls.add(targetedSql);

		List<Object> newParams = null;
		if (params != null) {
			newParams = new ArrayList<Object>(params);
		}

		routerResult.setMergeContext(new MergeContext(parsedResult.getMergeContext()));
		routerResult.setSqls(routerSqls);
		routerResult.setParams(newParams);
		return routerResult;
	}


	@Override
	public boolean validate(String sql) throws ShardParseException, ShardRouterException {
		return true;
	}

	@Override
	public RouterRule getRouterRule() {
		return this.routerRule;
	}

	private List<TableShardRule> findShardRules(RouterContext context, List<Object> params)
			throws ShardRouterException {
		Map<String, TableShardRule> tableShardRules = this.routerRule.getTableShardRules();
		List<TableShardRule> tableShardRuleList = new ArrayList<TableShardRule>();

		for (String relatedTable : context.getTableSet()) {
			TableShardRule tableShardRule = tableShardRules.get(relatedTable);
			if (tableShardRule != null) {
				tableShardRuleList.add(tableShardRule);
			}
		}

		if(tableShardRuleList.size() > 1) {
			tableShardRuleList = new ArrayList<TableShardRule>();
			for (String relatedTable : context.getTableSet()) {
				TableShardRule tableShardRule = tableShardRules.get(relatedTable);
				if (tableShardRule != null) {
					tableShardRuleList.add(tableShardRule);
				}
			}
		} else if(tableShardRuleList.isEmpty()){
			// throw exception if no default jdbcRef
			if (defaultDatasource == null) {
				throw new ShardRouterException("No table shard rule can be found for table " + context.getTableSet());
			}
		}

		return tableShardRuleList;
	}

	// build normal sql and multi queries
	private List<RouterTarget> buildSqls(Map<String, Set<String>> dbAndTables, SQLParsedResult parseResult, String logicTable) {
		List<RouterTarget> sqls = new ArrayList<RouterTarget>();

		for (Entry<String, Set<String>> entry : dbAndTables.entrySet()) {
			RouterTarget targetedSql = new RouterTarget(entry.getKey());

			for (String physicalTable : entry.getValue()) {
				String _sql = sqlRewrite.rewrite(parseResult, logicTable, physicalTable);
				String hintComment = parseResult.getRouterContext().getSqlhint().getHintComments();
				targetedSql.addSql((hintComment != null ? (hintComment + _sql) : _sql));
			}

			sqls.add(targetedSql);
		}

		return sqls;
	}

	// build multi queries sql
	private List<RouterTarget> buildMultiQueriesSqls(Map<String, Set<String>> dbAndTables, SQLParsedResult parseResult, String logicTable) {
		List<RouterTarget> sqls = new ArrayList<RouterTarget>();

		for (Entry<String, Set<String>> entry : dbAndTables.entrySet()) {
			RouterTarget targetedSql = new RouterTarget(entry.getKey());

			for (String physicalTable : entry.getValue()) {
				Set<Integer> variantRefIndexSet = new HashSet<Integer>();
				String _sql = sqlRewrite.rewrite(parseResult, logicTable, physicalTable, variantRefIndexSet);

				String hintComment = parseResult.getRouterContext().getSqlhint().getHintComments();
				targetedSql.addSql((hintComment != null ? (hintComment + _sql) : _sql));
				targetedSql.addVariantRefIndexes(variantRefIndexSet);
			}
			sqls.add(targetedSql);
		}

		return sqls;
	}

	// build sql optimize in
	private List<RouterTarget> buildSqls(Map<String, Set<String>> dbAndTables, SQLParsedResult parseResult, String logicTable,
			Map<String, Map<String, Set<SQLInExprWrapper>>> skInSqlExprMap, Set<String> shardColumns) {
		List<RouterTarget> sqls = new ArrayList<RouterTarget>();

		for (Entry<String, Set<String>> entry : dbAndTables.entrySet()) {
			RouterTarget targetedSql = new RouterTarget(entry.getKey());
			Map<String, Set<SQLInExprWrapper>> skInMap = skInSqlExprMap.get(entry.getKey());

			for (String physicalTable : entry.getValue()) {
				Set<SQLInExprWrapper> skInSet = null;
				if (skInMap != null) {
					skInSet = skInMap.get(physicalTable);
				}

				Set<Integer> skInIgnoreParams = new HashSet<Integer>();
				String _sql = sqlRewrite.rewrite(parseResult, logicTable, physicalTable, null,
						new SQLRewriteInParam(skInSet, shardColumns, skInIgnoreParams));

				String hintComment = parseResult.getRouterContext().getSqlhint().getHintComments();
				if (hintComment != null) {
					targetedSql.addSql(hintComment + _sql);
				} else {
					targetedSql.addSql(_sql);
				}
				targetedSql.addSkInIgnoreParams(skInIgnoreParams);
			}

			sqls.add(targetedSql);
		}

		return sqls;
	}

	// binding table
	private List<RouterTarget> buildSqls(Map<String, List<Map<String, String>>> dbAndTables, SQLParsedResult parseResult) {
		List<RouterTarget> sqls = new ArrayList<RouterTarget>();

		for (Entry<String, List<Map<String, String>>> entry : dbAndTables.entrySet()) {
			RouterTarget targetedSql = new RouterTarget(entry.getKey());

			for (Map<String, String> tables : entry.getValue()) {
				String _sql = sqlRewrite.rewrite(parseResult.getStmt(), tables);

				String hintComment = parseResult.getRouterContext().getSqlhint().getForceMasterComment();
				if (hintComment != null) {
					targetedSql.addSql(hintComment + _sql);
				} else {
					targetedSql.addSql(_sql);
				}
			}

			sqls.add(targetedSql);
		}

		return sqls;
	}

	private List<Object> buildParams(List<Object> params, RouterResult rr) {
		List<Object> newParams = null;
		if (params != null) {
			newParams = new ArrayList<Object>(params);
			MySqlSelectQueryBlock.Limit limitExpr = rr.getMergeContext().getLimitExpr();
			if (limitExpr != null) {
				int offset = Integer.MIN_VALUE;
				int offsetRefIndex = -1;
				int limit = Integer.MIN_VALUE;
				int limitRefIndex = -1;
				int originOffset = Integer.MIN_VALUE;
				boolean isSingleTarget = isSingleRouterTarget(rr);

				if (limitExpr.getOffset() instanceof SQLVariantRefExpr) {
					SQLVariantRefExpr ref = (SQLVariantRefExpr) limitExpr.getOffset();
					offsetRefIndex = ref.getIndex();
					offset = (Integer) newParams.get(ref.getIndex());
					originOffset = offset;
					if (!isSingleTarget) {
						rr.getMergeContext().setOffset(offset);
						// 不是可拆分limit SQL才重写offset
						if (!rr.getMergeContext().isOrderBySplitSql()) {
							offset = 0;
						}
					}
				}

				if (limitExpr.getRowCount() instanceof SQLVariantRefExpr) {
					SQLVariantRefExpr ref = (SQLVariantRefExpr) limitExpr.getRowCount();
					limitRefIndex = ref.getIndex();
					limit = (Integer) newParams.get(ref.getIndex());
					if (!isSingleTarget) {
						rr.getMergeContext().setLimit(limit);
						if (originOffset != Integer.MIN_VALUE) {
							limit = originOffset + limit;
						}
					}
				}

				if (offsetRefIndex > limitRefIndex && offsetRefIndex != -1 && limitRefIndex != -1) {
					newParams.set(limitRefIndex, offset);
					newParams.set(offsetRefIndex, limit);
				} else {
					if (limitRefIndex != -1) {
						newParams.set(limitRefIndex, limit);
					}

					if (offsetRefIndex != -1) {
						newParams.set(offsetRefIndex, offset);
					}
				}
			}
		}

		return newParams;
	}


	// batch insert
	private void buildBatchInsertSqls(ShardEvalResult shardResult, SQLParsedResult parseResult, String logicTable, RouterResult routerResult) {
		List<RouterTarget> sqls = new ArrayList<RouterTarget>();
		Map<String, Set<String>> dbAndTables = shardResult.getDbAndTables();
		Map<String, Map<String, Set<Integer>>> insertClauseIndexMap = shardResult.getInsertClauseIndexMap();

		MySqlInsertStatement stmt = (MySqlInsertStatement) parseResult.getStmt();
		int clauseColumnSize = stmt.getColumns().size();

		for (Entry<String, Set<String>> entry : dbAndTables.entrySet()) {
			String database = entry.getKey();
			RouterTarget targetedSql = new RouterTarget(database);
			Map<String, Set<Integer>> tbParamIndexMap = insertClauseIndexMap.get(database);

			int tableIndex = 0;
			for (String physicalTable : entry.getValue()) {
				Set<Integer> paramIndexes = null;
				if (tbParamIndexMap != null) {
					paramIndexes = tbParamIndexMap.get(physicalTable);
				}
				String newSql = sqlRewrite.rewrite(parseResult, logicTable, physicalTable, paramIndexes, null);

				String hintComment = parseResult.getRouterContext().getSqlhint().getHintComments();

				if (hintComment != null) {
					newSql = hintComment + newSql;
				}
				targetedSql.addSql(newSql);
				targetedSql.addPhysicalTable(physicalTable);

				List<Integer> mappingList = new ArrayList<Integer>();
				if (paramIndexes != null) {
					for (Integer index : paramIndexes) {
						for(int i = index * clauseColumnSize; i < (index + 1) * clauseColumnSize; ++i) {
							mappingList.add(i+1);
						}
					}
				}
				targetedSql.putParamIndexMappingList(tableIndex, mappingList);
				tableIndex++;
			}

			sqls.add(targetedSql);
		}

		routerResult.setSqls(sqls);
	}

	public boolean isSingleRouterTarget(RouterResult routerResult) {
		if (routerResult.getSqls().size() > 1) {
			return false;
		}
		RouterTarget routerTarget = routerResult.getSqls().get(0);

		if (routerTarget.getSqls().size() > 1) {
			return false;
		}

		return true;
	}

	public boolean isOptimizeShardKeyInSql() {
		return optimizeShardKeyInSql;
	}

	public void setOptimizeShardKeyInSql(boolean optimizeShardKeyInSql) {
		this.optimizeShardKeyInSql = optimizeShardKeyInSql;
	}
}
