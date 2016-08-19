package com.dianping.zebra.shard.jdbc.parallel;

import java.sql.ResultSet;

public class UpdateResult {
	private int affectedRows;

	// If is an insert sql, then has gennerateKey
	private ResultSet generatedKey;

	public UpdateResult(int affectedRows) {
		this(affectedRows, null);
	}

	public UpdateResult(int affectedRows, ResultSet generatedKey) {
		this.affectedRows = affectedRows;
		this.generatedKey = generatedKey;
	}

	public int getAffectedRows() {
		return affectedRows;
	}

	public void setAffectedRows(int affectedRows) {
		this.affectedRows = affectedRows;
	}

	public ResultSet getGeneratedKey() {
		return generatedKey;
	}

	public void setGeneratedKey(ResultSet generatedKey) {
		this.generatedKey = generatedKey;
	}
}