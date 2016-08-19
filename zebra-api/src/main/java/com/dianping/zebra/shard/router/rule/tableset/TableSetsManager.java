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

import java.util.Map;
import java.util.Set;

/**
 * @author hao.zhu
 *
 */
public interface TableSetsManager {

	TableSets getTableSetsByPos(int dbPos);

	Map<String, Set<String>> getAllTableSets();

}
