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
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;

import java.util.List;

public class ShardLimitSqlSplitRewrite {
	public String rewrite(SQLParsedResult limitSql, int splitNum, List<Object> params) {
		SQLStatement stmt = limitSql.getStmt();

		StringBuilder out = new StringBuilder();
		ShardLimitSqlSplitVisior visitor = new ShardLimitSqlSplitVisior(out, splitNum, params);

		stmt.accept(visitor);

		return out.toString();
	}

	class ShardLimitSqlSplitVisior extends MySqlOutputVisitor {
		private int splitNum;

		private List<Object> params;

		public ShardLimitSqlSplitVisior(Appendable appender, int splitNum, List<Object> params) {
			super(appender);
			this.splitNum = splitNum;
			this.params = params;
		}

		@Override
		public boolean visit(MySqlSelectQueryBlock x) {
			if (x.getLimit() != null) {
				if (x.getLimit().getOffset() != null) {
					if (x.getLimit().getOffset() instanceof SQLVariantRefExpr && params != null && params.size() > 0) {
						SQLVariantRefExpr ref = (SQLVariantRefExpr) x.getLimit().getOffset();
						Integer offset = (Integer) params.get(ref.getIndex());
						params.set(ref.getIndex(), offset / this.splitNum);
					} else {
						SQLIntegerExpr offset = (SQLIntegerExpr) x.getLimit().getOffset();
						// 去尾法是符合算法的
						offset.setNumber(offset.getNumber().longValue() / this.splitNum);
					}
				}
			}

			return super.visit(x);
		}
	}
}
