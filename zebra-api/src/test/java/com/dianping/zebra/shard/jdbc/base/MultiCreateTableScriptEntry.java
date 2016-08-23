package com.dianping.zebra.shard.jdbc.base;

import java.util.Map;

public class MultiCreateTableScriptEntry {
	private String dbName;
	private Map<String, String> tableNameScriptMapping;

	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @param dbName
	 *            the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * @return the tableNameScriptMapping
	 */
	public Map<String, String> getTableNameScriptMapping() {
		return tableNameScriptMapping;
	}

	/**
	 * @param tableNameScriptMapping
	 *            the tableNameScriptMapping to set
	 */
	public void setTableNameScriptMapping(Map<String, String> tableNameScriptMapping) {
		this.tableNameScriptMapping = tableNameScriptMapping;
	}

}