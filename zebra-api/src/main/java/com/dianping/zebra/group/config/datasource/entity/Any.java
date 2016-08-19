package com.dianping.zebra.group.config.datasource.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.zebra.group.config.datasource.BaseEntity;
import com.dianping.zebra.group.config.datasource.IVisitor;

public class Any extends BaseEntity<Any> {
   private String m_name;

   private String m_value;

   private Map<String, String> m_attributes;

   private List<Any> m_children;

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitAny(this);
   }

   public Any addChild(Any any) {
      children(false).add(any);
      return this;
   }

   protected Map<String, String> attributes(boolean readonly) {
      if (m_attributes == null) {
         if (readonly) {
            return Collections.emptyMap();
         }

         m_attributes = new HashMap<String, String>();
      }

      return m_attributes;
   }

   protected List<Any> children(boolean readonly) {
      if (m_children == null) {
         if (readonly) {
            return Collections.emptyList();
         }

         m_children = new ArrayList<Any>();
      }

      return m_children;
   }

   public List<Any> getAllChildren(String name) {
      List<Any> all = new ArrayList<Any>();

      for (Any child : m_children) {
         if (child.getName().equals(name)) {
            all.add(child);
         }
      }

      return all;
   }

   public String getAttribute(String name) {
      return attributes(true).get(name);
   }

   public Map<String, String> getAttributes() {
      return attributes(true);
   }

   public List<Any> getChildren() {
      return children(true);
   }

   public Any getFirstChild(String name) {
      for (Any child : children(true)) {
         if (child.getName().equals(name)) {
            return child;
         }
      }

      return null;
   }

   public String getName() {
      return m_name;
   }

   public String getValue() {
      return m_value;
   }

   public boolean hasValue() {
      return m_value != null;
   }

   @Override
   public void mergeAttributes(Any other) {
      attributes(false).putAll(other.getAttributes());
   }

   public Any setAttribute(String name, String value) {
      attributes(false).put(name, value);
      return this;
   }

   public Any setName(String name) {
      m_name = name;
      return this;
   }

   public Any setValue(String value) {
      m_value = value;
      return this;
   }

   @Override
   public String toString() {
      if (m_value != null) {
         return String.format("<%s>%s</%1$s>", m_name,m_value);
      } else {
         return super.toString();
      }
   }
}
