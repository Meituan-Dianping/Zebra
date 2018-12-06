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
package com.dianping.zebra.shard.idgen.impl;

import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.shard.exception.ShardIdGenException;
import com.dianping.zebra.shard.idgen.IdGenerator;
import com.dianping.zebra.shard.idgen.IdRange;
import com.dianping.zebra.shard.jdbc.DataSourceRepository;

import javax.sql.DataSource;
import java.sql.*;

public class MySqlIdGenerator implements IdGenerator {

	protected static final Logger LOGGER = LoggerFactory.getLogger(MySqlIdGenerator.class);

	private static final int MIN_STEP = 1;

	private static final int MAX_STEP = 100000;

	private int maxRetryTimes = 5;

	private int defaultIncreaseStep = 1000;

	private String jdbcRef;

	private DataSource dataSource;

	private long warningThreshold = Long.MAX_VALUE - 1000000000L;

	private long errorThreshold = Long.MAX_VALUE - 100000000L;

	private volatile String tableName = "ShardId";

	private volatile String nameColumn = "Name";

	private volatile String valueColumn = "Value";

	private volatile String selectSql;

	private volatile String updateSql;

	public MySqlIdGenerator() {
	}

	public MySqlIdGenerator(String jdbcRef) {
		this.jdbcRef = jdbcRef;
	}

	public MySqlIdGenerator(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void init() {
		initDatasource();
	}

	@Override
	public long nextId(String nameColumn) {
		return nextBatch(nameColumn, 1).next();
	}

	@Override
	public IdRange nextBatch(String nameColumn) {
		return nextBatch(nameColumn, this.defaultIncreaseStep);
	}

	@Override
	public IdRange nextBatch(String nameColumn, int batchSize) {
		if (nameColumn == null) {
			throw new ShardIdGenException("The value of name column cannot be null in shard id generator");
		}

		if (batchSize < MIN_STEP) {
			batchSize = MIN_STEP;
		} else if (batchSize > MAX_STEP) {
			batchSize = MAX_STEP;
		}

		Connection connection = null;
		PreparedStatement updateStatement = null;
		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;

		SQLException innerException = null;
		for (int i = 0; i < this.maxRetryTimes + 1; ++i) {
			Boolean autoCommit = null;
			try {
				connection = this.dataSource.getConnection();
				autoCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
				updateStatement = connection.prepareStatement(generateUpdateSql());
				updateStatement.setInt(1, batchSize);
				updateStatement.setString(2, nameColumn);
				updateStatement.executeUpdate();
				selectStatement = connection.prepareStatement(generateSelectSql());
				selectStatement.setString(1, nameColumn);
				resultSet = selectStatement.executeQuery();

				long current = -1;
				boolean noResult = false;
				if (resultSet.next()) {
					current = resultSet.getLong(1);
					if (current > warningThreshold) {
						LOGGER.warn("Generate id value is greater than the warning threshold, value = " + current
						      + ", threshold = " + this.warningThreshold + ", table = " + this.tableName + ", column = "
						      + nameColumn);
					}
				} else {
					noResult = true;
				}
				connection.commit();

				if (noResult) {
					throw new ShardIdGenException("Generate id result is null, please check table " + this.tableName
					      + " and column " + nameColumn);
				}
				if (current < 0) {
					throw new ShardIdGenException("Generate id cannot be less than zero, value = " + current
					      + ", please check table " + this.tableName + " and column " + nameColumn);
				} else if (current > errorThreshold) {
					throw new ShardIdGenException("Generate id overflow, value = " + current + ", please check table "
					      + this.tableName + " and column " + nameColumn);
				}

				return new IdRange(current - batchSize, current);
			} catch (SQLException e) {
				try {
					if (connection != null) {
						connection.rollback();
					}
				} catch (SQLException e1) {
					LOGGER.debug("Unexpected exception on shard id generator rollback!");
				}
				innerException = e;
			} finally {
				closeResultSet(resultSet);
				resultSet = null;
				closeStatement(updateStatement, selectStatement);
				updateStatement = null;
				selectStatement = null;
				closeConnection(connection, autoCommit);
				connection = null;
			}
		}
		if (innerException != null) {
			throw new ShardIdGenException("Generate id failed, retry too many times, maxRetryTimes = "
			      + this.maxRetryTimes, innerException);
		} else {
			throw new ShardIdGenException("Generate id failed, retry too many times, maxRetryTimes = "
			      + this.maxRetryTimes);
		}
	}

	@Override
	public long nextId() {
		throw new ShardIdGenException("MySqlIdGenerator not support nextId without specified column name!");
	}

	private void initDatasource() {
		if (this.dataSource == null) {
			if (this.jdbcRef != null) {
				this.dataSource = DataSourceRepository.getInstance().getDataSource(this.jdbcRef);
			} else {
				throw new ShardIdGenException("The datasource of shard id generator cannot be null!");
			}
		}
	}

	private String generateSelectSql() {
		if (this.selectSql == null) {
			synchronized (this) {
				if (this.selectSql == null) {
					StringBuilder builder = new StringBuilder();
					builder.append("SELECT ").append(this.valueColumn);
					builder.append(" FROM ").append(this.tableName);
					builder.append(" WHERE ").append(this.nameColumn).append(" = ?");
					this.selectSql = builder.toString();
				}
			}
		}
		return this.selectSql;
	}

	private String generateUpdateSql() {
		if (this.updateSql == null) {
			synchronized (this) {
				if (this.updateSql == null) {
					StringBuilder builder = new StringBuilder();
					builder.append("UPDATE ").append(this.tableName);
					builder.append(" SET ").append(this.valueColumn).append(" = ").append(this.valueColumn)
					      .append(" + ? WHERE ");
					builder.append(this.nameColumn).append(" = ?");
					this.updateSql = builder.toString();
				}
			}
		}

		return this.updateSql;
	}

	private void closeResultSet(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (Throwable t) {
				LOGGER.debug("Unexpected exception on closing shard id generator JDBC ResultSet", t);
			}
		}
	}

