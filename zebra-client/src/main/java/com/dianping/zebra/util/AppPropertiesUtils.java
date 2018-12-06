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
package com.dianping.zebra.util;

import com.dianping.zebra.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class AppPropertiesUtils {

	public static String getAppName() {
		URL appProperties = AppPropertiesUtils.class.getResource("/app.properties");

		if (appProperties != null) {
			InputStream in = null;
			try {
				in = appProperties.openStream();
				Properties properties = new Properties();
				properties.load(in);

				String appName = (String) properties.get("app.name");

				if (appName != null) {
					return appName;
				} else {
					return Constants.APP_NO_NAME;
				}
			} catch (IOException ignore) {
				return Constants.APP_NO_NAME;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
						// ignore it
					}
				}
			}
		} else {
			return Constants.APP_NO_NAME;
		}
	}
}
