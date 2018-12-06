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
package com.dianping.zebra.dao.dialect;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.dianping.zebra.dao.plugin.page.MysqlCountOutputVisitor;

import java.util.List;

/**
 * 
 * @author damonzhu
 *
 */
public class MySQLDialect extends Dialect {

	@Override
	public String getCountSql(String sql) {
		SQLStatementParser parser = new MySqlStatementParser(sql);
		List<SQLStatement> stmtList = parser.parseStatementList();

		// 将AST通过visitor输出
		StringBuilder out = new StringBuilder();
		MysqlCountOutputVisitor visitor = new MysqlCountOutputVisitor(out);

		for (SQLStatement stmt : stmtList) {
			if (stmt instanceof SQLSelectStatement) {
				stmt.accept(visitor);
				out.append(";");
			}
		}

		return out.toString();
	}

	@Override
	public String getLimitString(String sql, int offset, String offsetPlaceholder, int limit, String limitPlaceholder) {
		if (offset > 0) {
			return sql + " limit " + offsetPlaceholder + "," + limitPlaceholder;
		} else {
			return sql + " limit " + limitPlaceholder;
		}
	}
}