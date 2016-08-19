package com.dianping.zebra.group.config.datasource.transform;

import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

public interface IMaker<T> {

   public Any buildAny(T node);

   public DataSourceConfig buildDataSourceConfig(T node);

   public GroupDataSourceConfig buildGroupDataSourceConfig(T node);
}
