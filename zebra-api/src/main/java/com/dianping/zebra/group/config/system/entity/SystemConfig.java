package com.dianping.zebra.group.config.system.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.zebra.group.config.system.BaseEntity;
import com.dianping.zebra.group.config.system.IVisitor;

public class SystemConfig extends BaseEntity<SystemConfig> {
   private int m_retryTimes = 0;

   private Map<String, SqlFlowControl> m_sqlFlowControls = new LinkedHashMap<String, SqlFlowControl>();

   private Map<String, DataCenter> m_dataCenters = new LinkedHashMap<String, DataCenter>();

   public SystemConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitSystemConfig(this);
   }

   public SystemConfig addDataCenter(DataCenter dataCenter) {
      m_dataCenters.put(dataCenter.getName(), dataCenter);
      return this;
   }

   public SystemConfig addSqlFlowControl(SqlFlowControl sqlFlowControl) {
      m_sqlFlowControls.put(sqlFlowControl.getSqlId(), sqlFlowControl);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof SystemConfig) {
         SystemConfig _o = (SystemConfig) obj;
         int retryTimes = _o.getRetryTimes();
         Map<String, SqlFlowControl> sqlFlowControls = _o.getSqlFlowControls();
         Map<String, DataCenter> dataCenters = _o.getDataCenters();
         boolean result = true;

         result &= (m_retryTimes == retryTimes);
         result &= (m_sqlFlowControls == sqlFlowControls || m_sqlFlowControls != null && m_sqlFlowControls.equals(sqlFlowControls));
         result &= (m_dataCenters == dataCenters || m_dataCenters != null && m_dataCenters.equals(dataCenters));

         return result;
      }

      return false;
   }

   public DataCenter findDataCenter(String name) {
      return m_dataCenters.get(name);
   }

   public SqlFlowControl findSqlFlowControl(String sqlId) {
      return m_sqlFlowControls.get(sqlId);
   }

   public SqlFlowControl findOrCreateSqlFlowControl(String sqlId) {
      SqlFlowControl sqlFlowControl = m_sqlFlowControls.get(sqlId);

      if (sqlFlowControl == null) {
         synchronized (m_sqlFlowControls) {
            sqlFlowControl = m_sqlFlowControls.get(sqlId);

            if (sqlFlowControl == null) {
               sqlFlowControl = new SqlFlowControl(sqlId);
               m_sqlFlowControls.put(sqlId, sqlFlowControl);
            }
         }
      }

      return sqlFlowControl;
   }

   public Map<String, DataCenter> getDataCenters() {
      return m_dataCenters;
   }

   public int getRetryTimes() {
      return m_retryTimes;
   }

   public Map<String, SqlFlowControl> getSqlFlowControls() {
      return m_sqlFlowControls;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + m_retryTimes;
      hash = hash * 31 + (m_sqlFlowControls == null ? 0 : m_sqlFlowControls.hashCode());
      hash = hash * 31 + (m_dataCenters == null ? 0 : m_dataCenters.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(SystemConfig other) {
   }

   public boolean removeDataCenter(String name) {
      if (m_dataCenters.containsKey(name)) {
         m_dataCenters.remove(name);
         return true;
      }

      return false;
   }

   public boolean removeSqlFlowControl(String sqlId) {
      if (m_sqlFlowControls.containsKey(sqlId)) {
         m_sqlFlowControls.remove(sqlId);
         return true;
      }

      return false;
   }

   public SystemConfig setRetryTimes(int retryTimes) {
      m_retryTimes = retryTimes;
      return this;
   }

}
