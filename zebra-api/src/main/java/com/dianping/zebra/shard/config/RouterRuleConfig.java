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
package com.dianping.zebra.shard.config;

import java.io.Serializable;
import java.util.List;

public class RouterRuleConfig implements Serializable {
    /**
	 * 
	 */
   private static final long serialVersionUID = -3944193626687964466L;
	private List<TableShardRuleConfig> tableShardConfigs;

    public List<TableShardRuleConfig> getTableShardConfigs() {
        return tableShardConfigs;
    }

    public void setTableShardConfigs(List<TableShardRuleConfig> tableShardConfig) {
        this.tableShardConfigs = tableShardConfig;
    }
}
