package com.dianping.zebra.group.datasources;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FailoverDataSourceTest {
	Map<String, DataSourceConfig> configs;

	Connection coon = mock(Connection.class);

	Connection readOnlyCoon = mock(Connection.class);

	ResultSet readOnlyResult = mock(ResultSet.class);

	Statement readonlyState = mock(Statement.class);

	ResultSet result = mock(ResultSet.class);

	Statement state = mock(Statement.class);

	@Test
	public void length() throws UnsupportedEncodingException {
		System.out.println("ab".getBytes("UTF-8").length);
		System.out.println("ab一".getBytes("UTF-8").length);
		System.out.println("一".getBytes("UTF-8").length);
	}

	@Before
	public void init() throws SQLException {
		configs = new HashMap<String, DataSourceConfig>();
		DataSourceConfig write1 = new DataSourceConfig();
		write1.setId("db1");
		write1.setDriverClass("com.mysql.jdbc.Driver");
		configs.put("db1", write1);
		DataSourceConfig write2 = new DataSourceConfig();
		write2.setId("db2");
		write2.setDriverClass("com.mysql.jdbc.Driver");
		configs.put("db2", write2);

		when(result.next()).thenReturn(true);
		when(result.getInt(1)).thenReturn(0);
		when(readOnlyResult.next()).thenReturn(true);
		when(readOnlyResult.getInt(1)).thenReturn(1);

		when(state.executeQuery(anyString())).thenReturn(result);
		when(readonlyState.executeQuery(anyString())).thenReturn(readOnlyResult);

		when(coon.createStatement()).thenReturn(state);
		when(readOnlyCoon.createStatement()).thenReturn(readonlyState);
	}
//
//	@Test
//	public void test_check_write_data_source_result_error() throws Exception {
//		FailOverDataSource ds = new FailOverDataSource(configs, null);
//		FailOverDataSource.MasterDataSourceMonitor monitor = spy(new FailOverDataSource.MasterDataSourceMonitor(ds));
//
//		Connection errorCoon = mock(Connection.class);
//		when(errorCoon.createStatement()).thenThrow(new SQLException());
//
//		doReturn(errorCoon).when(monitor).getConnection(any(DataSourceConfig.class));
////		Assert.assertEquals(monitor.isMasterDataSource(configs.get("db1")),
////				FailOverDataSource.CheckMasterDataSourceResult.ERROR);
//		verify(errorCoon, atLeastOnce()).createStatement();
//	}
//
//	@Test
//	public void test_check_write_data_source_result_ok() throws Exception {
//		FailOverDataSource ds = new FailOverDataSource(configs, null);
//		FailOverDataSource.MasterDataSourceMonitor monitor = spy(new FailOverDataSource.MasterDataSourceMonitor(ds));
//
//		doReturn(coon).when(monitor).getConnection(any(DataSourceConfig.class));
////		Assert.assertEquals(monitor.isMasterDataSource(configs.get("db1")),
////				FailOverDataSource.CheckMasterDataSourceResult.READ_WRITE);
//		verify(coon, atLeastOnce()).createStatement();
//	}
//
//	@Test
//	public void test_check_write_data_source_result_readonly() throws Exception {
//		FailOverDataSource ds = new FailOverDataSource(configs, null);
//		FailOverDataSource.MasterDataSourceMonitor monitor = spy(new FailOverDataSource.MasterDataSourceMonitor(ds));
//
//		doReturn(readOnlyCoon).when(monitor).getConnection(any(DataSourceConfig.class));
////		Assert.assertEquals(monitor.isMasterDataSource(configs.get("db1")),
////				FailOverDataSource.CheckMasterDataSourceResult.READ_ONLY);
//		verify(readOnlyCoon, atLeastOnce()).createStatement();
//	}

//	@Test
//	public void test_find_write_data_source1() throws Exception {
//		FailOverDataSource ds = new FailOverDataSource(configs, null);
//		FailOverDataSource.MasterDataSourceMonitor monitor = spy(new FailOverDataSource.MasterDataSourceMonitor(ds));
//
//		doReturn(coon).when(monitor).getConnection(any(DataSourceConfig.class));
//
//		monitor.findMasterDataSource();
//
//		Assert.assertEquals(ds.getCurrentDataSourceMBean().getId(), "db1");
//		verify(coon, atLeastOnce()).createStatement();
//	}
//
//	@Test
//	public void test_find_write_data_source2() throws Exception {
//		FailOverDataSource ds = new FailOverDataSource(configs, null);
//		FailOverDataSource.MasterDataSourceMonitor monitor = spy(new FailOverDataSource.MasterDataSourceMonitor(ds));
//
//		doReturn(readOnlyCoon).when(monitor).getConnection(configs.get("db1"));
//		doReturn(coon).when(monitor).getConnection(configs.get("db2"));
//
//		monitor.findMasterDataSource();
//
//		Assert.assertEquals(ds.getCurrentDataSourceMBean().getId(), "db2");
//
//		verify(coon, atLeastOnce()).createStatement();
//		verify(readOnlyCoon, atLeastOnce()).createStatement();
//	}

	// @Test(timeout = 30000)
	// public void test_hot_switch() throws SQLException, InterruptedException {
	// FailOverDataSource ds = new FailOverDataSource(configs, null);
	// FailOverDataSource.MasterDataSourceMonitor monitor = spy(new
	// FailOverDataSource.MasterDataSourceMonitor(ds));
	//
	// ConnectionAnswer connectionAnswer = new ConnectionAnswer();
	// connectionAnswer.setCoon(coon);
	//
	// // use the db1
	// doAnswer(connectionAnswer).when(monitor).getConnection(configs.get("db1"));
	// doReturn(coon).when(monitor).getConnection(configs.get("db2"));
	//
	// new Thread(monitor).start();
	//
	// while (monitor.getSleepTimes() < 2) {
	// Thread.sleep(10);
	// }
	// Assert.assertEquals("db1", ds.getCurrentDataSourceMBean().getId());
	//
	// verify(coon, atLeastOnce()).createStatement();
	// verify(readOnlyCoon, never()).createStatement();
	//
	// // fail over db1
	// connectionAnswer.setCoon(readOnlyCoon);
	//
	// while (monitor.getSleepTimes() < 4) {
	// Thread.sleep(10);
	// }
	// Assert.assertEquals("db2", ds.getCurrentDataSourceMBean().getId());
	// verify(coon, atLeast(2)).createStatement();
	// verify(readOnlyCoon, times(2)).createStatement();
	// }
	//
	// @Test(timeout = 5000)
	// public void test_thread_auto_close() throws Exception {
	// FailOverDataSource ds = new FailOverDataSource(configs, null);
	// FailOverDataSource.MasterDataSourceMonitor monitor = spy(new
	// FailOverDataSource.MasterDataSourceMonitor(ds));
	//
	// FailOverDataSource.FindMasterDataSourceResult result = new
	// FailOverDataSource.FindMasterDataSourceResult();
	// result.setMasterExist(true);
	//
	// doReturn(result).when(monitor).findMasterDataSource();
	// doReturn(FailOverDataSource.CheckMasterDataSourceResult.READ_WRITE).when(monitor)
	// .isMasterDataSource(any(DataSourceConfig.class));
	//
	// Thread t = new Thread(monitor);
	// t.start();
	//
	// //清空引用，强制GC
	// ds = null;
	// System.gc();
	//
	// while (true) {
	// Thread.sleep(500);
	// if (!t.isAlive()) {
	// break;
	// }
	// }
	// }

	class ConnectionAnswer implements Answer<Connection> {
		private Connection coon;

		@Override
		public Connection answer(InvocationOnMock invocation) throws Exception {
			return coon;
		}

		public void setCoon(Connection coon) {
			this.coon = coon;
		}
	}

}