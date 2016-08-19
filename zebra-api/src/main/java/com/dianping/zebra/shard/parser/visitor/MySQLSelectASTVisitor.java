package com.dianping.zebra.shard.parser.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.dianping.zebra.shard.parser.SQLParsedResult;

public class MySQLSelectASTVisitor extends AbstractMySQLASTVisitor {

	public MySQLSelectASTVisitor(SQLParsedResult result) {
		super(result);
	}

	@Override
	public boolean visit(MySqlSelectQueryBlock x) {
		Map<String, SQLSelectItem> selectItemMap = result.getMergeContext().getSelectItemMap();
		Map<String, String> columnNameAliasMapping = result.getMergeContext().getColumnNameAliasMapping();

		for (SQLSelectItem column : x.getSelectList()) {
			String name = null;
			if (column.getExpr() instanceof SQLAggregateExpr) {
				SQLAggregateExpr expr = (SQLAggregateExpr) column.getExpr();
				SQLExpr argument = expr.getArguments().get(0);
				if (argument instanceof SQLAllColumnExpr) {
					name = expr.getMethodName() + "(*)";
				} else if(argument instanceof SQLIntegerExpr){
					name = expr.getMethodName() + "(1)";
				}else {
					name = expr.getMethodName() + "(" + ((SQLName) argument).getSimpleName() + ")";
					columnNameAliasMapping.put(((SQLName) argument).getSimpleName(), column.getAlias());
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
	public boolean visit(Limit x) {
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
		result.getMergeContext().setOrderBy(x);
		return true;
	}
}
