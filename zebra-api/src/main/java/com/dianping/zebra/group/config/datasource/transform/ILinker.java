package com.dianping.zebra.group.config.datasource.transform;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

public interface ILinker {

   public boolean onDataSourceConfig(GroupDataSourceConfig parent, DataSourceConfig dataSourceConfig);
}
