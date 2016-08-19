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

public class TableShardRuleConfig implements Serializable{

	/**
	 * 
	 */
   private static final long serialVersionUID = -5543539985358909050L;

	private String tableName;
	
	private List<TableShardDimensionConfig> dimensionConfigs;

	private String generatedPK;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<TableShardDimensionConfig> getDimensionConfigs() {
		return dimensionConfigs;
	}

	public void setDimensionConfigs(List<TableShardDimensionConfig> dimensionConfigs) {
		this.dimensionConfigs = dimensionConfigs;
	}

	public void setGeneratedPK(String generatedPK) {
		this.generatedPK = generatedPK;
	}

	public String getGeneratedPK() {
		return generatedPK;
	}
	
}
