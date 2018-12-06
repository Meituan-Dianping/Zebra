package com.dianping.zebra.shard.jdbc.base;

public class SingleCreateTableScriptEntry {
	private String tableName;
	private String createTableScript;

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the createTableScript
	 */
	public String getCreateTableScript() {
		return createTableScript;
	}

	/**
	 * @param createTableScript
	 *            the createTableScript to set
	 */
	public void setCreateTableScript(String createTableScript) {
		this.createTableScript = createTableScript;
	}

}