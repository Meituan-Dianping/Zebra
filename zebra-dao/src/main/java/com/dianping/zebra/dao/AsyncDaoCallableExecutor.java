package com.dianping.zebra.dao;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class AsyncDaoCallableExecutor implements Callable<Object> {

	private Object mapper;

	private Method method;

	private Object[] args;

	public AsyncDaoCallableExecutor(Object mapper, Method method, Object[] args) {
		this.mapper = mapper;
		this.method = method;
		this.args = args;
	}

	@Override
	public Object call() throws Exception {
		return method.invoke(mapper, args);
	}
}