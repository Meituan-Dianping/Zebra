package com.dianping.zebra.group.config.datasource.transform;

import java.util.ArrayList;
import java.util.List;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

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
   public boolean onDataSourceConfig(final GroupDataSourceConfig parent, final DataSourceConfig dataSourceConfig) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addDataSourceConfig(dataSourceConfig);
            }
         });
      } else {
         parent.addDataSourceConfig(dataSourceConfig);
      }

      return true;
   }
}
