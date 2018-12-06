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

import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlReplaceStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

import java.util.*;

public class DefaultSQLRewrite implements SQLRewrite {

	// binding table
	@Override
	public String rewrite(SQLStatement stmt, Map<String, String> tableMapping) {
		StringBuilder out = new StringBuilder();
		SimpleRewriteTableOutputVisitor visitor = new SimpleRewriteTableOutputVisitor(out, tableMapping);
		stmt.accept(visitor);

		return out.toString();
	}

	// normal sql
	@Override
	public String rewrite(SQLParsedResult pr, String logicalTable, String physicalTable) {
		SQLStatement stmt = pr.getStmt();
		StringBuilder out = new StringBuilder();
		Map<String, String> tableMapping = new LinkedHashMap<String, String>(2);
		tableMapping.put(logicalTable, physicalTable);

		ShardRewriteTableOutputVisitor visitor = new ShardRewriteTableOutputVisitor(out, tableMapping, pr);
		stmt.accept(visitor);

		return out.toString();
	}

	// multi queries
	@Override
	public String rewrite(SQLParsedResult pr, String logicalTable, String physicalTable,
	      Set<Integer> allVariantRefIndexes) {
		SQLStatement stmt = pr.getStmt();
		StringBuilder out = new StringBuilder();
		Map<String, String> tableMapping = new LinkedHashMap<String, String>(2);
		tableMapping.put(logicalTable, physicalTable);

		ShardRewriteTableOutputVisitor visitor = new ShardRewriteTableOutputVisitor(out, tableMapping, pr,
		      allVariantRefIndexes);
		stmt.accept(visitor);

		return out.toString();
	}

	// build sql optimize in and batch insert
	@Override
	public String rewrite(SQLParsedResult pr, String logicalTable, String physicalTable,
	      Set<Integer> batchInsertParamIndexes, SQLRewriteInParam sqlRewriteInParam) {
		SQLStatement stmt = pr.getStmt();
		StringBuilder out = new StringBuilder();
		Map<String, String> tableMapping = new LinkedHashMap<String, String>(2);
		tableMapping.put(logicalTable, physicalTable);

		ShardRewriteTableOutputVisitor visitor = new ShardRewriteTableOutputVisitor(out, tableMapping, pr,
		      batchInsertParamIndexes, sqlRewriteInParam);
		stmt.accept(visitor);

		return out.toString();
	}

	class SimpleRewriteTableOutputVisitor extends MySqlOutputVisitor {

		private Map<String, String> tableMapping;

		private static final String PROPERTY_NAME_PATTERN = "%s.%s";

		private static final String PROPERTY_QUOT_NAME_PATTERN = "`%s`.%s";

		public SimpleRewriteTableOutputVisitor(Appendable appender, Map<String, String> tableMapping) {
			super(appender);
			this.tableMapping = tableMapping;
		}

