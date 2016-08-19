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
