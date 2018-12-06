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

public class RemoteRegionManager extends AbstractZebraRegionManager {
	private volatile static RemoteRegionManager instance = null;

	public static RemoteRegionManager getInstance() {
		if (instance == null) {
			synchronized (LocalRegionManager.class) {
				if (instance == null) {
					instance = new RemoteRegionManager();
				}
			}
		}

		return instance;
	}

	// TODO customize this method to get config from remote
	private RemoteRegionManager() {
		super(Constants.CONFIG_MANAGER_TYPE_REMOTE, null);
	}

	// TODO customize this method to get config from remote
	@Override
	public boolean isInSameIdc(String address1, String address2) {
		return true;
	}

	// TODO customize this method to get config from remote
	@Override
	public boolean isInLocalRegion(String address) {
		return true;
	}

	// TODO customize this method to get config from remote
	@Override
	public boolean isInLocalIdc(String address) {
		return true;
	}

	// TODO customize this method to get config from remote
	@Override
	public String getIdc(String address) {
		return null;
	}

	// TODO customize this method to get config from remote
	@Override
	public String findCenter(String address) {
		return null;
	}

	// TODO customize this method to get config from remote
	@Override
	public boolean isInLocalCenter(String address) {
		return true;
	}

	// TODO customize this method to get config from remote
	public boolean isInSameCenter(String address1, String address2) {
		return true;
	}
}
