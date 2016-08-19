package com.dianping.zebra.shard.jdbc.unsupport;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public abstract class UnsupportedShardConnection implements Connection {

	public void abort(Executor executor) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport abort");
	}

	@Override
	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport clearWarnings");
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport createArrayOf");
	}

	@Override
	public Blob createBlob() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport createBlob");
	}

	@Override
	public Clob createClob() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport createClob");
	}

	@Override
	public NClob createNClob() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport createNClob");
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport createSQLXML");
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport createStruct");
	}

	@Override
	public String getCatalog() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getCatalog");
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getClientInfo");
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getClientInfo");
	}

	@Override
	public int getHoldability() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getHoldability");
	}

	public int getNetworkTimeout() throws SQLException {
		throw new UnsupportedOperationException("getNetworkTimeout");
	}

	public String getSchema() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getSchema");
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getTypeMap");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getWarnings");
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport isValid");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport isWrapperFor");
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport nativeSQL");
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport prepareCall");
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport prepareCall");
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport prepareCall");
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport releaseSavePoint");
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport rollback with savepoint");
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setCatalog");
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		throw new UnsupportedOperationException("Zebra unsupport setClientInfo");
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		throw new UnsupportedOperationException("Zebra unsupport setClientInfo");
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setHoldability");
	}

	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setNetworkTimeout");
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setSavepoint");
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setSavepoint");
	}

	public void setSchema(String schema) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setSchema");
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport setTypeMap");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport unwrap");
	}
}
