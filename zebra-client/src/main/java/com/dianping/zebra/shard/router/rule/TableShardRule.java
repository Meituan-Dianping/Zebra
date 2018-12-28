/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra.shard.router.rule;

import java.util.*;

import com.dianping.zebra.shard.api.ShardDataSourceHelper;
import com.dianping.zebra.shard.exception.ShardRouterException;
import com.dianping.zebra.shard.parser.SQLHint;
import com.dianping.zebra.shard.router.rule.ShardEvalContext.ColumnValue;
import com.dianping.zebra.shard.router.rule.dimension.DimensionRule;
import com.dianping.zebra.shard.util.ShardColumnValueUtil;
import com.dianping.zebra.util.SqlType;

public class TableShardRule {

	private final String tableName;

	private List<DimensionRule> dimensions = new ArrayList<DimensionRule>();

	private DimensionRule masterDimension;

	private boolean forbidNoShardKeyWrite;

	public TableShardRule(String tableName) {
		this.tableName = tableName;
	}

	public TableShardRule(String tableName, boolean forbidNoShardKeyWrite) {
		this.tableName = tableName;
		this.forbidNoShardKeyWrite = forbidNoShardKeyWrite;
	}


	/**
	 * 根据shard key数量排序(降序)
	 */
	public void sortDimensionRule() {
		if(dimensions != null && dimensions.size() > 1) {
			Collections.sort(dimensions, new Comparator<DimensionRule>() {
				@Override
				public int compare(DimensionRule o1, DimensionRule o2) {
					return (o2.getShardColumns().size() - o1.getShardColumns().size());
				}
			});
		}
	}

	public ShardEvalResult eval(ShardEvalContext ctx) {
		SqlType type = ctx.getParseResult().getType();

		try {
			// git hint shard column from thread local
			Set<String> hintShardColumns = ShardDataSourceHelper.getHintShardColumn();
			// force dimension from hint
			if(hintShardColumns == null || hintShardColumns.size() == 0) {
				SQLHint sqlhint = ctx.getParseResult().getRouterContext().getSqlhint();
				if(sqlhint != null) {
					hintShardColumns = sqlhint.getShardColumns();
				}
			}
			if (hintShardColumns != null && hintShardColumns.size() > 0) {
				DimensionRule rule = findDimensionRule(hintShardColumns);
				if (rule != null) {
					ShardEvalResult result = evalDimension(ctx, type, rule, true);

					if (result != null) {
						return result;
					}
				} else {
					throw new ShardRouterException("Fail to force dimension in your hint, since cannot find any dimension!");
				}
			}

			for (DimensionRule rule : dimensions) {
				ShardEvalResult result = evalDimension(ctx, type, rule);

				if (result != null) {
					return result;
				}
			}

			// full table scan if is not insert sql.
			if (type != SqlType.INSERT && type != SqlType.REPLACE) {
				if (forbidNoShardKeyWrite) {
					if (SqlType.UPDATE == type || SqlType.DELETE == type) {
						throw new ShardRouterException("Update or delete is forbidden without shard key!");
					}
				}
				return new ShardEvalResult(tableName, masterDimension.getAllDBAndTables());
			} else {
				throw new ShardRouterException("Cannot find any shard columns in your insert sql.");
			}
		} finally {
			// clear thread local
			if (!ShardDataSourceHelper.isGlobalParams()) {
				ShardDataSourceHelper.clearAllThreadLocal();
			}
			ShardDataSourceHelper.clearHintShardColumn();
		}
	}

	private DimensionRule findDimensionRule(Set<String> hintShardColumns) {
		for (DimensionRule rule : dimensions) {
			Set<String> shardColumns = rule.getShardColumns();
			if(shardColumns.equals(hintShardColumns)) {
				return rule;
			}
		}

		return null;
	}

	private ShardEvalResult evalDimension(ShardEvalContext ctx, SqlType type, DimensionRule rule) {
		return evalDimension(ctx, type, rule, false);
	}

	private ShardEvalResult evalDimension(ShardEvalContext ctx, SqlType type, DimensionRule rule, boolean isSqlHint) {
		SQLHint sqlHint = ctx.getParseResult().getRouterContext().getSqlhint();
		boolean isBatchInsert = (sqlHint == null) ? false : sqlHint.isBatchInsert();

		List<ColumnValue> columnValues = ShardColumnValueUtil.eval(ctx, rule.getShardColumns(), rule.isRange(), isBatchInsert);
		ctx.setColumnValues(columnValues);
		ctx.setTableName(tableName);

		if (columnValues.size() > 0) {
			if (type == SqlType.SELECT) {
				return rule.eval(ctx);
			} else if (type == SqlType.INSERT || type == SqlType.UPDATE || type == SqlType.DELETE
					|| type == SqlType.REPLACE) {	// add for replace
				if (rule.isMaster() || isSqlHint) {
					return rule.eval(ctx, isBatchInsert);
				}
			} else {
				throw new ShardRouterException("Unsupported Sql type");
			}
		} else {
			if (type == SqlType.INSERT || type == SqlType.REPLACE) {
				throw new ShardRouterException("Router column[" + rule.getShardColumns() + "] not found in the sql!");
			}
		}

		return null;
	}

	public List<DimensionRule> getDimensionRules() {
		return this.dimensions;
	}

	public void addDimensionRule(DimensionRule dimensionRule) {
		this.dimensions.add(dimensionRule);

		if (dimensionRule.isMaster()) {
			this.masterDimension = dimensionRule;
		}
	}

	public String getTableName() {
		return tableName;
	}

	public boolean isForbidNoShardKeyWrite() {
		return forbidNoShardKeyWrite;
	}

	public void setForbidNoShardKeyWrite(boolean forbidNoShardKeyWrite) {
		this.forbidNoShardKeyWrite = forbidNoShardKeyWrite;
	}
}
