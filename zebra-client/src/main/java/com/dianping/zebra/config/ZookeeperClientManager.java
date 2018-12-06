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
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.util.FileUtils;
import com.dianping.zebra.util.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ZookeeperClientManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperClientManager.class);

	protected static ZookeeperClientManager manager;

	protected static Map<String, NodeCache> nodeCacheMap = new ConcurrentHashMap<>();

	private int baseSleepTimeMills = 1000;

	private int maxRetries = 3;

	private int connectionTimeoutMs = 1000;

	private int sessionTimeoutMs = 1000;

	private String propertiesFileName;

	private CuratorFramework client = null;

	private ZookeeperClientManager() {
	}

	public static ZookeeperClientManager getInstance() {
		if (manager == null) {
			synchronized (ZookeeperClientManager.class) {
				if (manager == null) {
					manager = new ZookeeperClientManager();
					manager.init();
				}
			}
		}

		return manager;
	}

	public void init() {
		this.propertiesFileName = Constants.DEFAULT_ZK_FILENAME + ".properties";

		String zkAddr = loadZookeeperAdderss();

		try {
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMills, maxRetries);
			CuratorFramework newClient = CuratorFrameworkFactory.builder().connectString(zkAddr)
			      .sessionTimeoutMs(connectionTimeoutMs).connectionTimeoutMs(sessionTimeoutMs).retryPolicy(retryPolicy)
			      .build();
			newClient.start();
			newClient.getZookeeperClient().blockUntilConnectedOrTimedOut();

			this.client = newClient;
			LOGGER.info("DAL.Zookeeper connected");
		} catch (Exception e) {
			LOGGER.error("connect to zookeeper server failed", e);
			throw new ZebraException("connect to zookeeper server failed", e);
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

	public void close() {
		for(NodeCache nodeCache : nodeCacheMap.values()) {
			try {
				nodeCache.close();
			} catch (IOException e) {
			}
		}
		client.close();
	}

	public NodeCache getNodeCache(String path) {
		return nodeCacheMap.get(path);
	}

	public synchronized NodeCache registerNodeCache(String path) throws Exception {
		final NodeCache nodeCache = new NodeCache(client, path);
		nodeCache.start();

		nodeCacheMap.put(path, nodeCache);

		return nodeCache;
	}
}
