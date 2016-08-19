package com.dianping.zebra.group.config.system.transform;

import java.util.ArrayList;
import java.util.List;
import com.dianping.zebra.group.config.system.entity.DataCenter;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

public class DefaultLinker implements ILinker {
   private boolean m_deferrable;

   private List<Runnable> m_deferedJobs = new ArrayList<Runnable>();

   public DefaultLinker(boolean deferrable) {
      m_deferrable = deferrable;
   }

   public void finish() {
      for (Runnable job : m_deferedJobs) {
         job.run();
      }
   }

   @Override
   public boolean onDataCenter(final SystemConfig parent, final DataCenter dataCenter) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addDataCenter(dataCenter);
            }
         });
      } else {
         parent.addDataCenter(dataCenter);
      }

      return true;
   }

   @Override
   public boolean onSqlFlowControl(final SystemConfig parent, final SqlFlowControl sqlFlowControl) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addSqlFlowControl(sqlFlowControl);
            }
         });
      } else {
         parent.addSqlFlowControl(sqlFlowControl);
      }

      return true;
   }
}
