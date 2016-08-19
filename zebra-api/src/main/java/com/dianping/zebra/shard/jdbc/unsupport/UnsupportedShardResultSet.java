package com.dianping.zebra.shard.jdbc.unsupport;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;

public abstract class UnsupportedShardResultSet implements ResultSet {

	@Override
	public boolean absolute(int row) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport absolute");
	}

	@Override
	public void afterLast() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport afterLast");
	}

	@Override
	public void beforeFirst() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport beforeFirst");
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport cancelRowUpdates");
	}

	@Override
	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport clearWarnings");
	}

	@Override
	public void deleteRow() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport deleteRow");
	}

	@Override
	public boolean first() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport first");
	}

	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		throw new UnsupportedOperationException("getObject");
	}

	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		throw new UnsupportedOperationException("getObject");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport getWarnings");
	}

	@Override
	public void insertRow() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport insertRow");
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport isAfterLast");
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport isBeforeFirst");
	}

	@Override
	public boolean isFirst() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport isFirst");
	}

	@Override
	public boolean isLast() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport isLast");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport isWrapperFor");
	}

	@Override
	public boolean last() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport last");
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport moveToCurrentRow");
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport moveToInsertRow");
	}

	@Override
	public boolean previous() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport previous");
	}

	@Override
	public void refreshRow() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport refreshRow");
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport relative");
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport rowDeleted");
	}

	@Override
	public boolean rowInserted() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport rowInserted");
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport rowUpdated");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport unwrap");
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateArray");
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateArray");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateAsciiStream");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateAsciiStream");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateAsciiStream");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateAsciiStream");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateAsciiStream");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateAsciiStream");
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBigDecimal");
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBigDecimal");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBinaryStream");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBinaryStream");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBinaryStream");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBinaryStream");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBinaryStream");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBinaryStream");
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBlob");
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBlob");
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBlob");
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBlob");
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBlob");
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBlob");
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBoolean");
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBoolean");
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateByte");
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateByte");
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBytes");
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateBytes");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateCharacterStream");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateCharacterStream");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateCharacterStream");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateCharacterStream");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateCharacterStream");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateCharacterStream");
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateClob");
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateClob");
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateClob");
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateClob");
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateClob");
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateClob");
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateDate");
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateDate");
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateDouble");
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateDouble");
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateFloat");
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateFloat");
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateInt");
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateInt");
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateLong");
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateLong");
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNCharacterStream");
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNCharacterStream");
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNCharacterStream");
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNCharacterStream");
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNClob");
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNClob");
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNClob");
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNClob");
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNClob");
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNClob");
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNString");
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNString");
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNull");
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateNull");
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateObject");
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateObject");
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateObject");
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateObject");
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateRef");
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateRef");
	}

	@Override
	public void updateRow() throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateRow");
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateRowId");
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateRowId");
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateShort");
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateShort");
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateSQLXML");
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateSQLXML");
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateString");
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateString");
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateTime");
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateTime");
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateTimestamp");
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		throw new UnsupportedOperationException("Zebra unsupport updateTimestamp");
	}
}
