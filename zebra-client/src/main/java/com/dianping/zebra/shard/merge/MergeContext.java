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
package com.dianping.zebra.shard.merge;

import java.util.*;

import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.dianping.zebra.shard.router.RouterResult;

public class MergeContext {

	public static final int NO_OFFSET = Integer.MIN_VALUE;

	public static final int NO_LIMIT = Integer.MAX_VALUE;

	private int offset = NO_OFFSET;

	private int limit = NO_LIMIT;

	private MySqlSelectQueryBlock.Limit limitExpr;

	private List<String> groupByColumns = new ArrayList<String>();

	private Map<String, SQLObjectImpl> selectItemMap = new LinkedHashMap<String, SQLObjectImpl>();

	private Map<String, String> columnNameAliasMapping = new HashMap<String, String>();

	private SQLOrderBy orderBy;

	private int joinCount = 0;

	private int unionCount = 0;

	private int queryCount = 0;

	private int orderByCount = 0;

	private int groupByCount = 0;

	private boolean distinct;

	private boolean aggregate;

	private boolean canUseSplit = false;

	public MergeContext() {
	}

	public MergeContext(MergeContext ctx) {
		this.offset = ctx.offset;
		this.limit = ctx.limit;
		this.limitExpr = ctx.limitExpr;
		this.groupByColumns = ctx.groupByColumns;
		this.selectItemMap = ctx.selectItemMap;
		this.columnNameAliasMapping = ctx.columnNameAliasMapping;
		this.orderBy = ctx.orderBy;
		this.joinCount = ctx.joinCount;
		this.unionCount = ctx.joinCount;
		this.queryCount = ctx.queryCount;
		this.orderByCount = ctx.orderByCount;
		this.groupByCount = ctx.groupByCount;
		this.distinct = ctx.distinct;
		this.aggregate = ctx.aggregate;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public MySqlSelectQueryBlock.Limit getLimitExpr() {
		return limitExpr;
	}

	public void setLimitExpr(MySqlSelectQueryBlock.Limit limitExpr) {
		this.limitExpr = limitExpr;
	}

	public List<String> getGroupByColumns() {
		return groupByColumns;
	}

	public void setGroupByColumns(List<String> groupByColumns) {
		this.groupByColumns = groupByColumns;
	}

	public Map<String, SQLObjectImpl> getSelectItemMap() {
		return selectItemMap;
	}

	public void setSelectItemMap(Map<String, SQLObjectImpl> selectItemMap) {
		this.selectItemMap = selectItemMap;
	}

	public Map<String, String> getColumnNameAliasMapping() {
		return columnNameAliasMapping;
	}

	public void setColumnNameAliasMapping(Map<String, String> columnNameAliasMapping) {
		this.columnNameAliasMapping = columnNameAliasMapping;
	}

	public SQLOrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(SQLOrderBy orderBy) {
		this.orderBy = orderBy;
	}

	public void increJoinCount() {
		this.joinCount++;
	}

	public int getJoinCount() {
		return joinCount;
	}

	public void setJoinCount(int joinCount) {
		this.joinCount = joinCount;
	}

	public void increUnionCount() {
		this.unionCount++;
	}

	public int getUnionCount() {
		return unionCount;
	}

	public void setUnionCount(int unionCount) {
		this.unionCount = unionCount;
	}

	public void increQueryCount() {
		this.queryCount++;
	}

	public int getQueryCount() {
		return queryCount;
	}

	public void setQueryCount(int queryCount) {
		this.queryCount = queryCount;
	}

	public void increOrderByCount() {
		this.orderByCount++;
	}

	public int getOrderByCount() {
		return orderByCount;
	}

	public void setOrderByCount(int orderByCount) {
		this.orderByCount = orderByCount;
	}

	public void increGroupByCount() {
		this.groupByCount++;
	}

	public int getGroupByCount() {
		return groupByCount;
	}

	public void setGroupByCount(int groupByCount) {
		this.groupByCount = groupByCount;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public boolean isAggregate() {
		return aggregate;
	}

	public void setAggregate(boolean aggregate) {
		this.aggregate = aggregate;
	}

	public boolean isOrderBySplitSql() {
		return this.getOrderByCount() > 0 && (this.getLimit() != NO_LIMIT || this.offset != NO_OFFSET)
		      && this.getOffset() != 0 && this.getJoinCount() == 0 && this.getUnionCount() == 0
		      && this.isDistinct() == false && this.getQueryCount() == 1 && this.getGroupByCount() == 0;
	}

	public boolean canUseSplitMethod(RouterResult target) {
		int offset = this.offset == NO_OFFSET ? 0 : this.offset;
		int tableNum = 0;
		for (RouterResult.RouterTarget sql : target.getSqls()) {
			tableNum += sql.getSqls().size();
		}

		this.canUseSplit = isOrderBySplitSql()
		      && this.getLimit() < (1 - (tableNum * 1.0 - 1) / (2 * tableNum * tableNum)) * offset;
		return this.canUseSplit;
	}

	public boolean isCanUseSplit() {
		return canUseSplit;
	}

	public void setCanUseSplit(boolean canUseSplit) {
		this.canUseSplit = canUseSplit;
	}
}
