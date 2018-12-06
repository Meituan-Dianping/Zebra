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

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.shard.config.ExceptionalDimensionConfig;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.dianping.zebra.util.json.JsonArray;
import com.dianping.zebra.util.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ShardRuleParser {
	public static RouterRuleConfig parse(JsonObject jo) {
		RouterRuleConfig result = new RouterRuleConfig();
		List<TableShardRuleConfig> tbShardRuleConfigs = new ArrayList<TableShardRuleConfig>();
		JsonArray tableShardConfigJa = jo.getJSONArray("tableShardConfigs");

		for (int i = 0; i < tableShardConfigJa.length(); ++i) {
			JsonObject ob = tableShardConfigJa.getJSONObject(i);
			JsonArray dimensionJa = ob.getJSONArray("dimensionConfigs");
			TableShardRuleConfig tbConfig = new TableShardRuleConfig();

			tbConfig.setDimensionConfigs(dimensionParser(dimensionJa, ob.getString("tableName")));
			tbConfig.setTableName(ob.getString("tableName"));

			tbShardRuleConfigs.add(tbConfig);
		}

		result.setTableShardConfigs(tbShardRuleConfigs);

		return result;
	}

	private static List<TableShardDimensionConfig> dimensionParser(JsonArray dmsJa, String tableName) {
		List<TableShardDimensionConfig> result = new ArrayList<TableShardDimensionConfig>();

		for (int j = 0; j < dmsJa.length(); ++j) {
			TableShardDimensionConfig tbShardConfig = new TableShardDimensionConfig();
			JsonObject tbShardRule = dmsJa.getJSONObject(j);

			tbShardConfig.setTableName(tableName);
			tbShardConfig.setTbSuffix(tbShardRule.getString("tbSuffix"));
			tbShardConfig.setTbRule(tbShardRule.getString("tbRule"));
			tbShardConfig.setMaster(tbShardRule.getBoolean("isMaster"));
			tbShardConfig.setDbIndexes(tbShardRule.getString("dbIndexes"));
			tbShardConfig.setDbRule(tbShardRule.getString("dbRule"));

			try {
				JsonArray exJa = tbShardRule.getJSONArray("exceptionalDimensionConfig");
				tbShardConfig.setExceptionalDimensionConfig(exParser(exJa));
			} catch (NoSuchElementException ignore) {
			}

			try {
				tbShardConfig.setTbSuffixZeroPadding(tbShardRule.getBoolean("tbSuffixZeroPadding"));
			} catch (NoSuchElementException ignore) {
				tbShardConfig.setTbSuffixZeroPadding(false);
			}

			result.add(tbShardConfig);
		}

		return result;
	}

	private static List<ExceptionalDimensionConfig> exParser(JsonArray exJa) {
		List<ExceptionalDimensionConfig> exList = new ArrayList<ExceptionalDimensionConfig>();

		for (int k = 0; k < exJa.length(); ++k) {
			try {
				ExceptionalDimensionConfig ex = new ExceptionalDimensionConfig();
				JsonObject exJo = exJa.getJSONObject(k);

				ex.setCondition(exJo.getString("condition"));
				ex.setDb(exJo.getString("db"));
				ex.setTable(exJo.getString("table"));

				exList.add(ex);
			} catch (NoSuchElementException noe) {
				throw new ZebraConfigException("Incomplete ExceptionalDimension", noe);
			}
		}

		return exList;
	}
}
