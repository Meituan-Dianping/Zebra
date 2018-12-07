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

import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.group.config.AdvancedPropertyChangeEvent;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.util.StringUtils;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Spi(name = "zookeeper", scope = Scope.SINGLETON)
public class ZookeeperConfigService implements ConfigService {

	protected static final Logger logger = LoggerFactory.getLogger(ZookeeperConfigService.class);

	protected ZookeeperClientManager clientManager = null;

	protected Map<String, String> keyMap = new ConcurrentHashMap<>();

	protected List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

	protected static volatile boolean init = false;

	@Override
	public synchronized void init(Map<String, Object> serviceConfigs) {
		if (!this.init) {
			try {
				clientManager = ZookeeperClientManager.getInstance();
				this.init = true;
			} catch (Exception e) {
				logger.error("fail to initilize zk Config Manager for DAL", e);
				throw new ZebraConfigException(e);
			}
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public String getProperty(String key) {
		if (StringUtils.isBlank(key)) {
			throw new ZebraException("ConfigSerivce.getProperty key is null");
		}

		String result = keyMap.get(key);

		if (result == null) {
			try {
				result = loadKeyWithWatcher(key);
			} catch (Exception e) {
				logger.error("get config value from zk failed", e);
			}
		}

		return result;
	}

	private String getConfigPath(String key) {
		String pathKey = key.replace('.', Constants.DEFAULT_PATH_SEPARATOR.charAt(0));
		return Constants.DEFAULT_PATH_SEPARATOR + pathKey;
	}

	private String loadKeyWithWatcher(final String key) throws Exception {
		String path = getConfigPath(key);
		NodeCache nodeCache = clientManager.getNodeCache(path);

		if (nodeCache == null) {
			final NodeCache newNodeCache = clientManager.registerNodeCache(path);
			newNodeCache.getListenable().addListener(new NodeCacheListener() {
				@Override
				public void nodeChanged() throws Exception {
					String oldValue = keyMap.get(key);
					String newValue = new String(newNodeCache.getCurrentData().getData());
					keyMap.put(key, newValue);
					notifyListeners(key, oldValue, newValue);
				}
			});
			nodeCache = newNodeCache;
		}

		byte[] data = nodeCache.getCurrentData().getData();
		return new String(data, Constants.DEFAULT_CHARSET);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

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
}
