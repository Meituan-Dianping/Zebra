/**
 * Project: com.dianping.zebra.zebra-client-0.1.0
 *
 * File Created at 2011-6-14
 * $Id$
 *
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.router.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.dianping.zebra.shard.api.ShardDataSourceHelper;
import com.dianping.zebra.shard.exception.ShardRouterException;
import com.dianping.zebra.shard.parser.SQLHint;
import com.dianping.zebra.shard.router.rule.ShardEvalContext.ColumnValue;
import com.dianping.zebra.shard.router.rule.dimension.DimensionRule;
import com.dianping.zebra.shard.util.ShardColumnValueUtil;
import com.dianping.zebra.util.SqlType;

public class TableShardRule {

	private final String tableName;

	private String generatedPk;

	private List<DimensionRule> dimensions = new ArrayList<DimensionRule>();

	private DimensionRule masterDimension;

	public TableShardRule(String tableName) {
		this.tableName = tableName;
	}

	public ShardEvalResult eval(ShardEvalContext ctx) {
		SqlType type = ctx.getParseResult().getType();

		try {
			// force dimension from hint
			SQLHint sqlhint = ctx.getParseResult().getRouterContext().getSqlhint();
			if (sqlhint != null & sqlhint.getShardColumn() != null) {
				DimensionRule rule = findDimensionRule(sqlhint.getShardColumn());
				if (rule != null) {
					ShardEvalResult result = evalDimension(ctx, type, rule);

					if (result != null) {
						return result;
					}
				} else {
					throw new ShardRouterException(
							"Fail to force dimension in your hint, since cannot find any dimension!");
				}
			}

			for (DimensionRule rule : dimensions) {
				ShardEvalResult result = evalDimension(ctx, type, rule);

				if (result != null) {
					return result;
				}
			}

			// full table scan if is not insert sql.
			if (type != SqlType.INSERT) {
				return new ShardEvalResult(masterDimension.getAllDBAndTables());
			} else {
				throw new ShardRouterException("Cannot find any shard columns in your insert sql.");
			}
		} finally {
			// clear thread local
			ShardDataSourceHelper.clearAllThreadLocal();
		}
	}

	private DimensionRule findDimensionRule(String shardColumn) {
		for (DimensionRule rule : dimensions) {
			Set<String> shardColumns = rule.getShardColumns();

			if (shardColumns.size() == 1 && shardColumns.contains(shardColumn)) {
				return rule;
			}
		}

		return null;
	}

	private ShardEvalResult evalDimension(ShardEvalContext ctx, SqlType type, DimensionRule rule) {
		List<ColumnValue> columnValues = ShardColumnValueUtil.eval(ctx, rule.getShardColumns());
		ctx.setColumnValues(columnValues);

		if (columnValues.size() > 0) {
			if (type == SqlType.SELECT) {
				return rule.eval(ctx);
			} else if (type == SqlType.INSERT || type == SqlType.UPDATE || type == SqlType.DELETE) {
				if (rule.isMaster()) {
					return rule.eval(ctx);
				}
			} else {
				throw new ShardRouterException("Unsupported Sql type");
			}
		} else {
			if (type == SqlType.INSERT) {
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

	public String getGeneratedPk() {
		return generatedPk;
	}

	public void setGeneratedPk(String generatedPk) {
		this.generatedPk = generatedPk;
	}
}
