package com.dianping.zebra.shard.parser;

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;

public class DefaultSQLRewrite implements SQLRewrite {

	@Override
	public String rewrite(SQLParsedResult pr, String logicalTable, String physicalTable) {
		SQLStatement stmt = pr.getStmt();

		StringBuilder out = new StringBuilder();
		RewriteTableNameOutputVisitor visitor = new RewriteTableNameOutputVisitor(out, logicalTable, physicalTable);

		stmt.accept(visitor);

		return out.toString();
	}

	class RewriteTableNameOutputVisitor extends MySqlOutputVisitor {

		private String physicalTable;

		private String logicalTable;

		public RewriteTableNameOutputVisitor(Appendable appender, String logicalTable, String physicalTable) {
			super(appender);
			this.logicalTable = logicalTable;
			this.physicalTable = physicalTable;
		}

		@Override
		public boolean visit(SQLExprTableSource x) {
			SQLName name = (SQLName) x.getExpr();
			if (logicalTable.equalsIgnoreCase(name.getSimpleName())) {
				print0(physicalTable);
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

		@Override
		public boolean visit(MySqlSelectQueryBlock.Limit x) {
			print0(ucase ? "LIMIT " : "limit ");

			int offset = Integer.MIN_VALUE;
			if (x.getOffset() != null) {
				if (x.getOffset() instanceof SQLIntegerExpr) {
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
			if (x.getRowCount() instanceof SQLIntegerExpr) {
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
	}
}
