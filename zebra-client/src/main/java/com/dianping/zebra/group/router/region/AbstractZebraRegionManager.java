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

import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.util.NetworkUtils;
import com.dianping.zebra.util.json.JsonArray;
import com.dianping.zebra.util.json.JsonObject;

import java.text.ParseException;
import java.util.*;

public abstract class AbstractZebraRegionManager implements ZebraRegionManager {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractZebraRegionManager.class);

	protected static String localAddress;

	protected Map<String, String> centerMap = new HashMap<String, String>();

	protected Map<String, Integer> priorityMap = new HashMap<String, Integer>();

	protected String localRegion;

	protected String localCenter;

	protected String localIdc;

	protected String routerManagerType;

	protected TrieNode<Integer, String> regionTrie;

	protected ConfigService remoteConfigServce = null;

	public AbstractZebraRegionManager(String routerManagerType, ConfigService configService) {
		this.localAddress = NetworkUtils.IpHelper.INSTANCE.getLocalHostAddress();
		this.routerManagerType = routerManagerType;
		this.remoteConfigServce = configService;
		this.priorityMap.put(NO_CENTER, 0);
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

	protected TrieNode<Integer, String> parseRegionConfig(String regionConfig) throws ParseException {
		TrieNode<Integer, String> rootNode = new TrieNode<Integer, String>();
		JsonObject jsonObject = new JsonObject(regionConfig);
		JsonArray jsonArray = jsonObject.getJSONArray("regions");
		for (Object o : jsonArray.getArrayList()) {
			JsonObject regionJsonObject = (JsonObject) o;
			String rname = regionJsonObject.getString("region");
			JsonArray idcsJsonArray = (JsonArray) regionJsonObject.get("idcs");
			for (Object idcObject : idcsJsonArray.getArrayList()) {
				JsonObject idcJsonObject = (JsonObject) idcObject;
				String iname = idcJsonObject.getString("idc");
				String desc = idcJsonObject.getString("desc");

				JsonArray netJsonArray = (JsonArray) idcJsonObject.get("net");
				for (Object netObject : netJsonArray.getArrayList()) {
					String ip = (String) netObject;
					String[] dots = ip.split("\\.");
					TrieNode<Integer, String> parentNode = rootNode;
					for (String dot : dots) {
						Integer key = Integer.valueOf(dot.trim());
						TrieNode<Integer, String> node = parentNode.getChild(key);
						if (node == null) {
							node = new TrieNode<Integer, String>(key);
							parentNode.addChild(node);
						}
						parentNode = node;
					}
					parentNode.setValue(rname);
					parentNode.setAttribute("idc", iname);
					parentNode.setAttribute("desc", desc);
				}
			}
		}
		return rootNode;
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

	public String findCenterByIdc(String idcName) {
		if (idcName == null) {
			return NO_CENTER;
		}
		String center = centerMap.get(idcName);
		return (center == null ? NO_CENTER : center);
	}

	@Override
	public boolean isInSameIdc(String address1, String address2) {
		if (address1 == null || address2 == null) {
			throw new NullPointerException("address is null");
		}
		if (address1.equals(address2)) {
			return true;
		}
		String idc1 = _getIdc(address1);
		if (idc1 == null) {
			return false;
		}
		String idc2 = _getIdc(address2);
		if (idc2 == null) {
			return false;
		}
		return idc1.equalsIgnoreCase(idc2);
	}

	@Override
	public boolean isInLocalRegion(String address) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		if (localAddress.equals(address)) {
			return true;
		}
		if (localRegion == null) {
			return false;
		}
		String region = _getRegion(address);
		return localRegion.equalsIgnoreCase(region);
	}

	public String getRegion(String address) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		return _getRegion(address);
	}

	protected String _getRegion(String address) {
		TrieNode<Integer, String> node = _getNode(address);
		return node.getValue();
	}

	@Override
	public boolean isInLocalIdc(String address) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		if (localAddress != null && localAddress.equals(address)) {
			return true;
		}
		if (localIdc == null) {
			return false;
		}
		String idc = _getIdc(address);
		return localIdc.equalsIgnoreCase(idc);
	}

	@Override
	public String getIdc(String address) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		return _getIdc(address);
	}

	protected String _getIdc(String address) {
		TrieNode<Integer, String> node = _getNode(address);
		return node.getAttribute("idc");
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

	@Override
	public String findCenter(String address) {
		String idcName = getIdc(address);
		if (idcName == null) {
			return NO_CENTER;
		}
		return findCenterByIdc(idcName);
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

	@Override
	public String getLocalCenter() {
		return this.localCenter;
	}

	private TrieNode<Integer, String> _getNode(String address) {
		if (regionTrie == null)
			return null;
		String[] dots = address.split("\\.");
		TrieNode<Integer, String> parentNode = regionTrie;
		for (String dot : dots) {
			try {
				Integer key = Integer.valueOf(dot.trim());
				TrieNode<Integer, String> node = parentNode.getChild(key);
				if (node != null) {
					parentNode = node;
				} else {
					break;
				}
			} catch (NumberFormatException nfe) {
				break;
			}
		}
		return parentNode;
	}

	public boolean isInRegion(String address, String region) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		if (region == null) {
			throw new NullPointerException("region is null");
		}
		String region2 = _getRegion(address);
		return region.equalsIgnoreCase(region2);
	}

	public boolean isInIdc(String address, String idc) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		if (idc == null) {
			throw new NullPointerException("idc is null");
		}
		String idc2 = _getIdc(address);
		return idc.equalsIgnoreCase(idc2);
	}

	public boolean isInSameRegion(String address1, String address2) {
		if (address1 == null || address2 == null) {
			throw new NullPointerException("address is null");
		}
		if (address1.equals(address2)) {
			return true;
		}
		String region1 = _getRegion(address1);
		if (region1 == null) {
			return false;
		}
		String region2 = _getRegion(address2);
		if (region2 == null) {
			return false;
		}
		return region1.equalsIgnoreCase(region2);
	}

	private List<String> _filterAddressByRegion(List<String> addressList, String region) {
		List<String> filteredAddressList = new ArrayList<String>();
		for (String address : addressList) {
			int idx = address.indexOf(':');
			String ip = idx == -1 ? address : address.substring(0, idx);
			if (region.equalsIgnoreCase(_getRegion(ip))) {
				filteredAddressList.add(address);
			}
		}
		return filteredAddressList;
	}

	private List<String> _filterAddressByRegion(String[] addressList, String region) {
		List<String> filteredAddressList = new ArrayList<String>();
		for (String address : addressList) {
			int idx = address.indexOf(':');
			String ip = idx == -1 ? address : address.substring(0, idx);
			if (region.equalsIgnoreCase(_getRegion(ip))) {
				filteredAddressList.add(address);
			}
		}
		return filteredAddressList;
	}

	public List<String> filterLocalAddress(List<String> addressList) {
		if (addressList == null) {
			throw new NullPointerException("address list is null");
		}
		if (localRegion == null) {
			return Collections.emptyList();
		}
		return _filterAddressByRegion(addressList, localRegion);
	}

	public String filterLocalAddress(String addressList) {
		if (addressList == null) {
			throw new NullPointerException("address list is null");
		}
		if (localRegion == null) {
			return null;
		}
		List<String> filteredList = _filterAddressByRegion(addressList.split(","), localRegion);
		return _toString(filteredList);
	}

	public List<String> filterAddressByRegion(List<String> addressList, String region) {
		if (addressList == null) {
			throw new NullPointerException("address list is null");
		}
		if (region == null) {
			throw new NullPointerException("region is null");
		}
		return _filterAddressByRegion(addressList, region);
	}

	public String filterAddressByRegion(String addressList, String region) {
		if (addressList == null) {
			throw new NullPointerException("address list is null");
		}
		if (region == null) {
			throw new NullPointerException("region is null");
		}
		List<String> filteredList = _filterAddressByRegion(addressList.split(","), region);
		return _toString(filteredList);
	}

	public List<String> filterAddressByAddress(List<String> addressList, String address) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		if (addressList == null) {
			throw new NullPointerException("address list is null");
		}
		String region = _getRegion(address);
		if (region == null) {
			return Collections.emptyList();
		}
		return _filterAddressByRegion(addressList, region);
	}

	public String filterAddressByAddress(String addressList, String address) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		if (addressList == null) {
			throw new NullPointerException("address list is null");
		}
		String region = _getRegion(address);
		if (region == null) {
			return null;
		}
		List<String> filteredList = _filterAddressByRegion(addressList.split(","), region);
		return _toString(filteredList);
	}

	private String _toString(List<String> addressList) {
		if (addressList == null || addressList.size() <= 0) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		result.append(addressList.get(0));
		if (addressList.size() > 1) {
			for (int i = 1; i < addressList.size(); i++) {
				result.append(',').append(addressList.get(i));
			}
		}
		return result.toString();
	}
}
