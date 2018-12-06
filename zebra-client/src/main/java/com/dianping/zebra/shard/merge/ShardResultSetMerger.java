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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.dianping.zebra.shard.merge.distinct.DistinctDataMerger;
import com.dianping.zebra.shard.merge.groupby.GroupByDataMerger;
import com.dianping.zebra.shard.merge.orderby.OrderByDataMerger;
import com.dianping.zebra.shard.router.RouterResult;
import com.dianping.zebra.shard.router.RouterResult.RouterTarget;

/**
 * <p>
 * 默认的数据合并器
 * </p>
 *
 * @author Leo Liang
 */
public class ShardResultSetMerger {

	private DistinctDataMerger distinctMerger = new DistinctDataMerger();

	private GroupByDataMerger groubyMerger = new GroupByDataMerger();

	private OrderByDataMerger orderbyMerger = new OrderByDataMerger();

	/**
	 * <p>
	 * 处理步骤：
	 * <ol>
	 * <li>如果路由结果中仅包含一个数据源或者路由结果包含多个数据源但是SQL不包含order
	 * by子句和聚合函数，且没有distinct，则直接把真实ResultSet List进行limit处理后保存于dataPool中。</li>
	 * <li>非第一种情况，则需要从ResultSet
	 * List中弹出所有记录，进行distinct处理，聚合函数计算，排序，limit计算。并把结果保存于dataPool中。</li>
	 * </ol>
	 * </p>
	 */
	public void merge(ShardResultSetAdaptor adaptor, RouterResult routerTarget, List<ResultSet> actualResultSets)
			throws SQLException {
		List<RouterTarget> sqls = routerTarget.getSqls();
		MergeContext mergeContext = routerTarget.getMergeContext();

		if (sqls == null || sqls.size() == 0) {
			throw new SQLException("Can not proc merge, since no router result.");
		}

		if (sqls.size() == 1 && sqls.get(0).getSqls().size() == 1) {
			adaptor.setResultSets(actualResultSets);
		} else if ((sqls.size() > 1 || sqls.get(0).getSqls().size() > 1) && (mergeContext.getOrderBy() == null)
				&& !mergeContext.isAggregate() && !mergeContext.isDistinct()) {
			adaptor.setResultSets(actualResultSets);
		} else {
			adaptor.setResultSets(actualResultSets);
			List<RowData> rowDatas = popResultSets(actualResultSets, mergeContext);

			if (rowDatas == null || rowDatas.size() == 0) {
				//				adaptor.setMemoryData(rowDatas);
				return;
			}

			List<RowData> afterDistinctDatas = distinctMerger.process(rowDatas, mergeContext);
			List<RowData> afterGroupByDatas = groubyMerger.process(afterDistinctDatas, mergeContext);
			List<RowData> afterOrderByDatas = orderbyMerger.process(afterGroupByDatas, mergeContext);

			adaptor.setMemoryData(afterOrderByDatas);
		}

		if (sqls.size() >= 1 || sqls.get(0).getSqls().size() >= 1) {
			adaptor.setMax(mergeContext.getLimit());
			adaptor.setSkip(mergeContext.getOffset());
			adaptor.procLimit();
		}
	}

