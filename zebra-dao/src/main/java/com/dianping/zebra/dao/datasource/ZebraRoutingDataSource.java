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

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ZebraRoutingDataSource extends AbstractAutoProxyCreator
      implements InitializingBean, DataSource, PointcutAdvisor {
	private Map<String, String> packageDataSourceKeyMap = new ConcurrentHashMap<String, String>();

	private ZebraRoutingPointcut pointcut = new ZebraRoutingPointcut(packageDataSourceKeyMap);

	private Advice advice = new ZebraRoutingAdvice(packageDataSourceKeyMap);

	private Map<String, DataSource> targetDataSources;

	private Object defaultTargetDataSource;

	private DataSource resolvedDefaultTargetDataSource;

	private Map<String, Advised> advisedMap = new ConcurrentHashMap<String, Advised>();

	// ---------------------------------------------------------------------
	// PointcutAdvisor methods
	// ---------------------------------------------------------------------
	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}

	@Override
	public Advice getAdvice() {
		return advice;
	}

	@Override
	public boolean isPerInstance() {
		return false;
	}

	// ---------------------------------------------------------------------
	// AbstractAutoProxyCreator methods
	// ---------------------------------------------------------------------
	@Override
	protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
		if (bean instanceof Advised) {
			advisedMap.put(beanName, (Advised) bean);
		}
		return super.wrapIfNecessary(bean, beanName, cacheKey);
	}

	@Override
	protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource customTargetSource)
	      throws BeansException {
		Method[] declaredMethods = beanClass.getDeclaredMethods();
		for (Method declaredMethod : declaredMethods) {
			if (pointcut.match(declaredMethod, beanClass)) {
				Advised advised = this.advisedMap.get(beanName);
				if (advised != null) {
					Advisor[] advisors = advised.getAdvisors();
					boolean added = false;
					for (int i = 0; i < advisors.length; i++) {
						if (advisors[i].getAdvice() instanceof TransactionInterceptor) {
							advised.addAdvisor(i, this);
							added = true;
							break;
						}
					}
					if (!added) {
						advised.addAdvisor(this);
					}
					return DO_NOT_PROXY;
				}
				return new Object[] { this };
			}
		}
		return DO_NOT_PROXY;
	}

	// ---------------------------------------------------------------------
	// InitializingBean methods
	// ---------------------------------------------------------------------
	@Override
	public void afterPropertiesSet() {
		assert targetDataSources != null : "property targetDataSources can't be null";
		if (defaultTargetDataSource != null) {
			if (defaultTargetDataSource instanceof DataSource) {
				this.resolvedDefaultTargetDataSource = (DataSource) defaultTargetDataSource;
				return;
			}
			if (defaultTargetDataSource instanceof String) {
				DataSource dataSource = targetDataSources.get(defaultTargetDataSource);
				if (dataSource != null) {
					this.resolvedDefaultTargetDataSource = dataSource;
					return;
				}
				String defaultDataSourceBeanName = (String) this.defaultTargetDataSource;
				this.resolvedDefaultTargetDataSource = getBeanFactory().getBean(defaultDataSourceBeanName,
				      DataSource.class);
			}
			throw new IllegalArgumentException(
			      "defaultTargetDataSource must be instance of DataSource or the bean " + "name of a DataSource");
		}
	}

	// ---------------------------------------------------------------------
	// DataSource methods
	// ---------------------------------------------------------------------
	/**
	 * Returns 0, indicating the default system timeout is to be used.
	 */
	public int getLoginTimeout() {
		return 0;
	}

	public void setLoginTimeout(int timeout) {
		throw new UnsupportedOperationException("setLoginTimeout");
	}

	public PrintWriter getLogWriter() {
		throw new UnsupportedOperationException("getLogWriter");
	}

	public void setLogWriter(PrintWriter pw) {
		throw new UnsupportedOperationException("setLogWriter");
	}

	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return (T) this;
		}
		return determineTargetDataSource().unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
	}

	public Logger getParentLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}

	public Connection getConnection() throws SQLException {
		return determineTargetDataSource().getConnection();
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return determineTargetDataSource().getConnection(username, password);
	}

	private DataSource determineTargetDataSource() {
		String lookupKey = ZebraRoutingDataSourceUtil.getCurrentDataSource();
		DataSource dataSource = this.targetDataSources.get(lookupKey);
		if (dataSource == null) {
			dataSource = this.resolvedDefaultTargetDataSource;
		}
		return dataSource;
	}

	// ---------------------------------------------------------------------
	// setters and getters
	// ---------------------------------------------------------------------
	public Map<String, DataSource> getTargetDataSources() {
		return targetDataSources;
	}

	public void setTargetDataSources(Map<String, DataSource> targetDataSources) {
		this.targetDataSources = targetDataSources;
	}

	public Object getDefaultTargetDataSource() {
		return defaultTargetDataSource;
	}

	public void setDefaultTargetDataSource(Object defaultTargetDataSource) {
		this.defaultTargetDataSource = defaultTargetDataSource;
	}

	public Map<String, String> getPackageDataSourceKeyMap() {
		return packageDataSourceKeyMap;
	}

	public void setPackageDataSourceKeyMap(Map<String, String> packageDataSourceKeyMap) {
		assert packageDataSourceKeyMap != null;
		this.packageDataSourceKeyMap.putAll(packageDataSourceKeyMap);
	}
}
