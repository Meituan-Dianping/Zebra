package com.dianping.zebra.group.config.datasource.transform;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

public interface IParser<T> {
   public GroupDataSourceConfig parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForDataSourceConfig(IMaker<T> maker, ILinker linker, DataSourceConfig parent, T node);
}
