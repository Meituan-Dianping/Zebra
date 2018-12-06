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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra.shard.router;

import java.util.*;

import com.dianping.zebra.shard.merge.MergeContext;

/**
 * 
 * @author hao.zhu
 *
 */
public class RouterResult {
	private List<RouterTarget> sqls;

	private List<Object> params;

	private MergeContext mergeContext;

	private boolean batchInsert;

	private int concurrencyLevel;

	private boolean optimizeShardKeyInSql;

	private boolean multiQueries;

	public List<RouterTarget> getSqls() {
		return sqls;
	}

	public void setSqls(List<RouterTarget> sqls) {
		this.sqls = sqls;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> newParams) {
		this.params = newParams;
	}

	public MergeContext getMergeContext() {
		return mergeContext;
	}

	public void setMergeContext(MergeContext mergeContext) {
		this.mergeContext = mergeContext;
	}

	public boolean isBatchInsert() {
		return batchInsert;
	}

	public void setBatchInsert(boolean batchInsert) {
		this.batchInsert = batchInsert;
	}

	public int getConcurrencyLevel() {
		return concurrencyLevel;
	}

	public void setConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
	}

	public boolean isOptimizeShardKeyInSql() {
		return optimizeShardKeyInSql;
	}

	public void setOptimizeShardKeyInSql(boolean optimizeShardKeyInSql) {
		this.optimizeShardKeyInSql = optimizeShardKeyInSql;
	}

	public boolean isMultiQueries() {
		return multiQueries;
	}

	public void setMultiQueries(boolean multiQueries) {
		this.multiQueries = multiQueries;
	}

	public RouterTarget getTargetByOffset(int offset) {
		int i = 0;
		for (RouterTarget sql : sqls) {
			for (String realSql : sql.getSqls()) {
				if (i == offset) {
					RouterTarget tmpTarget = new RouterTarget(sql.getDatabaseName());
					tmpTarget.setSqls(new ArrayList<String>(Arrays.asList(realSql)));
					return tmpTarget;
				}
				i++;
			}
		}

		return null;
	}

	public static class RouterTarget {

		private String dbName;

		private List<String> sqls;

		private Map<Integer, List<Integer>> paramIndexMapping = new HashMap<Integer, List<Integer>>(); // batchInsert: new position
																																	  // --> old position

		private List<Set<Integer>> skInIgnoreParamList; // optimize in: record values which need to be removed

		private List<Set<Integer>> allVariantRefIndexList; // multi queries: record all variant ref index

		private List<String> physicalTables;

		public RouterTarget(String dbName) {
			this.dbName = dbName;
		}

		public RouterTarget(String dbName, List<String> sqls) {
			this.dbName = dbName;
			this.sqls = sqls;
		}

		public String getDatabaseName() {
			return dbName;
		}

		public void setDatabaseName(String dbName) {
			this.dbName = dbName;
		}

		public List<String> getSqls() {
			return sqls;
		}

		public void setSqls(List<String> sqls) {
			this.sqls = sqls;
		}

		public void addSql(String sql) {
			if (this.sqls == null) {
				this.sqls = new ArrayList<String>();
			}
			this.sqls.add(sql);
		}

		public Map<Integer, List<Integer>> getParamIndexMapping() {
			return paramIndexMapping;
		}

		public void setParamIndexMapping(Map<Integer, List<Integer>> paramIndexMapping) {
			this.paramIndexMapping = paramIndexMapping;
		}

		public void putParamIndexMappingList(int newIndex, List<Integer> mappingList) {
			if (this.paramIndexMapping == null) {
				this.paramIndexMapping = new HashMap<Integer, List<Integer>>();
			}
			this.paramIndexMapping.put(newIndex, mappingList);
		}

		public List<Set<Integer>> getSkInIgnoreParamList() {
			return skInIgnoreParamList;
		}

		public void setSkInIgnoreParamList(List<Set<Integer>> skInIgnoreParamList) {
			this.skInIgnoreParamList = skInIgnoreParamList;
		}

		public void addSkInIgnoreParams(Set<Integer> skInIgnoreParams) {
			if (this.skInIgnoreParamList == null) {
				this.skInIgnoreParamList = new ArrayList<Set<Integer>>();
			}
			this.skInIgnoreParamList.add(skInIgnoreParams);
		}

		public Set<Integer> getSkInIgnoreParams(int index) {
			if (this.skInIgnoreParamList != null && index < this.skInIgnoreParamList.size()) {
				return this.skInIgnoreParamList.get(index);
			}
			return new HashSet<Integer>();
		}

		public List<String> getPhysicalTables() {
			return physicalTables;
		}

		public void setPhysicalTables(List<String> physicalTables) {
			this.physicalTables = physicalTables;
		}

		public void addPhysicalTable(String physicalTable) {
			if (this.physicalTables == null) {
				this.physicalTables = new ArrayList<String>();
			}
			this.physicalTables.add(physicalTable);
		}

		public List<Set<Integer>> getAllVariantRefIndexList() {
			return allVariantRefIndexList;
		}

		public void setAllVariantRefIndexList(List<Set<Integer>> allVariantRefIndexList) {
			this.allVariantRefIndexList = allVariantRefIndexList;
		}

		public void addVariantRefIndexes(Set<Integer> variantRefIndexes) {
			if (this.allVariantRefIndexList == null) {
				this.allVariantRefIndexList = new ArrayList<Set<Integer>>();
			}
			this.allVariantRefIndexList.add(variantRefIndexes);
		}

		public Set<Integer> getVariantRefIndexes(int index) {
			if (this.allVariantRefIndexList != null && index < this.allVariantRefIndexList.size()) {
				return this.allVariantRefIndexList.get(index);
			}
			return new HashSet<Integer>();
		}
	}
}
