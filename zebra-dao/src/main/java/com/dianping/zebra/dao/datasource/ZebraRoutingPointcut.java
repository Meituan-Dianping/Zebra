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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;

public class ZebraRoutingPointcut implements Pointcut, MethodMatcher {
	private Map<String, String> packageDataSourceKeyMap = null;

	ZebraRoutingPointcut(Map<String, String> packageDataSourceKeyMap) {
		this.packageDataSourceKeyMap = packageDataSourceKeyMap;
	}

	// ---------------------------------------------------------------------
	// Pointcut methods
	// ---------------------------------------------------------------------
	@Override
	public ClassFilter getClassFilter() {
		return ClassFilter.TRUE;
	}

	@Override
	public MethodMatcher getMethodMatcher() {
		return this;
	}

	// ---------------------------------------------------------------------
	// MethodMatcher methods
	// ---------------------------------------------------------------------
	@Override
	public boolean isRuntime() {
		return false;
	}

	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return match(method, targetClass);
	}

	@Override
	public boolean matches(Method method, Class<?> targetClass, Object... args) {
		return match(method, targetClass);
	}

	boolean match(Method method, Class<?> targetClass) {
		// origin
		if (doMatch(method, targetClass)) {
			return true;
		}
		// cglib
		Class<?> userClass = ClassUtils.getUserClass(targetClass);
		Method specificMethod = ClassUtils.getMostSpecificMethod(method, userClass);
		specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
		if (!specificMethod.equals(method)) {
			if (doMatch(specificMethod, userClass)) {
				return true;
			}
		}
		// jdk proxy
		if (Proxy.isProxyClass(targetClass)) {
			Class<?>[] interfaces = targetClass.getInterfaces();
			for (Class<?> interfaceClass : interfaces) {
				Method interfaceMethod = ClassUtils.getMethodIfAvailable(interfaceClass, method.getName(),
				      method.getParameterTypes());
				if (interfaceMethod != null && doMatch(interfaceMethod, interfaceClass)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean doMatch(Method method, Class<?> targetClass) {
		if (method.isAnnotationPresent(ZebraRouting.class) || targetClass.isAnnotationPresent(ZebraRouting.class)
		      || method.getDeclaringClass().isAnnotationPresent(ZebraRouting.class)) {
			return true;
		}
		if (packageDataSourceKeyMap != null) {
			for (String _package : packageDataSourceKeyMap.keySet()) {
				if (targetClass.getName().startsWith(_package)
				      || method.getDeclaringClass().getName().startsWith(_package)) {
					return true;
				}
			}
		}
		return false;
	}
}