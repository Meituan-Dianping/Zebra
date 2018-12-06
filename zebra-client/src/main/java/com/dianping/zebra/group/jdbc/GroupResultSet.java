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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra.group.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by Dozer on 9/1/14.
 */
public class GroupResultSet implements ResultSet {

	private final ResultSet innerResultSet;

	public GroupResultSet(ResultSet resultSet) {
		this.innerResultSet = resultSet;
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		return innerResultSet.absolute(row);
	}

	@Override
	public void afterLast() throws SQLException {
		innerResultSet.afterLast();
	}

	@Override
	public void beforeFirst() throws SQLException {
		innerResultSet.beforeFirst();
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		innerResultSet.cancelRowUpdates();
	}

	@Override
	public void clearWarnings() throws SQLException {
		innerResultSet.clearWarnings();
	}

	@Override
	public void close() throws SQLException {
		innerResultSet.close();
	}

	@Override
	public void deleteRow() throws SQLException {
		innerResultSet.deleteRow();
	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		return innerResultSet.findColumn(columnLabel);
	}

	@Override
	public boolean first() throws SQLException {
		return innerResultSet.first();
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		return innerResultSet.getArray(columnIndex);
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		return innerResultSet.getArray(columnLabel);
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return innerResultSet.getAsciiStream(columnIndex);
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return innerResultSet.getAsciiStream(columnLabel);
	}

