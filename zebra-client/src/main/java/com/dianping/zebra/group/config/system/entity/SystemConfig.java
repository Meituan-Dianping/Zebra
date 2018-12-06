/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra.group.config.system.entity;

import java.util.ArrayList;
import java.util.List;

import com.dianping.zebra.group.config.system.BaseEntity;
import com.dianping.zebra.group.config.system.IVisitor;

public class SystemConfig extends BaseEntity<SystemConfig> {
   private int m_retryTimes = 0;

   private List<SqlFlowControl> m_sqlFlowControls = new ArrayList<SqlFlowControl>();

   public SystemConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitSystemConfig(this);
   }

   public SystemConfig addSqlFlowControl(SqlFlowControl sqlFlowControl) {
      m_sqlFlowControls.add(sqlFlowControl);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof SystemConfig) {
         SystemConfig _o = (SystemConfig) obj;
         int retryTimes = _o.getRetryTimes();
         List<SqlFlowControl> sqlFlowControls = _o.getSqlFlowControls();
         boolean result = true;

         result &= (m_retryTimes == retryTimes);
         result &= (m_sqlFlowControls == sqlFlowControls || m_sqlFlowControls != null && m_sqlFlowControls.equals(sqlFlowControls));

         return result;
      }

      return false;
   }

   public SqlFlowControl findSqlFlowControl(String sqlId) {
      for (SqlFlowControl sqlFlowControl : m_sqlFlowControls) {
         if (!sqlFlowControl.getSqlId().equals(sqlId)) {
            continue;
         }

         return sqlFlowControl;
      }

      return null;
   }

   public SqlFlowControl findOrCreateSqlFlowControl(String sqlId) {
      synchronized (m_sqlFlowControls) {
         for (SqlFlowControl sqlFlowControl : m_sqlFlowControls) {
            if (!sqlFlowControl.getSqlId().equals(sqlId)) {
               continue;
            }

            return sqlFlowControl;
         }

         SqlFlowControl sqlFlowControl = new SqlFlowControl(sqlId);

         m_sqlFlowControls.add(sqlFlowControl);
         return sqlFlowControl;
      }
   }

   public int getRetryTimes() {
      return m_retryTimes;
   }

   public List<SqlFlowControl> getSqlFlowControls() {
      return m_sqlFlowControls;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + m_retryTimes;
      hash = hash * 31 + (m_sqlFlowControls == null ? 0 : m_sqlFlowControls.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(SystemConfig other) {
   }

   public boolean removeSqlFlowControl(String sqlId) {
      int len = m_sqlFlowControls.size();

      for (int i = 0; i < len; i++) {
         SqlFlowControl sqlFlowControl = m_sqlFlowControls.get(i);

         if (!sqlFlowControl.getSqlId().equals(sqlId)) {
            continue;
         }

         m_sqlFlowControls.remove(i);
         return true;
      }

      return false;
   }

   public SystemConfig setRetryTimes(int retryTimes) {
      m_retryTimes = retryTimes;
      return this;
   }
}
