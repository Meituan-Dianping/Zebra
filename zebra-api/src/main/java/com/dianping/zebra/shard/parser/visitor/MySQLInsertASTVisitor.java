package com.dianping.zebra.shard.parser.visitor;

import com.dianping.zebra.shard.parser.SQLParsedResult;

public class MySQLInsertASTVisitor extends AbstractMySQLASTVisitor {

	public MySQLInsertASTVisitor(SQLParsedResult result) {
		super(result);
	}

}
