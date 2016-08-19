package com.dianping.zebra.group.config.system.transform;

import com.dianping.zebra.group.config.system.entity.DataCenter;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

public interface IMaker<T> {

   public DataCenter buildDataCenter(T node);

   public SqlFlowControl buildSqlFlowControl(T node);

   public SystemConfig buildSystemConfig(T node);
}
