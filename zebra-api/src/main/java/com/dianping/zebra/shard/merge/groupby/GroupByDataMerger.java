package com.dianping.zebra.shard.merge.groupby;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.dianping.zebra.shard.merge.ColumnData;
import com.dianping.zebra.shard.merge.DataMerger;
import com.dianping.zebra.shard.merge.MergeContext;
import com.dianping.zebra.shard.merge.RowData;
import com.dianping.zebra.shard.merge.groupby.aggregate.AggregateException;
import com.dianping.zebra.shard.merge.groupby.aggregate.CountAggregator;
import com.dianping.zebra.shard.merge.groupby.aggregate.MaxAggregator;
import com.dianping.zebra.shard.merge.groupby.aggregate.MinAggregator;
import com.dianping.zebra.shard.merge.groupby.aggregate.SumAggregator;

public class GroupByDataMerger implements DataMerger {

	private static Map<String, Aggregator> aggregateFunctionProcessors;

	static {
		aggregateFunctionProcessors = new HashMap<String, Aggregator>();
		aggregateFunctionProcessors.put("MAX", new MaxAggregator());
		aggregateFunctionProcessors.put("MIN", new MinAggregator());
		aggregateFunctionProcessors.put("COUNT", new CountAggregator());
		aggregateFunctionProcessors.put("SUM", new SumAggregator());
	}

	public List<RowData> process(List<RowData> sourceData, MergeContext mergeContext) throws SQLException {
		List<RowData> processedDatas = new ArrayList<RowData>();

		// 没有group by并且没有聚合函数，则直接返回源数据
		if (mergeContext.getGroupByColumns().size() <= 0 && !mergeContext.isAggregate()) {
			return sourceData;
		}

		Map<String, SQLSelectItem> columnNameFunctionMapping = mergeContext.getSelectItemMap();

		if (mergeContext.getGroupByColumns() == null || mergeContext.getGroupByColumns().size() <= 0) {
			RowData aggregateRow = null;
			for (RowData row : sourceData) {
				if (aggregateRow == null) {
					aggregateRow = new RowData(row);
				} else {
					calAggregateFunctionValue(columnNameFunctionMapping, row, aggregateRow, null);
				}
			}
			processedDatas.add(aggregateRow);
		} else {
			procGroupBy(sourceData, mergeContext, processedDatas, columnNameFunctionMapping);
		}

		return processedDatas;
	}

	private void calAggregateFunctionValue(Map<String, SQLSelectItem> columnNameFunctionMapping, RowData row,
			RowData newRowData, List<Integer> ignoreColumnList) throws SQLException {
		try {
			for (ColumnData col : row.getColumnDatas()) {
				if (ignoreColumnList != null && ignoreColumnList.contains(col.getColumnIndex())) {
					continue;
				}
				SQLSelectItem columnType = columnNameFunctionMapping.get(col.getColumnName());
				SQLAggregateExpr aggregateExpr = (SQLAggregateExpr) columnType.getExpr();
				Aggregator dataProcessor = aggregateFunctionProcessors.get(aggregateExpr.getMethodName());
				if (dataProcessor != null) {
					ColumnData oldCol = newRowData.get(col.getColumnIndex());
					Object value = dataProcessor.process(oldCol.getValue(), col.getValue());

					if (value != null) {
						oldCol.setWasNull(false);
					}
					oldCol.setValue(value);
				} else {
					throw new SQLException("Zebra unsupported groupby function exists");
				}
			}
		} catch (AggregateException e) {
			throw new SQLException("Proc aggregate merge failed.");
		}
	}

	private void procGroupBy(List<RowData> sourceData, MergeContext mergeContext, List<RowData> processedDatas,
			Map<String, SQLSelectItem> columnNameFunctionMapping) throws SQLException {
		// 因为group by后面跟的只能是列名，但是如果select中包含别名，则ColumnData中存放的是别名
		// 所以先获得列名和别名的map
		Map<String, String> columnNameAliasMapping = mergeContext.getColumnNameAliasMapping();

		List<Integer> groupByColumnIndexes = new ArrayList<Integer>();
		for (String columnName : mergeContext.getGroupByColumns()) {
			groupByColumnIndexes.add(sourceData.get(0).get(columnNameAliasMapping.containsKey(columnName)
					? columnNameAliasMapping.get(columnName) : columnName).getColumnIndex());
		}

		Map<MultiKey, RowData> tmpMap = new LinkedHashMap<MultiKey, RowData>();
		for (RowData row : sourceData) {
			List<Object> groupByValues = new ArrayList<Object>(mergeContext.getGroupByColumns().size());

			for (Integer groupByColIndex : groupByColumnIndexes) {
				groupByValues.add(row.get(groupByColIndex).getValue());
			}

			// 多个group by的值作为一个MultiKey用于聚合Map
			MultiKey multiKey = new MultiKey(groupByValues);

			RowData groupByRowData = tmpMap.get(multiKey);

			if (groupByRowData == null) {
				groupByRowData = new RowData(row);
				tmpMap.put(multiKey, groupByRowData);
			} else {
				calAggregateFunctionValue(columnNameFunctionMapping, row, groupByRowData, groupByColumnIndexes);
			}
		}

		for (Map.Entry<MultiKey, RowData> entry : tmpMap.entrySet()) {
			processedDatas.add(entry.getValue());
		}
	}
}