		// select
		public boolean visit(MySqlSelectQueryBlock x) {
			print0(ucase ? "SELECT " : "select ");

			for (int i = 0, size = x.getHintsSize(); i < size; ++i) {
				SQLCommentHint hint = x.getHints().get(i);
				hint.accept(this);
				print(' ');
			}

			if (SQLSetQuantifier.ALL == x.getDistionOption()) {
				print0(ucase ? "ALL " : "all ");
			} else if (SQLSetQuantifier.DISTINCT == x.getDistionOption()) {
				print0(ucase ? "DISTINCT " : "distinct ");
			} else if (SQLSetQuantifier.DISTINCTROW == x.getDistionOption()) {
				print0(ucase ? "DISTINCTROW " : "distinctrow ");
			}

			if (x.isHignPriority()) {
				print0(ucase ? "HIGH_PRIORITY " : "high_priority ");
			}

			if (x.isStraightJoin()) {
				print0(ucase ? "STRAIGHT_JOIN " : "straight_join ");
			}

			if (x.isSmallResult()) {
				print0(ucase ? "SQL_SMALL_RESULT " : "sql_small_result ");
			}

			if (x.isBigResult()) {
				print0(ucase ? "SQL_BIG_RESULT " : "sql_big_result ");
			}

			if (x.isBufferResult()) {
				print0(ucase ? "SQL_BUFFER_RESULT " : "sql_buffer_result ");
			}

			if (x.getCache() != null) {
				if (x.getCache().booleanValue()) {
					print0(ucase ? "SQL_CACHE " : "sql_cache ");
				} else {
					print0(ucase ? "SQL_NO_CACHE " : "sql_no_cache ");
				}
			}

			if (x.isCalcFoundRows()) {
				print0(ucase ? "SQL_CALC_FOUND_ROWS " : "sql_calc_found_rows ");
			}

			printSelectList(x.getSelectList());

			if (x.getOrderBy() != null) {
				x.getOrderBy().setParent(x);

				boolean isSQLAllColumnExpr = false;
				Set<String> itemSet = new HashSet<String>(8);
				for (SQLSelectItem item : x.getSelectList()) {
					if (item.getExpr() instanceof SQLAllColumnExpr) {
						isSQLAllColumnExpr = true;
						break;
					} else if (item.getExpr() instanceof SQLIdentifierExpr || item.getExpr() instanceof SQLPropertyExpr) {
						String name = ((SQLName) item.getExpr()).getSimpleName();
						itemSet.add(name);
					} else if (item.getExpr() instanceof SQLAggregateExpr) {
						SQLAggregateExpr expr = (SQLAggregateExpr) item.getExpr();
						SQLExpr argument = expr.getArguments().get(0);
						if (argument instanceof SQLAllColumnExpr) {
						} else if (argument instanceof SQLIntegerExpr) {
						} else {
							itemSet.add(argument.toString());
						}
					}
					if (item.getAlias() != null) {
						itemSet.add(item.getAlias());
					}
				}

				if (!isSQLAllColumnExpr) {
					for (SQLSelectOrderByItem orderbyItem : x.getOrderBy().getItems()) {
						String orderByName = ((SQLName) orderbyItem.getExpr()).getSimpleName();
						if (!itemSet.contains(orderByName)) {
							print0(", ");
							orderbyItem.getExpr().accept(this);
						}
					}
				}
			}

			if (x.getInto() != null) {
				println();
				print0(ucase ? "INTO " : "into ");
				x.getInto().accept(this);
			}

			if (x.getFrom() != null) {
				println();
				print0(ucase ? "FROM " : "from ");
				x.getFrom().accept(this);
			}

			if (x.getWhere() != null) {
				println();
				print0(ucase ? "WHERE " : "where ");
				x.getWhere().setParent(x);
				x.getWhere().accept(this);
			}

			if (x.getGroupBy() != null) {
				println();
				x.getGroupBy().accept(this);
			}

			if (x.getOrderBy() != null) {
				println();
				x.getOrderBy().accept(this);
			}

			if (x.getLimit() != null) {
				println();
				x.getLimit().accept(this);
			}

			if (x.getProcedureName() != null) {
				print0(ucase ? " PROCEDURE " : " procedure ");
				x.getProcedureName().accept(this);
				if (x.getProcedureArgumentList().size() > 0) {
					print('(');
					printAndAccept(x.getProcedureArgumentList(), ", ");
					print(')');
				}
			}

			if (x.isForUpdate()) {
				println();
				print0(ucase ? "FOR UPDATE" : "for update");
			}

			if (x.isLockInShareMode()) {
				println();
				print0(ucase ? "LOCK IN SHARE MODE" : "lock in share mode");
			}

			return false;
		}

		@Override
		public boolean visit(SQLExprTableSource x) {
			SQLName name = (SQLName) x.getExpr();
			String simpleName = name.getSimpleName();
			boolean hasQuote = simpleName.charAt(0) == '`';
			String tableName = hasQuote ? parseTableName(simpleName) : simpleName;
			String finalTable = tableMapping.get(tableName);

			if (finalTable != null) {
				if (hasQuote) {
					print0("`" + finalTable + "`");
				} else {
					print0(finalTable);
				}
			} else {
				x.getExpr().accept(this);
			}

			if (x.getAlias() != null) {
				print(' ');
				print0(x.getAlias());
			}

			for (int i = 0; i < x.getHintsSize(); ++i) {
				print(' ');
				x.getHints().get(i).accept(this);
			}

			return false;
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

		@Override
		public boolean visit(SQLPropertyExpr x) {
			String name = x.getSimpleName();
			SQLIdentifierExpr owner = (SQLIdentifierExpr) x.getOwner();
			String simpleName = owner.getSimpleName();
			boolean hasQuote = simpleName.charAt(0) == '`';
			String tableName = hasQuote ? parseTableName(simpleName) : simpleName;
			String finalTable = tableMapping.get(tableName);

			if (finalTable != null) {
				if (hasQuote) {
					print0(String.format(PROPERTY_QUOT_NAME_PATTERN, finalTable, name));
				} else {
					print0(String.format(PROPERTY_NAME_PATTERN, finalTable, name));
				}
			} else {
				print0(String.format(PROPERTY_NAME_PATTERN, simpleName, name));
			}

			return false;
		}

		// replace sql rewrite
		@Override
		public boolean visit(MySqlReplaceStatement x) {
			print0(ucase ? "REPLACE " : "replace ");

			if (x.isLowPriority()) {
				print0(ucase ? "LOW_PRIORITY " : "low_priority ");
			}

			if (x.isDelayed()) {
				print0(ucase ? "DELAYED " : "delayed ");
			}

			print0(ucase ? "INTO " : "into ");

			x.getTableSource().accept(this);

			if (x.getColumns().size() > 0) {
				print0(" (");
				for (int i = 0, size = x.getColumns().size(); i < size; ++i) {
					if (i != 0) {
						print0(", ");
					}
					x.getColumns().get(i).accept(this);
				}
				print(')');
			}

			if (x.getValuesList().size() != 0) {
				println();
				print0(ucase ? "VALUES " : "values ");
				int size = x.getValuesList().size();
				if (size == 0) {
					print0("()");
				} else {
					for (int i = 0; i < size; ++i) {
						if (i != 0) {
							print0(", ");
						}
						x.getValuesList().get(i).accept(this);
					}
				}
			}

			if (x.getQuery() != null) {
				x.getQuery().accept(this);
			}

			return false;
		}
	}

