package com.dianping.zebra.shard.merge.orderby;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.dianping.zebra.shard.merge.MergeContext;
import com.dianping.zebra.shard.merge.DataMerger;
import com.dianping.zebra.shard.merge.RowData;

public class OrderByDataMerger implements DataMerger {

	public List<RowData> process(List<RowData> sourceData, MergeContext mergeContext) throws SQLException {
		if (mergeContext.getOrderBy() != null) {
			SQLOrderBy orderBy = mergeContext.getOrderBy();
			final List<SQLSelectOrderByItem> items = orderBy.getItems();
			Collections.sort(sourceData, new Comparator<RowData>() {
				@Override
				public int compare(RowData o1, RowData o2) {
					try {
						for (SQLSelectOrderByItem orderByEle : items) {
							SQLName identifier = (SQLName) orderByEle.getExpr();

							Object value1 = o1.get(identifier.getSimpleName()).getValue();
							Class<?> type1 = o1.get(identifier.getSimpleName()).getType();
							Object value2 = o2.get(identifier.getSimpleName()).getValue();
							Class<?> type2 = o2.get(identifier.getSimpleName()).getType();

							if (!type1.equals(type2)) {
								throw new SQLException("Invalid data");
							}

							if (!Comparable.class.isAssignableFrom(type1)) {
								throw new SQLException(
										"Can not orderBy column : " + identifier + " which isn't comparable.");
							}

							@SuppressWarnings({ "unchecked", "rawtypes" })
							int compareRes = ((Comparable) value1).compareTo((Comparable) value2);

							if (orderByEle.getType() == null
									|| ((SQLOrderingSpecification) orderByEle.getType()).name().equals("ASC")) {
								if (compareRes != 0) {
									return compareRes;
								}
							} else {
								if (compareRes != 0) {
									return compareRes < 0 ? 1 : -1;
								}
							}
						}

						return 0;

					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}

			});
		}

		return sourceData;
	}

}
