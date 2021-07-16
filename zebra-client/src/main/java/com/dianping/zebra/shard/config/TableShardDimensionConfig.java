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
package com.dianping.zebra.shard.config;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "shard-dimension")
public class TableShardDimensionConfig implements Serializable {
	
   private static final long serialVersionUID = 5970624038037667068L;

	private String dbRule;

	private String dbIndexes;

	private String tbRule;

	private String tbSuffix;

	private boolean tbSuffixZeroPadding;

	private boolean isMaster;

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

	public boolean isTbSuffixZeroPadding() {
		return tbSuffixZeroPadding;
	}

	public void setTbSuffixZeroPadding(boolean tbSuffixZeroPadding) {
		this.tbSuffixZeroPadding = tbSuffixZeroPadding;
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
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
