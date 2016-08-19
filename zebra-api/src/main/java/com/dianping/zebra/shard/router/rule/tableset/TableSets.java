/**
 * Project: com.dianping.zebra.zebra-client-0.1.0
 * 
 * File Created at 2011-6-15
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
package com.dianping.zebra.shard.router.rule.tableset;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hao.zhu
 * 
 */
public class TableSets {

	private final String dbIndex;

	private final List<String> tableSets = new ArrayList<String>(32);

	public TableSets(String dbIndex) {
		this.dbIndex = dbIndex;
	}

	public void addIntoTableSets(String tableName) {
		this.tableSets.add(tableName);
	}

	public List<String> getTableSets() {
		return tableSets;
	}

	public String getDbIndex() {
		return dbIndex;
	}
}
