/**
 * Project: zebra-client
 * 
 * File Created at 2011-6-23
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.merge;

import java.io.Serializable;
import java.sql.RowId;

/**
 * 数据列<br>
 * 主要用于内存数据池
 * 
 * @author Leo Liang
 * 
 */
public class ColumnData implements Serializable {

	private static final long	serialVersionUID	= -7920844322554948331L;

	private int					columnIndex;
	private String				columnName;
	private Object				value;
	private Class<?>			type;
	private transient RowId		rowId;
	private boolean				wasNull;

	public ColumnData(int columnIndex, String columnName, Object value, Class<?> type, RowId rowId, boolean wasNull) {
		this.columnIndex = columnIndex;
		this.columnName = columnName;
		this.value = value;
		this.type = type;
		this.rowId = rowId;
		this.wasNull = wasNull;
	}

	/**
	 * @return the wasNull
	 */
	public boolean isWasNull() {
		return wasNull;
	}

	/**
	 * @param wasNull
	 *            the wasNull to set
	 */
	public void setWasNull(boolean wasNull) {
		this.wasNull = wasNull;
	}

	/**
	 * @return the columnIndex
	 */
	public int getColumnIndex() {
		return columnIndex;
	}

	/**
	 * @param columnIndex
	 *            the columnIndex to set
	 */
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @param columnName
	 *            the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the type
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Class<?> type) {
		this.type = type;
	}

	/**
	 * @return the rowId
	 */
	public RowId getRowId() {
		return rowId;
	}

	/**
	 * @param rowId
	 *            the rowId to set
	 */
	public void setRowId(RowId rowId) {
		this.rowId = rowId;
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
		result = prime * result + columnIndex;
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + (wasNull ? 1231 : 1237);
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
		ColumnData other = (ColumnData) obj;
		if (columnIndex != other.columnIndex)
			return false;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (wasNull != other.wasNull)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[columnIndex=" + columnIndex + ", value=" + value + "]";
	}
}
