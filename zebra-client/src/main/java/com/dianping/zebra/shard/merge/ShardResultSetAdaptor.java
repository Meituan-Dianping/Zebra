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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
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
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.dianping.zebra.shard.jdbc.unsupport.UnsupportedShardResultSet;


/**
 * 数据池， 用于隐藏真实数据的来源。<br>
 * 真实数据来源包括两个可能：
 * <ol>
 * <li>由若干个<tt>ResultSet</tt>简单串联组成的复合<tt>ResultSet</tt> ，这种情况通常不需要Zebra进行数据处理（没有跨表跨库orderby并且没有跨库跨表聚合函数列存在）。</li>
 * <li>由若干个<tt>ResultSet</tt>中的所有数据经过全局排序以及数据合并（主要针对跨库跨表的全局聚合函数）而得到的 <tt>List</tt>组成。这种情况的数据池称作内存数据池</li>
 * </ol>
 * 在遍历数据的时候，<br>
 * 对于第一种情况，我们只要简单的按照顺序遍历每一个<tt>ResultSet</tt>并且调用具体的<tt>ResultSet</tt>方法即可。<br>
 * 对于第二种情况，我们需要遍历<tt>List</tt>，同时进行必要的数据类型转换。<br>
 * <p/>
 * 数据池支持limit子句的，通过设定对应的<tt>skip</tt>，<tt>max</tt>属性并调用<tt>procLimit</tt> 方法以调整数据池的初始状态。<br>
 *
 * @author Leo Liang
 */
public class ShardResultSetAdaptor extends UnsupportedShardResultSet implements ResultSet {

	protected List<ResultSet> resultSets = new ArrayList<ResultSet>();

	protected List<RowData> memoryData;

	protected boolean inMemory = false;

	protected int resultSetIndex = 0;

	protected int rowNum = 0;

	protected int skip = MergeContext.NO_OFFSET;

	protected int max = MergeContext.NO_LIMIT;

	private boolean wasNull = false;

	private ResultSetMetaData memoryResultSetMetaData;			// for getMetaData bug when procLimit return 0 rows

	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;	// for getMetaData bug when procLimit return 0 rows

	/**
	 * 滚动数据池游标到下一条记录
	 *
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean next() throws SQLException {
		rowNum++;
		if (!inMemory) {
			if (max != MergeContext.NO_LIMIT && rowNum > max) {
				return false;
			}
			if (resultSets.size() > 0) {
				if (resultSetIndex >= resultSets.size()) {
					return false;
				}
				if (!resultSets.get(resultSetIndex).next()) {
					while (++resultSetIndex < resultSets.size()) {
						if (resultSets.get(resultSetIndex).next()) {
							break;
						}
					}
					if (resultSetIndex >= resultSets.size()) {
						return false;
					} else {
						return true;
					}
				} else {
					return true;
				}
			} else {
				return false;
			}
		} else {
			return rowNum - 1 < memoryData.size();
		}
	}

	/**
	 * @return the skip
	 */
	public int getSkip() {
		return skip;
	}

	/**
	 * @param skip
	 *           the skip to set
	 */
	public void setSkip(int skip) {
		this.skip = skip;
	}

	/**
	 * @return the max
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @param max
	 *           the max to set
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * 设定内存数据(<tt>List</tt>)
	 *
	 * @param memoryData
	 *           the memoryData to set
	 */
	public void setMemoryData(List<RowData> memoryData) {
		this.inMemory = true;
		this.memoryData = memoryData;
	}

	/**
	 * 是否内存数据池
	 *
	 * @return the inMemory
	 */
	public boolean isInMemory() {
		return inMemory;
	}

	public void setResultSets(List<ResultSet> resultSets) {
		this.resultSets = resultSets;
	}

	/**
	 * <p>
	 * 处理limit
	 * </p>
	 *
	 * @throws java.sql.SQLException
	 */
	public void procLimit() throws SQLException {
		if (inMemory) {
			int fromIndex = skip == MergeContext.NO_OFFSET ? 0 : skip;
			if (fromIndex >= memoryData.size()) {
				if (this.memoryData.size() > 0) {
					this.memoryResultSetMetaData = memoryData.get(0).getResultSetMetaData();
					this.resultSetType = memoryData.get(0).getResultSetType();
				}
				this.memoryData = new ArrayList<RowData>();
				return;
			}
			int toIndex = max == MergeContext.NO_LIMIT ? memoryData.size() : fromIndex + max;
			toIndex = toIndex > memoryData.size() ? memoryData.size() : toIndex;
			List<RowData> subDataList = memoryData.subList(fromIndex, toIndex);

			this.memoryData = new ArrayList<RowData>(subDataList);
		} else {
			if (skip > 0) {
				int rowSkipped = 0;
				for (int i = 0; i < resultSets.size(); i++) {
					resultSetIndex = i;
					while (resultSets.get(i).next()) {
						if (++rowSkipped >= skip) {
							break;
						}
					}

					if (rowSkipped >= skip) {
						break;
					}
				}
			}
		}
	}

