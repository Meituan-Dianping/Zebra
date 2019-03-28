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
package com.dianping.zebra.single.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.filter.JdbcOperationCallback;
import com.dianping.zebra.group.jdbc.param.ArrayParamContext;
import com.dianping.zebra.group.jdbc.param.AsciiParamContext;
import com.dianping.zebra.group.jdbc.param.BigDecimalParamContext;
import com.dianping.zebra.group.jdbc.param.BinaryStreamParamContext;
import com.dianping.zebra.group.jdbc.param.BlobParamContext;
import com.dianping.zebra.group.jdbc.param.BooleanParamContext;
import com.dianping.zebra.group.jdbc.param.ByteArrayParamContext;
import com.dianping.zebra.group.jdbc.param.ByteParamContext;
import com.dianping.zebra.group.jdbc.param.CharacterStreamParamContext;
import com.dianping.zebra.group.jdbc.param.ClobParamContext;
import com.dianping.zebra.group.jdbc.param.DateParamContext;
import com.dianping.zebra.group.jdbc.param.DoubleParamContext;
import com.dianping.zebra.group.jdbc.param.FloatParamContext;
import com.dianping.zebra.group.jdbc.param.IntParamContext;
import com.dianping.zebra.group.jdbc.param.LongParamContext;
import com.dianping.zebra.group.jdbc.param.NCharacterStreamParamContext;
import com.dianping.zebra.group.jdbc.param.NClobParamContext;
import com.dianping.zebra.group.jdbc.param.NStringParamContext;
import com.dianping.zebra.group.jdbc.param.NullParamContext;
import com.dianping.zebra.group.jdbc.param.ObjectParamContext;
import com.dianping.zebra.group.jdbc.param.ParamContext;
import com.dianping.zebra.group.jdbc.param.RefParamContext;
import com.dianping.zebra.group.jdbc.param.RowIdParamContext;
import com.dianping.zebra.group.jdbc.param.SQLXMLParamContext;
import com.dianping.zebra.group.jdbc.param.ShortParamContext;
import com.dianping.zebra.group.jdbc.param.StringParamContext;
import com.dianping.zebra.group.jdbc.param.TimeParamContext;
import com.dianping.zebra.group.jdbc.param.TimestampParamContext;
import com.dianping.zebra.group.jdbc.param.URLParamContext;
import com.dianping.zebra.util.SqlType;
import com.dianping.zebra.util.SqlUtils;

/**
 * @author hao.zhu
 */
public class SinglePreparedStatement extends SingleStatement implements PreparedStatement {

	private String sql;

	private List<ParamContext> params = new LinkedList<>();

	private List<List<ParamContext>> pstBatchedArgs;

	private static Comparator PARAM_COMPARATOR = new Comparator<ParamContext>(){
		@Override
		public int compare(ParamContext paramContext1, ParamContext paramContext2) {
			return paramContext1.getIndex() - paramContext2.getIndex();
		}
	};

	public SinglePreparedStatement(String dsId, SingleConnection innerConnection, List<JdbcFilter> filters,
			Statement stmt, String sql) throws SQLException {
		super(dsId, innerConnection, stmt, filters);
		this.sql = sql;
	}

	private PreparedStatement getPreparedStatement() throws SQLException {
		checkClosed();
		return (PreparedStatement) innerStatement;
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return getPreparedStatement().getMetaData();
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		return getPreparedStatement().getParameterMetaData();
	}

	@Override
	public boolean execute() throws SQLException {
		SqlType sqlType = SqlUtils.getSqlType(sql);
		if (sqlType.isQuery()) {
			executeQuery();
			return true;
		} else {
			executeUpdate();
			return false;
		}
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		Collections.sort(params, PARAM_COMPARATOR);
		return executeWithFilter(new JdbcOperationCallback<ResultSet>() {
			@Override
			public ResultSet doAction(Connection conn) throws SQLException {
				ResultSet rs = getPreparedStatement().executeQuery();
				return new SingleResultSet(rs, filters);
			}
		}, sql, params, false);
	}

