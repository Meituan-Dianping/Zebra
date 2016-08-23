package com.dianping.zebra.group.jdbc;

public class DataSourceEntry {
	private String dataSets;

	private String jdbcUrl;

	private boolean isWrite;

	public DataSourceEntry(String jdbcUrl, String dataSets, boolean isWrite) {
		this.jdbcUrl = jdbcUrl;
		this.dataSets = dataSets;
		this.isWrite = isWrite;
	}

	public String getDataSets() {
		return dataSets;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public boolean isWrite() {
		return isWrite;
	}
}