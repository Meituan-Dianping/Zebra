package com.dianping.zebra.group.config.datasource.transform;

import com.dianping.zebra.group.config.datasource.IVisitor;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

public abstract class BaseVisitor implements IVisitor {
   @Override
   public void visitAny(Any any) {
   }
   @Override
   public void visitDataSourceConfig(DataSourceConfig dataSourceConfig) {
   }

   @Override
   public void visitGroupDataSourceConfig(GroupDataSourceConfig groupDataSourceConfig) {
      for (DataSourceConfig dataSourceConfig : groupDataSourceConfig.getDataSourceConfigs().values()) {
         visitDataSourceConfig(dataSourceConfig);
      }
   }
}
