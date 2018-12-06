package com.dianping.zebra.filter;

import com.dianping.zebra.Constants;
import com.dianping.zebra.filter.DefaultJdbcFilter;
import com.dianping.zebra.filter.DefaultJdbcFilterChain;
import com.dianping.zebra.filter.FilterManagerFactory;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.single.jdbc.SingleConnection;
import com.dianping.zebra.single.jdbc.SingleStatement;
import junit.framework.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FilterChainTest {
	@Test
	public void test_execute_with_order() throws SQLException {
		final AtomicInteger counter = new AtomicInteger();

		JdbcFilter filter1 = new DefaultJdbcFilter() {
			@Override
			public <T> T executeSingleStatement(SingleStatement source, SingleConnection conn, String sql,
												List<String> batchedSql, boolean isBatched, boolean autoCommit, Object params, JdbcFilter chain)
					throws SQLException {
				Assert.assertEquals(3, counter.incrementAndGet());
				T executeResult = chain.executeSingleStatement(source, null, null, null, false, false, null, chain);
				Assert.assertEquals(5, counter.incrementAndGet());
				return executeResult;
			}

			@Override
			public int getOrder() {
				return 1;
			}
		};

		JdbcFilter filter2 = new DefaultJdbcFilter() {
			@Override
			public <T> T executeSingleStatement(SingleStatement source, SingleConnection conn, String sql,
												List<String> batchedSql, boolean isBatched, boolean autoCommit, Object params, JdbcFilter chain)
					throws SQLException {

				Assert.assertEquals(2, counter.incrementAndGet());
				T executeResult = chain.executeSingleStatement(source, null, null, null, false, false, null, chain);
				Assert.assertEquals(6, counter.incrementAndGet());
				return executeResult;
			}

			@Override
			public int getOrder() {
				return 2;
			}
		};

		JdbcFilter filter3 = new DefaultJdbcFilter() {
			@Override
			public <T> T executeSingleStatement(SingleStatement source, SingleConnection conn, String sql,
												List<String> batchedSql, boolean isBatched, boolean autoCommit, Object params, JdbcFilter chain)
					throws SQLException {

				Assert.assertEquals(1, counter.incrementAndGet());
				T executeResult = chain.executeSingleStatement(source, null, null, null, false, false, null, chain);
				Assert.assertEquals(7, counter.incrementAndGet());
				return executeResult;
			}

			@Override
			public int getOrder() {
				return 3;
			}
		};

		FilterManagerFactory.getFilterManager().addFilter("1", filter1);
		FilterManagerFactory.getFilterManager().addFilter("2", filter2);
		FilterManagerFactory.getFilterManager().addFilter("3", filter3);

		List<JdbcFilter> filters = FilterManagerFactory.getFilterManager().loadFilters("1,2,3", Constants.CONFIG_MANAGER_TYPE_REMOTE, null);
		JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
			@Override
			public <T> T executeSingleStatement(SingleStatement source, SingleConnection conn, String sql,
												List<String> batchedSql, boolean isBatched, boolean autoCommit, Object params, JdbcFilter chain)
					throws SQLException {
				if (index < filters.size()) {
					return filters.get(index++).executeSingleStatement(source, conn, sql, batchedSql, isBatched,
							autoCommit, params, chain);
				} else {
					Assert.assertEquals(4, counter.incrementAndGet());
					return null;
				}
			}
		};
		chain.executeSingleStatement(null, null, null, null, false, false, null, chain);
	}
}
