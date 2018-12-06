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
package com.dianping.zebra.dao.datasource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZebraRoutingAdvice implements MethodInterceptor {

	private Map<String, String> packageDataSourceKeyMap = null;

	public ZebraRoutingAdvice(Map<String, String> packageDataSourceKeyMap) {
		this.packageDataSourceKeyMap = packageDataSourceKeyMap;
	}

	private static final Map<Method, String> METHOD_DATASOURCE_KEY_CACHE = new ConcurrentHashMap<Method, String>();

	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (ZebraRoutingDataSourceUtil.getCurrentDataSource() != null) {
			return invocation.proceed();
		}
		String targetDataSource = determineDataSourceKeyWitchCache(invocation);
		try {
			ZebraRoutingDataSourceUtil.setDatasource(targetDataSource);
			return invocation.proceed();
		} finally {
			ZebraRoutingDataSourceUtil.clear();
		}
	}

	private String determineDataSourceKeyWitchCache(MethodInvocation invocation) {
		String targetDataSource = null;
		Method method = invocation.getMethod();
		if (METHOD_DATASOURCE_KEY_CACHE.containsKey(method)) {
			targetDataSource = METHOD_DATASOURCE_KEY_CACHE.get(method);
		}
		// method level
		if (method.getAnnotation(ZebraRouting.class) != null) {
			targetDataSource = method.getAnnotation(ZebraRouting.class).value();
		}
		// class level
		if (targetDataSource == null && method.getDeclaringClass().getAnnotation(ZebraRouting.class) != null) {
			targetDataSource = method.getDeclaringClass().getAnnotation(ZebraRouting.class).value();
		}
		// package level
		if (targetDataSource == null && packageDataSourceKeyMap != null) {
			// exact mapping ,tackle different package has same prefix ,eg :xx.zebra,xx.zebra_ut
			String packageName = method.getDeclaringClass().getPackage().getName();
			targetDataSource = packageDataSourceKeyMap.get(packageName);
			// line lookup mapping
			if (targetDataSource == null) {
				for (Map.Entry<String, String> entry : packageDataSourceKeyMap.entrySet()) {
					if (packageName.startsWith(entry.getKey())) {
						targetDataSource = entry.getValue();
						break;
					}
				}
			}
		}
		if (targetDataSource != null) {
			METHOD_DATASOURCE_KEY_CACHE.put(method, targetDataSource);
		}
		return targetDataSource;
	}
}
