package com.dianping.zebra.group.config.system.transform;

import static com.dianping.zebra.group.config.system.Constants.ELEMENT_RETRY_TIMES;

import static com.dianping.zebra.group.config.system.Constants.ENTITY_DATA_CENTER;
import static com.dianping.zebra.group.config.system.Constants.ENTITY_SQL_FLOW_CONTROL;
import static com.dianping.zebra.group.config.system.Constants.ENTITY_SYSTEM_CONFIG;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.dianping.zebra.group.config.system.IEntity;
import com.dianping.zebra.group.config.system.entity.DataCenter;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

public class DefaultSaxParser extends DefaultHandler {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private DefaultSaxMaker m_maker = new DefaultSaxMaker();

   private Stack<String> m_tags = new Stack<String>();

   private Stack<Object> m_objs = new Stack<Object>();

   private IEntity<?> m_entity;

   private StringBuilder m_text = new StringBuilder();

   public static SystemConfig parse(InputSource is) throws SAXException, IOException {
      return parseEntity(SystemConfig.class, is);
   }

   public static SystemConfig parse(InputStream in) throws SAXException, IOException {
      return parse(new InputSource(in));
   }

   public static SystemConfig parse(Reader reader) throws SAXException, IOException {
      return parse(new InputSource(reader));
   }

   public static SystemConfig parse(String xml) throws SAXException, IOException {
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

         if (currentObj instanceof SystemConfig) {
            SystemConfig systemConfig = (SystemConfig) currentObj;

            if (ELEMENT_RETRY_TIMES.equals(currentTag)) {
               systemConfig.setRetryTimes(convert(Integer.class, getText(), 0));
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

   private void parseForDataCenter(DataCenter parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      m_objs.push(parentObj);
      m_tags.push(qName);
   }

   private void parseForSqlFlowControl(SqlFlowControl parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      m_objs.push(parentObj);
      m_tags.push(qName);
   }

   private void parseForSystemConfig(SystemConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ELEMENT_RETRY_TIMES.equals(qName)) {
         m_objs.push(parentObj);
      } else if (ENTITY_SQL_FLOW_CONTROL.equals(qName)) {
         SqlFlowControl sqlFlowControl = m_maker.buildSqlFlowControl(attributes);

         m_linker.onSqlFlowControl(parentObj, sqlFlowControl);
         m_objs.push(sqlFlowControl);
      } else if (ENTITY_DATA_CENTER.equals(qName)) {
         DataCenter dataCenter = m_maker.buildDataCenter(attributes);

         m_linker.onDataCenter(parentObj, dataCenter);
         m_objs.push(dataCenter);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under system-config!", qName));
      }

      m_tags.push(qName);
   }

   private void parseRoot(String qName, Attributes attributes) throws SAXException {
      if (ENTITY_SYSTEM_CONFIG.equals(qName)) {
         SystemConfig systemConfig = m_maker.buildSystemConfig(attributes);

         m_entity = systemConfig;
         m_objs.push(systemConfig);
         m_tags.push(qName);
      } else if (ENTITY_SQL_FLOW_CONTROL.equals(qName)) {
         SqlFlowControl sqlFlowControl = m_maker.buildSqlFlowControl(attributes);

         m_entity = sqlFlowControl;
         m_objs.push(sqlFlowControl);
         m_tags.push(qName);
      } else if (ENTITY_DATA_CENTER.equals(qName)) {
         DataCenter dataCenter = m_maker.buildDataCenter(attributes);

         m_entity = dataCenter;
         m_objs.push(dataCenter);
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

            if (parent instanceof SystemConfig) {
               parseForSystemConfig((SystemConfig) parent, tag, qName, attributes);
            } else if (parent instanceof SqlFlowControl) {
               parseForSqlFlowControl((SqlFlowControl) parent, tag, qName, attributes);
            } else if (parent instanceof DataCenter) {
               parseForDataCenter((DataCenter) parent, tag, qName, attributes);
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
