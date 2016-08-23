package com.dianping.zebra.shard.jdbc.base;

import java.util.List;

public class DBDataEntry {
	private String dbName;
	private List<String> scripts;

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
	 * @return the scripts
	 */
	public List<String> getScripts() {
		return scripts;
	}

	/**
	 * @param scripts
	 *            the scripts to set
	 */
	public void setScripts(List<String> scripts) {
		this.scripts = scripts;
	}

}