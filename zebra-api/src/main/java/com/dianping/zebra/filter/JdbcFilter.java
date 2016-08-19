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
 * Created by Dozer on 9/2/14. <br>
 * Modified by hao.zhu on 05/10/16
 */
public interface JdbcFilter {
	int DEFAULT_ORDER = 0;

	int MAX_ORDER = Integer.MAX_VALUE;

	int MIN_ORDER = Integer.MIN_VALUE;

	/**
	 * filter ordering <br>
	 * filter_with_order_3_start filter_with_order_2_start
	 * filter_with_order_1_start targer_start filter_with_order_1_finish
	 * filter_with_order_2_finish filter_with_order_3_finish
	 *
	 * @return the order of execute
	 */
	int getOrder();

	/**
	 * init filter
	 */
	void init();

	/** GroupDataSource Filter **/
	void initGroupDataSource(GroupDataSource source, JdbcFilter chain);

	void refreshGroupDataSource(GroupDataSource source, String propertiesName, JdbcFilter chain);

	GroupConnection getGroupConnection(GroupDataSource source, JdbcFilter chain) throws SQLException;

	FailOverDataSource.FindMasterDataSourceResult findMasterFailOverDataSource(
			FailOverDataSource.MasterDataSourceMonitor source, JdbcFilter chain);

	void closeGroupConnection(GroupConnection source, JdbcFilter chain) throws SQLException;

	void closeGroupDataSource(GroupDataSource source, JdbcFilter chain) throws SQLException;

	void switchFailOverDataSource(FailOverDataSource source, JdbcFilter chain);

	/** SingleDataSource Filter **/
	DataSource initSingleDataSource(SingleDataSource source, JdbcFilter chain);

	SingleConnection getSingleConnection(SingleDataSource source, JdbcFilter chain) throws SQLException;

	String processSQL(String dsId, String sql, boolean isPreparedStmt, JdbcFilter chain) throws SQLException;

	<T> T executeSingleStatement(SingleStatement source, SingleConnection conn, String sql, List<String> batchedSql,
			boolean isBatched, boolean autoCommit, Object params, JdbcFilter chain) throws SQLException;

	void closeSingleConnection(SingleConnection source, JdbcFilter chain) throws SQLException;

	void closeSingleDataSource(SingleDataSource source, JdbcFilter chain) throws SQLException;

	void closeSingleResultSet(SingleResultSet source, JdbcFilter chain) throws SQLException;

}
