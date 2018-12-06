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
package com.dianping.zebra.group.config.datasource.transform;

import com.dianping.zebra.group.config.datasource.IEntity;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
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

public class DefaultSaxParser extends DefaultHandler {
	private DefaultLinker m_linker = new DefaultLinker(true);

	private DefaultSaxMaker m_maker = new DefaultSaxMaker();

	private Stack<String> m_tags = new Stack();

	private Stack<Object> m_objs = new Stack();

	private Stack<Any> m_anys = new Stack();

	private IEntity<?> m_entity;

	private StringBuilder m_text = new StringBuilder();

	public DefaultSaxParser() {
	}

	public static GroupDataSourceConfig parse(InputSource is) throws SAXException, IOException {
		return (GroupDataSourceConfig) parseEntity(GroupDataSourceConfig.class, is);
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

	public static <T extends IEntity<?>> T parseEntity(Class<T> type, InputSource is) throws SAXException, IOException {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			DefaultSaxParser handler = new DefaultSaxParser();
			parser.parse(is, handler);
			return (T) handler.getEntity();
		} catch (ParserConfigurationException var4) {
			throw new IllegalStateException("Unable to get SAX parser instance!", var4);
		}
	}

	protected Any buildAny(String qName, Attributes attributes) {
		Any any = new Any();
		int length = attributes == null ? 0 : attributes.getLength();
		any.setName(qName);
		if (length > 0) {
			Map<String, String> dynamicAttributes = any.getAttributes();

			for (int i = 0; i < length; ++i) {
				String name = attributes.getQName(i);
				String value = attributes.getValue(i);
				dynamicAttributes.put(name, value);
			}
		}

		this.m_anys.push(any);
		return any;
	}

	protected <T> T convert(Class<T> type, String value, T defaultValue) {
		if (value != null && value.length() != 0) {
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
			} else {
				return (T) (type == Character.class ? value.charAt(0) : value);
			}
		} else {
			return defaultValue;
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		this.m_text.append(ch, start, length);
	}

	public void endDocument() throws SAXException {
		this.m_linker.finish();
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (uri == null || uri.length() == 0) {
			Object currentObj = this.m_objs.pop();
			String currentTag = (String) this.m_tags.pop();
			if (!this.m_anys.isEmpty()) {
				Any any = (Any) this.m_anys.pop();
				any.setValue(this.getText());
			} else if (currentObj instanceof DataSourceConfig) {
				DataSourceConfig dataSourceConfig = (DataSourceConfig) currentObj;
				if ("time-window".equals(currentTag)) {
					dataSourceConfig.setTimeWindow((Long) this.convert(Long.class, this.getText(), 0L));
				} else if ("punish-limit".equals(currentTag)) {
					dataSourceConfig.setPunishLimit((Long) this.convert(Long.class, this.getText(), 0L));
				} else if ("jdbc-url".equals(currentTag)) {
					dataSourceConfig.setJdbcUrl(this.getText());
				} else if ("username".equals(currentTag)) {
					dataSourceConfig.setUsername(this.getText());
				} else if ("driver-class".equals(currentTag)) {
					dataSourceConfig.setDriverClass(this.getText());
				} else if ("password".equals(currentTag)) {
					dataSourceConfig.setPassword(this.getText());
				} else if ("warmup-time".equals(currentTag)) {
					dataSourceConfig.setWarmupTime((Integer) this.convert(Integer.class, this.getText(), 0));
				}
			}
		}

		this.m_text.setLength(0);
	}

	private IEntity<?> getEntity() {
		return this.m_entity;
	}

	protected String getText() {
		return this.m_text.toString();
	}

	private void parseForDataSourceConfig(DataSourceConfig parentObj, String parentTag, String qName,
	      Attributes attributes) throws SAXException {
		if (!"time-window".equals(qName) && !"punish-limit".equals(qName) && !"jdbc-url".equals(qName)
		      && !"username".equals(qName) && !"driver-class".equals(qName) && !"password".equals(qName)
		      && !"warmup-time".equals(qName)) {
			this.m_objs.push(parentObj);
			parentObj.getProperties().add(this.buildAny(qName, attributes));
		} else {
			this.m_objs.push(parentObj);
		}

		this.m_tags.push(qName);
	}

	private void parseForGroupDataSourceConfig(GroupDataSourceConfig parentObj, String parentTag, String qName,
	      Attributes attributes) throws SAXException {
		if ("data-source-configs".equals(qName)) {
			this.m_objs.push(parentObj);
		} else {
			if (!"data-source-config".equals(qName)) {
				throw new SAXException(String.format("Element(%s) is not expected under group-data-source-config!", qName));
			}

			DataSourceConfig dataSourceConfig = this.m_maker.buildDataSourceConfig(attributes);
			this.m_linker.onDataSourceConfig(parentObj, dataSourceConfig);
			this.m_objs.push(dataSourceConfig);
		}

		this.m_tags.push(qName);
	}

	private void parseRoot(String qName, Attributes attributes) throws SAXException {
		if ("group-data-source-config".equals(qName)) {
			GroupDataSourceConfig groupDataSourceConfig = this.m_maker.buildGroupDataSourceConfig(attributes);
			this.m_entity = groupDataSourceConfig;
			this.m_objs.push(groupDataSourceConfig);
			this.m_tags.push(qName);
		} else {
			if (!"data-source-config".equals(qName)) {
				throw new SAXException("Unknown root element(" + qName + ") found!");
			}

			DataSourceConfig dataSourceConfig = this.m_maker.buildDataSourceConfig(attributes);
			this.m_entity = dataSourceConfig;
			this.m_objs.push(dataSourceConfig);
			this.m_tags.push(qName);
		}

	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (uri != null && uri.length() != 0) {
			throw new SAXException(String.format("Namespace(%s) is not supported by %s.", uri, this.getClass().getName()));
		} else {
			if (this.m_objs.isEmpty()) {
				this.parseRoot(qName, attributes);
			} else {
				Object parent = this.m_objs.peek();
				String tag = (String) this.m_tags.peek();
				if (!this.m_anys.isEmpty()) {
					Any any = (Any) this.m_anys.peek();
					this.m_objs.push(this.m_anys.peek());
					this.m_tags.push(qName);
					any.addChild(this.buildAny(qName, attributes));
				} else if (parent instanceof GroupDataSourceConfig) {
					this.parseForGroupDataSourceConfig((GroupDataSourceConfig) parent, tag, qName, attributes);
				} else {
					if (!(parent instanceof DataSourceConfig)) {
						throw new RuntimeException(String.format("Unknown entity(%s) under %s!", qName, parent.getClass()
						      .getName()));
					}

					this.parseForDataSourceConfig((DataSourceConfig) parent, tag, qName, attributes);
				}
			}

			this.m_text.setLength(0);
		}
	}
}
