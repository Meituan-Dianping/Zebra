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

/**
 * @author kezhu.wu
 */
public enum SqlType {
	SELECT(true, true, false, 0), //
	INSERT(false, false, true, 1), //
	UPDATE(false, false, true, 2), //
	DELETE(false, false, true, 3), //
	SELECT_FOR_UPDATE(false, true, true, 4), //
	REPLACE(false, false, true, 5), //
	TRUNCATE(false, false, true, 6), //
	CREATE(false, false, true, 7), //
	DROP(false, false, true, 8), //
	LOAD(false, false, true, 9), //
	MERGE(false, false, true, 10), //
	SHOW(true, true, false, 11), //
	EXECUTE(false, false, true, 12), //
	SELECT_FOR_IDENTITY(false, true, false, 13), //
	EXPLAIN(true, true, false, 14), //
	ALTER(false, false, true, 15), //
	UNKNOWN_SQL_TYPE(false, false, true, -100); //

	private boolean isRead;

	private boolean isQuery;

	private boolean isWrite;

	private int i;

	SqlType(boolean isRead, boolean isQuery, boolean isWrite, int i) {
		this.isRead = isRead;
		this.isQuery = isQuery;
		this.isWrite = isWrite;
		this.i = i;
	}

	public int value() {
		return this.i;
	}

	public boolean isRead() {
		return isRead;
	}

	public boolean isQuery() {
		return isQuery;
	}

	public boolean isWrite() {
		return isWrite;
	}

	public static SqlType valueOf(int i) {
		for (SqlType t : values()) {
			if (t.value() == i) {
				return t;
			}
		}
		throw new IllegalArgumentException("Invalid SqlType:" + i);
	}
}
