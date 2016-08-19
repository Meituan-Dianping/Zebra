package com.dianping.zebra.filter;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.dianping.zebra.Constants;
import com.dianping.zebra.group.datasources.FailOverDataSource;
import com.dianping.zebra.group.jdbc.GroupConnection;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.single.jdbc.SingleConnection;
import com.dianping.zebra.single.jdbc.SingleDataSource;
import com.dianping.zebra.single.jdbc.SingleResultSet;
import com.dianping.zebra.single.jdbc.SingleStatement;

/**
 * Created by Dozer on 9/2/14.
 */
public class DefaultJdbcFilter implements JdbcFilter {

	protected String configManagerType = Constants.CONFIG_MANAGER_TYPE_REMOTE;

	@Override
	public void closeGroupConnection(GroupConnection source, JdbcFilter chain) throws SQLException {
		chain.closeGroupConnection(source, chain);
	}

	@Override
	public void closeGroupDataSource(GroupDataSource source, JdbcFilter chain) throws SQLException {
		chain.closeGroupDataSource(source, chain);
	}

	@Override
	public void closeSingleConnection(SingleConnection source, JdbcFilter chain) throws SQLException {
		chain.closeSingleConnection(source, chain);
	}

	@Override
	public void closeSingleDataSource(SingleDataSource source, JdbcFilter chain) throws SQLException {
		chain.closeSingleDataSource(source, chain);
	}

	@Override
	public <T> T executeSingleStatement(SingleStatement source, SingleConnection conn, String sql, List<String> batchedSql,
			boolean isBatched, boolean autoCommit, Object params, JdbcFilter chain) throws SQLException {
		return chain.executeSingleStatement(source, conn, sql, batchedSql, isBatched, autoCommit, params, chain);
	}

	@Override
	public FailOverDataSource.FindMasterDataSourceResult findMasterFailOverDataSource(
			FailOverDataSource.MasterDataSourceMonitor source, JdbcFilter chain) {
		return chain.findMasterFailOverDataSource(source, chain);
	}

	@Override
	public GroupConnection getGroupConnection(GroupDataSource source, JdbcFilter chain) throws SQLException {
		return chain.getGroupConnection(source, chain);
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public SingleConnection getSingleConnection(SingleDataSource source, JdbcFilter chain) throws SQLException {
		return chain.getSingleConnection(source, chain);
	}

	@Override
	public void init() {
	}

	@Override
	public void initGroupDataSource(GroupDataSource source, JdbcFilter chain) {
		chain.initGroupDataSource(source, chain);
	}

	@Override
	public DataSource initSingleDataSource(SingleDataSource source, JdbcFilter chain) {
		return chain.initSingleDataSource(source, chain);
	}

	@Override
	public void refreshGroupDataSource(GroupDataSource source, String propertiesName, JdbcFilter chain) {
		chain.refreshGroupDataSource(source, propertiesName, chain);
	}

	public void setConfigManagerType(String configManagerType) {
		this.configManagerType = configManagerType;
	}

	@Override
	public void switchFailOverDataSource(FailOverDataSource source, JdbcFilter chain) {
		chain.switchFailOverDataSource(source, chain);
	}

	@Override
	public String processSQL(String dsId, String sql, boolean isPreparedStmt, JdbcFilter chain) throws SQLException {
		return chain.processSQL(dsId, sql, isPreparedStmt, chain);
	}

	@Override
	public void closeSingleResultSet(SingleResultSet source, JdbcFilter chain) throws SQLException {
		chain.closeSingleResultSet(source, chain);
	}
}
