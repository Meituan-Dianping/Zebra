package com.dianping.zebra.group.config.datasource.transform;

import java.util.Stack;

import com.dianping.zebra.group.config.datasource.IEntity;
import com.dianping.zebra.group.config.datasource.IVisitor;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

public class DefaultMerger implements IVisitor {

   private Stack<Object> m_objs = new Stack<Object>();

   private GroupDataSourceConfig m_groupDataSourceConfig;

   public DefaultMerger() {
   }

   public DefaultMerger(GroupDataSourceConfig groupDataSourceConfig) {
      m_groupDataSourceConfig = groupDataSourceConfig;
      m_objs.push(groupDataSourceConfig);
   }

   public GroupDataSourceConfig getGroupDataSourceConfig() {
      return m_groupDataSourceConfig;
   }

   protected Stack<Object> getObjects() {
      return m_objs;
   }

   public <T> void merge(IEntity<T> to, IEntity<T> from) {
      m_objs.push(to);
      from.accept(this);
      m_objs.pop();
   }

   protected void mergeDataSourceConfig(DataSourceConfig to, DataSourceConfig from) {
      to.mergeAttributes(from);
      to.getProperties().addAll(from.getProperties());
      to.setTestReadOnlySql(from.getTestReadOnlySql());
      to.setTimeWindow(from.getTimeWindow());
      to.setPunishLimit(from.getPunishLimit());
      to.setJdbcUrl(from.getJdbcUrl());
      to.setUsername(from.getUsername());
      to.setDriverClass(from.getDriverClass());
      to.setPassword(from.getPassword());
      to.setWarmupTime(from.getWarmupTime());
   }

   protected void mergeGroupDataSourceConfig(GroupDataSourceConfig to, GroupDataSourceConfig from) {
      to.mergeAttributes(from);
   }

   @Override
   public void visitAny(Any any) {
      // do nothing here
   }

   @Override
   public void visitDataSourceConfig(DataSourceConfig from) {
      DataSourceConfig to = (DataSourceConfig) m_objs.peek();

      mergeDataSourceConfig(to, from);
      visitDataSourceConfigChildren(to, from);
   }

   protected void visitDataSourceConfigChildren(DataSourceConfig to, DataSourceConfig from) {
      to.getProperties().addAll(from.getProperties());
   }

   @Override
   public void visitGroupDataSourceConfig(GroupDataSourceConfig from) {
      GroupDataSourceConfig to = (GroupDataSourceConfig) m_objs.peek();

      mergeGroupDataSourceConfig(to, from);
      visitGroupDataSourceConfigChildren(to, from);
   }

   protected void visitGroupDataSourceConfigChildren(GroupDataSourceConfig to, GroupDataSourceConfig from) {
      for (DataSourceConfig source : from.getDataSourceConfigs().values()) {
         DataSourceConfig target = to.findDataSourceConfig(source.getId());

         if (target == null) {
            target = new DataSourceConfig(source.getId());
            to.addDataSourceConfig(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }
}
