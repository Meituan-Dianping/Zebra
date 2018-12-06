package com.dianping.zebra.shard.jdbc.parallel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;

public class ParallelPerformanceTest2 extends MultiDBBaseTestCase {

	@Override
	protected String getDBBaseUrl() {
		return "jdbc:h2:mem:";
	}

	@Override
	protected String getCreateScriptConfigFile() {
		return "db-datafiles/createtable-multidb-lifecycle.xml";
	}

	@Override
	protected String getDataFile() {
		return "db-datafiles/data-multidb-lifecycle.xml";
	}

	@Override
	protected String[] getSpringConfigLocations() {
		return new String[] { "ctx-multidb-lifecycle.xml" };
	}

	@Test
	public void test() {
		DataSource ds = (DataSource) context.getBean("zebraDS");

		try {
			Connection conn = ds.getConnection();

			long now = System.currentTimeMillis();
			for(int i = 0 ; i < 10; i++){
				PreparedStatement stmt = conn.prepareStatement("update test set name='testupdate'");
//				stmt.setString(1, "leo3");
				
				stmt.executeUpdate();
			}

			System.out.println("Time = " + (System.currentTimeMillis() - now));
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
