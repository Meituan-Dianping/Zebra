package com.dianping.zebra.group.config.datasource.transform;

import com.dianping.zebra.group.config.datasource.IEntity;
import com.dianping.zebra.group.config.datasource.IVisitor;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

import static com.dianping.zebra.group.config.datasource.Constants.*;

public class DefaultXmlBuilder implements IVisitor {

   private IVisitor m_visitor = this;

   private int m_level;

   private StringBuilder m_sb;

   private boolean m_compact;

   public DefaultXmlBuilder() {
      this(false);
   }

   public DefaultXmlBuilder(boolean compact) {
      this(compact, new StringBuilder(4096));
   }

   public DefaultXmlBuilder(boolean compact, StringBuilder sb) {
      m_compact = compact;
      m_sb = sb;
      m_sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
   }

   public String buildXml(IEntity<?> entity) {
      entity.accept(m_visitor);
      return m_sb.toString();
   }

   protected void endTag(String name) {
      m_level--;

      indent();
      m_sb.append("</").append(name).append(">\r\n");
   }

   protected String escape(Object value) {
      return escape(value, false);
   }


   protected String escape(Object value, boolean text) {
      if (value == null) {
         return null;
      }

      String str = value.toString();
      int len = str.length();
      StringBuilder sb = new StringBuilder(len + 16);

      for (int i = 0; i < len; i++) {
         final char ch = str.charAt(i);

         switch (ch) {
         case '<':
            sb.append("&lt;");
            break;
         case '>':
            sb.append("&gt;");
            break;
         case '&':
            sb.append("&amp;");
            break;
         case '"':
            if (!text) {
               sb.append("&quot;");
               break;
            }
         default:
            sb.append(ch);
            break;
         }
      }

      return sb.toString();
   }
   
   protected void indent() {
      if (!m_compact) {
         for (int i = m_level - 1; i >= 0; i--) {
            m_sb.append("   ");
         }
      }
   }

   protected void startTag(String name) {
      startTag(name, false, null);
   }
   
   protected void startTag(String name, boolean closed, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
      startTag(name, null, closed, dynamicAttributes, nameValues);
   }

   protected void startTag(String name, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
      startTag(name, null, false, dynamicAttributes, nameValues);
   }

   protected void startTag(String name, Object text, boolean closed, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
      indent();

      m_sb.append('<').append(name);

      int len = nameValues.length;

      for (int i = 0; i + 1 < len; i += 2) {
         Object attrName = nameValues[i];
         Object attrValue = nameValues[i + 1];

         if (attrValue != null) {
            m_sb.append(' ').append(attrName).append("=\"").append(escape(attrValue)).append('"');
         }
      }

      if (dynamicAttributes != null) {
         for (java.util.Map.Entry<String, String> e : dynamicAttributes.entrySet()) {
            m_sb.append(' ').append(e.getKey()).append("=\"").append(escape(e.getValue())).append('"');
         }
      }

      if (text != null && closed) {
         m_sb.append('>');
         m_sb.append(escape(text, true));
         m_sb.append("</").append(name).append(">\r\n");
      } else {
         if (closed) {
            m_sb.append('/');
         } else {
            m_level++;
         }
   
         m_sb.append(">\r\n");
      }
   }

   protected void tagWithText(String name, String text, Object... nameValues) {
      if (text == null) {
         return;
      }
      
      indent();

      m_sb.append('<').append(name);

      int len = nameValues.length;

      for (int i = 0; i + 1 < len; i += 2) {
         Object attrName = nameValues[i];
         Object attrValue = nameValues[i + 1];

         if (attrValue != null) {
            m_sb.append(' ').append(attrName).append("=\"").append(escape(attrValue)).append('"');
         }
      }

      m_sb.append(">");
      m_sb.append(escape(text, true));
      m_sb.append("</").append(name).append(">\r\n");
   }

   protected void element(String name, String text, boolean escape) {
      if (text == null) {
         return;
      }
      
      indent();
      
      m_sb.append('<').append(name).append(">");
      
      if (escape) {
         m_sb.append(escape(text, true));
      } else {
         m_sb.append("<![CDATA[").append(text).append("]]>");
      }
      
      m_sb.append("</").append(name).append(">\r\n");
   }

   @Override
   public void visitAny(Any any) {
      if (any.getChildren().isEmpty()) {
         if (any.hasValue()) {
            tagWithText(any.getName(), any.getValue());
         } else {
            startTag(any.getName(), true, any.getAttributes());
         }
      } else {
         startTag(any.getName(), false, any.getAttributes());

         for (Any child : any.getChildren()) {
            child.accept(m_visitor);
         }

         endTag(any.getName());
      }
   }

   @Override
   public void visitDataSourceConfig(DataSourceConfig dataSourceConfig) {
      startTag(ENTITY_DATA_SOURCE_CONFIG, null, ATTR_ID, dataSourceConfig.getId(), ATTR_WEIGHT, dataSourceConfig.getWeight(), ATTR_CANREAD, dataSourceConfig.isCanRead(), ATTR_CANWRITE, dataSourceConfig.isCanWrite(), ATTR_ACTIVE, dataSourceConfig.isActive(), ATTR_TYPE, dataSourceConfig.getType(), ATTR_TAG, dataSourceConfig.getTag());

      element(ELEMENT_TEST_READ_ONLY_SQL, dataSourceConfig.getTestReadOnlySql(), true);

      tagWithText(ELEMENT_TIME_WINDOW, String.valueOf(dataSourceConfig.getTimeWindow()));

      tagWithText(ELEMENT_PUNISH_LIMIT, String.valueOf(dataSourceConfig.getPunishLimit()));

      element(ELEMENT_JDBC_URL, dataSourceConfig.getJdbcUrl(), true);

      element(ELEMENT_USERNAME, dataSourceConfig.getUsername(), true);

      element(ELEMENT_DRIVER_CLASS, dataSourceConfig.getDriverClass(), true);

      element(ELEMENT_PASSWORD, dataSourceConfig.getPassword(), true);

      tagWithText(ELEMENT_WARMUP_TIME, String.valueOf(dataSourceConfig.getWarmupTime()));

      for (Any any : dataSourceConfig.getProperties()) {
         any.accept(m_visitor);
      }

      endTag(ENTITY_DATA_SOURCE_CONFIG);
   }

   @Override
   public void visitGroupDataSourceConfig(GroupDataSourceConfig groupDataSourceConfig) {
      startTag(ENTITY_GROUP_DATA_SOURCE_CONFIG, null, ATTR_ROUTER_STRATEGY, groupDataSourceConfig.getRouterStrategy(), ATTR_FILTERS, groupDataSourceConfig.getFilters(), ATTR_FORCE_WRITE_ON_LOGIN, groupDataSourceConfig.isForceWriteOnLogin());

      if (!groupDataSourceConfig.getDataSourceConfigs().isEmpty()) {
         startTag(ENTITY_DATA_SOURCE_CONFIGS);

         for (DataSourceConfig dataSourceConfig : groupDataSourceConfig.getDataSourceConfigs().values().toArray(new DataSourceConfig[0])) {
            dataSourceConfig.accept(m_visitor);
         }

         endTag(ENTITY_DATA_SOURCE_CONFIGS);
      }

      endTag(ENTITY_GROUP_DATA_SOURCE_CONFIG);
   }
}
