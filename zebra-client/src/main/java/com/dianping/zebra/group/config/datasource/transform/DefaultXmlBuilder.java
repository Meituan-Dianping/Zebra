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
import com.dianping.zebra.group.config.datasource.IVisitor;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

import java.util.Iterator;
import java.util.Map;

public class DefaultXmlBuilder implements IVisitor {
	private IVisitor m_visitor;

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
		this.m_visitor = this;
		this.m_compact = compact;
		this.m_sb = sb;
		this.m_sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
	}

	public String buildXml(IEntity<?> entity) {
		entity.accept(this.m_visitor);
		return this.m_sb.toString();
	}

	protected void endTag(String name) {
		--this.m_level;
		this.indent();
		this.m_sb.append("</").append(name).append(">\r\n");
	}

	protected String escape(Object value) {
		return this.escape(value, false);
	}

	protected String escape(Object value, boolean text) {
		if (value == null) {
			return null;
		} else {
			String str = value.toString();
			int len = str.length();
			StringBuilder sb = new StringBuilder(len + 16);

			for (int i = 0; i < len; ++i) {
				char ch = str.charAt(i);
				switch (ch) {
				case '"':
					if (!text) {
						sb.append("&quot;");
						break;
					}
				default:
					sb.append(ch);
					break;
				case '&':
					sb.append("&amp;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
				}
			}

			return sb.toString();
		}
	}

	protected void indent() {
		if (!this.m_compact) {
			for (int i = this.m_level - 1; i >= 0; --i) {
				this.m_sb.append("   ");
			}
		}

	}

	protected void startTag(String name) {
		this.startTag(name, false, (Map) null);
	}

	protected void startTag(String name, boolean closed, Map<String, String> dynamicAttributes, Object... nameValues) {
		this.startTag(name, (Object) null, closed, dynamicAttributes, nameValues);
	}

	protected void startTag(String name, Map<String, String> dynamicAttributes, Object... nameValues) {
		this.startTag(name, (Object) null, false, dynamicAttributes, nameValues);
	}

	protected void startTag(String name, Object text, boolean closed, Map<String, String> dynamicAttributes,
	      Object... nameValues) {
		this.indent();
		this.m_sb.append('<').append(name);
		int len = nameValues.length;

		for (int i = 0; i + 1 < len; i += 2) {
			Object attrName = nameValues[i];
			Object attrValue = nameValues[i + 1];
			if (attrValue != null) {
				this.m_sb.append(' ').append(attrName).append("=\"").append(this.escape(attrValue)).append('"');
			}
		}

		if (dynamicAttributes != null) {
			Iterator var10 = dynamicAttributes.entrySet().iterator();

			while (var10.hasNext()) {
				Map.Entry<String, String> e = (Map.Entry) var10.next();
				this.m_sb.append(' ').append((String) e.getKey()).append("=\"").append(this.escape(e.getValue()))
				      .append('"');
			}
		}

		if (text != null && closed) {
			this.m_sb.append('>');
			this.m_sb.append(this.escape(text, true));
			this.m_sb.append("</").append(name).append(">\r\n");
		} else {
			if (closed) {
				this.m_sb.append('/');
			} else {
				++this.m_level;
			}

			this.m_sb.append(">\r\n");
		}

	}

	protected void tagWithText(String name, String text, Object... nameValues) {
		if (text != null) {
			this.indent();
			this.m_sb.append('<').append(name);
			int len = nameValues.length;

			for (int i = 0; i + 1 < len; i += 2) {
				Object attrName = nameValues[i];
				Object attrValue = nameValues[i + 1];
				if (attrValue != null) {
					this.m_sb.append(' ').append(attrName).append("=\"").append(this.escape(attrValue)).append('"');
				}
			}

			this.m_sb.append(">");
			this.m_sb.append(this.escape(text, true));
			this.m_sb.append("</").append(name).append(">\r\n");
		}
	}

	protected void element(String name, String text, boolean escape) {
		if (text != null) {
			this.indent();
			this.m_sb.append('<').append(name).append(">");
			if (escape) {
				this.m_sb.append(this.escape(text, true));
			} else {
				this.m_sb.append("<![CDATA[").append(text).append("]]>");
			}

			this.m_sb.append("</").append(name).append(">\r\n");
		}
	}

	public void visitAny(Any any) {
		if (any.getChildren().isEmpty()) {
			if (any.hasValue()) {
				this.tagWithText(any.getName(), any.getValue());
			} else {
				this.startTag(any.getName(), true, any.getAttributes());
			}
		} else {
			this.startTag(any.getName(), false, any.getAttributes());
			Iterator var2 = any.getChildren().iterator();

			while (var2.hasNext()) {
				Any child = (Any) var2.next();
				child.accept(this.m_visitor);
			}

			this.endTag(any.getName());
		}

	}

	public void visitDataSourceConfig(DataSourceConfig dataSourceConfig) {
		this.startTag("data-source-config", (Map) null, "id", dataSourceConfig.getId(), "weight",
		      dataSourceConfig.getWeight(), "canRead", dataSourceConfig.isCanRead(), "canWrite",
		      dataSourceConfig.isCanWrite(), "active", dataSourceConfig.isActive(), "type", dataSourceConfig.getType(),
		      "tag", dataSourceConfig.getTag(), "jdbcref", dataSourceConfig.getJdbcref(), "lazyInit",
		      dataSourceConfig.getLazyInit());
		this.tagWithText("time-window", String.valueOf(dataSourceConfig.getTimeWindow()));
		this.tagWithText("punish-limit", String.valueOf(dataSourceConfig.getPunishLimit()));
		this.element("jdbc-url", dataSourceConfig.getJdbcUrl(), true);
		this.element("username", dataSourceConfig.getUsername(), true);
		this.element("driver-class", dataSourceConfig.getDriverClass(), true);
		this.element("password", dataSourceConfig.getPassword(), true);
		this.tagWithText("warmup-time", String.valueOf(dataSourceConfig.getWarmupTime()));
		Iterator var2 = dataSourceConfig.getProperties().iterator();

		while (var2.hasNext()) {
			Any any = (Any) var2.next();
			any.accept(this.m_visitor);
		}

		this.endTag("data-source-config");
	}

	public void visitGroupDataSourceConfig(GroupDataSourceConfig groupDataSourceConfig) {
		this.startTag("group-data-source-config", (Map) null, "filters", groupDataSourceConfig.getFilters(),
		      "router-strategy", groupDataSourceConfig.getRouterStrategy());
		if (!groupDataSourceConfig.getDataSourceConfigs().isEmpty()) {
			this.startTag("data-source-configs");
			DataSourceConfig[] var2 = (DataSourceConfig[]) groupDataSourceConfig.getDataSourceConfigs().values()
			      .toArray(new DataSourceConfig[0]);
			int var3 = var2.length;

			for (int var4 = 0; var4 < var3; ++var4) {
				DataSourceConfig dataSourceConfig = var2[var4];
				dataSourceConfig.accept(this.m_visitor);
			}

			this.endTag("data-source-configs");
		}

		this.endTag("group-data-source-config");
	}
}
