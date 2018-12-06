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
package com.dianping.zebra.group.config;

public class ReadOrWriteRole {

	private static final String READ_SUFFIX = "-read";

	private static final String WRITE_SUFFIX = "-write";

	private String dsName = "";

	private boolean read = false;

	private boolean write = false;

	public ReadOrWriteRole() {

	}

	public ReadOrWriteRole(String name, boolean read, boolean write) {
		this.dsName = name;
		this.read = read;
		this.write = write;
	}

	public String getDsName() {
		return dsName;
	}

	public void setDsName(String dsName) {
		this.dsName = dsName;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isWrite() {
		return write;
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	public String getReadOrWriteDsName() {
		if (read) {
			return dsName + READ_SUFFIX;
		} else if (write) {
			return dsName + WRITE_SUFFIX;
		}

		return dsName;
	}

	public String getRealDsName() {
		if (read && dsName.endsWith(READ_SUFFIX)) {
			return dsName.substring(0, dsName.length() - READ_SUFFIX.length());
		} else if (write && dsName.endsWith(WRITE_SUFFIX)) {
			return dsName.substring(0, dsName.length() - WRITE_SUFFIX.length());
		}

		return dsName;
	}
}
