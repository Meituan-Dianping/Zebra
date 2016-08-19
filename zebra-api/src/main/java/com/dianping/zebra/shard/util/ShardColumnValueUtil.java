/**
 * Project: com.dianping.zebra.zebra-client-0.1.0
 *
 * File Created at 2011-6-17
 * $Id$
 *
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.util;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement.ValuesClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.dianping.zebra.shard.api.ShardDataSourceHelper;
import com.dianping.zebra.shard.exception.ShardRouterException;
import com.dianping.zebra.shard.parser.SQLParsedResult;
import com.dianping.zebra.shard.router.rule.ShardEvalContext;
import com.dianping.zebra.shard.router.rule.ShardEvalContext.ColumnValue;
import com.dianping.zebra.util.SqlType;

import java.util.*;

/**
 * 计算相关DML语句对于指定的column可能的值
 *
 * @author hao.zhu
 */
public class ShardColumnValueUtil {

	public static List<ColumnValue> eval(ShardEvalContext ctx, Set<String> shardColumns) {
		List<ColumnValue> result = new LinkedList<ColumnValue>();

		Map<Integer, Map<String, Object>> tmpResult = new LinkedHashMap<Integer, Map<String, Object>>();

		// get shard column params from threadlocal
		for (String shardColumn : shardColumns) {
			List<Object> datas = ShardDataSourceHelper.getShardParams(shardColumn);
			if (datas != null) {
				int index = 0;
				for (Object data : datas) {
					Map<String, Object> map = tmpResult.get(index);

					if (map == null) {
						map = new HashMap<String, Object>();
						tmpResult.put(index, map);
					}

					map.put(shardColumn, data);
					index++;
				}
			}
		}

		// get shard column params from sql or params
		if (!ShardDataSourceHelper.extractParamsOnlyFromThreadLocal()) {
			for (String shardColumn : shardColumns) {
				Set<Object> columnValues = eval(ctx.getParseResult(), ctx.getParams(), shardColumn);
				int index = 0;
				for (Object o : columnValues) {
					Map<String, Object> map = tmpResult.get(index);

					if (map == null) {
						map = new HashMap<String, Object>();
						tmpResult.put(index, map);
					}

					map.put(shardColumn, o);
					index++;
				}
			}
		}

		for (Map<String, Object> columnValues : tmpResult.values()) {
			ColumnValue columnValue = new ColumnValue(columnValues);
			result.add(columnValue);
		}

		return result;
	}

	private static Set<Object> eval(SQLParsedResult parseResult, List<Object> params, String column) {
		if (parseResult.getType() == SqlType.INSERT) {
			return evalInsert(parseResult, column, params);
		}

		Set<Object> result = new LinkedHashSet<Object>();
		// TODO handle table in the future
		SQLExpr where = getWhere(parseResult);
		if (where != null) {
			List<Pair> pairs = new ArrayList<Pair>();

			eval(where, pairs);

			for (Pair pair : pairs) {
				String identifier = pair.getIdentifier();
				Object value = pair.getObj();
				SQLBinaryOperator operator = pair.getOperator();

				if (evalColumn(identifier, column)) {
					if (value instanceof SQLValuableExpr && operator == SQLBinaryOperator.Equality) {
						result.add(((SQLValuableExpr) value).getValue());
					} else if (value instanceof SQLVariantRefExpr && operator == SQLBinaryOperator.Equality) {
						SQLVariantRefExpr ref = (SQLVariantRefExpr) value;
						result.add(params.get(ref.getIndex()));
					} else if (value instanceof SQLInListExpr) {
						SQLInListExpr inListExpr = (SQLInListExpr) value;

						for (SQLExpr expr : inListExpr.getTargetList()) {
							if (expr instanceof SQLValuableExpr) {
								result.add(((SQLValuableExpr) expr).getValue());
							} else if (expr instanceof SQLVariantRefExpr) {
								SQLVariantRefExpr ref = (SQLVariantRefExpr) expr;
								result.add(params.get(ref.getIndex()));
							}
						}
					}
				}
			}
		}

		return result;
	}

