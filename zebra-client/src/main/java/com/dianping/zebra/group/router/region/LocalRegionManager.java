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
import com.dianping.zebra.util.NetworkUtils;
import com.dianping.zebra.util.json.JsonArray;
import com.dianping.zebra.util.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalRegionManager extends AbstractZebraRegionManager {
	private static final String REGION_CONFIG_FILE = "/region/idc.json";

	private volatile static ZebraRegionManager instance = null;

	protected TrieNode<Integer, String> regionTrie;

	public static ZebraRegionManager getInstance() {
		if (instance == null) {
			synchronized (LocalRegionManager.class) {
				if (instance == null) {
					instance = new LocalRegionManager();
				}
			}
		}

		return instance;
	}

	private LocalRegionManager() {
		super(Constants.CONFIG_MANAGER_TYPE_LOCAL, null);
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

	}

	// for test purpose
	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
		this.localRegion = _getRegion(localAddress);
		this.localIdc = _getIdc(localAddress);
		this.localCenter = findCenterByIdc(this.localIdc);
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

	@Override
	public String findCenter(String address) {
		String idcName = getIdc(address);
		if (idcName == null) {
			return NO_CENTER;
		}
		return findCenterByIdc(idcName);
	}

	public String findCenterByIdc(String idcName) {
		if (idcName == null) {
			return NO_CENTER;
		}
		String center = centerMap.get(idcName);
		return (center == null ? NO_CENTER : center);
	}

	public String getLocalRegion() {
		return localRegion;
	}

	public String getLocalIdc() {
		return localIdc;
	}

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

	public String getRegion(String address) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		return _getRegion(address);
	}

	@Override
	public String getIdc(String address) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		return _getIdc(address);
	}

	protected String _getRegion(String address) {
		TrieNode<Integer, String> node = _getNode(address);
		return node.getValue();
	}

	protected String _getIdc(String address) {
		TrieNode<Integer, String> node = _getNode(address);
		return node.getAttribute("idc");
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
}
