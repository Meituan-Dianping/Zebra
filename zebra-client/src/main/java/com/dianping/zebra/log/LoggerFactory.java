/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra.log;

import com.dianping.zebra.util.StringUtils;

import java.lang.reflect.Constructor;

@SuppressWarnings("rawtypes")
public class LoggerFactory {

	private static Constructor logConstructor;

	private static String DEFAULT_LOG_TYPE_PROPERTY = "zebra.log.type";

	private static String DEFAULT_LOG_DIR_PROPERTY = "zebra.log.dir";

	private static String DEFAULT_LOG_DIR = "/data/applogs/zebra";

	static {
		String dir = System.getProperty(DEFAULT_LOG_DIR_PROPERTY);
		if (StringUtils.isBlank(dir)) {
			System.setProperty(DEFAULT_LOG_DIR_PROPERTY, DEFAULT_LOG_DIR);
		}

		String logType = System.getProperty(DEFAULT_LOG_TYPE_PROPERTY);
		if (logType != null) {
			if (logType.equalsIgnoreCase("slf4j")) {
				tryImplementation("org.slf4j.Logger", "com.dianping.zebra.log.Slf4jLogger");
			} else if (logType.equalsIgnoreCase("log4j")) {
				tryImplementation("org.apache.log4j.Logger", "com.dianping.zebra.log.Log4JLogger");
			} else if (logType.equalsIgnoreCase("log4j2")) {
				tryImplementation("org.apache.logging.log4j.Logger", "com.dianping.zebra.log.Log4J2Logger");
			} else if (logType.equalsIgnoreCase("simple")) {
				tryImplementation("org.apache.logging.log4j.Logger", "com.dianping.zebra.log.SimpleLogger");
			} else if (logType.equalsIgnoreCase("nolog")) {
				try {
					logConstructor = EmptyLogger.class.getConstructor(String.class);
				} catch (Exception e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
		}

		tryImplementation("org.apache.logging.log4j.Logger", "com.dianping.zebra.log.Log4J2Logger");
		tryImplementation("org.apache.log4j.Logger", "com.dianping.zebra.log.Log4JLogger");
		tryImplementation("org.slf4j.Logger", "com.dianping.zebra.log.Slf4jLogger");

		if (logConstructor == null) {
			try {
				logConstructor = EmptyLogger.class.getConstructor(String.class);
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void tryImplementation(String testClassName, String implClassName) {
		if (logConstructor != null) {
			return;
		}

		try {
			Resources.classForName(testClassName);
			Class implClass = Resources.classForName(implClassName);
			logConstructor = implClass.getConstructor(new Class[] { String.class });

			Class<?> declareClass = logConstructor.getDeclaringClass();
			if (!Logger.class.isAssignableFrom(declareClass)) {
				logConstructor = null;
			}

			try {
				if (null != logConstructor) {
					logConstructor.newInstance(LoggerFactory.class.getName());
				}
			} catch (Throwable t) {
				logConstructor = null;
			}
		} catch (Throwable t) {
			// skip
		}
	}

	public static Logger getLogger(Class clazz) {
		return getLogger(clazz.getName());
	}

	public static Logger getLogger(String loggerName) {
		try {
			return (Logger) logConstructor.newInstance(loggerName);
		} catch (Throwable t) {
			throw new RuntimeException("Error creating LOGGER for LOGGER '" + loggerName + "'.  Cause: " + t, t);
		}
	}
}
