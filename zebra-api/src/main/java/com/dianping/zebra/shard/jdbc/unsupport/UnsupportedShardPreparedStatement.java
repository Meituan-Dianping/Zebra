package com.dianping.zebra.shard.jdbc.unsupport;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.dianping.zebra.shard.jdbc.ShardStatement;

public abstract class UnsupportedShardPreparedStatement extends ShardStatement implements PreparedStatement{

	@Override
	public void addBatch() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport addBatch");
	}
	
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getMetaData");
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getParameterMetaData");
	}

}
