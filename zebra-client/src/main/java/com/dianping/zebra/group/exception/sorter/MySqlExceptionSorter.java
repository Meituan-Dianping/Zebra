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
package com.dianping.zebra.group.exception.sorter;

import java.sql.SQLException;

public class MySqlExceptionSorter implements ExceptionSorter {
	private static final int MAX_LOOP_TIMES = 20;

	@Override
	public boolean isExceptionFatal(final SQLException e) {
		int i = 0;
		Throwable cause = e;
		while (cause != null && i < MAX_LOOP_TIMES) {
			if (!(cause instanceof SQLException)) {
				i++;
				cause = cause.getCause();
				continue;
			}
			String sqlState = ((SQLException) cause).getSQLState();
			if (sqlState != null && sqlState.startsWith("08")) {
				return true;
			}
			int errorCode = ((SQLException) cause).getErrorCode();
			switch (errorCode) {
			// Communications Errors
			case 1040: // ER_CON_COUNT_ERROR 已到达数据库的最大连接数，请加大数据库可用连接数
			case 1042: // ER_BAD_HOST_ERROR 无效的主机名
			case 1043: // ER_HANDSHAKE_ERROR 无效连接
			case 1047: // ER_UNKNOWN_COM_ERROR
			case 1081: // ER_IPSOCK_ERROR 不能建立Socket连接
			case 1129: // ER_HOST_IS_BLOCKED 数据库出现异常，请重启数据库
			case 1130: // ER_HOST_NOT_PRIVILEGED
				// Authentication Errors
			case 1045: // ER_ACCESS_DENIED_ERROR 不能连接数据库，用户名或密码错误
				// Resource errors
			case 1004: // ER_CANT_CREATE_FILE 创建文件失败
			case 1005: // ER_CANT_CREATE_TABLE 创建表失败
			case 1015: // ER_CANT_LOCK Can't lock file
			case 1021: // ER_DISK_FULL 硬盘剩余空间不足，请加大硬盘可用空间
			case 1041: // ER_OUT_OF_RESOURCES 系统内存不足,check if mysqld or some
				// other process uses all available memory; if not, you
				// may
				// have to use 'ulimit' to allow mysqld to use more memory or
				// you can add more swap space
				// Out-of-memory errors
			case 1037: // ER_OUTOFMEMORY 系统内存不足，请重启数据库或重启服务器
			case 1038: // ER_OUT_OF_SORTMEMORY 用于排序的内存不足，请增大排序缓冲区
				return true;
			default:
				break;
			}
			String className = cause.getClass().getName();
			if ("com.mysql.jdbc.CommunicationsException".equals(className)) {
				return true;
			}
			// c3p0
			if ("Connections could not be acquired from the underlying database!".equals(e.getMessage())) {
				return true;
			}
			i++;
			cause = cause.getCause();
		}
		return false;
	}
}
