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

import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Spi(name = "demo", scope = Scope.SINGLETON)
public class RemoteConfigService implements ConfigService {

	protected static final Logger logger = LoggerFactory.getLogger(RemoteConfigService.class);

	protected static volatile boolean init = false;

	private List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

	// TODO customize this method to get config from remote
	@Override
	public String getProperty(String key) {
		return null;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

	// TODO customize this method
	@Override
	public void init(Map<String, Object> serviceConfigs) {
		this.init = true;
	}

	// TODO customize this method
	@Override
	public void destroy() {
	}
}
