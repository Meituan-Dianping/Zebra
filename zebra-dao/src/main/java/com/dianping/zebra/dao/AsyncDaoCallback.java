package com.dianping.zebra.dao;

public interface AsyncDaoCallback<T> {

	void onSuccess(T result);

	void onException(Exception e);
}
