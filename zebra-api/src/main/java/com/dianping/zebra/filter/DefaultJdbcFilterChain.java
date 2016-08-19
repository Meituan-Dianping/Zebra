package com.dianping.zebra.filter;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.dianping.zebra.group.datasources.FailOverDataSource;
import com.dianping.zebra.group.jdbc.GroupConnection;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.single.jdbc.SingleConnection;
import com.dianping.zebra.single.jdbc.SingleDataSource;
import com.dianping.zebra.single.jdbc.SingleResultSet;
import com.dianping.zebra.single.jdbc.SingleStatement;

/**
 * Created by Dozer on 11/11/14.
 */
public class DefaultJdbcFilterChain implements JdbcFilter {
	protected final List<JdbcFilter> filters;

	protected int index = 0;

	public DefaultJdbcFilterChain(List<JdbcFilter> filters) {
		this.filters = filters;
	}

	@Override
	public void closeGroupConnection(GroupConnection source, JdbcFilter chain) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closeGroupDataSource(GroupDataSource source, JdbcFilter chain) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closeSingleConnection(SingleConnection source, JdbcFilter chain) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closeSingleDataSource(SingleDataSource source, JdbcFilter chain) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T executeSingleStatement(SingleStatement source, SingleConnection conn, String sql, List<String> batchedSql,
			boolean isBatched, boolean autoCommit, Object params, JdbcFilter chain) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FailOverDataSource.FindMasterDataSourceResult findMasterFailOverDataSource(
			FailOverDataSource.MasterDataSourceMonitor source, JdbcFilter chain) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GroupConnection getGroupConnection(GroupDataSource source, JdbcFilter chain) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getOrder() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SingleConnection getSingleConnection(SingleDataSource source, JdbcFilter chain) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void init() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initGroupDataSource(GroupDataSource source, JdbcFilter chain) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataSource initSingleDataSource(SingleDataSource source, JdbcFilter chain) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void refreshGroupDataSource(GroupDataSource source, String propertiesName, JdbcFilter chain) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void switchFailOverDataSource(FailOverDataSource source, JdbcFilter chain) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String processSQL(String dsId, String sql, boolean isPreparedStmt, JdbcFilter chain) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closeSingleResultSet(SingleResultSet source, JdbcFilter chain) throws SQLException {
		throw new UnsupportedOperationException();
	}
}