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
package com.dianping.zebra.shard.router.rule.dimension;

import java.util.*;

import com.dianping.zebra.shard.config.ExceptionalDimensionConfig;
import com.dianping.zebra.shard.router.rule.ShardEvalContext;
import com.dianping.zebra.shard.router.rule.ShardEvalResult;
import com.dianping.zebra.shard.router.rule.ShardEvalContext.ColumnValue;
import com.dianping.zebra.shard.router.rule.engine.GroovyRuleEngine;
import com.dianping.zebra.shard.router.rule.engine.RuleEngine;

/**
 * @author hao.zhu
 *
 */
public class ExceptionalDimensionRule extends AbstractDimensionRule {

	private String dataSource;

	private String table;

	private RuleEngine ruleEngine;

	public void init(ExceptionalDimensionConfig exceptionConfig) {
		this.dataSource = exceptionConfig.getDb();
		this.table = exceptionConfig.getTable();
		String condition = exceptionConfig.getCondition();
		this.ruleEngine = new GroovyRuleEngine(condition);
		this.initShardColumn(condition, null);
	}

	@Override
	public Map<String, Set<String>> getAllDBAndTables() {
		Map<String, Set<String>> dbAndTables = new HashMap<String, Set<String>>(1);
		Set<String> tableSet = new LinkedHashSet<String>(1);
		tableSet.add(table);
		dbAndTables.put(dataSource, tableSet);
		return dbAndTables;
	}

	@Override
	public ShardEvalResult eval(ShardEvalContext evalContext) {
		return eval(evalContext, false);
	}

	@Override
	public ShardEvalResult eval(ShardEvalContext ctx, boolean isBatchInsert) {
		ShardEvalResult result = new ShardEvalResult(ctx.getTableName());

		for (ColumnValue evalContext : ctx.getColumnValues()) {
			if (!evalContext.isUsed()) {
				if ((Boolean) ruleEngine.eval(evalContext.getValue())) {
					result.addDbAndTable(dataSource, table);
					evalContext.setUsed(true);
				}
			}
		}

		return result;
	}

	@Override
	public Set<String> getShardColumns() {
		return this.shardColumns;
	}
}
