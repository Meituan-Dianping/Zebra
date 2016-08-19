package com.dianping.zebra.shard.router;

import java.util.HashSet;
import java.util.Set;

import com.dianping.zebra.shard.parser.SQLHint;

public class RouterContext {

	private Set<String> tableSet = new HashSet<String>();

	private SQLHint sqlhint;
	
	public Set<String> getTableSet() {
		return tableSet;
	}

	public void setTableSets(Set<String> tableSet) {
		this.tableSet = tableSet;
	}

	public SQLHint getSqlhint() {
		return sqlhint;
	}

	public void setSqlhint(SQLHint sqlhint) {
		this.sqlhint = sqlhint;
	}

	public void setTableSet(Set<String> tableSet) {
		this.tableSet = tableSet;
	}
}
