package com.dianping.zebra.shard.merge;

import java.sql.SQLException;
import java.util.List;

public interface DataMerger {

	public List<RowData> process(List<RowData> sourceData, MergeContext mergeContext) throws SQLException;

}
