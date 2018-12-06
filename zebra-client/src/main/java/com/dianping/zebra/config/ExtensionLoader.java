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
package com.dianping.zebra.config;

import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;
import com.dianping.zebra.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtensionLoader<T> {
	private final static Logger LOGGER = LoggerFactory.getLogger(ExtensionLoader.class);

	private final static String PREFIX = "META-INF/services/";

	private Map<String, Class<T>> extensions = new ConcurrentHashMap<>();

	private static Map<Class<?>, ExtensionLoader<?>> loaderMap = new ConcurrentHashMap<>();

	private Map<String, T> singletonMap = new ConcurrentHashMap<>();

	private Class<T> service;

	private ClassLoader loader;

	private volatile boolean init;

	private ExtensionLoader(Class<T> service) {
		this(service, Thread.currentThread().getContextClassLoader());
	}

	private ExtensionLoader(Class<T> service, ClassLoader loader) {
		this.service = service;
		this.loader = loader;
	}

	private void init() {
		this.extensions = this.loadExtensions();
		this.init = true;
	}

	public T load(String serviceName) {
		if (!init) {
			this.init();
		}

		Class<T> clazz = this.extensions.get(serviceName);

		if (clazz != null) {
			try {
				Spi spi = clazz.getAnnotation(Spi.class);
				if (spi.scope() == Scope.SINGLETON) {
					return newSingletonInstance(clazz, serviceName);
				} else {
					return clazz.newInstance();
				}
			} catch (Exception e) {
				throw new ZebraException("newInstance fail className : " + clazz.getName());
			}
		}

		return null;
	}

	private T newSingletonInstance(Class<T> clazz, String serviceName)
	      throws IllegalAccessException, InstantiationException {
		synchronized (singletonMap) {
			T singleton = singletonMap.get(serviceName);

			if (singleton == null) {
				singleton = clazz.newInstance();
				singletonMap.put(serviceName, singleton);
			}

			return singleton;
		}
	}

	public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> clazz) {
		check(clazz);
		ExtensionLoader<?> extensionLoader = loaderMap.get(clazz);

		if (extensionLoader == null) {
			synchronized(loaderMap) {
				extensionLoader = loaderMap.get(clazz);
				if(extensionLoader == null) {
					extensionLoader = newExtensionLoader(clazz);
				}
			}
		}

		return (ExtensionLoader<T>) extensionLoader;
	}

	private static <T> void check(Class<T> clz) {
		if (clz == null) {
			throw new ZebraException("class won't be null");
		}
	}

	private static <T> ExtensionLoader<T> newExtensionLoader(Class<T> clazz) {
		ExtensionLoader<?> loader = loaderMap.get(clazz);

		if (loader == null) {
			loader = new ExtensionLoader<T>(clazz);
			loaderMap.put(clazz, loader);
		}

		return (ExtensionLoader<T>) loader;
	}

	private Map<String, Class<T>> loadExtensions() {
		String configFiles = PREFIX + this.service.getName();
		List<String> classNames = null;

		try {
			Enumeration<URL> url = null;
			if (this.loader != null) {
				url = this.loader.getResources(configFiles);
			} else {
				url = ClassLoader.getSystemResources(configFiles);
			}

			while (url.hasMoreElements()) {
				URL u = url.nextElement();
				classNames = parseClassNames(u);
			}
		} catch (Exception e) {
			throw new ZebraException("loadExtensions fail, configFileName:" + configFiles, e);
		}

		return this.loadAllClasses(classNames);
	}

	private List<String> parseClassNames(URL url) {
		List<String> result = new LinkedList<>();
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			inputStream = url.openStream();
			reader = new BufferedReader(new InputStreamReader(inputStream, Constants.DEFAULT_CHARSET));

			String line = reader.readLine();
			while (line != null && line.length() > 0) {
				int annIndex = line.indexOf("#");
				if (annIndex >= 0) {
					line = line.substring(0, annIndex);
				}
				line = line.trim();

				int length = line.length();
				if (length > 0) {
					char startCh = line.charAt(0);
					if (!Character.isJavaIdentifierStart(startCh)) {
						throw new ZebraException(String.format("Syntax error: [" + line + "]"));
					}

					StringBuilder sb = new StringBuilder();
					for (char ch : line.toCharArray()) {
						if (ch == ' ' || ch == '\t' || (!Character.isJavaIdentifierPart(ch) && ch != '.')) {
							throw new ZebraException("Syntax error: [" + line + "]");
						}
						sb.append(ch);
					}

					String className = sb.toString();
					if (!result.contains(className)) {
						result.add(className);
					}

					line = reader.readLine();
				}
			}

			return result;
		} catch (Exception e) {
			throw new ZebraException("parseClassNames fail");
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}

				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private ConcurrentHashMap<String, Class<T>> loadAllClasses(List<String> classNames) {
		ConcurrentHashMap<String, Class<T>> classes = new ConcurrentHashMap<>();

		if (classNames != null && !classNames.isEmpty()) {
			for (String className : classNames) {
				try {
					Class<T> clazz = null;
					if (this.loader != null) {
						clazz = (Class<T>) Class.forName(className, false, this.loader);
					} else {
						clazz = (Class<T>) Class.forName(className);
					}

					checkClass(clazz);
					String spiName = getSpiName(clazz);
					classes.putIfAbsent(spiName, clazz);
				} catch (Exception e) {
					throw new ZebraException("load class fail :" + className);
				}
			}
		}

		return classes;
	}

	private void checkClass(Class<T> clazz) {
		if (!Modifier.isPublic(clazz.getModifiers())) {
			throw new ZebraException(String.format("class :[%s] is not public type class", clazz.getName()));
		}

		if (!service.isAssignableFrom(clazz)) {
			throw new ZebraException(
			      String.format("class :[%s] is not assignable from interface ", clazz.getName(), service.getName()));
		}

		Constructor<?>[] constructors = clazz.getConstructors();
		for (Constructor constructor : constructors) {
			if (constructor.getParameterTypes().length == 0) {
				return;
			}
		}

		throw new ZebraException(String.format("class :[%s] is has not default constructor method ", clazz.getName()));
	}

	private String getSpiName(Class<T> clazz) {
		Spi annotation = clazz.getAnnotation(Spi.class);

		if (annotation != null && StringUtils.isNotBlank(annotation.name())) {
			return annotation.name();
		}

		return clazz.getSimpleName();
	}
}
