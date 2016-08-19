/**
 * Project: zebra-client
 *
 * File Created at 2011-6-22
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
package com.dianping.zebra.shard.merge;

import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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
				adaptor.setMemoryData(rowDatas);
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

	private List<RowData> popResultSets(List<ResultSet> actualResultSets, MergeContext mergeContext)
			throws SQLException {
		List<RowData> rows = new ArrayList<RowData>();

		for (int resultSetIndex = 0; resultSetIndex < actualResultSets.size(); resultSetIndex++) {
			while (actualResultSets.get(resultSetIndex).next()) {
				RowData row = new RowData(actualResultSets.get(resultSetIndex));

				for (Entry<String, SQLSelectItem> col : mergeContext.getSelectItemMap().entrySet()) {
					String columnName = col.getKey();
					int columnIndex = actualResultSets.get(resultSetIndex).findColumn(columnName);
					Object value = actualResultSets.get(resultSetIndex).getObject(columnIndex);
					boolean wasNull = actualResultSets.get(resultSetIndex).wasNull();
					RowId rowId = null;
					try {
						rowId = actualResultSets.get(resultSetIndex).getRowId(columnIndex);
					} catch (Throwable e) {
						// ignore
					}

					ColumnData columnData = new ColumnData(columnIndex, columnName, value,
							value == null ? null : value.getClass(), rowId, wasNull);
					row.addColumn(columnData);
				}

				rows.add(row);
			}
		}

		return rows;
	}
}
