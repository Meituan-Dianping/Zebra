package com.dianping.zebra.shard.parser;

public interface SQLRewrite{
	
	public String rewrite(SQLParsedResult pr,String logicalTable, String physicalTable);
	
}