	@Override
	public int executeUpdate() throws SQLException {
		Collections.sort(params, PARAM_COMPARATOR);
		return executeWithFilter(new JdbcOperationCallback<Integer>() {
			@Override
			public Integer doAction(Connection conn) throws SQLException {
				SqlType sqlType = SqlUtils.getSqlType(sql);
				if(SqlType.UNKNOWN_SQL_TYPE.equals(sqlType) ){
					getPreparedStatement().execute();
					if (null != getPreparedStatement().getResultSet()) {
						return -1;
					} else {
						return getPreparedStatement().getUpdateCount();
					}
				}else{
					return getPreparedStatement().executeUpdate();
				}

			}
		}, sql, params, false);
	}

	@Override
	public int[] executeBatch() throws SQLException {
		try {
			if (pstBatchedArgs == null || pstBatchedArgs.isEmpty()) {
				return new int[0];
			}

			return executeWithFilter(new JdbcOperationCallback<int[]>() {
				@Override
				public int[] doAction(Connection conn) throws SQLException {
					return getPreparedStatement().executeBatch();
				}
			}, sql, pstBatchedArgs, true);
		} finally {
			if (pstBatchedArgs != null) {
				pstBatchedArgs.clear();
			}
		}
	}

	@Override
	public void addBatch() throws SQLException {
		if (pstBatchedArgs == null) {
			pstBatchedArgs = new ArrayList<List<ParamContext>>();
		}

		List<ParamContext> newArgs = new ArrayList<ParamContext>(params);
		Collections.sort(newArgs, PARAM_COMPARATOR);

		params.clear();

		pstBatchedArgs.add(newArgs);
		getPreparedStatement().addBatch();
	}

