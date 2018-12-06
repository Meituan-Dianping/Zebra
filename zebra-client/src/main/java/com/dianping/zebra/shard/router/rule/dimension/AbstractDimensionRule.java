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
package com.dianping.zebra.shard.router.rule.dimension;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author hao.zhu
 *
 */
public abstract class AbstractDimensionRule implements DimensionRule {

	protected Set<String> shardColumns = new HashSet<String>();

	protected boolean isMaster;

	protected boolean isRange;

	public void initShardColumn(String dbRule, String tbRule) {
		Matcher matcher = RULE_COLUMN_PATTERN.matcher(dbRule);
		while (matcher.find()) {
			shardColumns.add(matcher.group(1));
		}

		if(tbRule != null && tbRule.trim().length() > 0) {
			matcher = RULE_COLUMN_PATTERN.matcher(tbRule);
			while (matcher.find()) {
				shardColumns.add(matcher.group(1));
			}
		}
	}

	@Override
	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	@Override
	public boolean isRange() {
		return isRange;
	}

	public void setRange(boolean range) {
		isRange = range;
	}
}
