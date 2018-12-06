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
package com.dianping.zebra.shard.merge;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据行<br>
 * 主要用于内存数据池
 * 
 * @author Leo Liang
 * 
 */
public class RowData implements Serializable {

	private static final long			serialVersionUID	= -5705077289803105656L;

	private Map<String, ColumnData>		nameColumnMapping	= new HashMap<String, ColumnData>();
	private Map<Integer, ColumnData>	indexColumnMapping	= new HashMap<Integer, ColumnData>();
	private List<ColumnData>			columnDatas			= new ArrayList<ColumnData>();
	private transient ResultSet			resultSet;

	public RowData(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public RowData(RowData row) {
		this.resultSet = row.resultSet;
		this.columnDatas = row.columnDatas;
		this.indexColumnMapping = row.indexColumnMapping;
		this.nameColumnMapping = row.nameColumnMapping;
	}

	/**
	 * @return the resultSet
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}

	/**
	 * @return the resultSetType
	 */
	public int getResultSetType() throws SQLException {
		return resultSet.getType();
	}

	/**
	 * @return the resultSetMetaData
	 */
	public ResultSetMetaData getResultSetMetaData() throws SQLException {
		return resultSet.getMetaData();
	}

	/**
	 * @return the holdability
	 */
	public int getHoldability() throws SQLException {
		return resultSet.getHoldability();
	}

	/**
	 * @return the cursorName
	 * @throws java.sql.SQLException
	 */
	public String getCursorName() throws SQLException {
		return resultSet.getCursorName();
	}

	/**
	 * @return the concurrency
	 */
	public int getConcurrency() throws SQLException {
		return resultSet.getConcurrency();
	}

	public ColumnData get(String colName) throws SQLException {
		// 因为select * 的时候会根据数据库本身的一些配置导致colName为大写或者小写，所以以下为尝试方法
		if (nameColumnMapping.containsKey(colName)) {
			return nameColumnMapping.get(colName);
		} else if (nameColumnMapping.containsKey(colName.toUpperCase())) {
			return nameColumnMapping.get(colName.toUpperCase());
		} else if (nameColumnMapping.containsKey(colName.toLowerCase())) {
			return nameColumnMapping.get(colName.toLowerCase());
		} else {
			throw new SQLException("No column named : " + colName);
		}
	}

	public ColumnData get(int index) throws SQLException {
		if (!indexColumnMapping.containsKey(index)) {
			throw new SQLException("No column index : " + index);
		}

		return indexColumnMapping.get(index);
	}

	public int getIndexByName(String colName) throws SQLException {
		if (!nameColumnMapping.containsKey(colName)) {
			throw new SQLException("No column named : " + colName);
		}
		return nameColumnMapping.get(colName).getColumnIndex();
	}

	public void addColumn(ColumnData col) {
		columnDatas.add(col);
		nameColumnMapping.put(col.getColumnName(), col);
		indexColumnMapping.put(col.getColumnIndex(), col);
	}

	public List<ColumnData> getColumnDatas() {
		return columnDatas;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnDatas == null) ? 0 : columnDatas.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RowData other = (RowData) obj;
		if (columnDatas == null) {
			if (other.columnDatas != null)
				return false;
		} else if (!columnDatas.equals(other.columnDatas))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RowData [columnDatas=" + columnDatas + "]";
	}
}
