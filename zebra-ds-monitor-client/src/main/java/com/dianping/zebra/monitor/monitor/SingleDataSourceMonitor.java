package com.dianping.zebra.monitor.monitor;

import com.dianping.cat.status.StatusExtension;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.group.util.DataSourceState;

import java.util.LinkedHashMap;
import java.util.Map;

public class SingleDataSourceMonitor implements StatusExtension {

	private SingleDataSourceMBean bean;

	public SingleDataSourceMonitor(SingleDataSourceMBean bean) {
		this.bean = bean;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getId() {
		return "dal";
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> status = new LinkedHashMap<String, String>();

		if (bean.getState() != DataSourceState.INITIAL) {
			String id = bean.getId();
			status.put(id + "-TotalConnection", Integer.toString(bean.getNumConnections()));
			status.put(id + "-BusyConnection", Integer.toString(bean.getNumBusyConnection()));
			status.put(id + "-IdleConnection", Integer.toString(bean.getNumIdleConnection()));
		}

		return status;
	}
}
