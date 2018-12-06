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
package com.dianping.zebra.group.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;

import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.util.StringUtils;

public abstract class AbstractConfigManager {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractConfigManager.class);

	protected final ConfigService configService;

	protected final InnerPropertyChangeListener innerPropertyChangeListener;

	protected List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

	public AbstractConfigManager(ConfigService configService) {
		this.configService = configService;
		this.innerPropertyChangeListener = new InnerPropertyChangeListener();
		this.configService.addPropertyChangeListener(this.innerPropertyChangeListener);
	}

	public void close() {
		configService.removePropertyChangeListener(innerPropertyChangeListener);
	}

	public boolean getProperty(String key, boolean defaultValue) {
		String value = configService.getProperty(key);

		if ("true".equalsIgnoreCase(value)) {
			return true;
		} else if ("false".equalsIgnoreCase(value)) {
			return false;
		}

		return defaultValue;
	}

	protected int getProperty(String key, int defaultValue) {
		String value = configService.getProperty(key);

		if (StringUtils.isNotBlank(value)) {
			return Integer.parseInt(value);
		} else {
			return defaultValue;
		}
	}

	protected long getProperty(String key, long defaultValue) {
		String value = configService.getProperty(key);

		if (StringUtils.isNotBlank(value)) {
			return Long.parseLong(value);
		} else {
			return defaultValue;
		}
	}

	protected String getProperty(String key, String defaultValue) {
		String value = configService.getProperty(key);

		if (StringUtils.isNotBlank(value)) {
			return value;
		} else {
			return defaultValue;
		}
	}

	private void notifyListeners(final PropertyChangeEvent evt) {
		for (final PropertyChangeListener listener : listeners) {
			listener.propertyChange(evt);
		}
	}

	protected abstract void onPropertyUpdated(PropertyChangeEvent evt);

	class InnerPropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			try {
				onPropertyUpdated(evt);
				notifyListeners(evt);
			} catch (Exception e) {
				logger.error("fail to update property, apply old config!", e);
			}
		}
	}
}
