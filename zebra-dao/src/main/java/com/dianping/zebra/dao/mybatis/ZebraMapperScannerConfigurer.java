package com.dianping.zebra.dao.mybatis;

import static org.springframework.util.Assert.notNull;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import com.dianping.zebra.dao.AsyncMapperExecutor;

public class ZebraMapperScannerConfigurer
		implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {

	private String basePackage;

	private boolean addToConfig = true;

	private SqlSessionFactory sqlSessionFactory;

	private SqlSessionTemplate sqlSessionTemplate;

	private String sqlSessionFactoryBeanName;

	private String sqlSessionTemplateBeanName;

	private Class<? extends Annotation> annotationClass;

	private Class<?> markerInterface;

	private ApplicationContext applicationContext;

	private String beanName;

	private boolean processPropertyPlaceHolders;

	private BeanNameGenerator nameGenerator;

	private int corePoolSize = 20;

	private int maxPoolSize = 200;

	private int queueSize = 500;

	/**
	 * This property lets you set the base package for your mapper interface
	 * files.
	 * <p>
	 * You can set more than one package by using a semicolon or comma as a
	 * separator.
	 * <p>
	 * Mappers will be searched for recursively starting in the specified
	 * package(s).
	 *
	 * @param basePackage
	 *            base package name
	 */
	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	/**
	 * Same as {@code MapperFactoryBean#setAddToConfig(boolean)}.
	 *
	 * @param addToConfig
	 * @see MapperFactoryBean#setAddToConfig(boolean)
	 */
	public void setAddToConfig(boolean addToConfig) {
		this.addToConfig = addToConfig;
	}

	/**
	 * This property specifies the annotation that the scanner will search for.
	 * <p>
	 * The scanner will register all interfaces in the base package that also
	 * have the specified annotation.
	 * <p>
	 * Note this can be combined with markerInterface.
	 *
	 * @param annotationClass
	 *            annotation class
	 */
	public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	/**
	 * This property specifies the parent that the scanner will search for.
	 * <p>
	 * The scanner will register all interfaces in the base package that also
	 * have the specified interface class as a parent.
	 * <p>
	 * Note this can be combined with annotationClass.
	 *
	 * @param superClass
	 *            parent class
	 */
	public void setMarkerInterface(Class<?> superClass) {
		this.markerInterface = superClass;
	}

	/**
	 * Specifies which {@code SqlSessionTemplate} to use in the case that there
	 * is more than one in the spring context. Usually this is only needed when
	 * you have more than one datasource.
	 * <p>
	 * Use {@link #setSqlSessionTemplateBeanName(String)} instead
	 *
	 * @param sqlSessionTemplate
	 */
	@Deprecated
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	/**
	 * Specifies which {@code SqlSessionTemplate} to use in the case that there
	 * is more than one in the spring context. Usually this is only needed when
	 * you have more than one datasource.
	 * <p>
	 * Note bean names are used, not bean references. This is because the
	 * scanner loads early during the start process and it is too early to build
	 * mybatis object instances.
	 *
	 * @since 1.1.0
	 *
	 * @param sqlSessionTemplateName
	 *            Bean name of the {@code SqlSessionTemplate}
	 */
	public void setSqlSessionTemplateBeanName(String sqlSessionTemplateName) {
		this.sqlSessionTemplateBeanName = sqlSessionTemplateName;
	}

	/**
	 * Specifies which {@code SqlSessionFactory} to use in the case that there
	 * is more than one in the spring context. Usually this is only needed when
	 * you have more than one datasource.
	 * <p>
	 * Use {@link #setSqlSessionFactoryBeanName(String)} instead.
	 *
	 * @param sqlSessionFactory
	 */
	@Deprecated
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}

	/**
	 * Specifies which {@code SqlSessionFactory} to use in the case that there
	 * is more than one in the spring context. Usually this is only needed when
	 * you have more than one datasource.
	 * <p>
	 * Note bean names are used, not bean references. This is because the
	 * scanner loads early during the start process and it is too early to build
	 * mybatis object instances.
	 *
	 * @since 1.1.0
	 *
	 * @param sqlSessionFactoryName
	 *            Bean name of the {@code SqlSessionFactory}
	 */
	public void setSqlSessionFactoryBeanName(String sqlSessionFactoryName) {
		this.sqlSessionFactoryBeanName = sqlSessionFactoryName;
	}

	/**
	 *
	 * @since 1.1.1
	 *
	 * @param processPropertyPlaceHolders
	 */
	public void setProcessPropertyPlaceHolders(boolean processPropertyPlaceHolders) {
		this.processPropertyPlaceHolders = processPropertyPlaceHolders;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBeanName(String name) {
		this.beanName = name;
	}

	/**
	 * Gets beanNameGenerator to be used while running the scanner.
	 *
	 * @return the beanNameGenerator BeanNameGenerator that has been configured
	 * @since 1.2.0
	 */
	public BeanNameGenerator getNameGenerator() {
		return nameGenerator;
	}

	/**
	 * Sets beanNameGenerator to be used while running the scanner.
	 *
	 * @param nameGenerator
	 *            the beanNameGenerator to set
	 * @since 1.2.0
	 */
	public void setNameGenerator(BeanNameGenerator nameGenerator) {
		this.nameGenerator = nameGenerator;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		notNull(this.basePackage, "Property 'basePackage' is required");

		AsyncMapperExecutor.init(corePoolSize, maxPoolSize, queueSize);
	}

	/**
	 * {@inheritDoc}
	 */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// left intentionally blank
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(String corePoolSize) {
		this.corePoolSize = Integer.parseInt(corePoolSize);
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(String maxPoolSize) {
		this.maxPoolSize = Integer.parseInt(maxPoolSize);
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(String queueSize) {
		this.queueSize = Integer.parseInt(queueSize);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @since 1.0.2
	 */
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		if (this.processPropertyPlaceHolders) {
			processPropertyPlaceHolders();
		}

		ZebraClassPathMapperScanner scanner = new ZebraClassPathMapperScanner(registry);
		scanner.setAddToConfig(this.addToConfig);
		scanner.setAnnotationClass(this.annotationClass);
		scanner.setMarkerInterface(this.markerInterface);
		scanner.setSqlSessionFactory(this.sqlSessionFactory);
		scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
		scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
		scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
		scanner.setResourceLoader(this.applicationContext);
		scanner.setBeanNameGenerator(this.nameGenerator);
		scanner.registerFilters();
		scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage,
				ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
	}

	/*
	 * BeanDefinitionRegistries are called early in application startup, before
	 * BeanFactoryPostProcessors. This means that PropertyResourceConfigurers
	 * will not have been loaded and any property substitution of this class'
	 * properties will fail. To avoid this, find any PropertyResourceConfigurers
	 * defined in the context and run them on this class' bean definition. Then
	 * update the values.
	 */
	private void processPropertyPlaceHolders() {
		Map<String, PropertyResourceConfigurer> prcs = applicationContext
				.getBeansOfType(PropertyResourceConfigurer.class);

		if (!prcs.isEmpty() && applicationContext instanceof GenericApplicationContext) {
			BeanDefinition mapperScannerBean = ((GenericApplicationContext) applicationContext).getBeanFactory()
					.getBeanDefinition(beanName);

			// PropertyResourceConfigurer does not expose any methods to
			// explicitly perform
			// property placeholder substitution. Instead, create a BeanFactory
			// that just
			// contains this mapper scanner and post process the factory.
			DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
			factory.registerBeanDefinition(beanName, mapperScannerBean);

			for (PropertyResourceConfigurer prc : prcs.values()) {
				prc.postProcessBeanFactory(factory);
			}

			PropertyValues values = mapperScannerBean.getPropertyValues();

			this.basePackage = updatePropertyValue("basePackage", values);
			this.sqlSessionFactoryBeanName = updatePropertyValue("sqlSessionFactoryBeanName", values);
			this.sqlSessionTemplateBeanName = updatePropertyValue("sqlSessionTemplateBeanName", values);
		}
	}

	private String updatePropertyValue(String propertyName, PropertyValues values) {
		PropertyValue property = values.getPropertyValue(propertyName);

		if (property == null) {
			return null;
		}

		Object value = property.getValue();

		if (value == null) {
			return null;
		} else if (value instanceof String) {
			return value.toString();
		} else if (value instanceof TypedStringValue) {
			return ((TypedStringValue) value).getValue();
		} else {
			return null;
		}
	}
}
