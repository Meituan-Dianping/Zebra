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

import java.util.HashMap;
import java.util.Map;

/**
 * @author hao.zhu
 *
 */
public class RouterRule {
	
	private Map<String, TableShardRule> tableShardRules = new HashMap<String, TableShardRule>();

	public Map<String, TableShardRule> getTableShardRules() {
		return tableShardRules;
	}

	public void setTableShardRules(Map<String, TableShardRule> tableShardRules) {
		this.tableShardRules = tableShardRules;
	}
}