	class ShardRewriteTableOutputVisitor extends SimpleRewriteTableOutputVisitor implements SQLASTVisitor {

		private SQLParsedResult pr;

		private Set<Integer> batchInsertParamIndexes;

		private Set<SQLInExprWrapper> skInSet;

		private Set<String> shardColumns;

		private Set<Integer> skInIgnoreParams;

		private Set<Integer> allVariantRefIndexes;

		public ShardRewriteTableOutputVisitor(Appendable appender, Map<String, String> tableMapping, SQLParsedResult pr) {
			super(appender, tableMapping);
			this.pr = pr;
		}

		public ShardRewriteTableOutputVisitor(Appendable appender, Map<String, String> tableMapping, SQLParsedResult pr,
		      Set<Integer> allVariantRefIndexes) {
			super(appender, tableMapping);
			this.pr = pr;
			this.allVariantRefIndexes = allVariantRefIndexes;
		}

		public ShardRewriteTableOutputVisitor(Appendable appender, Map<String, String> tableMapping, SQLParsedResult pr,
		      Set<Integer> batchInsertParamIndexes, SQLRewriteInParam sqlRewriteInParam) {
			super(appender, tableMapping);
			this.pr = pr;
			this.batchInsertParamIndexes = batchInsertParamIndexes;
			if (sqlRewriteInParam != null) {
				this.skInSet = sqlRewriteInParam.getSkInSet();
				this.shardColumns = sqlRewriteInParam.getShardColumns();
				this.skInIgnoreParams = sqlRewriteInParam.getSkInIgnoreParams();
			}
		}

		@Override
		public boolean visit(MySqlSelectQueryBlock.Limit x) {
			print0(ucase ? "LIMIT " : "limit ");

			int offset = Integer.MIN_VALUE;
			if (x.getOffset() != null) {
				if (x.getOffset() instanceof SQLIntegerExpr && !pr.getMergeContext().isOrderBySplitSql()) {
					SQLIntegerExpr offsetExpr = (SQLIntegerExpr) x.getOffset();
					offset = (Integer) offsetExpr.getValue();
					offsetExpr.setNumber(0);
					offsetExpr.accept(this);
				} else {
					x.getOffset().accept(this);
				}

				print0(", ");
			}

			int limit = Integer.MAX_VALUE;
			if (x.getRowCount() instanceof SQLIntegerExpr && !pr.getMergeContext().isOrderBySplitSql()) {
				SQLIntegerExpr rowCountExpr = (SQLIntegerExpr) x.getRowCount();
				if (offset != Integer.MIN_VALUE) {
					limit = (Integer) rowCountExpr.getValue();
					rowCountExpr.setNumber(offset + limit);
				}
				rowCountExpr.accept(this);
			} else {
				x.getRowCount().accept(this);
			}

			return false;
		}

		// add batch insert
		@Override
		protected void printValuesList(MySqlInsertStatement x) {
			print0(ucase ? "VALUES " : "values ");
			if (x.getValuesList().size() > 1) {
				incrementIndent();
			}

			if (this.batchInsertParamIndexes == null) { // original
				for (int i = 0; i < x.getValuesList().size(); ++i) {
					if (i != 0) {
						print(',');
						println();
					}
					x.getValuesList().get(i).accept(this);

				}
			} else { // batch insert
				int count = 0;
				for (int i = 0; i < x.getValuesList().size(); ++i) {
					if (this.batchInsertParamIndexes.contains(i)) {
						if (count > 0) {
							print(',');
							println();
						}
						x.getValuesList().get(i).accept(this);
						count++;
					}
				}
			}
			if (x.getValuesList().size() > 1) {
				decrementIndent();
			}
		}

