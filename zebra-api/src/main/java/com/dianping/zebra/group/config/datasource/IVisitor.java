package com.dianping.zebra.group.config.datasource;

import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

public interface IVisitor {

   public void visitAny(Any any);

   public void visitDataSourceConfig(DataSourceConfig dataSourceConfig);

   public void visitGroupDataSourceConfig(GroupDataSourceConfig groupDataSourceConfig);
}
