package com.dianping.zebra.group.config.datasource.transform;

import static com.dianping.zebra.group.config.datasource.Constants.ELEMENT_DRIVER_CLASS;
import static com.dianping.zebra.group.config.datasource.Constants.ELEMENT_JDBC_URL;
import static com.dianping.zebra.group.config.datasource.Constants.ELEMENT_PASSWORD;
import static com.dianping.zebra.group.config.datasource.Constants.ELEMENT_PUNISH_LIMIT;
import static com.dianping.zebra.group.config.datasource.Constants.ELEMENT_TEST_READ_ONLY_SQL;
import static com.dianping.zebra.group.config.datasource.Constants.ELEMENT_TIME_WINDOW;
import static com.dianping.zebra.group.config.datasource.Constants.ELEMENT_USERNAME;
import static com.dianping.zebra.group.config.datasource.Constants.ELEMENT_WARMUP_TIME;

import static com.dianping.zebra.group.config.datasource.Constants.ENTITY_DATA_SOURCE_CONFIG;
import static com.dianping.zebra.group.config.datasource.Constants.ENTITY_GROUP_DATA_SOURCE_CONFIG;
import static com.dianping.zebra.group.config.datasource.Constants.ENTITY_DATA_SOURCE_CONFIGS;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.dianping.zebra.group.config.datasource.IEntity;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

public class DefaultSaxParser extends DefaultHandler {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private DefaultSaxMaker m_maker = new DefaultSaxMaker();

   private Stack<String> m_tags = new Stack<String>();

   private Stack<Object> m_objs = new Stack<Object>();

   private Stack<Any> m_anys = new Stack<Any>();

   private IEntity<?> m_entity;

   private StringBuilder m_text = new StringBuilder();

   public static GroupDataSourceConfig parse(InputSource is) throws SAXException, IOException {
      return parseEntity(GroupDataSourceConfig.class, is);
   }

   public static GroupDataSourceConfig parse(InputStream in) throws SAXException, IOException {
      return parse(new InputSource(in));
   }

   public static GroupDataSourceConfig parse(Reader reader) throws SAXException, IOException {
      return parse(new InputSource(reader));
   }

   public static GroupDataSourceConfig parse(String xml) throws SAXException, IOException {
      return parse(new InputSource(new StringReader(xml)));
   }

   public static <T extends IEntity<?>> T parseEntity(Class<T> type, String xml) throws SAXException, IOException {
      return parseEntity(type, new InputSource(new StringReader(xml)));
   }

   @SuppressWarnings("unchecked")
   public static <T extends IEntity<?>> T parseEntity(Class<T> type, InputSource is) throws SAXException, IOException {
      try {
         SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
         DefaultSaxParser handler = new DefaultSaxParser();

         parser.parse(is, handler);
         return (T) handler.getEntity();
      } catch (ParserConfigurationException e) {
         throw new IllegalStateException("Unable to get SAX parser instance!", e);
      }
   }

   protected Any buildAny(String qName, Attributes attributes) {
      Any any = new Any();
      int length = attributes == null ? 0 : attributes.getLength();

      any.setName(qName);

      if (length > 0) {
         Map<String, String> dynamicAttributes = any.getAttributes();

         for (int i = 0; i < length; i++) {
            String name = attributes.getQName(i);
            String value = attributes.getValue(i);

            dynamicAttributes.put(name, value);
         }
      }

      m_anys.push(any);
      return any;
   }

