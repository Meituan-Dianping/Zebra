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

public class AsyncDaoRunnableExecutor<T> implements Runnable {

	private AsyncDaoCallback<T> callback;

	private Class<?> mapperInterface;

	private Object mapper;

	private Method method;

	private Object[] args;

	public AsyncDaoRunnableExecutor(Class<?> mapperInterface, Object mapper, Method method, Object[] args,
	      AsyncDaoCallback<T> callback) {
		super();
		this.mapperInterface = mapperInterface;
		this.mapper = mapper;
		this.method = method;
		this.args = args;
		this.callback = callback;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		T result = null;
		boolean success = false;
		try {
			DaoContextHolder.setSqlName(mapperInterface.getSimpleName() + "." + method.getName());
			result = (T) method.invoke(mapper, args);
			success = true;
		} catch (InvocationTargetException e) {
			callback.onException((Exception) e.getCause());
		} catch (Exception e) {
			// 仅仅捕获数据库访问异常
			callback.onException(e);
		} finally {
			DaoContextHolder.clearSqlName();
		}

		if (success) {
			// 不要捕获在onSuccess中业务逻辑自身的异常
			callback.onSuccess(result);
		}
	}
}