	/**
	 * 清理数据池中所有数据，并重置所有状态位
	 */
	public void clear() {
		this.resultSets.clear();
		if (inMemory && this.memoryData != null) {
			this.memoryData.clear();
		}
		this.inMemory = false;
		this.resultSetIndex = 0;
		this.rowNum = 0;
	}

	/**
	 * 获得当前数据偏移辆（以1开始）
	 *
	 * @return
	 */
	public int getCurrentRowNo() {
		return rowNum;
	}

	public int findColumn(String columnName) throws SQLException {
		if (inMemory) {
			return memoryData.get(rowNum - 1).getIndexByName(columnName);
		} else {
			return resultSets.get(resultSetIndex).findColumn(columnName);
		}
	}

	public Array getArray(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (Array) memoryData.get(rowNum - 1).get(columnIndex).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getArray(columnIndex);
		}
	}

	public Array getArray(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (Array) memoryData.get(rowNum - 1).get(columnName).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getArray(columnName);
		}
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (value != null) {
					return new ByteArrayInputStream((byte[]) value);
				}
				return null;
			}
		} else {
			return resultSets.get(resultSetIndex).getAsciiStream(columnIndex);
		}
	}

	public InputStream getAsciiStream(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (value != null) {
					return new ByteArrayInputStream((byte[]) value);
				}
				return null;
			}
		} else {
			return resultSets.get(resultSetIndex).getAsciiStream(columnName);
		}
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (value instanceof BigDecimal) {
					return (BigDecimal) value;
				} else {
					return new BigDecimal(value.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getBigDecimal(columnIndex);
		}
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (value instanceof BigDecimal) {
					return (BigDecimal) value;
				} else {
					return new BigDecimal(value.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getBigDecimal(columnName);
		}
	}

	@SuppressWarnings("deprecation")
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (value instanceof BigDecimal) {
					return ((BigDecimal) value).setScale(scale);
				} else {
					return new BigDecimal(value.toString()).setScale(scale);
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getBigDecimal(columnIndex, scale);
		}
	}

	@SuppressWarnings("deprecation")
	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (value instanceof BigDecimal) {
					return ((BigDecimal) value).setScale(scale);
				} else {
					return new BigDecimal(value.toString()).setScale(scale);
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getBigDecimal(columnName, scale);
		}
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (value != null) {
					return new ByteArrayInputStream((byte[]) value);
				}

				return null;
			}
		} else {
			return resultSets.get(resultSetIndex).getBinaryStream(columnIndex);
		}
	}

	public InputStream getBinaryStream(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (value != null) {
					return new ByteArrayInputStream((byte[]) value);
				}

				return null;
			}
		} else {
			return resultSets.get(resultSetIndex).getBinaryStream(columnName);
		}
	}

	public Blob getBlob(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				return new com.dianping.zebra.shard.resultset.Blob((byte[]) value);
			}
		} else {
			return resultSets.get(resultSetIndex).getBlob(columnIndex);
		}
	}

	public Blob getBlob(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				return new com.dianping.zebra.shard.resultset.Blob((byte[]) value);
			}
		} else {
			return resultSets.get(resultSetIndex).getBlob(columnName);
		}
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return false;
			} else {
				Object result = memoryData.get(rowNum - 1).get(columnIndex).getValue();

				if (result instanceof Boolean) {
					return (Boolean) result;
				} else if (result instanceof String) {
					String stringVal = String.valueOf(result);
					int c = Character.toLowerCase(stringVal.charAt(0));

					return ((c == 't') || (c == '1') || stringVal.equals("-1"));
				} else {
					Long longVal = Long.parseLong(String.valueOf(result));

					return (longVal > 0 || longVal == -1);
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getBoolean(columnIndex);
		}
	}

	public boolean getBoolean(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return false;
			} else {
				Object result = memoryData.get(rowNum - 1).get(columnName).getValue();

				if (result instanceof Boolean) {
					return (Boolean) result;
				} else if (result instanceof String) {
					String stringVal = String.valueOf(result);
					int c = Character.toLowerCase(stringVal.charAt(0));

					return ((c == 't') || (c == '1') || stringVal.equals("-1"));
				} else {
					Long longVal = Long.parseLong(String.valueOf(result));

					return (longVal > 0 || longVal == -1);
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getBoolean(columnName);
		}
	}

	public byte getByte(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				return Byte.parseByte(value.toString());
			}
		} else {
			return resultSets.get(resultSetIndex).getByte(columnIndex);
		}
	}

	public byte getByte(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				return Byte.parseByte(value.toString());
			}
		} else {
			return resultSets.get(resultSetIndex).getByte(columnName);
		}
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (byte[]) memoryData.get(rowNum - 1).get(columnIndex).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getBytes(columnIndex);
		}
	}

	public byte[] getBytes(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (byte[]) memoryData.get(rowNum - 1).get(columnName).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getBytes(columnName);
		}
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				return new StringReader(value.toString());
			}
		} else {
			return resultSets.get(resultSetIndex).getCharacterStream(columnIndex);
		}
	}

	public Reader getCharacterStream(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				return new StringReader(value.toString());
			}
		} else {
			return resultSets.get(resultSetIndex).getCharacterStream(columnName);
		}
	}

	public Clob getClob(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				return new com.dianping.zebra.shard.resultset.Clob(value.toString());
			}
		} else {
			return resultSets.get(resultSetIndex).getClob(columnIndex);
		}
	}

	public Clob getClob(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				return new com.dianping.zebra.shard.resultset.Clob(value.toString());
			}
		} else {
			return resultSets.get(resultSetIndex).getClob(columnName);
		}
	}

	public int getConcurrency() throws SQLException {
		if (inMemory) {
			return memoryData.get(rowNum - 1).getConcurrency();
		} else {
			return resultSets.get(resultSetIndex).getConcurrency();
		}

	}

	public String getCursorName() throws SQLException {
		if (inMemory) {
			return memoryData.get(rowNum - 1).getCursorName();
		} else {
			return resultSets.get(resultSetIndex).getCursorName();
		}
	}

	public Date getDate(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (value instanceof String) {
					return Date.valueOf(value.toString());
				} else {
					return (Date) value;
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getDate(columnIndex);
		}
	}

	public Date getDate(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();

				if (value instanceof String) {
					return Date.valueOf(value.toString());
				} else {
					return (Date) value;
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getDate(columnName);
		}
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		if (inMemory) {
			throw new UnsupportedOperationException(
					"Zebra unsupport getDate with Calendar in a multi actual datasource query.");
		} else {
			return resultSets.get(columnIndex).getDate(columnIndex, cal);
		}
	}

	public Date getDate(String columnName, Calendar cal) throws SQLException {
		if (inMemory) {
			throw new UnsupportedOperationException(
					"Zebra unsupport getDate with Calendar in a multi actual datasource query.");
		} else {
			return resultSets.get(resultSetIndex).getDate(columnName, cal);
		}
	}

	public double getDouble(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object val = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (val instanceof Double) {
					return (Double) val;
				} else {
					return Double.parseDouble(val.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getDouble(columnIndex);
		}
	}

	public double getDouble(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object val = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (val instanceof Double) {
					return (Double) val;
				} else {
					return Double.parseDouble(val.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getDouble(columnName);
		}
	}

	public float getFloat(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object val = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (val instanceof Float) {
					return (Float) val;
				} else {
					return Float.parseFloat(val.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getFloat(columnIndex);
		}
	}

	public float getFloat(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object val = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (val instanceof Float) {
					return (Float) val;
				} else {
					return Float.parseFloat(val.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getFloat(columnName);
		}
	}

	public int getHoldability() throws SQLException {
		if (inMemory) {
			return memoryData.get(rowNum - 1).getHoldability();
		} else {
			return resultSets.get(resultSetIndex).getHoldability();
		}
	}

	public int getInt(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object val = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (val instanceof Integer) {
					return (Integer) val;
				} else if (val instanceof Boolean) {
					return (Boolean) val ? 1 : 0;
				} else {
					return Integer.parseInt(val.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getInt(columnIndex);
		}
	}

	public int getInt(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object val = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (val instanceof Integer) {
					return (Integer) val;
				} else if (val instanceof Boolean) {
					return (Boolean) val ? 1 : 0;
				} else {
					return Integer.parseInt(val.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getInt(columnName);
		}
	}

	public long getLong(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object val = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (val instanceof Long) {
					return (Long) val;
				} else if (val instanceof BigInteger) {
					return ((BigInteger) val).longValue();
				} else if (val instanceof Boolean) {
					return (Boolean) val ? 1L : 0L;
				} else {
					return Long.parseLong(val.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getLong(columnIndex);
		}
	}

	public long getLong(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object val = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (val instanceof Long) {
					return (Long) val;
				} else if (val instanceof BigInteger) {
					return ((BigInteger) val).longValue();
				} else if (val instanceof Boolean) {
					return (Boolean) val ? 1L : 0L;
				} else {
					return Long.parseLong(val.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getLong(columnName);
		}
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		if (inMemory) {
			if (memoryData.size() > 0) {
				return memoryData.get(rowNum == 0 ? 0 : rowNum - 1).getResultSetMetaData();
			} else {
				if (this.memoryResultSetMetaData != null) {
					return this.memoryResultSetMetaData;
				} else {
					return resultSets.get(0).getMetaData();
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getMetaData();
		}
	}

	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (Reader) memoryData.get(rowNum - 1).get(columnIndex).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getNCharacterStream(columnIndex);
		}
	}

	public Reader getNCharacterStream(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (Reader) memoryData.get(rowNum - 1).get(columnName).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getNCharacterStream(columnName);
		}
	}

	public NClob getNClob(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (NClob) memoryData.get(rowNum - 1).get(columnIndex).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getNClob(columnIndex);
		}
	}

	public NClob getNClob(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (NClob) memoryData.get(rowNum - 1).get(columnName).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getNClob(columnName);
		}
	}

	public String getNString(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return String.valueOf(memoryData.get(rowNum - 1).get(columnIndex).getValue());
			}
		} else {
			return resultSets.get(resultSetIndex).getNString(columnIndex);
		}
	}

	public String getNString(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return String.valueOf(memoryData.get(rowNum - 1).get(columnName).getValue());
			}
		} else {
			return resultSets.get(resultSetIndex).getNString(columnName);
		}
	}

	public Object getObject(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();
			return memoryData.get(rowNum - 1).get(columnIndex).getValue();
		} else {
			return resultSets.get(resultSetIndex).getObject(columnIndex);
		}
	}

	public Object getObject(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();
			return memoryData.get(rowNum - 1).get(columnName).getValue();
		} else {
			return resultSets.get(resultSetIndex).getObject(columnName);
		}
	}

	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		// Mysql Connector-j doesn't use the parameter map at all....
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();
			return memoryData.get(rowNum - 1).get(columnIndex).getValue();
		} else {
			return resultSets.get(resultSetIndex).getObject(columnIndex);
		}
	}

	public Object getObject(String columnName, Map<String, Class<?>> map) throws SQLException {
		// Mysql Connector-j doesn't use the parameter map at all....
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();
			return memoryData.get(rowNum - 1).get(columnName).getValue();
		} else {
			return resultSets.get(resultSetIndex).getObject(columnName);
		}
	}

	public Ref getRef(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (Ref) memoryData.get(rowNum - 1).get(columnIndex).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getRef(columnIndex);
		}
	}

	public Ref getRef(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();
			return (Ref) memoryData.get(rowNum - 1).get(columnName).getValue();
		} else {
			return resultSets.get(resultSetIndex).getRef(columnName);
		}
	}

	public RowId getRowId(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (RowId) memoryData.get(rowNum - 1).get(columnIndex).getRowId();
			}
		} else {
			return resultSets.get(resultSetIndex).getRowId(columnIndex);
		}
	}

	public RowId getRowId(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (RowId) memoryData.get(rowNum - 1).get(columnName).getRowId();
			}
		} else {
			return resultSets.get(resultSetIndex).getRowId(columnName);
		}
	}

	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (SQLXML) memoryData.get(rowNum - 1).get(columnIndex).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getSQLXML(columnIndex);
		}
	}

	public SQLXML getSQLXML(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				return (SQLXML) memoryData.get(rowNum - 1).get(columnName).getValue();
			}
		} else {
			return resultSets.get(resultSetIndex).getSQLXML(columnName);
		}
	}

	public short getShort(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (value instanceof Short) {
					return (Short) value;
				} else {
					return Short.parseShort(value.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getShort(columnIndex);
		}
	}

	public short getShort(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return 0;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (value instanceof Short) {
					return (Short) value;
				} else {
					return Short.parseShort(value.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getShort(columnName);
		}
	}

	public String getString(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (value instanceof String) {
					return (String) value;
				} else if (value instanceof byte[]) {
					return new String((byte[]) value);
				} else {
					return String.valueOf(value);
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getString(columnIndex);
		}
	}

	public String getString(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();
			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (value instanceof String) {
					return (String) value;
				} else if (value instanceof byte[]) {
					return new String((byte[]) value);
				} else {
					return String.valueOf(value);
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getString(columnName);
		}
	}

	public Time getTime(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (value instanceof Time) {
					return (Time) value;
				} else {
					return Time.valueOf(value.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getTime(columnIndex);
		}
	}

	public Time getTime(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (value instanceof Time) {
					return (Time) value;
				} else {
					return Time.valueOf(value.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getTime(columnName);
		}
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		if (inMemory) {
			throw new UnsupportedOperationException(
					"Zebra unsupport getTime with Calendar in a multi actual datasource query.");
		} else {
			return resultSets.get(resultSetIndex).getTime(columnIndex, cal);
		}
	}

	public Time getTime(String columnName, Calendar cal) throws SQLException {
		if (inMemory) {
			throw new UnsupportedOperationException(
					"Zebra unsupport getTime with Calendar in a multi actual datasource query.");
		} else {
			return resultSets.get(resultSetIndex).getTime(columnName, cal);
		}
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (value instanceof Timestamp) {
					return (Timestamp) value;
				} else if (value instanceof Date) {
					return new Timestamp(((Date) value).getTime());
				} else if(value instanceof Time) {
					return new Timestamp(((Time) value).getTime());
				} else {
					return Timestamp.valueOf(value.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getTimestamp(columnIndex);
		}
	}

	public Timestamp getTimestamp(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (value instanceof Timestamp) {
					return (Timestamp) value;
				} else if (value instanceof Date) {
					return new Timestamp(((Date) value).getTime());
				} else if(value instanceof Time) {
					return new Timestamp(((Time) value).getTime());
				} else {
					return Timestamp.valueOf(value.toString());
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getTimestamp(columnName);
		}
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		if (inMemory) {
			throw new UnsupportedOperationException(
					"Zebra unsupport getTimestamp with Calendar in a multi actual datasource query.");
		} else {
			return resultSets.get(resultSetIndex).getTimestamp(columnIndex, cal);
		}
	}

	public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
		if (inMemory) {
			throw new UnsupportedOperationException(
					"Zebra unsupport getTimestamp with Calendar in a multi actual datasource query.");
		} else {
			return resultSets.get(resultSetIndex).getTimestamp(columnName, cal);
		}
	}

	public int getType() throws SQLException {
		if (inMemory) {
			if (rowNum >= 1) {
				return memoryData.get(rowNum - 1).getResultSetType();
			} else if (memoryData != null && memoryData.size() > 0) {
				return memoryData.get(0).getResultSetType();
			} else {
				if (resultSets.size() > 0) {
					return resultSets.get(resultSetIndex).getType();
				}
				return resultSetType;
			}
		} else {
			return resultSets.get(resultSetIndex).getType();
		}
	}

	public URL getURL(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				try {
					return new URL(value.toString());
				} catch (MalformedURLException mfe) {
					throw new SQLException("ResultSet.Malformed_URL '" + value + "'");
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getURL(columnIndex);
		}
	}

	public URL getURL(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				try {
					return new URL(value.toString());
				} catch (MalformedURLException mfe) {
					throw new SQLException("ResultSet.Malformed_URL '" + value + "'");
				}
			}
		} else {
			return resultSets.get(resultSetIndex).getURL(columnName);
		}
	}

	@SuppressWarnings("deprecation")
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnIndex).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnIndex).getValue();
				if (value != null) {
					return new ByteArrayInputStream((byte[]) value);
				}
				return null;
			}
		} else {
			return resultSets.get(resultSetIndex).getUnicodeStream(columnIndex);
		}
	}

	@SuppressWarnings("deprecation")
	public InputStream getUnicodeStream(String columnName) throws SQLException {
		if (inMemory) {
			wasNull = memoryData.get(rowNum - 1).get(columnName).isWasNull();

			if (wasNull) {
				return null;
			} else {
				Object value = memoryData.get(rowNum - 1).get(columnName).getValue();
				if (value != null) {
					return new ByteArrayInputStream((byte[]) value);
				}
				return null;
			}
		} else {
			return resultSets.get(resultSetIndex).getUnicodeStream(columnName);
		}
	}

	public boolean wasNull() throws SQLException {
		if (inMemory) {
			return wasNull;
		} else {
			return resultSets.get(resultSetIndex).wasNull();
		}
	}

	@Override
	public void close() throws SQLException {
	}

	@Override
	public int getRow() throws SQLException {
		return 0;
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return 0;
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
	}

	@Override
	public int getFetchSize() throws SQLException {
		return 0;
	}

	@Override
	public Statement getStatement() throws SQLException {
		return null;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}
}
