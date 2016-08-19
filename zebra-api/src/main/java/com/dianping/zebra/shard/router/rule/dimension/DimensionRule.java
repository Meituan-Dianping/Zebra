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

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.dianping.zebra.shard.router.rule.ShardEvalContext;
import com.dianping.zebra.shard.router.rule.ShardEvalResult;

/**
 * @author hao.zhu
 *
 */
public interface DimensionRule {

	public static final Pattern RULE_COLUMN_PATTERN = Pattern.compile("#(.+?)#");

	ShardEvalResult eval(ShardEvalContext evalContext);

	Map<String, Set<String>> getAllDBAndTables();
	
	Set<String> getShardColumns();

	boolean isMaster();
	
	boolean needSync();
}
