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

public class TableShardDimensionConfig implements Serializable {
	
	/**
	 * 
	 */
   private static final long serialVersionUID = 5970624038037667068L;

	private String dbRule;
	
	private String dbIndexes;
	
	private String tbRule;
	
	private String tbSuffix;
	
	private boolean isMaster;
	
	private boolean needSync;
	
	private List<ExceptionalDimensionConfig> exceptionalDimensionConfig;

	private String tableName;

	public String getDbRule() {
		return dbRule;
	}

	public void setDbRule(String dbRule) {
		this.dbRule = dbRule;
	}

	public String getDbIndexes() {
		return dbIndexes;
	}

	public void setDbIndexes(String dbIndexes) {
		this.dbIndexes = dbIndexes;
	}

	public String getTbRule() {
		return tbRule;
	}

	public void setTbRule(String tbRule) {
		this.tbRule = tbRule;
	}

	public String getTbSuffix() {
		return tbSuffix;
	}

	public void setTbSuffix(String tbSuffix) {
		this.tbSuffix = tbSuffix;
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public boolean isNeedSync() {
		return needSync;
	}

	public void setNeedSync(boolean needSync) {
		this.needSync = needSync;
	}

	public List<ExceptionalDimensionConfig> getExceptionalDimensionConfig() {
		return exceptionalDimensionConfig;
	}

	public void setExceptionalDimensionConfig(List<ExceptionalDimensionConfig> exceptions) {
		this.exceptionalDimensionConfig = exceptions;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}
	
}
