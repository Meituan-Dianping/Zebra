package com.dianping.zebra.group.config.system.entity;

import static com.dianping.zebra.group.config.system.Constants.ATTR_NAME;
import static com.dianping.zebra.group.config.system.Constants.ENTITY_DATA_CENTER;

import com.dianping.zebra.group.config.system.BaseEntity;
import com.dianping.zebra.group.config.system.IVisitor;

public class DataCenter extends BaseEntity<DataCenter> {
   private String m_name;

   private String m_ipPrefix;

   public DataCenter() {
   }

   public DataCenter(String name) {
      m_name = name;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitDataCenter(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof DataCenter) {
         DataCenter _o = (DataCenter) obj;
         String name = _o.getName();

         return m_name == name || m_name != null && m_name.equals(name);
      }

      return false;
   }

   public String getIpPrefix() {
      return m_ipPrefix;
   }

   public String getName() {
      return m_name;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(DataCenter other) {
      assertAttributeEquals(other, ENTITY_DATA_CENTER, ATTR_NAME, m_name, other.getName());

      if (other.getIpPrefix() != null) {
         m_ipPrefix = other.getIpPrefix();
      }
   }

   public DataCenter setIpPrefix(String ipPrefix) {
      m_ipPrefix = ipPrefix;
      return this;
   }

   public DataCenter setName(String name) {
      m_name = name;
      return this;
   }

}
