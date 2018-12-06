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
package com.dianping.zebra.shard.util;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.*;
import com.dianping.zebra.shard.api.ShardDataSourceHelper;
import com.dianping.zebra.shard.parser.SQLParsedResult;
import com.dianping.zebra.shard.router.rule.ShardEvalContext;
import com.dianping.zebra.shard.router.rule.ShardEvalContext.ColumnValue;
import com.dianping.zebra.shard.router.rule.ShardRange;
import com.dianping.zebra.util.SqlType;

import java.util.*;

/**
 * 计算相关DML语句对于指定的column可能的值
 *
 * @author hao.zhu
 */
public class ShardColumnValueUtil {

	public static List<ColumnValue> eval(ShardEvalContext ctx, Set<String> shardColumns, boolean isRange) {
		return eval(ctx, shardColumns, isRange, false);
	}

	public static List<ColumnValue> eval(ShardEvalContext ctx, Set<String> shardColumns, boolean isRange,
	      boolean isBatchInsert) {
		List<ColumnValue> result = new LinkedList<ColumnValue>();
		Map<Integer, Map<String, Object>> tmpResult = new LinkedHashMap<Integer, Map<String, Object>>();
		Map<String, List<SQLExpr>> inExprColumnMap = new LinkedHashMap<String, List<SQLExpr>>();

		// get shard column params from threadLocal
		for (String shardColumn : shardColumns) {
			List<Object> datas = ShardDataSourceHelper.getShardParams(shardColumn);
			if (datas != null) {
				int index = 0;
				for (Object data : datas) {
					addColumnValueToTempResult(tmpResult, index, shardColumn, data);
					index++;
				}
			}
		}

		// get shard column params from sql or params
		if (!ShardDataSourceHelper.extractParamsOnlyFromThreadLocal()) {
			Map<String, Collection<Object>> columnValueMap = new LinkedHashMap<String, Collection<Object>>();
			int maxColumnCount = 0;
			for (String shardColumn : shardColumns) {
				InExprListWrapper inExprWrapper = new InExprListWrapper();
				Collection<Object> columnValues = eval(ctx.getParseResult(), ctx.getParams(), shardColumn, isRange,
				      isBatchInsert, inExprWrapper);
				maxColumnCount = (columnValues.size() > maxColumnCount) ? columnValues.size() : maxColumnCount;
				columnValueMap.put(shardColumn, columnValues);

				// record in expr list
				if (ctx.isOptimizeShardKeyInSql() && inExprWrapper.isContainInExpr()) {
					inExprColumnMap.put(shardColumn, inExprWrapper.getInExprList());
				}
			}

			for (Map.Entry<String, Collection<Object>> entry : columnValueMap.entrySet()) {
				int index = 0;
				String shardColumn = entry.getKey();
				Collection<Object> columnValues = entry.getValue();
				if (columnValues.isEmpty()) {
					continue;
				}

				Object firstValue = null;
				for (Object obj : columnValues) {
					firstValue = (index == 0) ? obj : firstValue;
					addColumnValueToTempResult(tmpResult, index, shardColumn, obj);
					index++;
				}

				// for multi sk alignment
				if (index < maxColumnCount) {
					for (; index < maxColumnCount; index++) {
						addColumnValueToTempResult(tmpResult, index, shardColumn, firstValue);
					}
				}
			}
		}

		ctx.setNeedOptimizeShardKeyInSql(false);
		List<SQLExpr> inSqlExprList = null;
		if (inExprColumnMap.size() > 0) {
			inSqlExprList = inExprColumnMap.values().iterator().next();
		}

		int index = 0;
		int size = shardColumns.size();
		for (Map<String, Object> columnValues : tmpResult.values()) {
			if (columnValues.size() == size) {
				SQLExpr sqlExpr = null;
				if (inSqlExprList != null && index < inSqlExprList.size()) {
					sqlExpr = inSqlExprList.get(index);
					ctx.setNeedOptimizeShardKeyInSql(true);
				}

				ColumnValue columnValue = new ColumnValue(columnValues, sqlExpr);
				result.add(columnValue);
			}
			index++;
		}

		return result;
	}

	private static void addColumnValueToTempResult(Map<Integer, Map<String, Object>> tmpResult, int index,
	      String shardColumn, Object obj) {
		Map<String, Object> map = tmpResult.get(index);

		if (map == null) {
			map = new HashMap<String, Object>();
			tmpResult.put(index, map);
		}

		map.put(shardColumn, obj);
	}

