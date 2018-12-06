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
package com.dianping.zebra.shard.parser.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.dianping.zebra.shard.parser.SQLParsedResult;

public class MySQLSelectASTVisitor extends AbstractMySQLASTVisitor {

	public MySQLSelectASTVisitor(SQLParsedResult result) {
		super(result);
	}

	@Override
	public boolean visit(MySqlSelectQueryBlock x) {
		result.getMergeContext().increQueryCount();
		Map<String, SQLObjectImpl> selectItemMap = result.getMergeContext().getSelectItemMap();
		Map<String, String> columnNameAliasMapping = result.getMergeContext().getColumnNameAliasMapping();

		for (SQLSelectItem column : x.getSelectList()) {
			String name = null;
			if (column.getExpr() instanceof SQLAggregateExpr) {
				SQLAggregateExpr expr = (SQLAggregateExpr) column.getExpr();
				SQLExpr argument = expr.getArguments().get(0);
				if (argument instanceof SQLAllColumnExpr) {
					name = expr.getMethodName() + "(*)";
				} else if (argument instanceof SQLIntegerExpr) {
					name = expr.getMethodName() + "(1)";
				} else {
					name = expr.getMethodName() + "(" + argument.toString() + ")";
					if (column.getAlias() != null) {
						columnNameAliasMapping.put(name, column.getAlias());
					}
				}

				result.getMergeContext().setAggregate(true);
			} else if (column.getExpr() instanceof SQLIdentifierExpr || column.getExpr() instanceof SQLPropertyExpr) {
				name = ((SQLName) column.getExpr()).getSimpleName();

				if (column.getAlias() != null) {
					SQLName identifier = (SQLName) column.getExpr();
					columnNameAliasMapping.put(identifier.getSimpleName(), column.getAlias());
				}
			} else {
				// ignore SQLAllColumnExpr,SQLMethodInvokeExpr and etc.
			}

			selectItemMap.put(column.getAlias() == null ? name : column.getAlias(), column);
		}

		if (x.getDistionOption() == 2) {
			result.getMergeContext().setDistinct(true);
		}

		return true;
	}

	@Override
	public boolean visit(MySqlSelectQueryBlock.Limit x) {
		if (x.getOffset() instanceof SQLIntegerExpr) {
			SQLIntegerExpr offsetExpr = (SQLIntegerExpr) x.getOffset();
			if (offsetExpr != null) {
				int offset = offsetExpr.getNumber().intValue();
				result.getMergeContext().setOffset(offset);
			}
		}

		if (x.getRowCount() instanceof SQLIntegerExpr) {
			SQLIntegerExpr rowCountExpr = (SQLIntegerExpr) x.getRowCount();
			if (rowCountExpr != null) {
				int limit = rowCountExpr.getNumber().intValue();
				result.getMergeContext().setLimit(limit);
			}
		}

		result.getMergeContext().setLimitExpr(x);
		return true;
	}

	@Override
	public boolean visit(SQLSelectGroupByClause x) {
		result.getMergeContext().increGroupByCount();
		List<String> groupByColumns = new ArrayList<String>();
		List<SQLExpr> items = x.getItems();

		for (SQLExpr expr : items) {
			groupByColumns.add(((SQLName) expr).getSimpleName());
		}

		result.getMergeContext().setGroupByColumns(groupByColumns);
		return true;
	}

	@Override
	public boolean visit(SQLOrderBy x) {
		result.getMergeContext().increOrderByCount();
		result.getMergeContext().setOrderBy(x);
		return true;
	}

	@Override
	public boolean visit(SQLUnionQuery x) {
		result.getMergeContext().increUnionCount();
		return super.visit(x);
	}

	@Override
	public boolean visit(SQLJoinTableSource x) {
		result.getMergeContext().increJoinCount();
		return super.visit(x);
	}
}
