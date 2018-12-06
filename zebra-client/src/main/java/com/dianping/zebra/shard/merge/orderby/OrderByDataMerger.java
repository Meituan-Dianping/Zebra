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

	public List<RowData> process(List<RowData> sourceData, final MergeContext mergeContext) throws SQLException {
		if (mergeContext.getOrderBy() != null) {
			Collections.sort(sourceData, new Comparator<RowData>() {
				@Override
				public int compare(RowData o1, RowData o2) {
					return compareOrderByEle(o1, o2, mergeContext);
				}
			});
		}

		return sourceData;
	}

	public int compareOrderByEle(RowData o1, RowData o2, MergeContext mergeContext) {
		SQLOrderBy orderBy = mergeContext.getOrderBy();
		List<SQLSelectOrderByItem> items = orderBy.getItems();
		try {
			for (SQLSelectOrderByItem orderByEle : items) {
				SQLName identifier = (SQLName) orderByEle
				      .getExpr();
				String columnLabel = mergeContext.getColumnNameAliasMapping().get(identifier.getSimpleName());
				if (columnLabel == null) {
					columnLabel = identifier.getSimpleName();
				}

				Object value1 = o1.get(columnLabel).getValue();
				Class<?> type1 = o1.get(columnLabel).getType();
				Object value2 = o2.get(columnLabel).getValue();
				Class<?> type2 = o2.get(columnLabel).getType();

				if (!type1.equals(type2)) {
					throw new SQLException("Invalid data");
				}

				if (!Comparable.class.isAssignableFrom(type1)) {
					throw new SQLException("Can not orderBy column : " + identifier + " which isn't comparable.");
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				int compareRes = ((Comparable) value1).compareTo((Comparable) value2);

				if (orderByEle.getType() == null || ((SQLOrderingSpecification) orderByEle.getType()).name().equals("ASC")) {
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
}
