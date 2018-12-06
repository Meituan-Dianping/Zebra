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

import static com.dianping.zebra.group.config.system.Constants.ATTR_SQL_ID;
import static com.dianping.zebra.group.config.system.Constants.ENTITY_SQL_FLOW_CONTROL;

import com.dianping.zebra.group.config.system.BaseEntity;
import com.dianping.zebra.group.config.system.IVisitor;

public class SqlFlowControl extends BaseEntity<SqlFlowControl> {
   private String m_sqlId;

   private int m_allowPercent = 100;

   private String m_app;

   public SqlFlowControl() {
   }

   public SqlFlowControl(String sqlId) {
      m_sqlId = sqlId;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitSqlFlowControl(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof SqlFlowControl) {
         SqlFlowControl _o = (SqlFlowControl) obj;
         String sqlId = _o.getSqlId();

         return m_sqlId == sqlId || m_sqlId != null && m_sqlId.equals(sqlId);
      }

      return false;
   }

   public int getAllowPercent() {
      return m_allowPercent;
   }

   public String getApp() {
      return m_app;
   }

   public String getSqlId() {
      return m_sqlId;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_sqlId == null ? 0 : m_sqlId.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(SqlFlowControl other) {
      assertAttributeEquals(other, ENTITY_SQL_FLOW_CONTROL, ATTR_SQL_ID, m_sqlId, other.getSqlId());

      m_allowPercent = other.getAllowPercent();

      if (other.getApp() != null) {
         m_app = other.getApp();
      }
   }

   public SqlFlowControl setAllowPercent(int allowPercent) {
      m_allowPercent = allowPercent;
      return this;
   }

   public SqlFlowControl setApp(String app) {
      m_app = app;
      return this;
   }

   public SqlFlowControl setSqlId(String sqlId) {
      m_sqlId = sqlId;
      return this;
   }
}