	@Override
	public void clearParameters() throws SQLException {
		params.clear();
		getPreparedStatement().clearParameters();
	}

	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		params.add(new NullParamContext(parameterIndex, new Object[] { sqlType }));
		getPreparedStatement().setNull(parameterIndex, sqlType);
	}

	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		params.add(new BooleanParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setBoolean(parameterIndex, x);
	}

	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
		params.add(new ByteParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setByte(parameterIndex, x);
	}

	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
		params.add(new ShortParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setShort(parameterIndex, x);
	}

	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		params.add(new IntParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setInt(parameterIndex, x);
	}

	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
		params.add(new LongParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setLong(parameterIndex, x);
	}

	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
		params.add(new FloatParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setFloat(parameterIndex, x);
	}

	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
		params.add(new DoubleParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setDouble(parameterIndex, x);
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		params.add(new BigDecimalParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setBigDecimal(parameterIndex, x);
	}

	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		params.add(new StringParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setString(parameterIndex, x);
	}

	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		params.add(new ByteArrayParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setBytes(parameterIndex, x);
	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		params.add(new DateParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setDate(parameterIndex, x);
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		params.add(new TimeParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setTime(parameterIndex, x);
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		params.add(new TimestampParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setTimestamp(parameterIndex, x);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		params.add(new AsciiParamContext(parameterIndex, new Object[] { x, length }));
		getPreparedStatement().setAsciiStream(parameterIndex, x, length);
	}

	@Override
	@Deprecated
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
		params.add(new AsciiParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setUnicodeStream(parameterIndex, x, length);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
		params.add(new BinaryStreamParamContext(parameterIndex, new Object[] { x, length }));
		getPreparedStatement().setBinaryStream(parameterIndex, x, length);
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		params.add(new ObjectParamContext(parameterIndex, new Object[] { x, targetSqlType }));
		getPreparedStatement().setObject(parameterIndex, x, targetSqlType);
	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		params.add(new ObjectParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setObject(parameterIndex, x);
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		params.add(new CharacterStreamParamContext(parameterIndex, new Object[] { reader, length }));
		getPreparedStatement().setCharacterStream(parameterIndex, reader, length);
	}

	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
		params.add(new RefParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setRef(parameterIndex, x);
	}

	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		params.add(new BlobParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setBlob(parameterIndex, x);
	}

	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
		params.add(new ClobParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setClob(parameterIndex, x);
	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
		params.add(new ArrayParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setArray(parameterIndex, x);
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		params.add(new DateParamContext(parameterIndex, new Object[] { x, cal }));
		getPreparedStatement().setDate(parameterIndex, x, cal);
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		params.add(new TimeParamContext(parameterIndex, new Object[] { x, cal }));
		getPreparedStatement().setTime(parameterIndex, x, cal);
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		params.add(new TimestampParamContext(parameterIndex, new Object[] { x, cal }));
		getPreparedStatement().setTimestamp(parameterIndex, x, cal);
	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
		params.add(new NullParamContext(parameterIndex, new Object[] { sqlType, typeName }));
		getPreparedStatement().setNull(parameterIndex, sqlType, typeName);
	}

	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
		params.add(new URLParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setURL(parameterIndex, x);
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		params.add(new RowIdParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setRowId(parameterIndex, x);
	}

	@Override
	public void setNString(int parameterIndex, String value) throws SQLException {
		params.add(new NStringParamContext(parameterIndex, new Object[] { value }));
		getPreparedStatement().setNString(parameterIndex, value);
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		params.add(new NCharacterStreamParamContext(parameterIndex, new Object[] { value, length }));
		getPreparedStatement().setNCharacterStream(parameterIndex, value, length);
	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		params.add(new NClobParamContext(parameterIndex, new Object[] { value }));
		getPreparedStatement().setNClob(parameterIndex, value);
	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		params.add(new ClobParamContext(parameterIndex, new Object[] { reader, length }));
		getPreparedStatement().setClob(parameterIndex, reader, length);
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		params.add(new BlobParamContext(parameterIndex, new Object[] { inputStream, length }));
		getPreparedStatement().setBlob(parameterIndex, inputStream, length);
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		params.add(new NClobParamContext(parameterIndex, new Object[] { reader, length }));
		getPreparedStatement().setNClob(parameterIndex, reader, length);
	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		params.add(new SQLXMLParamContext(parameterIndex, new Object[] { xmlObject }));
		getPreparedStatement().setSQLXML(parameterIndex, xmlObject);
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
		params.add(new ObjectParamContext(parameterIndex, new Object[] { x, targetSqlType, scaleOrLength }));
		getPreparedStatement().setObject(parameterIndex, x, targetSqlType, scaleOrLength);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		params.add(new AsciiParamContext(parameterIndex, new Object[] { x, length }));
		getPreparedStatement().setAsciiStream(parameterIndex, x, length);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
		params.add(new BinaryStreamParamContext(parameterIndex, new Object[] { x, length }));
		getPreparedStatement().setBinaryStream(parameterIndex, x, length);
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		params.add(new CharacterStreamParamContext(parameterIndex, new Object[] { reader, length }));
		getPreparedStatement().setCharacterStream(parameterIndex, reader, length);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
		params.add(new AsciiParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setAsciiStream(parameterIndex, x);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
		params.add(new BinaryStreamParamContext(parameterIndex, new Object[] { x }));
		getPreparedStatement().setBinaryStream(parameterIndex, x);
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		params.add(new CharacterStreamParamContext(parameterIndex, new Object[] { reader }));
		getPreparedStatement().setCharacterStream(parameterIndex, reader);
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
		params.add(new NCharacterStreamParamContext(parameterIndex, new Object[] { value }));
		getPreparedStatement().setNCharacterStream(parameterIndex, value);
	}

	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		params.add(new ClobParamContext(parameterIndex, new Object[] { reader }));
		getPreparedStatement().setClob(parameterIndex, reader);
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		params.add(new BlobParamContext(parameterIndex, new Object[] { inputStream }));
		getPreparedStatement().setBlob(parameterIndex, inputStream);
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		params.add(new NClobParamContext(parameterIndex, new Object[] { reader }));
		getPreparedStatement().setNCharacterStream(parameterIndex, reader);
	}
}
