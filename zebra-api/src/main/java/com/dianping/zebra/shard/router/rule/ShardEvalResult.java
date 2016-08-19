package com.dianping.zebra.shard.router.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShardEvalResult {
	private Map<String, Set<String>> dbAndTables;

	public ShardEvalResult() {
		dbAndTables = new HashMap<String, Set<String>>();
	}

	public ShardEvalResult(Map<String, Set<String>> dbAndTables) {
		this.dbAndTables = dbAndTables;
	}

	public Map<String, Set<String>> getDbAndTables() {
		return dbAndTables;
	}

	public void setDbAndTables(Map<String, Set<String>> dbAndTables) {
		this.dbAndTables = dbAndTables;
	}

	public void addDbAndTable(String dataSource, String table) {
		if (!dbAndTables.containsKey(dataSource)) {
			dbAndTables.put(dataSource, new HashSet<String>());
		}
		dbAndTables.get(dataSource).add(table);
	}
}
