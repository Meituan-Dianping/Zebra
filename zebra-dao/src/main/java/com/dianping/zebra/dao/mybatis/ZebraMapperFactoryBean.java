package com.dianping.zebra.dao.mybatis;

import static org.springframework.util.Assert.notNull;

import java.lang.reflect.Proxy;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.FactoryBean;

import com.dianping.zebra.dao.AsyncMapperProxy;

public class ZebraMapperFactoryBean<T> extends SqlSessionDaoSupport implements FactoryBean<T> {

	private Class<T> mapperInterface;

	private boolean addToConfig = true;

	/**
	 * Sets the mapper interface of the MyBatis mapper
	 *
	 * @param mapperInterface
	 *           class of the interface
	 */
	public void setMapperInterface(Class<T> mapperInterface) {
		this.mapperInterface = mapperInterface;
	}

	/**
	 * If addToConfig is false the mapper will not be added to MyBatis. This means it must have been included in mybatis-config.xml.
	 * <p>
	 * If it is true, the mapper will be added to MyBatis in the case it is not already registered.
	 * <p>
	 * By default addToCofig is true.
	 *
	 * @param addToConfig
	 */
	public void setAddToConfig(boolean addToConfig) {
		this.addToConfig = addToConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkDaoConfig() {
		super.checkDaoConfig();

		notNull(this.mapperInterface, "Property 'mapperInterface' is required");

		Configuration configuration = getSqlSession().getConfiguration();
		if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
			try {
				configuration.addMapper(this.mapperInterface);
			} catch (Throwable t) {
				logger.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", t);
				throw new IllegalArgumentException(t);
			} finally {
				ErrorContext.instance().reset();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
   public T getObject() throws Exception {
		T mapper = getSqlSession().getMapper(this.mapperInterface);
		
		AsyncMapperProxy<T> asyncMapperProxy = new AsyncMapperProxy<T>(mapper);
		
		return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, asyncMapperProxy);
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<T> getObjectType() {
		return this.mapperInterface;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSingleton() {
		return true;
	}

}
