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

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.dianping.zebra.shard.parser.SQLParsedResult;

public class AbstractMySQLASTVisitor extends MySqlASTVisitorAdapter {

	protected SQLParsedResult result;

	public AbstractMySQLASTVisitor(SQLParsedResult result) {
		this.result = result;
	}

	@Override
	public boolean visit(SQLExprTableSource x) {
		SQLName table = (SQLName) x.getExpr();
		String simpleName = table.getSimpleName();
		String tableName = simpleName.startsWith("`") ? parseTableName(simpleName) : simpleName;

		result.getRouterContext().getTableSet().add(tableName);

		return true;
	}

	private String parseTableName(String tableName) {
		StringBuilder sb = new StringBuilder(tableName.length());
		for (int i = 0; i < tableName.length(); ++i) {
			if (tableName.charAt(i) != '`') {
				sb.append(tableName.charAt(i));
			}
		}

		return sb.toString();
	}

	public SQLParsedResult getResult() {
		return result;
	}
}
