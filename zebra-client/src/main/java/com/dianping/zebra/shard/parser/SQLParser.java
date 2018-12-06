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

import java.util.*;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlReplaceStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlLexer;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.dianping.zebra.shard.exception.ShardParseException;
import com.dianping.zebra.shard.parser.visitor.*;
import com.dianping.zebra.shard.util.LRUCache;
import com.dianping.zebra.util.SqlType;

public class SQLParser {

	private final static Map<String, SQLParsedResult> parsedSqlCache = Collections
	      .synchronizedMap(new LRUCache<String, SQLParsedResult>(1000));

	private static volatile boolean init = false;

	public static void init() {
		if (init == false) {
			parseInternal("SELECT 1 FROM TEST");
			init = true;
		}
	}

	public static SQLParsedResult parseWithCache(String sql) throws ShardParseException {
		SQLParsedResult result = parsedSqlCache.get(sql);

		if (null == result) {
			result = parseInternal(sql);
			parsedSqlCache.put(sql, result);
		}

		return result;
	}

	public static SQLParsedResult parseWithoutCache(String sql) throws ShardParseException {
		return parseInternal(sql);
	}

	private static SQLParsedResult parseInternal(String sql) {
		MySqlLexer lexer = new MySqlLexer(sql);
		HintCommentHandler commentHandler = new HintCommentHandler();
		lexer.setCommentHandler(commentHandler);
		lexer.nextToken();

		SQLStatementParser parser = new MySqlStatementParser(lexer);
		SQLHint sqlhint = SQLHint.parseHint(commentHandler);

		List<SQLStatement> stmtList = parser.parseStatementList();
		if (stmtList.size() == 1) {
			SQLParsedResult sqlParsedResult = parseInternal(stmtList.get(0));
			sqlParsedResult.getRouterContext().setSqlhint(sqlhint);
			return sqlParsedResult;
		} else {
			MultiSQLParsedResult multiSQLParsedResult = new MultiSQLParsedResult();
			multiSQLParsedResult.setSqlHint(sqlhint);
			for (SQLStatement stmt : stmtList) {
				SQLParsedResult sqlParsedResult = parseInternal(stmt);
				sqlParsedResult.getRouterContext().setSqlhint(sqlhint);
				multiSQLParsedResult.addSQLParsedResult(sqlParsedResult);
			}
			return multiSQLParsedResult;
		}
	}

	private static SQLParsedResult parseInternal(SQLStatement stmt) {
		SQLParsedResult result = null;
		if (stmt instanceof SQLSelectStatement) {
			result = new SQLParsedResult(SqlType.SELECT, stmt);
			SQLASTVisitor visitor = new MySQLSelectASTVisitor(result);
			stmt.accept(visitor);
		} else if (stmt instanceof SQLInsertStatement) {
			result = new SQLParsedResult(SqlType.INSERT, stmt);
			SQLASTVisitor visitor = new MySQLInsertASTVisitor(result);
			stmt.accept(visitor);
		} else if (stmt instanceof SQLUpdateStatement) {
			result = new SQLParsedResult(SqlType.UPDATE, stmt);
			SQLASTVisitor visitor = new MySQLUpdateASTVisitor(result);
			stmt.accept(visitor);
		} else if (stmt instanceof SQLDeleteStatement) {
			result = new SQLParsedResult(SqlType.DELETE, stmt);
			SQLASTVisitor visitor = new MySQLDeleteASTVisitor(result);
			stmt.accept(visitor);
		} else if (stmt instanceof MySqlReplaceStatement) { // add for replace
			result = new SQLParsedResult(SqlType.REPLACE, stmt);
			SQLASTVisitor visitor = new MySQLReplaceASTVisitor(result);
			stmt.accept(visitor);
		} else {
			throw new ShardParseException("Unsupported sql type in shard datasource.");
		}
		return result;
	}

	public static class HintCommentHandler implements Lexer.CommentHandler {

		private String zebraHintComment = null;

		private Set<String> otherHintComments = new LinkedHashSet<String>(4);

		@Override
		public boolean handle(Token lastToken, String comment) {
			if (comment.contains("zebra")) {
				zebraHintComment = comment;
			} else {
				otherHintComments.add(comment);
			}

			return false;
		}

		public Set<String> getOtherHintComments() {
			return otherHintComments;
		}

		public String getZebraHintComment() {
			return zebraHintComment;
		}
	}
}
