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
package com.dianping.zebra.shard.util;

import java.util.Random;

/**
 * 不支持int型 ，int最大为：20亿+，如果最后四位被占用，最大20W+，这个量不需要进行分库分表 Created by tianshouzhi on
 * 2017/7/7.
 */
public class IdDecorator {
	private static Random dbRandom = new Random();

	private static Random tbRandom = new Random();

	private static final String ZERO = "0";

	// xxx{00}{00}
	public static synchronized Long decorate100(Long id, Integer realDbNum, Integer realTbNum) {
		if (id == null || realDbNum == null || realTbNum == null) {
			throw new NullPointerException();
		}
		if (realDbNum > 100 || realTbNum > 100) {
			throw new IllegalArgumentException("'realDbNum' and 'realTbNum' must less than 100");
		}
		StringBuilder resultId = new StringBuilder(10);
		resultId.append(id);
		int db = Math.abs(dbRandom.nextInt(realDbNum));
		if (db < 10) {
			resultId.append(ZERO);
		}
		resultId.append(db);
		int tb = Math.abs(tbRandom.nextInt(realTbNum));
		if (tb < 10) {
			resultId.append(ZERO);
		}
		resultId.append(tb);
		return Long.parseLong(resultId.toString());
	}

	// xxx{0}{0}
	public static synchronized Long decorate10(Long id, Integer realDbNum, Integer realTbNum) {
		if (id == null || realDbNum == null || realTbNum == null) {
			throw new NullPointerException();
		}
		if (realDbNum > 10 || realTbNum > 10) {
			throw new IllegalArgumentException("'realDbNum' and 'realTbNum' must less than 10");
		}
		StringBuilder resultId = new StringBuilder(10);
		resultId.append(id);
		int db = Math.abs(dbRandom.nextInt(realDbNum));
		resultId.append(db);
		int tb = Math.abs(tbRandom.nextInt(realTbNum));
		resultId.append(tb);
		return Long.parseLong(resultId.toString());
	}

}