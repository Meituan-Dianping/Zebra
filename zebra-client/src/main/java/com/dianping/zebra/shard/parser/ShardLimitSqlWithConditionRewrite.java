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
package com.dianping.zebra.shard.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.dianping.zebra.shard.merge.ColumnData;
import com.dianping.zebra.shard.merge.MergeContext;
import com.dianping.zebra.shard.merge.RowData;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ShardLimitSqlWithConditionRewrite {
	public String rewrite(String limitSql, RowData startData, RowData endData, MergeContext context, List<Object> params) {
		SQLStatement stmt = SQLParser.parseWithoutCache(limitSql).getStmt();

		StringBuilder out = new StringBuilder();
		ShardLimitSqlConditionVisitor visitor = new ShardLimitSqlConditionVisitor(out, startData, endData, context,
		      params);

		stmt.accept(visitor);

		return out.toString();
	}

	class ShardLimitSqlConditionVisitor extends MySqlOutputVisitor {

		private SQLExpr nullExpr = new SQLNullExpr();

		private RowData startData;

		private RowData endData;

		private MergeContext context;

		private SQLExpr startCondition = null;

		private SQLExpr endCondition = null;

		private List<Object> params;

		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		public ShardLimitSqlConditionVisitor(Appendable appender, RowData startData, RowData endData,
		      MergeContext context, List<Object> params) {
			super(appender);

			this.startData = startData;
			this.endData = endData;
			this.context = context;
			this.params = params;
		}

		@Override
		public boolean visit(MySqlSelectQueryBlock x) {
			initConditionExpr();

			SQLBinaryOpExpr condition = new SQLBinaryOpExpr(startCondition, SQLBinaryOperator.BooleanAnd, endCondition);
			if (x.getWhere() != null) {
				x.setWhere(new SQLBinaryOpExpr(condition, SQLBinaryOperator.BooleanAnd, x.getWhere()));
			} else {
				x.setWhere(condition);
			}
			if (x.getLimit() != null) {
				// 这里去掉本来的limit，并加上split后的offset+limit
				int index1 = -1;
				int index2 = -1;
				if (x.getLimit().getOffset() instanceof SQLVariantRefExpr && params != null && params.size() > 0) {
					SQLVariantRefExpr ref = (SQLVariantRefExpr) x.getLimit().getOffset();
					index1 = ref.getIndex();
				}
				if (x.getLimit().getRowCount() instanceof SQLVariantRefExpr && params != null && params.size() > 0) {
					SQLVariantRefExpr ref = (SQLVariantRefExpr) x.getLimit().getRowCount();
					index2 = ref.getIndex();
				}
				// 先去掉大index的值，然后去掉小index的值，以免使需要去掉元素在list的位置发生变化
				if (Math.max(index1, index2) >= 0) {
					params.remove(Math.max(index1, index2));
				}
				if (Math.min(index1, index2) >= 0) {
					params.remove(Math.min(index1, index2));
				}

				int splitOffset = context.getOffset() == MergeContext.NO_OFFSET ? 0 : context.getOffset();
				int splitLimit = context.getLimit() == MergeContext.NO_LIMIT ? 0 : context.getLimit();
				// 因为splitOffset是去尾法得到了，所以+1就能保证splitNum * (splitOffset + splitLimit + 1) > (offset + limit)
				SQLNumberExpr rowCount = new SQLNumberExpr(splitOffset + splitLimit + 1);
				MySqlSelectQueryBlock.Limit limit = new MySqlSelectQueryBlock.Limit(rowCount);

				x.setLimit(limit);
			}

			return super.visit(x);
		}

		private void initConditionExpr() {
			SQLBinaryOperator startOperator, endOperator;
			// 上下线每个条件的边际表达式
			SQLBinaryOpExpr startBoundaryExpr = null, endBoundaryExpr = null;

			for (SQLSelectOrderByItem orderByEle : context.getOrderBy().getItems()) {
				if (orderByEle.getType() == null || ((SQLOrderingSpecification) orderByEle.getType()).name().equals("ASC")) {
					startOperator = SQLBinaryOperator.GreaterThan;
					endOperator = SQLBinaryOperator.LessThan;
				} else {
					startOperator = SQLBinaryOperator.LessThan;
					endOperator = SQLBinaryOperator.GreaterThan;
				}
				try {
					SQLExpr starColumnExpr = convertColumnDataToSQLExpr(startData.get(orderByEle.getExpr().toString()));
					SQLExpr endColumnExpr = convertColumnDataToSQLExpr(endData.get(orderByEle.getExpr().toString()));
					SQLExpr compareExpr = orderByEle.getExpr();
					// 如果orderby的column是一个表达式的结果，则把表达式作为新条件的比较变量
					SQLObjectImpl selectItem = context.getSelectItemMap().get(orderByEle.getExpr().toString());
					if (selectItem == null) {
						selectItem = context.getSelectItemMap().get(orderByEle.getExpr().toString().toLowerCase());

						if (selectItem == null) {
							selectItem = context.getSelectItemMap().get(orderByEle.getExpr().toString().toUpperCase());
						}
					}

					if (selectItem != null) {
						if (selectItem instanceof SQLSelectItem) {
							compareExpr = ((SQLSelectItem) selectItem).getExpr();
						}
					}

					SQLBinaryOpExpr startExpr = new SQLBinaryOpExpr(compareExpr, startOperator, starColumnExpr);
					SQLBinaryOpExpr endExpr = new SQLBinaryOpExpr(compareExpr, endOperator, endColumnExpr);

					if (startBoundaryExpr != null) {
						// 在上一个边际表达式的基础上加上and条件
						addStartConditionExpr(new SQLBinaryOpExpr(startBoundaryExpr, SQLBinaryOperator.BooleanAnd, startExpr));
						addEndConditionExpr(new SQLBinaryOpExpr(endBoundaryExpr, SQLBinaryOperator.BooleanAnd, endExpr));
						startBoundaryExpr = new SQLBinaryOpExpr(startBoundaryExpr, SQLBinaryOperator.BooleanAnd,
						      new SQLBinaryOpExpr(compareExpr, SQLBinaryOperator.Equality, starColumnExpr));
						endBoundaryExpr = new SQLBinaryOpExpr(endBoundaryExpr, SQLBinaryOperator.BooleanAnd,
						      new SQLBinaryOpExpr(compareExpr, SQLBinaryOperator.Equality, endColumnExpr));
					} else {
						addStartConditionExpr(startExpr);
						addEndConditionExpr(endExpr);
						startBoundaryExpr = new SQLBinaryOpExpr(compareExpr, SQLBinaryOperator.Equality, starColumnExpr);
						endBoundaryExpr = new SQLBinaryOpExpr(compareExpr, SQLBinaryOperator.Equality, endColumnExpr);
					}
				} catch (SQLException ignore) {
				}
			}
			// 最后补上边际条件
			addStartConditionExpr(startBoundaryExpr);
			addEndConditionExpr(endBoundaryExpr);
		}

		private SQLExpr convertColumnDataToSQLExpr(ColumnData columnData) {
			Object value = columnData.getValue();

			if (columnData.isWasNull()) {
				return nullExpr;
			} else if (value instanceof Boolean) {
				return new SQLBooleanExpr((Boolean) columnData.getValue());
			} else if (value instanceof Integer || value instanceof Long || value instanceof BigInteger
			      || value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
				return new SQLNumberExpr((Number) columnData.getValue());
			} else if (columnData.getValue() instanceof Byte[]) {
				return new SQLBinaryExpr(columnData.getValue().toString());
			} else if (columnData.getValue() instanceof Date) {
				return new SQLCharExpr(sdf.format((Date) columnData.getValue()));
			} else {
				return new SQLCharExpr((String) columnData.getValue());
			}
		}

		private void addStartConditionExpr(SQLExpr addCondition) {
			if (startCondition == null) {
				startCondition = addCondition;
			} else {
				startCondition = new SQLBinaryOpExpr(startCondition, SQLBinaryOperator.BooleanOr, addCondition);
			}
		}

		private void addEndConditionExpr(SQLExpr addCondition) {
			if (endCondition == null) {
				endCondition = addCondition;
			} else {
				endCondition = new SQLBinaryOpExpr(endCondition, SQLBinaryOperator.BooleanOr, addCondition);
			}
		}
	}
}
