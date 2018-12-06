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
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.ConfigServiceFactory;
import com.dianping.zebra.config.RemoteConfigService;
import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.util.NetworkUtils;
import com.dianping.zebra.util.json.JsonArray;
import com.dianping.zebra.util.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractZebraRegionManager implements ZebraRegionManager {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractZebraRegionManager.class);

	private static final String CENTER_CONFIG_FILE = "region/center.json";

	protected Map<String, String> centerMap = new HashMap<String, String>();

	protected Map<String, Integer> priorityMap = new HashMap<String, Integer>();

	protected String localRegion;

	protected String localCenter;

	protected String localIdc;

	protected String localAddress;

	protected String configManagerType;

	public AbstractZebraRegionManager(String configManagerType, Map<String, Object> configs) {
		this.configManagerType = configManagerType;
		this.priorityMap.put(NO_CENTER, 0);

		String centerConfig = null;
		// 如果是remote, 先从lion上获取配置, 如果为空从本地读取
		if (Constants.CONFIG_MANAGER_TYPE_LOCAL.equalsIgnoreCase(configManagerType)) {
			centerConfig = readConfigFromFile();
		} else {
			centerConfig = readConfigFromRemote(configManagerType, configs);
		}

		if (centerConfig == null) {
			throw new ZebraConfigException("Cannot find the router center config!");
		}

		try {
			parseCenterConfig(centerConfig);
		} catch (Exception e) {
		}

		this.localAddress = NetworkUtils.IpHelper.INSTANCE.getLocalHostAddress();
	}

	private String readConfigFromRemote(String configManagerType, Map<String, Object> configs) {
		try {
			ConfigService configService = ConfigServiceFactory.getConfigService(configManagerType, configs);
			String centerConfig = configService.getProperty(Constants.ROUTER_CENTER_CONFIG_LION_KEY);
			return centerConfig;
		} catch (Exception e) {
			LOGGER.warn("Read router center connfig from remote failed! " + e.getMessage());
		}
		return null;
	}

	private String readConfigFromFile() {
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

	protected void parseCenterConfig(String centerConfig) throws ParseException {
		JsonObject jsonObject = new JsonObject(centerConfig);
		JsonArray jsonArray = jsonObject.getJSONArray("regions");

		for (Object o : jsonArray.getArrayList()) {
			JsonObject regionJsonObject = (JsonObject) o;
			JsonArray centerJsonArray = (JsonArray) regionJsonObject.get("centers");

			for (Object centerObject : centerJsonArray.getArrayList()) {
				JsonObject centerJsonObject = (JsonObject) centerObject;
				String centerName = centerJsonObject.getString("center");
				int priority = centerJsonObject.getInt("priority");
				this.priorityMap.put(centerName, priority);

				JsonArray idcJsonArray = (JsonArray) centerJsonObject.get("idcs");
				for (Object idcObject : idcJsonArray.getArrayList()) {
					JsonObject idcJsonObject = (JsonObject) idcObject;
					String idcName = idcJsonObject.getString("idc");
					centerMap.put(idcName, centerName);
				}
			}
		}
	}

	@Override
	public String getLocalCenter() {
		return localCenter;
	}

	@Override
	public boolean isInLocalCenter(String address) {
		if (address == null) {
			return false;
		}
		if (address.equals(this.localAddress)) {
			return true;
		}

		// 是否是同一个idc
		String idc = getIdc(address);
		if (this.localIdc != null && this.localIdc.equals(idc)) {
			return true;
		}

		// 判断本机地址是不是不在本中心内
		if (localCenter == null || NO_CENTER.equals(localCenter)) {
			return false;
		}

		String center = findCenterByIdc(idc);
		if (NO_CENTER.equals(center)) {
			return false;
		}
		return localCenter.equalsIgnoreCase(center);
	}

	public boolean isInSameCenter(String address1, String address2) {
		if (address1 == null || address2 == null) {
			return false;
		}
		if (address1.equals(address2)) {
			return true;
		}

		String idc1 = getIdc(address1);
		String idc2 = getIdc(address2);
		if (idc1 == null || idc2 == null) {
			return false;
		}
		if (idc1.equalsIgnoreCase(idc2)) {
			return true;
		}
		String center1 = findCenterByIdc(idc1);
		if (center1 == null || NO_CENTER.equals(center1)) {
			return false;
		}
		String center2 = findCenterByIdc(idc2);
		if (center2 == null || NO_CENTER.equals(center2)) {
			return false;
		}
		return center1.equalsIgnoreCase(center2);
	}

	@Override
	public int getCenterPriority(String centerName) {
		if (centerName != null) {
			Integer priority = priorityMap.get(centerName);
			if (priority != null) {
				return priority;
			}
		}
		return 0;
	}

	public String findCenterByIdc(String idcName) {
		if (idcName == null) {
			return NO_CENTER;
		}
		String center = centerMap.get(idcName);
		return (center == null ? NO_CENTER : center);
	}
}
