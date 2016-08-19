package com.dianping.zebra.group.config.system;

import com.dianping.zebra.group.config.system.entity.DataCenter;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

public interface IVisitor {

   public void visitDataCenter(DataCenter dataCenter);

   public void visitSqlFlowControl(SqlFlowControl sqlFlowControl);

   public void visitSystemConfig(SystemConfig systemConfig);
}
