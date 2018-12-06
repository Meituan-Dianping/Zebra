package com.dianping.zebra.group.exception;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.Ignore;
import org.junit.Test;

import com.dianping.zebra.Constants;
import com.dianping.zebra.group.jdbc.DataSourceEntry;
import com.dianping.zebra.group.jdbc.MultiDatabaseTestCase;
import com.dianping.zebra.group.util.DaoContextHolder;

@Ignore
public class DalStatusExtensionTest extends MultiDatabaseTestCase {

	@Test
	public void testPreparedStatementQuery() throws Exception {
		DaoContextHolder.setSqlName("testPreparedStatementQuery");
		while (true) {
			execute(new ConnectionCallback() {

				@Override
				public Object doInConnection(Connection conn) throws Exception {
					PreparedStatement stmt = conn.prepareStatement("SELECT ID,NAME,AGE from PERSON where AGE = ?");
					stmt.setInt(1, 18);
					stmt.execute();
					return null;
				}
			});

			Thread.sleep(100);
		}
	}

	@Override
	protected String getConfigManagerType() {
		return Constants.CONFIG_MANAGER_TYPE_LOCAL;
	}

	@Override
	protected String getResourceId() {
		return "sample.ds.v2";
	}

	@Override
	protected String getSchema() {
		return "src/test/resources/schema.sql";
	}

	@Override
	protected DataSourceEntry[] getDataSourceEntryArray() {
		DataSourceEntry[] entries = new DataSourceEntry[3];

		DataSourceEntry entry1 = new DataSourceEntry("jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets.xml",
				true);
		DataSourceEntry entry2 = new DataSourceEntry("jdbc:h2:mem:test1;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets1.xml",
				false);
		DataSourceEntry entry3 = new DataSourceEntry("jdbc:h2:mem:test2;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets2.xml",
				false);

		entries[0] = entry1;
		entries[1] = entry2;
		entries[2] = entry3;

		return entries;
	}
}
