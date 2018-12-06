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

import com.dianping.zebra.exception.ZebraConfigException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public final class FileUtils {

	public static File getFile(String fileName) {
		URL propUrl = FileUtils.class.getClassLoader().getResource(fileName);

		if (propUrl != null) {
			return toFile(propUrl);
		} else {
			throw new ZebraConfigException(String.format("config file[%s] doesn't exist.", fileName));
		}
	}

	public static Properties loadProperties(File file) {
		Properties prop = new Properties();

		if (file != null) {
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
				prop.load(inputStream);
			} catch (Exception e) {
				throw new ZebraConfigException(String.format("fail to read properties file[%s]", file.getName()), e);
			} finally {
				closeQuietly(inputStream);
			}
		}

		return prop;
	}

	public static File toFile(URL url) {
		if (url == null || !url.getProtocol().equals("file")) {
			return null;
		} else {
			String filename = url.getFile().replace('/', File.separatorChar);
			int pos = 0;
			while ((pos = filename.indexOf('%', pos)) >= 0) {
				if (pos + 2 < filename.length()) {
					String hexStr = filename.substring(pos + 1, pos + 3);
					char ch = (char) Integer.parseInt(hexStr, 16);
					filename = filename.substring(0, pos) + ch + filename.substring(pos + 3);
				}
			}
			return new File(filename);
		}
	}

	public static void closeQuietly(InputStream input) {
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}
}
