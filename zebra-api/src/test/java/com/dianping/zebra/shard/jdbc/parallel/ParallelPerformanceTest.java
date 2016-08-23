package com.dianping.zebra.shard.jdbc.parallel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.Test;

import com.dianping.zebra.group.jdbc.DataSourceEntry;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.group.jdbc.MultiDatabaseTestCase;
import com.dianping.zebra.group.util.SqlAliasManager;

import junit.framework.Assert;

public class ParallelPerformanceTest extends MultiDatabaseTestCase {

	@Override
	protected String getConfigManagerType() {
		return "local";
	}

	@Override
	protected DataSourceEntry[] getDataSourceEntryArray() {
		DataSourceEntry[] entries = new DataSourceEntry[3];

		entries[0] = new DataSourceEntry("jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets.xml", true);
		entries[1] = new DataSourceEntry("jdbc:h2:mem:test1;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets1.xml", false);
		entries[2] = new DataSourceEntry("jdbc:h2:mem:test2;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets2.xml", false);

		return entries;
	}

	@Override
	protected String getResourceId() {
		return "sample.ds.v2";
	}

	@Override
	protected String getSchema() {
		return getClass().getResource("/schema.sql").getPath();
	}

	@Test
	public void test() {
		GroupDataSource ds = new GroupDataSource(getResourceId());
		ds.setConfigManagerType(getConfigManagerType());
		ds.init();

		List<Callable<UpdateResult>> tasks = new ArrayList<Callable<UpdateResult>>();
		try {
			Connection conn = ds.getConnection();

			long now = System.currentTimeMillis();
			for (int i = 0; i < 5; i++) {

				PreparedStatement stmt = conn.prepareStatement("update PERSON p set p.Name = ?");
				stmt.setString(1, "leo6");

				tasks.add(new PreparedStatementExecuteUpdateCallable(stmt, SqlAliasManager.getSqlAlias(),
						String.valueOf(i), -1));
			}

			List<Future<UpdateResult>> futures = SQLThreadPoolExecutor.getInstance().invokeSQLs(tasks);

			int affectedRows = 0;
			for (Future<UpdateResult> f : futures) {
				try {
					UpdateResult updateResult = f.get();

					affectedRows += updateResult.getAffectedRows();
				} catch (Exception e) {
					// normally can't be here
					throw new SQLException(e);
				}
			}

			Assert.assertEquals(5, affectedRows);

			System.out.println("Time = " + (System.currentTimeMillis() - now));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
