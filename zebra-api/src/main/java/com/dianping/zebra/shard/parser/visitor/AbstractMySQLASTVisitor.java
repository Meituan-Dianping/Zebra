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
		result.getRouterContext().getTableSet().add(table.getSimpleName());

		return true;
	}

	public SQLParsedResult getResult() {
		return result;
	}
}
