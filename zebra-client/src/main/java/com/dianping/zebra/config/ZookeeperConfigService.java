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
import com.dianping.zebra.util.FileUtils;
import com.dianping.zebra.util.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Spi(name = "zookeeper", scope = Scope.SINGLETON)
public class ZookeeperConfigService implements ConfigService {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfigService.class);

	protected static Map<String, String> keyMap = new ConcurrentHashMap<>();

	protected static Map<String, NodeCache> nodeCacheMap = new ConcurrentHashMap<>();

	protected List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

	protected String propertiesFileName;

	protected volatile static CuratorFramework client = null;

	private final int baseSleepTimeMills = 1000;

	private final int maxRetries = 3;

	private final int connectionTimeoutMs = 1000;

	private final int sessionTimeoutMs = 1000;
	
	protected static volatile boolean init = false;

	@Override
	public synchronized void init(Map<String, Object> serviceConfigs) {
		if (!this.init) {
			try {
				this.propertiesFileName = Constants.DEFAULT_ZK_FILENAME + ".properties";
				String zkAddr = loadZookeeperAdderss();

				RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMills, maxRetries);
				CuratorFramework newClient = CuratorFrameworkFactory.builder().connectString(zkAddr)
				      .sessionTimeoutMs(connectionTimeoutMs).connectionTimeoutMs(sessionTimeoutMs).retryPolicy(retryPolicy)
				      .build();
				newClient.start();
				newClient.getZookeeperClient().blockUntilConnectedOrTimedOut();

				this.client = newClient;
				this.init = true;
				LOGGER.info("DAL.Zookeeper connected");
			} catch (Exception e) {
				LOGGER.error("connect to zookeeper server failed", e);
				throw new ZebraException("connect to zookeeper server failed", e);
			}
		}
	}

	private String loadZookeeperAdderss() {
		File zkConfigFile = FileUtils.getFile(this.propertiesFileName);
		Properties props = FileUtils.loadProperties(zkConfigFile);
		String zkAddr = props.getProperty(Constants.ZK_ADDR_KEY);

		if (StringUtils.isBlank(zkAddr)) {
			throw new ZebraException("zookeeper server address won't be null");
		}

		return zkAddr;
	}

	@Override
	public void destroy() {
	}

	@Override
	public String getProperty(String key) {
		if (StringUtils.isBlank(key)) {
			throw new ZebraException("ConfigSerivce.getProperty key is null");
		}

		String result = null;

		try {
			result = loadKeyFromNodeCache(key);
		} catch (Exception e) {
			LOGGER.error("get config value from zk failed", e);
		}

		return result;
	}

	private String getConfigPath(String key) {
		String pathKey = key.replace('.', Constants.DEFAULT_PATH_SEPARATOR.charAt(0));
		return Constants.DEFAULT_PATH_SEPARATOR + pathKey;
	}

	private String loadKeyFromNodeCache(final String key) throws Exception {
		NodeCache nodeCache = nodeCacheMap.get(key);

		if (nodeCache == null) {
			nodeCache = newNodeCache(key);
			nodeCacheMap.put(key, nodeCache);
		}

		byte[] data = nodeCache.getCurrentData().getData();
		return new String(data, Constants.DEFAULT_CHARSET);
	}
	
	private NodeCache newNodeCache(final String key) throws Exception {
		String path = getConfigPath(key);
		final NodeCache nodeCache = new NodeCache(client, path);
		nodeCache.getListenable().addListener(new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				String oldValue = keyMap.get(key);
				String newValue = new String(nodeCache.getCurrentData().getData());
				keyMap.put(key, newValue);
				notifyListeners(key, oldValue, newValue);
			}
		});
		nodeCache.start(true);

		keyMap.put(key, new String(nodeCache.getCurrentData().getData(), Constants.DEFAULT_CHARSET));

		return nodeCache;
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
				LOGGER.warn("fail to notify listener", e);
			}
		}
	}
}
