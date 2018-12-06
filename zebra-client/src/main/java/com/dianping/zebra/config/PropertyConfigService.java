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
package com.dianping.zebra.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.group.config.AdvancedPropertyChangeEvent;
import com.dianping.zebra.util.FileUtils;
import com.dianping.zebra.util.StringUtils;

@Spi(name = "local", scope = Scope.OTHERS)
public class PropertyConfigService implements ConfigService {

	protected static final Logger logger = LoggerFactory.getLogger(PropertyConfigService.class);

	private String resourceFileName;

	private File resourceFile;

	private List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

	private AtomicReference<Properties> props = new AtomicReference<Properties>();

	private AtomicLong lastModifiedTime = new AtomicLong(-1);

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.listeners.add(listener);
	}

	private long getLastModifiedTime() {
		if (this.resourceFile.exists()) {
			return this.resourceFile.lastModified();
		} else {
			logger.warn(String.format("config file[%s] doesn't exist.", this.resourceFileName));
			return -1;
		}
	}

	@Override
	public String getProperty(String key) {
		return props.get().getProperty(key);
	}

	@Override
	public void init(Map<String, Object> serviceConfigs) {
		try {
			if (serviceConfigs == null) {
				throw new ZebraException("serviceConfigs is null");
			}

			String resourceName = String.valueOf(serviceConfigs.get(Constants.CONFIG_SERVICE_NAME_KEY));
			this.resourceFileName = resourceName + ".properties";
			this.resourceFile = FileUtils.getFile(resourceFileName);
			this.props.set(FileUtils.loadProperties(resourceFile));
			this.lastModifiedTime.set(getLastModifiedTime());

			Thread updateTask = new Thread(new ConfigPeroidCheckerTask());
			updateTask.setDaemon(true);
			updateTask.setName("Thread-" + ConfigPeroidCheckerTask.class.getName());
			updateTask.start();
		} catch (Exception e) {
			logger.error("fail to initilize Local Config Manager for DAL", e);
			throw new ZebraConfigException(e);
		}
	}

	class ConfigPeroidCheckerTask implements Runnable {
		private void notifyListeners(String key, Object oldValue, Object newValue) {
			PropertyChangeEvent evt = new AdvancedPropertyChangeEvent(this, key, oldValue, newValue);

			for (PropertyChangeListener listener : listeners) {
				try {
					listener.propertyChange(evt);
				} catch (Exception e) {
					logger.warn("fail to notify listener", e);
				}
			}
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					long newModifiedTime = getLastModifiedTime();
					if (newModifiedTime > lastModifiedTime.get()) {
						Properties oldProps = props.get();
						Properties newProps = FileUtils.loadProperties(resourceFile);

						for (String key : newProps.stringPropertyNames()) {
							String oldValue = oldProps.getProperty(key);
							String newValue = newProps.getProperty(key);

							if (!StringUtils.equals(oldValue, newValue)) {
								notifyListeners(key, oldValue, newValue);
							}
						}

						lastModifiedTime.set(newModifiedTime);
						props.set(newProps);
					}
				} catch (Exception exp) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("fail to reload the datasource config[%s]", resourceFileName), exp);
					}
				}

				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					// ignore it
				}
			}
		}

	}

	@Override
	public void destroy() {
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}
}
