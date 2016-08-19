package com.dianping.zebra.shard.router.rule;

import java.util.List;
import java.util.Map;

import com.dianping.zebra.shard.parser.SQLParsedResult;

public class ShardEvalContext {
	private final SQLParsedResult parseResult;

	private final List<Object> params;

	private List<ColumnValue> columnValues;

	public ShardEvalContext(SQLParsedResult parseResult, List<Object> params) {
		this.parseResult = parseResult;
		this.params = params;
	}

	public SQLParsedResult getParseResult() {
		return parseResult;
	}

	public List<Object> getParams() {
		return params;
	}

	public List<ColumnValue> getColumnValues() {
		return columnValues;
	}

	public void setColumnValues(List<ColumnValue> columnValues) {
		this.columnValues = columnValues;
	}

	public static class ColumnValue {
		private boolean used = false;

		private Map<String, Object> value;

		public ColumnValue(Map<String, Object> value) {
			this.value = value;
		}

		public boolean isUsed() {
			return used;
		}

		public void setUsed(boolean used) {
			this.used = used;
		}

		public Map<String, Object> getValue() {
			return value;
		}
	}
}
