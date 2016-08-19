package com.dianping.zebra.monitor.monitor;

import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.group.monitor.GroupDataSourceMBean;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class GroupDataSourceMonitorTest {
	@Test
	public void test_get_properties() {
		GroupDataSourceMBean bean = Mockito.mock(GroupDataSourceMBean.class);
		Mockito.when(bean.getConfig()).thenReturn(new GroupDataSourceConfig());
		SingleDataSourceMBean single = Mockito.mock(SingleDataSourceMBean.class);

		Mockito.when(bean.getWriteSingleDataSourceMBean()).thenReturn(single);

		Map<String, SingleDataSourceMBean> readers = new HashMap<String, SingleDataSourceMBean>();

		readers.put("db1", single);

		Mockito.when(bean.getReaderSingleDataSourceMBean()).thenReturn(readers);

		GroupDataSourceMonitor monitor = new GroupDataSourceMonitor(bean);

		System.out.println(monitor.getDescription());
	}
}