	@Deprecated
	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return innerResultSet.getBigDecimal(columnIndex, scale);
	}

	@Deprecated
	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		return innerResultSet.getBigDecimal(columnLabel, scale);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return innerResultSet.getBigDecimal(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return innerResultSet.getBigDecimal(columnLabel);
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return innerResultSet.getBinaryStream(columnIndex);
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return innerResultSet.getBinaryStream(columnLabel);
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		return innerResultSet.getBlob(columnIndex);
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		return innerResultSet.getBlob(columnLabel);
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		return innerResultSet.getBoolean(columnIndex);
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		return innerResultSet.getBoolean(columnLabel);
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		return innerResultSet.getByte(columnIndex);
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		return innerResultSet.getByte(columnLabel);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		return innerResultSet.getBytes(columnIndex);
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		return innerResultSet.getBytes(columnLabel);
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return innerResultSet.getCharacterStream(columnIndex);
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return innerResultSet.getCharacterStream(columnLabel);
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		return innerResultSet.getClob(columnIndex);
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		return innerResultSet.getClob(columnLabel);
	}

	@Override
	public int getConcurrency() throws SQLException {
		return innerResultSet.getConcurrency();
	}

	@Override
	public String getCursorName() throws SQLException {
		return innerResultSet.getCursorName();
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		return innerResultSet.getDate(columnIndex);
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return innerResultSet.getDate(columnLabel);
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return innerResultSet.getDate(columnIndex, cal);
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return innerResultSet.getDate(columnLabel, cal);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		return innerResultSet.getDouble(columnIndex);
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return innerResultSet.getDouble(columnLabel);
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return innerResultSet.getFetchDirection();
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		innerResultSet.setFetchDirection(direction);
	}

	@Override
	public int getFetchSize() throws SQLException {
		return innerResultSet.getFetchSize();
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		innerResultSet.setFetchSize(rows);
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		return innerResultSet.getFloat(columnIndex);
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return innerResultSet.getFloat(columnLabel);
	}

	@Override
	public int getHoldability() throws SQLException {
		return innerResultSet.getHoldability();
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		return innerResultSet.getInt(columnIndex);
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		return innerResultSet.getInt(columnLabel);
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		return innerResultSet.getLong(columnIndex);
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		return innerResultSet.getLong(columnLabel);
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return innerResultSet.getMetaData();
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return innerResultSet.getNCharacterStream(columnIndex);
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return innerResultSet.getNCharacterStream(columnLabel);
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		return innerResultSet.getNClob(columnLabel);
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		return innerResultSet.getNString(columnIndex);
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		return innerResultSet.getNString(columnLabel);
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		return innerResultSet.getObject(columnIndex);
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return innerResultSet.getObject(columnLabel);
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		return innerResultSet.getObject(columnIndex, map);
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		return innerResultSet.getObject(columnLabel, map);
	}

	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		throw new UnsupportedOperationException("getObject");
	}

	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		throw new UnsupportedOperationException("getObject");
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		return innerResultSet.getRef(columnIndex);
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		return innerResultSet.getRef(columnLabel);
	}

	@Override
	public int getRow() throws SQLException {
		return innerResultSet.getRow();
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return innerResultSet.getRowId(columnIndex);
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return innerResultSet.getRowId(columnLabel);
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return innerResultSet.getSQLXML(columnIndex);
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return innerResultSet.getSQLXML(columnLabel);
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		return innerResultSet.getShort(columnIndex);
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		return innerResultSet.getShort(columnLabel);
	}

	@Override
	public Statement getStatement() throws SQLException {
		return innerResultSet.getStatement();
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		return innerResultSet.getString(columnIndex);
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return innerResultSet.getString(columnLabel);
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		return innerResultSet.getTime(columnIndex);
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return innerResultSet.getTime(columnLabel);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return innerResultSet.getTime(columnIndex, cal);
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return innerResultSet.getTime(columnLabel, cal);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return innerResultSet.getTimestamp(columnIndex);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return innerResultSet.getTimestamp(columnLabel);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return innerResultSet.getTimestamp(columnIndex, cal);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		return innerResultSet.getTimestamp(columnLabel, cal);
	}

	@Override
	public int getType() throws SQLException {
		return innerResultSet.getType();
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		return innerResultSet.getURL(columnIndex);
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		return innerResultSet.getURL(columnLabel);
	}

	@Deprecated
	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return innerResultSet.getUnicodeStream(columnIndex);
	}

	@Deprecated
	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return innerResultSet.getUnicodeStream(columnLabel);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return innerResultSet.getWarnings();
	}

	@Override
	public void insertRow() throws SQLException {
		innerResultSet.insertRow();
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return innerResultSet.isAfterLast();
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return innerResultSet.isBeforeFirst();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return innerResultSet.isClosed();
	}

	@Override
	public boolean isFirst() throws SQLException {
		return innerResultSet.isFirst();
	}

	@Override
	public boolean isLast() throws SQLException {
		return innerResultSet.isLast();
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return innerResultSet.isWrapperFor(iface);
	}

	@Override
	public boolean last() throws SQLException {
		return innerResultSet.last();
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		innerResultSet.moveToCurrentRow();
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		innerResultSet.moveToInsertRow();
	}

	@Override
	public boolean next() throws SQLException {
		return innerResultSet.next();
	}

	@Override
	public boolean previous() throws SQLException {
		return innerResultSet.previous();
	}

	@Override
	public void refreshRow() throws SQLException {
		innerResultSet.refreshRow();
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		return innerResultSet.relative(rows);
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return innerResultSet.rowDeleted();
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return innerResultSet.rowInserted();
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return innerResultSet.rowUpdated();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return innerResultSet.unwrap(iface);
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		innerResultSet.updateArray(columnIndex, x);
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		innerResultSet.updateArray(columnLabel, x);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		innerResultSet.updateAsciiStream(columnIndex, x, length);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		innerResultSet.updateAsciiStream(columnLabel, x, length);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		innerResultSet.updateAsciiStream(columnIndex, x, length);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		innerResultSet.updateAsciiStream(columnLabel, x, length);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		innerResultSet.updateAsciiStream(columnIndex, x);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		innerResultSet.updateAsciiStream(columnLabel, x);
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		innerResultSet.updateBigDecimal(columnIndex, x);
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		innerResultSet.updateBigDecimal(columnLabel, x);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		innerResultSet.updateBinaryStream(columnIndex, x, length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		innerResultSet.updateBinaryStream(columnLabel, x, length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		innerResultSet.updateBinaryStream(columnIndex, x, length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		innerResultSet.updateBinaryStream(columnLabel, x, length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		innerResultSet.updateBinaryStream(columnIndex, x);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		innerResultSet.updateBinaryStream(columnLabel, x);
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		innerResultSet.updateBlob(columnIndex, x);
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		innerResultSet.updateBlob(columnLabel, x);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		innerResultSet.updateBlob(columnIndex, inputStream, length);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		innerResultSet.updateBlob(columnLabel, inputStream, length);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		innerResultSet.updateBlob(columnIndex, inputStream);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		innerResultSet.updateBlob(columnLabel, inputStream);
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		innerResultSet.updateBoolean(columnIndex, x);
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		innerResultSet.updateBoolean(columnLabel, x);
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		innerResultSet.updateByte(columnIndex, x);
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		innerResultSet.updateByte(columnLabel, x);
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		innerResultSet.updateBytes(columnIndex, x);
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		innerResultSet.updateBytes(columnLabel, x);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		innerResultSet.updateCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		innerResultSet.updateCharacterStream(columnLabel, reader, length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		innerResultSet.updateCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		innerResultSet.updateCharacterStream(columnLabel, reader, length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		innerResultSet.updateCharacterStream(columnIndex, x);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		innerResultSet.updateCharacterStream(columnLabel, reader);
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		innerResultSet.updateClob(columnIndex, x);
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		innerResultSet.updateClob(columnLabel, x);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		innerResultSet.updateClob(columnIndex, reader, length);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		innerResultSet.updateClob(columnLabel, reader, length);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		innerResultSet.updateClob(columnIndex, reader);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		innerResultSet.updateClob(columnLabel, reader);
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		innerResultSet.updateDate(columnIndex, x);
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		innerResultSet.updateDate(columnLabel, x);
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		innerResultSet.updateDouble(columnIndex, x);
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		innerResultSet.updateDouble(columnLabel, x);
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		innerResultSet.updateFloat(columnIndex, x);
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		innerResultSet.updateFloat(columnLabel, x);
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		innerResultSet.updateInt(columnIndex, x);
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		innerResultSet.updateInt(columnLabel, x);
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		innerResultSet.updateLong(columnIndex, x);
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		innerResultSet.updateLong(columnLabel, x);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		innerResultSet.updateNCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		innerResultSet.updateNCharacterStream(columnLabel, reader, length);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		innerResultSet.updateNCharacterStream(columnIndex, x);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		innerResultSet.updateNCharacterStream(columnLabel, reader);
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		innerResultSet.updateNClob(columnIndex, nClob);
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		innerResultSet.updateNClob(columnLabel, nClob);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		innerResultSet.updateNClob(columnIndex, reader, length);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		innerResultSet.updateNClob(columnLabel, reader, length);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		innerResultSet.updateNClob(columnIndex, reader);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		innerResultSet.updateNClob(columnLabel, reader);
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		innerResultSet.updateNString(columnIndex, nString);
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		innerResultSet.updateNString(columnLabel, nString);
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		innerResultSet.updateNull(columnIndex);
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		innerResultSet.updateNull(columnLabel);
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		innerResultSet.updateObject(columnIndex, x, scaleOrLength);
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		innerResultSet.updateObject(columnIndex, x);
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		innerResultSet.updateObject(columnLabel, x, scaleOrLength);
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		innerResultSet.updateObject(columnLabel, x);
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		innerResultSet.updateRef(columnIndex, x);
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		innerResultSet.updateRef(columnLabel, x);
	}

	@Override
	public void updateRow() throws SQLException {
		innerResultSet.updateRow();
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		innerResultSet.updateRowId(columnIndex, x);
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		innerResultSet.updateRowId(columnLabel, x);
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		innerResultSet.updateSQLXML(columnIndex, xmlObject);
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		innerResultSet.updateSQLXML(columnLabel, xmlObject);
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		innerResultSet.updateShort(columnIndex, x);
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		innerResultSet.updateShort(columnLabel, x);
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		innerResultSet.updateString(columnIndex, x);
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		innerResultSet.updateString(columnLabel, x);
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		innerResultSet.updateTime(columnIndex, x);
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		innerResultSet.updateTime(columnLabel, x);
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		innerResultSet.updateTimestamp(columnIndex, x);
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		innerResultSet.updateTimestamp(columnLabel, x);
	}

	@Override
	public boolean wasNull() throws SQLException {
		return innerResultSet.wasNull();
	}
}
