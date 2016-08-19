package com.dianping.zebra.shard.jdbc.parallel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Callable;

import com.dianping.zebra.group.util.DaoContextHolder;

public class PreparedStatementExecuteQueryCallable implements Callable<ResultSet> {

	private PreparedStatement stmt;
	private String sqlName;

	public PreparedStatementExecuteQueryCallable(PreparedStatement stmt, String sqlName) {
		this.stmt = stmt;
		this.sqlName = sqlName;
	}

	@Override
	public ResultSet call() throws Exception {
		DaoContextHolder.setSqlName(sqlName);
		return stmt.executeQuery();
	}
}
