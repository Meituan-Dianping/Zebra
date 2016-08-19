package com.dianping.zebra.monitor.monitor;

import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.group.util.DataSourceState;
import org.junit.Test;
import org.mockito.Mockito;

public class SingleDataSourceMonitorTest {
	@Test
	public void test_get_properties() {
		SingleDataSourceMBean single = Mockito.mock(SingleDataSourceMBean.class);

		Mockito.when(single.getState()).thenReturn(DataSourceState.UP);

		SingleDataSourceMonitor monitor = new SingleDataSourceMonitor(single);

		System.out.println(monitor.getDescription());
	}
}