	private void closeStatement(Statement updateStatement, Statement selectStatement) {
		if (updateStatement != null) {
			try {
				updateStatement.close();
			} catch (Throwable t) {
				LOGGER.debug("Unexpected exception on closing shard id generator JDBC update statement", t);
			}
		}
		if (selectStatement != null) {
			try {
				selectStatement.close();
			} catch (Throwable t) {
				LOGGER.debug("Unexpected exception on closing shard id generator JDBC select statement", t);
			}
		}
	}

	private void closeConnection(Connection connection, Boolean autoCommit) {
		if (connection != null) {
			if (autoCommit != null) {
				try {
					connection.setAutoCommit(autoCommit);
				} catch (SQLException e) {
				}
			}
			try {
				connection.close();
			} catch (Throwable t) {
				LOGGER.debug("Unexpected exception on closing shard id generator JDBC Connection", t);
			}
		}
	}

	public int getMaxRetryTimes() {
		return maxRetryTimes;
	}

	public void setMaxRetryTimes(int maxRetryTimes) {
		if (maxRetryTimes < 0) {
			this.maxRetryTimes = 0;
		}
		this.maxRetryTimes = maxRetryTimes;
	}

	public int getDefaultIncreaseStep() {
		return defaultIncreaseStep;
	}

	public void setDefaultIncreaseStep(int defaultIncreaseStep) {
		if (defaultIncreaseStep < MIN_STEP) {
			this.defaultIncreaseStep = MIN_STEP;
		} else if (defaultIncreaseStep > MAX_STEP) {
			this.defaultIncreaseStep = MAX_STEP;
		} else {
			this.defaultIncreaseStep = defaultIncreaseStep;
		}
	}

	public String getJdbcRef() {
		return jdbcRef;
	}

	public void setJdbcRef(String jdbcRef) {
		this.jdbcRef = jdbcRef;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getNameColumn() {
		return nameColumn;
	}

	public void setNameColumn(String nameColumn) {
		this.nameColumn = nameColumn;
	}

	public String getValueColumn() {
		return valueColumn;
	}

	public void setValueColumn(String valueColumn) {
		this.valueColumn = valueColumn;
	}

	public String getSelectSql() {
		return selectSql;
	}

	public String getUpdateSql() {
		return updateSql;
	}

}
