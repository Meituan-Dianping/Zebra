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
package com.dianping.zebra.shard.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ShardDataSourceHelper {

	private static final ThreadLocal<ShardParamsHolder> SHARD_PARAMS = new ThreadLocal<ShardParamsHolder>() {
		protected ShardParamsHolder initialValue() {
			return new ShardParamsHolder();
		}
	};

	private static final ThreadLocal<Boolean> IS_GLOBAL_PARAMS = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};

	private static final ThreadLocal<Boolean> EXTRACT_PARAMS_ONLY_FROM_THREAD_LOCAL = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};

	private static final ThreadLocal<HashSet<String>> HINT_SHARD_COLUMN = new ThreadLocal<HashSet<String>>() {
		protected HashSet<String> initialValue() {
			return null;
		}
	};

	public static void setHintShardColumn(HashSet<String> shardColumns) {
		HINT_SHARD_COLUMN.set(shardColumns);
	}

	public static HashSet<String> getHintShardColumn() {
		return HINT_SHARD_COLUMN.get();
	}

	public static void clearHintShardColumn() {
		HINT_SHARD_COLUMN.remove();
	}

	public static List<Object> getShardParams(String shardColumn) {
		ShardParamsHolder holder = SHARD_PARAMS.get();

		return holder.getShardParams(shardColumn);
	}

	public static void setShardParams(String shardColumn, List<Object> params) {
		ShardParamsHolder holder = SHARD_PARAMS.get();

		holder.setShardParams(shardColumn, params);
	}

	public static void clearAllThreadLocal() {
		SHARD_PARAMS.remove();
		IS_GLOBAL_PARAMS.remove();
		EXTRACT_PARAMS_ONLY_FROM_THREAD_LOCAL.remove();
	}

	public static void setGlobalParams(boolean globalParams) {
		IS_GLOBAL_PARAMS.set(globalParams);
	}

	public static boolean isGlobalParams() {
		return IS_GLOBAL_PARAMS.get();
	}

	public static void setExtractParamsOnlyFromThreadLocal(boolean extractParamsOnlyFromThreadLocal) {
		EXTRACT_PARAMS_ONLY_FROM_THREAD_LOCAL.set(extractParamsOnlyFromThreadLocal);
	}

	public static boolean extractParamsOnlyFromThreadLocal() {
		return EXTRACT_PARAMS_ONLY_FROM_THREAD_LOCAL.get();
	}

	public static class ShardParamsHolder {
		private Map<String, List<Object>> shardParams = new HashMap<String, List<Object>>();

		public void setShardParams(String shardColumn, List<Object> params) {
			this.shardParams.put(shardColumn, params);
		}

		public List<Object> getShardParams(String shardColumn) {
			return this.shardParams.get(shardColumn);
		}
	}
}
