package com.dianping.zebra.group.config.system.transform;

import com.dianping.zebra.group.config.system.entity.DataCenter;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

public interface ILinker {

   public boolean onDataCenter(SystemConfig parent, DataCenter dataCenter);

   public boolean onSqlFlowControl(SystemConfig parent, SqlFlowControl sqlFlowControl);
}
