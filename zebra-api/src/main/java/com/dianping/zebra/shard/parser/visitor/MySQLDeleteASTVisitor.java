package com.dianping.zebra.shard.parser.visitor;

import com.dianping.zebra.shard.parser.SQLParsedResult;

public class MySQLDeleteASTVisitor extends AbstractMySQLASTVisitor {

	public MySQLDeleteASTVisitor(SQLParsedResult result) {
		super(result);
	}

}
