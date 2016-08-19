package com.dianping.zebra.group.config;

import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

import java.beans.PropertyChangeListener;

public interface DataSourceConfigManager {

	public void addListerner(PropertyChangeListener listener);

	public GroupDataSourceConfig getGroupDataSourceConfig();

	public void init();
	
	public void close();

}