	private static Object parseValue(Object value, List<Object> params) {
		if (value instanceof SQLValuableExpr) {
			return ((SQLValuableExpr) value).getValue();
		} else if (value instanceof SQLVariantRefExpr) {
			SQLVariantRefExpr ref = (SQLVariantRefExpr) value;
			return params.get(ref.getIndex());
		}

		return value;
	}

	private static Collection<Object> eval(SQLParsedResult parseResult, List<Object> params, String column,
	      boolean isRange, boolean isBatchInsert, InExprListWrapper inExprWrapper) {
		if (parseResult.getType() == SqlType.INSERT) {
			return evalInsert(parseResult, column, params, isBatchInsert);
		} else if (parseResult.getType() == SqlType.REPLACE) { // add for replace
			MySqlReplaceStatement stmt = (MySqlReplaceStatement) parseResult.getStmt();
			// if query != null, there is a sub query in replace
			if (stmt.getQuery() == null) {
				return evalReplace(parseResult, column, params);
			}
		}

		List<Object> result = new LinkedList<Object>();
		SQLExpr where = getWhere(parseResult);
		if (where != null) {
			List<Pair> pairs = new ArrayList<Pair>();

			eval(where, pairs, isRange);

			Set<ShardRange> rangeParams = new LinkedHashSet<ShardRange>();
			for (Pair pair : pairs) {
				String identifier = pair.getIdentifier();
				Object value = pair.getObj();
				SQLBinaryOperator operator = pair.getOperator();

				if (evalColumn(identifier, column)) {
					if (value instanceof SQLValuableExpr || value instanceof SQLVariantRefExpr) {
						if (operator == SQLBinaryOperator.Equality) {
							Object newValue = parseValue(value, params);
							if (isRange) {
								rangeParams.add(new ShardRange(ShardRange.OP_Equal, newValue));
							} else {
								result.add(newValue);
							}

							// in optimize, multi in here
							inExprWrapper.add(pair.getInSqlExpr());
						} else if (isRange
						      && (operator == SQLBinaryOperator.GreaterThan
						            || operator == SQLBinaryOperator.GreaterThanOrEqual
						            || operator == SQLBinaryOperator.LessThan || operator == SQLBinaryOperator.LessThanOrEqual)) {
							Object newValue = parseValue(value, params);
							rangeParams.add(new ShardRange(operator, newValue));
							inExprWrapper.add(null);
						} else {
							inExprWrapper.clear();
							return new LinkedHashSet<Object>();
						}

					} else if (value instanceof SQLInListExpr) {
						SQLInListExpr inListExpr = (SQLInListExpr) value;

						Set<Object> inSet = new LinkedHashSet<Object>();
						for (SQLExpr expr : inListExpr.getTargetList()) {
							if (expr instanceof SQLValuableExpr || expr instanceof SQLVariantRefExpr) {
								Object newValue = parseValue(expr, params);
								if (isRange) {
									inSet.add(newValue);
								} else {
									result.add(newValue);
								}
								inExprWrapper.add(expr);
							}
						}
						if (isRange) {
							rangeParams.add(new ShardRange(ShardRange.OP_InList, inSet));
						}
					} else {
						// @author keren.chen 2016-11-02 如果还包含其它表达式，返回为空
						inExprWrapper.clear();
						return new LinkedHashSet<Object>();
					}
				}
			}

			if (isRange && !rangeParams.isEmpty()) {
				result.add(rangeParams);
			}
		}

		return result;
	}

