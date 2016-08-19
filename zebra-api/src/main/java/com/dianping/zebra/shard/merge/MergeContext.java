package com.dianping.zebra.shard.merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;

public class MergeContext {

	public static final int NO_OFFSET = Integer.MIN_VALUE;

	public static final int NO_LIMIT = Integer.MAX_VALUE;

	private int offset = NO_OFFSET;

	private int limit = NO_LIMIT;

	private Limit limitExpr;

	private List<String> groupByColumns = new ArrayList<String>();

	private Map<String, SQLSelectItem> selectItemMap = new LinkedHashMap<String, SQLSelectItem>();
	
	private Map<String,String> columnNameAliasMapping = new HashMap<String, String>();

	private SQLOrderBy orderBy;

	private boolean distinct;
	
	private boolean aggregate;
	
	public MergeContext(){
	}

	public MergeContext(MergeContext ctx){
		this.offset = ctx.offset;
		this.limit = ctx.limit;
		this.limitExpr = ctx.limitExpr;
		this.groupByColumns = ctx.groupByColumns;
		this.selectItemMap = ctx.selectItemMap;
		this.columnNameAliasMapping = ctx.columnNameAliasMapping;
		this.orderBy = ctx.orderBy;
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

	public Limit getLimitExpr() {
		return limitExpr;
	}

	public void setLimitExpr(Limit limitExpr) {
		this.limitExpr = limitExpr;
	}

	public List<String> getGroupByColumns() {
		return groupByColumns;
	}

	public void setGroupByColumns(List<String> groupByColumns) {
		this.groupByColumns = groupByColumns;
	}
	

	public Map<String, SQLSelectItem> getSelectItemMap() {
		return selectItemMap;
	}

	public void setSelectItemMap(Map<String, SQLSelectItem> selectItemMap) {
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
}