	public void merge(ShardResultSetAdaptor adaptor, List<List<RowData>> firstResult, List<List<RowData>> secondResult,
			MergeContext context, int offset, int splitOffset, int splitNum, int limit, List<Long> countResult)
			throws SQLException {
		if (offset == MergeContext.NO_OFFSET) {
			offset = 0;
		}
		if (splitOffset == MergeContext.NO_OFFSET) {
			splitOffset = 0;
		}
		/*
		 * 遍历第二个结果集的每一个结果list，与第一个结果集的对应list的第一个数据进行比较，倒推出第二个结果集的每个list的offset
		 * 然后将第二个结果集进行排序，所有offset相加则是排序后结果的第一个数据的offset，
		 * 与原有的offset进行相减就可以得到最终的offset
		 */
		ArrayList<RowData> finalResults = new ArrayList<RowData>();
		int totalDiffIndex = 0;
		int specialRealOffset = 0;
		for (int i = 0; i < firstResult.size(); i++) {
			// 如果第一个结果集为空，则用count值去减去第二次的结果集大小，得到最小值在该集合的realOffset
			if (firstResult.get(i).size() == 0) {
				splitNum--;
				specialRealOffset += countResult.remove(0) - secondResult.get(i).size();
				finalResults.addAll(secondResult.get(i));
				continue;
			}
			RowData start = firstResult.get(i).get(0);
			int diffIndex = 0;
			finalResults.addAll(secondResult.get(i));
			for (RowData dt : secondResult.get(i)) {
				// 找到第一个结果集第一个数据在第二个结果集的位置，break，记下位置
				if (dt.equals(start)) {
					break;
				}
				diffIndex++;
			}
			totalDiffIndex += diffIndex;
		}
		// 拆分后的总共位移和减去二次查询相比较后的总共位移偏移就是二次查询结果最小值的offset，然后用最初的去减，得到小结果集的realOffset
		// 因为splitOffset是去尾法得到，realOffset一定会大于0
		int realOffset = offset - (splitOffset * splitNum + specialRealOffset - totalDiffIndex);
		orderbyMerger.process(finalResults, context);
		adaptor.setMemoryData(finalResults);
		adaptor.setSkip(realOffset);
		adaptor.setMax(limit);
		adaptor.procLimit();
	}

	public List<RowData> popResultSet(ResultSet rs, MergeContext mergeContext) throws SQLException {
		ArrayList<RowData> rows = new ArrayList<RowData>();
		Map<String, SQLObjectImpl> selectItemMap = resultSetColumnWapper(rs, mergeContext);
		while (rs.next()) {
			RowData row = new RowData(rs);

			for (Entry<String, SQLObjectImpl> col : selectItemMap.entrySet()) {
				String columnName = col.getKey();
				int columnIndex = rs.findColumn(columnName);
				Object value = rs.getObject(columnIndex);
				boolean wasNull = rs.wasNull();
				RowId rowId = null;
				try {
					rowId = rs.getRowId(columnIndex);
				} catch (Throwable e) {
					// ignore
				}

				ColumnData columnData = new ColumnData(columnIndex, columnName, value,
						value == null ? null : value.getClass(), rowId, wasNull);
				row.addColumn(columnData);
			}

			rows.add(row);
		}

		return rows;
	}

	/*
	 * 把从mysql获取的数据转换成数据对象
	 */
	private List<RowData> popResultSets(List<ResultSet> actualResultSets, MergeContext mergeContext)
			throws SQLException {
		List<RowData> rows = new ArrayList<RowData>();

		for (int resultSetIndex = 0; resultSetIndex < actualResultSets.size(); resultSetIndex++) {
			ResultSet rs = actualResultSets.get(resultSetIndex);
			rows.addAll(popResultSet(rs, mergeContext));
		}

		return rows;
	}

	private Map<String, SQLObjectImpl> resultSetColumnWapper(ResultSet rs, MergeContext mergeContext)
			throws SQLException {
		boolean isSQLAllColumnExpr = false;

		for (SQLObjectImpl item : mergeContext.getSelectItemMap().values()) {
			if(item instanceof SQLSelectItem) {
				if (((SQLSelectItem)item).getExpr() instanceof SQLAllColumnExpr) {
					isSQLAllColumnExpr = true;
				} else if (((SQLSelectItem)item).getExpr() instanceof SQLPropertyExpr && "*".equals(((SQLPropertyExpr) ((SQLSelectItem)item).getExpr()).getName())) {
					isSQLAllColumnExpr = true;
				}
			}
			break;
		}

		if (isSQLAllColumnExpr) {
			Map<String, SQLObjectImpl> selectItemMap = new HashMap<String, SQLObjectImpl>();
			final ResultSetMetaData metaData = rs.getMetaData();
			final int columnCount = metaData.getColumnCount();

			for (int i = 1; i <= columnCount; i++) {
				String columnLabel = metaData.getColumnLabel(i);
				SQLIdentifierExpr expr = new SQLIdentifierExpr(columnLabel);
				SQLSelectItem item = new SQLSelectItem(expr, columnLabel);
				selectItemMap.put(columnLabel, item);
			}

			return selectItemMap;
		} else {
			return mergeContext.getSelectItemMap();
		}
	}
}
