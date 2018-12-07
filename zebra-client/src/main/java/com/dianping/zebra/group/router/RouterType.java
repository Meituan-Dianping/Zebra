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
package com.dianping.zebra.group.router;

public enum RouterType {

	// default routerType,
	@Deprecated
	ROUND_ROBIN("round-robin"),
	MASTER_SLAVE("master-slave"), 	// new version > 2.8.3

	@Deprecated
	LOAD_BALANCE("load-balance"), 	
	SLAVE_ONLY("slave-only"), 		// new version > 2.8.3

	@Deprecated
	FAIL_OVER("fail-over"), 		
	MASTER_ONLY("master-only"); 	// new version > 2.8.3

	public static RouterType getRouterType(String type) {
		if (type.equalsIgnoreCase("load-balance") || type.equalsIgnoreCase("slave-only")) {
			return SLAVE_ONLY;
		} else if (type.equalsIgnoreCase("master-only") || type.equalsIgnoreCase("fail-over")) {
			return MASTER_ONLY;
		} else {
			return MASTER_SLAVE;
		}
	}

	private String type;

	private RouterType(String type) {
		this.type = type;
	}

	public String getRouterType() {
		return type;
	}
}
