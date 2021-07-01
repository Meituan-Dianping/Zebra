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

/**
 * Created by Dozer on 8/1/14.
 */
public class JdbcDriverClassHelper {

	public static String getDriverClassNameByJdbcUrl(String url) {
		if (url.startsWith("jdbc:mysql:")) {
			return "com.mysql.jdbc.Driver";
		} else if (url.startsWith("jdbc:postgresql:")) {
			return "org.postgresql.Driver";
		} else if (url.startsWith("jdbc:sqlserver:")) {
			return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		} else if (url.startsWith("jdbc:jtds:sqlserver:")) {
			return "net.sourceforge.jtds.jdbc.Driver";
		} else if (url.startsWith("jdbc:h2:")) {
			return "org.h2.Driver";
		} else if(url.startsWith("jdbc:kylin:")) {
			return "org.apache.kylin.jdbc.Driver";
		} else if (url.startsWith("jdbc:oracle:")) {
			return "oracle.jdbc.driver.OracleDriver";
		} else {
			return "";
		}
	}

	public static void loadDriverClass(String driverClass, String url) throws ZebraConfigException {
		try {
			Class.forName(StringUtils.isEmpty(driverClass) ? getDriverClassNameByJdbcUrl(url) : driverClass);
		} catch (ClassNotFoundException e) {
			throw new ZebraConfigException("Cannot find driver class : " + url, e);
		}
	}
}
