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
package com.dianping.zebra.shard.router.rule.dimension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		this.initShardColumn(condition);
	}

	@Override
	public Map<String, Set<String>> getAllDBAndTables() {
		Map<String, Set<String>> dbAndTables = new HashMap<String, Set<String>>(1);
		Set<String> tableSet = new HashSet<String>(1);
		tableSet.add(table);
		dbAndTables.put(dataSource, tableSet);
		return dbAndTables;
	}

	@Override
	public ShardEvalResult eval(ShardEvalContext ctx) {
		ShardEvalResult result = new ShardEvalResult();

		for (ColumnValue evalContext : ctx.getColumnValues()) {
			if(!evalContext.isUsed()){
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
