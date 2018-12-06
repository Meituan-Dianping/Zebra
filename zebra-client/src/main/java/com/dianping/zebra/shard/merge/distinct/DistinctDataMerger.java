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
package com.dianping.zebra.shard.merge.distinct;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.dianping.zebra.shard.merge.DataMerger;
import com.dianping.zebra.shard.merge.MergeContext;
import com.dianping.zebra.shard.merge.RowData;

public class DistinctDataMerger implements DataMerger {

	@Override
	public List<RowData> process(List<RowData> sourceData, MergeContext mergeContext) throws SQLException {
		if (mergeContext.isDistinct()) {
			Set<RowData> distinctRowSet = new LinkedHashSet<RowData>();
			for (RowData row : sourceData) {
				distinctRowSet.add(row);
			}
			return new ArrayList<RowData>(distinctRowSet);
		} else {
			return sourceData;
		}
	}
}
