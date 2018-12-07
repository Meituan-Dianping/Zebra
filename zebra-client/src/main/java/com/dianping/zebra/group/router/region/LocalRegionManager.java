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
package com.dianping.zebra.group.router.region;

import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.util.NetworkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LocalRegionManager extends AbstractZebraRegionManager {

	private static final String CENTER_CONFIG_FILE = "region/center.json";

	private static final String REGION_CONFIG_FILE = "/region/idc.json";

	public LocalRegionManager() {
		super(Constants.CONFIG_MANAGER_TYPE_LOCAL, null);
	}

	public void init() {
		try {
			String centerConfig = readCenterConfigFromFile();

			parseCenterConfig(centerConfig);

			String regionConfig = null;
			InputStream in = this.getClass().getResourceAsStream(REGION_CONFIG_FILE);
			if (in == null) {
				throw new ZebraConfigException("cannot locate any region config file");
			}
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				StringBuilder buf = new StringBuilder(1024);
				String line = null;
				while ((line = reader.readLine()) != null) {
					buf.append(line);
				}
				regionConfig = buf.toString();
			} catch (Exception e) {
				throw new ZebraConfigException("failed to read region config from file: " + REGION_CONFIG_FILE, e);
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
				}
			}

			if (regionConfig != null) {
				try {
					this.regionTrie = parseRegionConfig(regionConfig);
				} catch (Exception e) {
				}
			}
			if (this.regionTrie == null) {
				this.regionTrie = new TrieNode<Integer, String>();
			}

			if (this.localAddress == null) {
				this.localAddress = NetworkUtils.IpHelper.INSTANCE.getLocalHostAddress();
			}
			if (this.localAddress != null) {
				this.localRegion = _getRegion(this.localAddress);
				this.localIdc = _getIdc(this.localAddress);
				this.localCenter = findCenterByIdc(this.localIdc);
			}
		} catch (Exception e) {
			throw new ZebraException("init ocalRegionManager file", e);
		}
	}

	private String readCenterConfigFromFile() {
		String centerConfig = null;
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(CENTER_CONFIG_FILE);
		if (in == null) {
			throw new ZebraConfigException("cannot locate any region config file");
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			StringBuilder buf = new StringBuilder(1024);
			String line = null;
			while ((line = reader.readLine()) != null) {
				buf.append(line);
			}
			centerConfig = buf.toString();
			return centerConfig;
		} catch (Exception e) {
			throw new ZebraConfigException("Failed to read router center config from file: " + CENTER_CONFIG_FILE, e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
	}

	// for test purpose
	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
		this.localRegion = _getRegion(localAddress);
		this.localIdc = _getIdc(localAddress);
		this.localCenter = findCenterByIdc(this.localIdc);
	}
}
