package com.dianping.zebra.single.manager;

import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.single.jdbc.SingleDataSource;

import java.util.List;

public interface SingleDataSourceManager {

	public SingleDataSource createDataSource(DataSourceConfig config, List<JdbcFilter> filter);

	public void destoryDataSource(SingleDataSource dataSource);

	public void init();
	
	public void stop();
}