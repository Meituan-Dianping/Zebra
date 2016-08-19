package com.dianping.zebra.group.config.datasource.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.zebra.group.config.datasource.BaseEntity;
import com.dianping.zebra.group.config.datasource.IVisitor;

public class GroupDataSourceConfig extends BaseEntity<GroupDataSourceConfig> {
   private String m_routerStrategy = "roundrobin";

   private String m_filters = "";

   private boolean m_forceWriteOnLogin = true;

   private Map<String, DataSourceConfig> m_dataSourceConfigs = new LinkedHashMap<String, DataSourceConfig>();

   public GroupDataSourceConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitGroupDataSourceConfig(this);
   }

   public GroupDataSourceConfig addDataSourceConfig(DataSourceConfig dataSourceConfig) {
      m_dataSourceConfigs.put(dataSourceConfig.getId(), dataSourceConfig);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof GroupDataSourceConfig) {
         GroupDataSourceConfig _o = (GroupDataSourceConfig) obj;
         String routerStrategy = _o.getRouterStrategy();
         String filters = _o.getFilters();
         boolean forceWriteOnLogin = _o.getForceWriteOnLogin();
         Map<String, DataSourceConfig> dataSourceConfigs = _o.getDataSourceConfigs();
         boolean result = true;

         result &= (m_routerStrategy == routerStrategy || m_routerStrategy != null && m_routerStrategy.equals(routerStrategy));
         result &= (m_filters == filters || m_filters != null && m_filters.equals(filters));
         result &= (m_forceWriteOnLogin == forceWriteOnLogin);
         result &= (m_dataSourceConfigs == dataSourceConfigs || m_dataSourceConfigs != null && m_dataSourceConfigs.equals(dataSourceConfigs));

         return result;
      }

      return false;
   }

   public DataSourceConfig findDataSourceConfig(String id) {
      return m_dataSourceConfigs.get(id);
   }

   public DataSourceConfig findOrCreateDataSourceConfig(String id) {
      DataSourceConfig dataSourceConfig = m_dataSourceConfigs.get(id);

      if (dataSourceConfig == null) {
         synchronized (m_dataSourceConfigs) {
            dataSourceConfig = m_dataSourceConfigs.get(id);

            if (dataSourceConfig == null) {
               dataSourceConfig = new DataSourceConfig(id);
               m_dataSourceConfigs.put(id, dataSourceConfig);
            }
         }
      }

      return dataSourceConfig;
   }

   public Map<String, DataSourceConfig> getDataSourceConfigs() {
      return m_dataSourceConfigs;
   }

   public String getFilters() {
      return m_filters;
   }

   public boolean getForceWriteOnLogin() {
      return m_forceWriteOnLogin;
   }

   public String getRouterStrategy() {
      return m_routerStrategy;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_routerStrategy == null ? 0 : m_routerStrategy.hashCode());
      hash = hash * 31 + (m_filters == null ? 0 : m_filters.hashCode());
      hash = hash * 31 + (m_forceWriteOnLogin ? 1 : 0);
      hash = hash * 31 + (m_dataSourceConfigs == null ? 0 : m_dataSourceConfigs.hashCode());

      return hash;
   }

   public boolean isForceWriteOnLogin() {
      return m_forceWriteOnLogin;
   }

   @Override
   public void mergeAttributes(GroupDataSourceConfig other) {
      if (other.getRouterStrategy() != null) {
         m_routerStrategy = other.getRouterStrategy();
      }

      if (other.getFilters() != null) {
         m_filters = other.getFilters();
      }

      m_forceWriteOnLogin = other.getForceWriteOnLogin();
   }

   public boolean removeDataSourceConfig(String id) {
      if (m_dataSourceConfigs.containsKey(id)) {
         m_dataSourceConfigs.remove(id);
         return true;
      }

      return false;
   }

   public GroupDataSourceConfig setFilters(String filters) {
      m_filters = filters;
      return this;
   }

   public GroupDataSourceConfig setForceWriteOnLogin(boolean forceWriteOnLogin) {
      m_forceWriteOnLogin = forceWriteOnLogin;
      return this;
   }

   public GroupDataSourceConfig setRouterStrategy(String routerStrategy) {
      m_routerStrategy = routerStrategy;
      return this;
   }

}
