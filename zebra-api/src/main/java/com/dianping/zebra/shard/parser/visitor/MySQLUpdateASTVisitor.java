package com.dianping.zebra.shard.parser.visitor;

import com.dianping.zebra.shard.parser.SQLParsedResult;

public class MySQLUpdateASTVisitor extends AbstractMySQLASTVisitor {

	public MySQLUpdateASTVisitor(SQLParsedResult result) {
		super(result);
	}

}
