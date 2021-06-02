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
package com.dianping.zebra.dao;

import com.dianping.zebra.group.util.DaoContextHolder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class AsyncDaoCallableExecutor implements Callable<Object> {

	private Class<?> mapperInterface;

	private Object mapper;

	private Method method;

	private Object[] args;

	public AsyncDaoCallableExecutor(Class<?> mapperInterface, Object mapper, Method method, Object[] args) {
		super();
		this.mapperInterface = mapperInterface;
		this.mapper = mapper;
		this.method = method;
		this.args = args;
	}

	@Override
	public Object call() throws Exception {
		try {
			DaoContextHolder.setSqlName(mapperInterface.getSimpleName() + "." + method.getName());
			return method.invoke(mapper, args);
		} catch (InvocationTargetException e) {
			throw (Exception) e.getCause();
		} finally {
			DaoContextHolder.clearSqlName();
		}
	}
}
