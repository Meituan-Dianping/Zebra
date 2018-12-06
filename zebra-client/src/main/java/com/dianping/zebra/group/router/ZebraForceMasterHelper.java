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
package com.dianping.zebra.group.router;

public class ZebraForceMasterHelper {

	/**
	 * 使用本地的context来设置走主库，该方式只会影响本应用内本次请求的所有sql走主库，不会影响到piegon后端服务。
	 * 调用过该方法后，一定要在请求的末尾调用clearLocalContext进行清理操作。
	 * 优先级比forceMasterInPiegonContext低。
	 */
	public static void forceMasterInLocalContext() {
		LocalContextReadWriteStrategy.setReadFromMaster();
	}

	/**
	 * 配合forceMasterInLocalContext进行使用，在请求的末尾调用该方法，对LocalContext进行清理。
	 */
	public static void clearLocalContext() {
		LocalContextReadWriteStrategy.clearContext();
	}
}