   @SuppressWarnings("unchecked")
   protected <T> T convert(Class<T> type, String value, T defaultValue) {
      if (value == null || value.length() == 0) {
         return defaultValue;
      }

      if (type == Boolean.class) {
         return (T) Boolean.valueOf(value);
      } else if (type == Integer.class) {
         return (T) Integer.valueOf(value);
      } else if (type == Long.class) {
         return (T) Long.valueOf(value);
      } else if (type == Short.class) {
         return (T) Short.valueOf(value);
      } else if (type == Float.class) {
         return (T) Float.valueOf(value);
      } else if (type == Double.class) {
         return (T) Double.valueOf(value);
      } else if (type == Byte.class) {
         return (T) Byte.valueOf(value);
      } else if (type == Character.class) {
         return (T) (Character) value.charAt(0);
      } else {
         return (T) value;
      }
   }

   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
      m_text.append(ch, start, length);
   }

   @Override
   public void endDocument() throws SAXException {
      m_linker.finish();
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
      if (uri == null || uri.length() == 0) {
         Object currentObj = m_objs.pop();
         String currentTag = m_tags.pop();

         if (!m_anys.isEmpty()) {
            Any any = m_anys.pop();

            any.setValue(getText());
         } else if (currentObj instanceof DataSourceConfig) {
            DataSourceConfig dataSourceConfig = (DataSourceConfig) currentObj;

            if (ELEMENT_TEST_READ_ONLY_SQL.equals(currentTag)) {
               dataSourceConfig.setTestReadOnlySql(getText());
            } else if (ELEMENT_TIME_WINDOW.equals(currentTag)) {
               dataSourceConfig.setTimeWindow(convert(Long.class, getText(), 0L));
            } else if (ELEMENT_PUNISH_LIMIT.equals(currentTag)) {
               dataSourceConfig.setPunishLimit(convert(Long.class, getText(), 0L));
            } else if (ELEMENT_JDBC_URL.equals(currentTag)) {
               dataSourceConfig.setJdbcUrl(getText());
            } else if (ELEMENT_USERNAME.equals(currentTag)) {
               dataSourceConfig.setUsername(getText());
            } else if (ELEMENT_DRIVER_CLASS.equals(currentTag)) {
               dataSourceConfig.setDriverClass(getText());
            } else if (ELEMENT_PASSWORD.equals(currentTag)) {
               dataSourceConfig.setPassword(getText());
            } else if (ELEMENT_WARMUP_TIME.equals(currentTag)) {
               dataSourceConfig.setWarmupTime(convert(Integer.class, getText(), 0));
            }
         }
      }

      m_text.setLength(0);
   }

   private IEntity<?> getEntity() {
      return m_entity;
   }

   protected String getText() {
      return m_text.toString();
   }

   private void parseForDataSourceConfig(DataSourceConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ELEMENT_TEST_READ_ONLY_SQL.equals(qName) || ELEMENT_TIME_WINDOW.equals(qName) || ELEMENT_PUNISH_LIMIT.equals(qName) || ELEMENT_JDBC_URL.equals(qName) || ELEMENT_USERNAME.equals(qName) || ELEMENT_DRIVER_CLASS.equals(qName) || ELEMENT_PASSWORD.equals(qName) || ELEMENT_WARMUP_TIME.equals(qName)) {
         m_objs.push(parentObj);
      } else {
         m_objs.push(parentObj);
         parentObj.getProperties().add(buildAny(qName, attributes));
      }

      m_tags.push(qName);
   }

   private void parseForGroupDataSourceConfig(GroupDataSourceConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ENTITY_DATA_SOURCE_CONFIGS.equals(qName)) {
         m_objs.push(parentObj);
      } else if (ENTITY_DATA_SOURCE_CONFIG.equals(qName)) {
         DataSourceConfig dataSourceConfig = m_maker.buildDataSourceConfig(attributes);

         m_linker.onDataSourceConfig(parentObj, dataSourceConfig);
         m_objs.push(dataSourceConfig);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under group-data-source-config!", qName));
      }

      m_tags.push(qName);
   }

   private void parseRoot(String qName, Attributes attributes) throws SAXException {
      if (ENTITY_GROUP_DATA_SOURCE_CONFIG.equals(qName)) {
         GroupDataSourceConfig groupDataSourceConfig = m_maker.buildGroupDataSourceConfig(attributes);

         m_entity = groupDataSourceConfig;
         m_objs.push(groupDataSourceConfig);
         m_tags.push(qName);
      } else if (ENTITY_DATA_SOURCE_CONFIG.equals(qName)) {
         DataSourceConfig dataSourceConfig = m_maker.buildDataSourceConfig(attributes);

         m_entity = dataSourceConfig;
         m_objs.push(dataSourceConfig);
         m_tags.push(qName);
      } else {
         throw new SAXException("Unknown root element(" + qName + ") found!");
      }
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (uri == null || uri.length() == 0) {
         if (m_objs.isEmpty()) { // root
            parseRoot(qName, attributes);
         } else {
            Object parent = m_objs.peek();
            String tag = m_tags.peek();

            if (!m_anys.isEmpty()) {
               Any any = m_anys.peek();

               m_objs.push(m_anys.peek());
               m_tags.push(qName);
               any.addChild(buildAny(qName, attributes));
            } else if (parent instanceof GroupDataSourceConfig) {
               parseForGroupDataSourceConfig((GroupDataSourceConfig) parent, tag, qName, attributes);
            } else if (parent instanceof DataSourceConfig) {
               parseForDataSourceConfig((DataSourceConfig) parent, tag, qName, attributes);
            } else {
               throw new RuntimeException(String.format("Unknown entity(%s) under %s!", qName, parent.getClass().getName()));
            }
         }

         m_text.setLength(0);
        } else {
         throw new SAXException(String.format("Namespace(%s) is not supported by %s.", uri, this.getClass().getName()));
      }
   }
}
