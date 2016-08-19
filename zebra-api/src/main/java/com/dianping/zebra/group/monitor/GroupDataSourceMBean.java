package com.dianping.zebra.group.monitor;

import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

import java.util.Map;

public interface GroupDataSourceMBean {

	public Map<String, SingleDataSourceMBean> getReaderSingleDataSourceMBean();

	public SingleDataSourceMBean getWriteSingleDataSourceMBean();

	public GroupDataSourceConfig getConfig();
}
