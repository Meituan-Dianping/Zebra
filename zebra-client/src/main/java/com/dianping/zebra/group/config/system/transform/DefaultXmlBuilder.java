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
package com.dianping.zebra.group.config.system.transform;

import static com.dianping.zebra.group.config.system.Constants.ATTR_ALLOW_PERCENT;
import static com.dianping.zebra.group.config.system.Constants.ATTR_APP;
import static com.dianping.zebra.group.config.system.Constants.ATTR_SQL_ID;
import static com.dianping.zebra.group.config.system.Constants.ELEMENT_RETRY_TIMES;
import static com.dianping.zebra.group.config.system.Constants.ENTITY_SQL_FLOW_CONTROL;
import static com.dianping.zebra.group.config.system.Constants.ENTITY_SYSTEM_CONFIG;

import com.dianping.zebra.group.config.system.IEntity;
import com.dianping.zebra.group.config.system.IVisitor;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

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

	protected void startTag(String name, boolean closed, java.util.Map<String, String> dynamicAttributes,
	      Object... nameValues) {
		startTag(name, null, closed, dynamicAttributes, nameValues);
	}

	protected void startTag(String name, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
		startTag(name, null, false, dynamicAttributes, nameValues);
	}

	protected void startTag(String name, Object text, boolean closed, java.util.Map<String, String> dynamicAttributes,
	      Object... nameValues) {
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
	public void visitSqlFlowControl(SqlFlowControl sqlFlowControl) {
		startTag(ENTITY_SQL_FLOW_CONTROL, true, null, ATTR_SQL_ID, sqlFlowControl.getSqlId(), ATTR_ALLOW_PERCENT,
		      sqlFlowControl.getAllowPercent(), ATTR_APP, sqlFlowControl.getApp());
	}

	@Override
	public void visitSystemConfig(SystemConfig systemConfig) {
		startTag(ENTITY_SYSTEM_CONFIG, null);

		tagWithText(ELEMENT_RETRY_TIMES, String.valueOf(systemConfig.getRetryTimes()));

		if (!systemConfig.getSqlFlowControls().isEmpty()) {
			for (SqlFlowControl sqlFlowControl : systemConfig.getSqlFlowControls().toArray(new SqlFlowControl[0])) {
				sqlFlowControl.accept(m_visitor);
			}
		}

		endTag(ENTITY_SYSTEM_CONFIG);
	}
}
