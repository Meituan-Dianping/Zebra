package com.dianping.zebra.group.config.system.transform;

import com.dianping.zebra.group.config.system.entity.DataCenter;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

public interface IParser<T> {
   public SystemConfig parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForDataCenter(IMaker<T> maker, ILinker linker, DataCenter parent, T node);

   public void parseForSqlFlowControl(IMaker<T> maker, ILinker linker, SqlFlowControl parent, T node);
}
