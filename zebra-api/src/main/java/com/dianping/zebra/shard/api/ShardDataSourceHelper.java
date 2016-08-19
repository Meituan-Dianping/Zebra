package com.dianping.zebra.shard.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShardDataSourceHelper {

	private static final ThreadLocal<ShardParamsHolder> SHARD_PARAMS = new ThreadLocal<ShardParamsHolder>(){
		
		protected ShardParamsHolder initialValue() {
			return new ShardParamsHolder();
		}
	};

	private static final ThreadLocal<Boolean> EXTRACT_PARAMS_ONLY_FROM_THREAD_LOCAL = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};

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
		EXTRACT_PARAMS_ONLY_FROM_THREAD_LOCAL.remove();
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