		// optimize in
		public boolean visit(SQLInListExpr x) {
			if (shardColumns == null || !containShardKeyIn(x.getExpr())) {
				return super.visit(x);
			}

			if (skInSet == null || skInSet.isEmpty()) {
				return false;
			}

			x.getExpr().accept(this);

			if (x.isNot()) {
				print0(ucase ? " NOT IN (" : " not in (");
			} else {
				print0(ucase ? " IN (" : " in (");
			}

			final List<SQLExpr> list = x.getTargetList();

			boolean printLn = false;
			if (list.size() > 5) {
				printLn = true;
				for (int i = 0, size = list.size(); i < size; ++i) {
					if (!(list.get(i) instanceof SQLCharExpr)) {
						printLn = false;
						break;
					}
				}
			}

			if (printLn) {
				incrementIndent();
				println();
				int count = 0;
				for (SQLExpr expr : list) {
					SQLInExprWrapper wrapper = new SQLInExprWrapper(expr);
					if (skInSet.contains(wrapper)) {
						if (count > 0) {
							print0(", ");
							println();
						}
						expr.accept(this);
						count++;
					} else {
						skInIgnoreParams.addAll(wrapper.getValueRefSet());
					}
				}
				decrementIndent();
				println();
			} else {
				printInList(x.getTargetList(), ", ");
			}

			print(')');
			return false;
		}

		// multi queries: record all value index
		public boolean visit(SQLVariantRefExpr x) {
			{
				int parametersSize = this.getParametersSize();
				int index = x.getIndex();

				if (index >= 0 && index < parametersSize) {
					Object param = this.getParameters().get(index);
					printParameter(param);
					return false;
				}
			}

			String varName = x.getName();
			if (x.isGlobal()) {
				print0("@@global.");
			} else {
				if ((!varName.startsWith("@")) // /
				      && (!varName.equals("?")) //
				      && (!varName.startsWith("#")) //
				      && (!varName.startsWith("$")) //
				      && (!varName.startsWith(":"))) {
					print0("@@");
				}
			}

			for (int i = 0; i < x.getName().length(); ++i) {
				char ch = x.getName().charAt(i);
				if (ch == '\'') {
					if (x.getName().startsWith("@@") && i == 2) {
						print(ch);
					} else if (x.getName().startsWith("@") && i == 1) {
						print(ch);
					} else if (i != 0 && i != x.getName().length() - 1) {
						print0("\\'");
					} else {
						print(ch);
					}
				} else {
					print(ch);
					if (allVariantRefIndexes != null) {
						allVariantRefIndexes.add(x.getIndex());
					}
				}
			}

			String collate = (String) x.getAttribute("COLLATE");
			if (collate != null) {
				print0(ucase ? " COLLATE " : " collate ");
				print0(collate);
			}

			return false;
		}

		protected void printInList(List<SQLExpr> sqlExprList, String seperator) {
			int count = 0;
			for (SQLExpr expr : sqlExprList) {
				SQLInExprWrapper wrapper = new SQLInExprWrapper(expr);
				if (skInSet != null && skInSet.contains(wrapper)) {
					if (count > 0) {
						print0(seperator);
					}
					expr.accept(this);
					count++;
				} else {
					skInIgnoreParams.addAll(wrapper.getValueRefSet());
				}
			}
		}

		private boolean containShardKeyIn(SQLExpr expr) {
			if (expr instanceof SQLIdentifierExpr) {
				String colName = ((SQLIdentifierExpr) expr).getSimpleName();
				return containShardKeyIn(colName);
			} else if (expr instanceof SQLListExpr) {
				List<SQLExpr> identifiers = ((SQLListExpr) expr).getItems();
				if (identifiers != null) {
					for (SQLExpr identifier : identifiers) {
						String colName = ((SQLIdentifierExpr) identifier).getSimpleName();
						if (containShardKeyIn(colName)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		private boolean containShardKeyIn(String colName) {
			if (shardColumns.contains(colName)) {
				return true;
			}
			if (colName.startsWith("`")) {
				int idx = colName.indexOf('`', 1);
				if (idx > 0 && shardColumns.contains(colName.substring(1, idx))) {
					return true;
				}
			}
			return false;
		}

	}
}