	private static SQLExpr getWhere(SQLParsedResult parseResult) {
		SQLExpr expr = null;
		SQLStatement stmt = parseResult.getStmt();

		if (parseResult.getType() == SqlType.SELECT) {
			MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) (((SQLSelectStatement) stmt).getSelect()).getQuery();
			expr = query.getWhere();
		} else if (parseResult.getType() == SqlType.UPDATE) {
			expr = ((MySqlUpdateStatement) stmt).getWhere();
		} else if (parseResult.getType() == SqlType.DELETE) {
			expr = ((MySqlDeleteStatement) stmt).getWhere();
		}

		return expr;
	}

	private static void eval(SQLExpr sqlExpr, List<Pair> pairs) {
		SQLBinaryOpExpr where = null;
		if (sqlExpr instanceof SQLBinaryOpExpr) {
			where = (SQLBinaryOpExpr) sqlExpr;
		} else if (sqlExpr instanceof SQLInListExpr) {
			SQLInListExpr inListExpr = (SQLInListExpr) sqlExpr;
			SQLName indentifier = (SQLName) inListExpr.getExpr();
			pairs.add(new Pair(indentifier.getSimpleName(), null, inListExpr));
			return;
		} else {
			return;
		}

		SQLBinaryOperator operator = where.getOperator();

		if (operator != SQLBinaryOperator.Equality && operator != SQLBinaryOperator.GreaterThan
				&& operator != SQLBinaryOperator.GreaterThanOrEqual && operator != SQLBinaryOperator.LessThan
				&& operator != SQLBinaryOperator.LessThanOrEqual && operator != SQLBinaryOperator.NotEqual) {
			eval(where.getLeft(), pairs);
			eval(where.getRight(), pairs);

			return;
		} else {
			SQLExpr left = where.getLeft();
			SQLExpr right = where.getRight();

			if (left instanceof SQLIdentifierExpr) {
				SQLIdentifierExpr indentifier = (SQLIdentifierExpr) left;
				pairs.add(new Pair(indentifier.getName(), operator, right));
			} else if (left instanceof SQLPropertyExpr) {
				SQLPropertyExpr indentifier = (SQLPropertyExpr) left;
				pairs.add(new Pair(indentifier.getName(), operator, right));
			} else if (left instanceof SQLBinaryOpExpr) {
				eval((SQLBinaryOpExpr) left, pairs);
				eval((SQLBinaryOpExpr) right, pairs);
			}
		}
	}

	private static boolean evalColumn(String indentifier, String column) {
		return column.equalsIgnoreCase(indentifier) || ("`" + column + "`").equalsIgnoreCase(indentifier);
	}

	private static Set<Object> evalInsert(SQLParsedResult parseResult, String column, List<Object> params) {
		Set<Object> evalSet = new LinkedHashSet<Object>();
		MySqlInsertStatement stmt = (MySqlInsertStatement) parseResult.getStmt();

		List<SQLExpr> columns = stmt.getColumns();
		List<ValuesClause> valuesList = stmt.getValuesList();

		if (valuesList.size() > 1) {
			throw new ShardRouterException("Multipal rows insertion is currently unsupported!");
		}

		ValuesClause values = valuesList.get(0);
		for (int i = 0; i < columns.size(); i++) {
			SQLName columnObj = (SQLName) columns.get(i);
			if (evalColumn(columnObj.getSimpleName(), column)) {
				SQLExpr sqlExpr = values.getValues().get(i);
				if (sqlExpr instanceof SQLVariantRefExpr) {
					SQLVariantRefExpr ref = (SQLVariantRefExpr) sqlExpr;
					evalSet.add(params.get(ref.getIndex()));
//					evalSet.add(params.get(i));
				} else if (sqlExpr instanceof SQLValuableExpr) {
					evalSet.add(((SQLValuableExpr) sqlExpr).getValue());
				}
				break;
			}
		}

		return evalSet;
	}

	private static class Pair {
		private String identifier;

		private SQLBinaryOperator operator;

		private Object obj;

		public Pair(String identifier, SQLBinaryOperator operator, Object obj) {
			super();
			this.identifier = identifier;
			this.operator = operator;
			this.obj = obj;
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
	}
}
