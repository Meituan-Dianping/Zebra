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

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.dianping.zebra.group.jdbc.param.ParamContext;

import java.util.List;

public class SqlToCountSqlRewrite {
	public static final String countAlias = "zebra_count";

	public String rewrite(String sql, List<ParamContext> countParams) {
		MySqlStatementParser parser = new MySqlStatementParser(sql);
		SQLStatement stmt = parser.parseStatement();
		RewriteSqlToCountSqlVisitor visitor = new RewriteSqlToCountSqlVisitor(countParams);
		stmt.accept(visitor);

		return stmt.toString();
	}

	class RewriteSqlToCountSqlVisitor extends MySqlASTVisitorAdapter {

		private List<ParamContext> countParams;

		public RewriteSqlToCountSqlVisitor(List<ParamContext> countParams) {
			this.countParams = countParams;
		}

		@Override
		public boolean visit(MySqlSelectQueryBlock x) {
			List<SQLSelectItem> selectList = x.getSelectList();
			SQLAggregateExpr countExpr = new SQLAggregateExpr("COUNT");
			countExpr.getArguments().add(new SQLAllColumnExpr());
			if (x.getLimit() != null) {
				if (x.getLimit().getOffset() instanceof SQLVariantRefExpr && countParams != null && countParams.size() > 0) {
					SQLVariantRefExpr ref = (SQLVariantRefExpr) x.getLimit().getOffset();
					Object[] values = { 0 };
					countParams.get(ref.getIndex()).setValues(values);
				} else if (x.getLimit().getOffset() != null) {
					x.getLimit().setOffset(new SQLNumberExpr(0));
				}
				if (x.getLimit().getRowCount() instanceof SQLVariantRefExpr && countParams != null
				      && countParams.size() > 0) {
					SQLVariantRefExpr ref = (SQLVariantRefExpr) x.getLimit().getRowCount();
					Object[] values = { 1 };
					countParams.get(ref.getIndex()).setValues(values);
				} else if (x.getLimit().getRowCount() != null) {
					x.getLimit().setRowCount(new SQLNumberExpr(1));
				}
			}
			x.setOrderBy(null);
			selectList.clear();
			selectList.add(new SQLSelectItem(countExpr, countAlias));

			return false;
		}
	}
}