	private static SQLExpr getWhere(SQLParsedResult parseResult) {
		SQLExpr expr = null;
		SQLStatement stmt = parseResult.getStmt();

		if (parseResult.getType() == SqlType.SELECT || parseResult.getType() == SqlType.SELECT_FOR_UPDATE) {
			MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) (((SQLSelectStatement) stmt).getSelect()).getQuery();
			expr = query.getWhere();
		} else if (parseResult.getType() == SqlType.UPDATE) {
			expr = ((MySqlUpdateStatement) stmt).getWhere();
		} else if (parseResult.getType() == SqlType.DELETE) {
			expr = ((MySqlDeleteStatement) stmt).getWhere();
		} else if (parseResult.getType() == SqlType.REPLACE) { // add for replace
			MySqlReplaceStatement replaceStatement = (MySqlReplaceStatement) stmt;
			SQLQueryExpr queryExpr = replaceStatement.getQuery();
			if (queryExpr != null) {
				SQLSelect sqlSelect = queryExpr.getSubQuery();
				sqlSelect.getQuery();
				if (sqlSelect != null) {
					MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) sqlSelect.getQuery();
					if (queryBlock != null) {
						expr = queryBlock.getWhere();
					}
				}
			}
		}

		return expr;
	}

	private static void eval(SQLExpr sqlExpr, List<Pair> pairs, boolean isRange) {
		SQLBinaryOpExpr where = null;
		if (sqlExpr instanceof SQLBinaryOpExpr) {
			where = (SQLBinaryOpExpr) sqlExpr;
		} else if (sqlExpr instanceof SQLInListExpr) {
			SQLInListExpr inListExpr = (SQLInListExpr) sqlExpr;
			SQLExpr inLExpr = inListExpr.getExpr();
			if (inLExpr instanceof SQLListExpr) {
				for (SQLExpr expr : inListExpr.getTargetList()) {
					SQLListExpr valueLExpr = (SQLListExpr) expr;
					SQLListExpr columnLExpr = (SQLListExpr) inLExpr;

					for (int i = 0; i < columnLExpr.getItems().size(); ++i) {
						SQLName columnName = (SQLName) columnLExpr.getItems().get(i);
						SQLExpr columnValue = valueLExpr.getItems().get(i);
						pairs.add(new Pair(columnName.getSimpleName(), SQLBinaryOperator.Equality, columnValue, expr));
					}
				}
			} else {
				SQLName identifier = (SQLName) inListExpr.getExpr();
				pairs.add(new Pair(identifier.getSimpleName(), null, inListExpr));
			}
			return;
		} else if (isRange && sqlExpr instanceof SQLBetweenExpr) {
			SQLBetweenExpr betweenExpr = (SQLBetweenExpr) sqlExpr;
			SQLExpr testExpr = betweenExpr.getTestExpr();
			if (testExpr instanceof SQLIdentifierExpr) {
				SQLIdentifierExpr identifierExpr = (SQLIdentifierExpr) testExpr;
				pairs.add(new Pair(identifierExpr.getName(), SQLBinaryOperator.GreaterThanOrEqual, betweenExpr
				      .getBeginExpr()));
				pairs.add(new Pair(identifierExpr.getName(), SQLBinaryOperator.LessThanOrEqual, betweenExpr.getEndExpr()));
			} else if (testExpr instanceof SQLPropertyExpr) {
				SQLPropertyExpr identifierExpr = (SQLPropertyExpr) testExpr;
				pairs.add(new Pair(identifierExpr.getName(), SQLBinaryOperator.GreaterThanOrEqual, betweenExpr
				      .getBeginExpr()));
				pairs.add(new Pair(identifierExpr.getName(), SQLBinaryOperator.LessThanOrEqual, betweenExpr.getEndExpr()));
			}
			return;
		} else {
			return;
		}

		SQLBinaryOperator operator = where.getOperator();

		if (operator != SQLBinaryOperator.Equality && operator != SQLBinaryOperator.GreaterThan
		      && operator != SQLBinaryOperator.GreaterThanOrEqual && operator != SQLBinaryOperator.LessThan
		      && operator != SQLBinaryOperator.LessThanOrEqual && operator != SQLBinaryOperator.NotEqual
		      && operator != SQLBinaryOperator.BitwiseAnd) {
			eval(where.getLeft(), pairs, isRange);
			eval(where.getRight(), pairs, isRange);

			return;
		} else {
			SQLExpr left = where.getLeft();
			SQLExpr right = where.getRight();

			if (left instanceof SQLIdentifierExpr) {
				SQLIdentifierExpr identifier = (SQLIdentifierExpr) left;
				pairs.add(new Pair(identifier.getName(), operator, right));
			} else if (left instanceof SQLPropertyExpr) {
				SQLPropertyExpr identifier = (SQLPropertyExpr) left;
				pairs.add(new Pair(identifier.getName(), operator, right));
			} else if (left instanceof SQLBinaryOpExpr) {
				eval((SQLBinaryOpExpr) left, pairs, isRange);
				if (right instanceof SQLBinaryOpExpr) {
					eval((SQLBinaryOpExpr) right, pairs, isRange);
				}
			}
		}
	}

	private static boolean evalColumn(String identifier, String column) {
		return column.equalsIgnoreCase(identifier) || ("`" + column + "`").equalsIgnoreCase(identifier);
	}

	private static Collection<Object> evalInsert(SQLParsedResult parseResult, String column, List<Object> params,
	      boolean isBatchInsert) {
		MySqlInsertStatement stmt = (MySqlInsertStatement) parseResult.getStmt();

		List<SQLExpr> columns = stmt.getColumns();
		List<SQLInsertStatement.ValuesClause> valuesList = stmt.getValuesList();

		if (isBatchInsert) {
			List<Object> evalList = new LinkedList<Object>();
			parseBatchValueList(evalList, params, columns, valuesList, column);
			return evalList;
		} else {
			// use the first value in the values
			Set<Object> evalSet = new LinkedHashSet<Object>();
			parseValueList(evalSet, params, columns, valuesList, column);
			return evalSet;
		}
	}

	// add for replace
	private static Set<Object> evalReplace(SQLParsedResult parseResult, String column, List<Object> params) {
		Set<Object> evalSet = new LinkedHashSet<Object>();
		MySqlReplaceStatement stmt = (MySqlReplaceStatement) parseResult.getStmt();
		List<SQLExpr> columns = stmt.getColumns();
		List<SQLInsertStatement.ValuesClause> valuesList = stmt.getValuesList();

		// use the first value in the values
		parseValueList(evalSet, params, columns, valuesList, column);

		return evalSet;
	}

	private static void parseValueList(Set<Object> evalSet, List<Object> params, List<SQLExpr> columns,
	      List<SQLInsertStatement.ValuesClause> valuesList, String column) {
		SQLInsertStatement.ValuesClause values = valuesList.get(0);
		for (int i = 0; i < columns.size(); i++) {
			SQLName columnObj = (SQLName) columns.get(i);
			if (evalColumn(columnObj.getSimpleName(), column)) {
				SQLExpr sqlExpr = values.getValues().get(i);
				if (sqlExpr instanceof SQLVariantRefExpr) {
					SQLVariantRefExpr ref = (SQLVariantRefExpr) sqlExpr;
					evalSet.add(params.get(ref.getIndex()));
				} else if (sqlExpr instanceof SQLValuableExpr) {
					evalSet.add(((SQLValuableExpr) sqlExpr).getValue());
				}
				break;
			}
		}
	}

	private static void parseBatchValueList(List<Object> evalList, List<Object> params, List<SQLExpr> columns,
	      List<SQLInsertStatement.ValuesClause> valuesList, String column) {
		int shardColumnIndex = -1;
		for (SQLInsertStatement.ValuesClause clause : valuesList) {
			if (shardColumnIndex >= 0) {
				SQLName columnObj = (SQLName) columns.get(shardColumnIndex);
				if (evalColumn(columnObj.getSimpleName(), column)) {
					SQLExpr sqlExpr = clause.getValues().get(shardColumnIndex);
					if (sqlExpr instanceof SQLVariantRefExpr) {
						SQLVariantRefExpr ref = (SQLVariantRefExpr) sqlExpr;
						evalList.add(params.get(ref.getIndex()));
					} else if (sqlExpr instanceof SQLValuableExpr) {
						evalList.add(((SQLValuableExpr) sqlExpr).getValue());
					}
				}
			} else {
				for (int i = 0; i < columns.size(); i++) {
					SQLName columnObj = (SQLName) columns.get(i);
					if (evalColumn(columnObj.getSimpleName(), column)) {
						SQLExpr sqlExpr = clause.getValues().get(i);
						if (sqlExpr instanceof SQLVariantRefExpr) {
							SQLVariantRefExpr ref = (SQLVariantRefExpr) sqlExpr;
							evalList.add(params.get(ref.getIndex()));
						} else if (sqlExpr instanceof SQLValuableExpr) {
							evalList.add(((SQLValuableExpr) sqlExpr).getValue());
						}
						shardColumnIndex = i;
						break;
					}
				}
			}
		}

	}

	private static class Pair {
		private String identifier;

		private SQLBinaryOperator operator;

		private Object obj;

		private SQLExpr inSqlExpr;

		public Pair(String identifier, SQLBinaryOperator operator, Object obj) {
			super();
			this.identifier = identifier;
			this.operator = operator;
			this.obj = obj;
		}

		public Pair(String identifier, SQLBinaryOperator operator, Object obj, SQLExpr inSqlExpr) {
			super();
			this.identifier = identifier;
			this.operator = operator;
			this.obj = obj;
			this.inSqlExpr = inSqlExpr;
		}

		public String getIdentifier() {
			return identifier;
		}

		public SQLBinaryOperator getOperator() {
			return operator;
		}

		public Object getObj() {
			return obj;
		}

		public SQLExpr getInSqlExpr() {
			return inSqlExpr;
		}
	}

	private static class InExprListWrapper {
		private boolean containInExpr;

		private List<SQLExpr> inExprList;

		public InExprListWrapper() {
			this.inExprList = new ArrayList<SQLExpr>();
		}

		public void add(SQLExpr expr) {
			this.inExprList.add(expr);
			if (expr != null) {
				this.containInExpr = true;
			}
		}

		public void clear() {
			this.inExprList.clear();
		}

		public boolean isContainInExpr() {
			return containInExpr;
		}

		public List<SQLExpr> getInExprList() {
			return inExprList;
		}
	}
}
