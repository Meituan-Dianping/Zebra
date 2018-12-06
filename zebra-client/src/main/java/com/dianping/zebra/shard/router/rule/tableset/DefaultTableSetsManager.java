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
package com.dianping.zebra.shard.router.rule.tableset;

import java.util.*;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.util.StringUtils;

/**
 * @author hao.zhu
 */
public class DefaultTableSetsManager implements TableSetsManager {

	public static final String TB_SUFFIX_STYLE_ALL = "alldb";

	public static final String TB_SUFFIX_STYLE_EVERY = "everydb";

	private final String tableName;

	private final String dbIndexes;

	private final String tbSuffix;

	private boolean tbSuffixZeraPadding = false;

	private List<TableSets> tableMappings = new ArrayList<TableSets>();

	public DefaultTableSetsManager(String tableName, String dbIndexes, String tbSuffix) {
		this(tableName, dbIndexes, tbSuffix, false);
	}

	public DefaultTableSetsManager(String tableName, String dbIndexes, String tbSuffix, boolean tbSuffixZeraPadding) {
		this.tableName = tableName;
		this.dbIndexes = dbIndexes;
		this.tbSuffix = tbSuffix;
		this.tbSuffixZeraPadding = tbSuffixZeraPadding;
		this.init();
	}

	private void init() {
		List<String> dbs = splitDb(dbIndexes);
		for (String db : dbs) {
			String jdbcRef = db.trim();

			TableSets mapping = new TableSets(jdbcRef);
			tableMappings.add(mapping);
		}

		setTables2DataSource(tableMappings);
	}

	@Override
	public TableSets getTableSetsByPos(int dbPos) {
		return tableMappings.get(dbPos);
	}

	@Override
	public Map<String, Set<String>> getAllTableSets() {
		Map<String, Set<String>> dbAndTables = new HashMap<String, Set<String>>();
		for (TableSets dataSourceBO : tableMappings) {
			String db = dataSourceBO.getDbIndex();
			if (!dbAndTables.containsKey(db)) {
				dbAndTables.put(db, new LinkedHashSet<String>());
			}
			Set<String> tableSet = dbAndTables.get(db);
			for (String physicalTable : dataSourceBO.getTableSets()) {
				tableSet.add(physicalTable);
			}
		}
		return dbAndTables;
	}

	private List<String> splitDb(String dbIndexes) {
		List<String> result = new ArrayList<String>();
		String[] dbConfig = dbIndexes.split(",");
		for (String config : dbConfig) {
			if (config.contains("[") && config.contains("]") && config.contains("-")) {
				String suffix = config.substring(0, config.indexOf("["));
				String perfix = config.substring(config.indexOf("]") + 1, config.length());
				String tbSuffixRange = StringUtils.substringBetween(config, "[", "]");
				String[] ranges = tbSuffixRange.split("-");
				int startNum = Integer.parseInt(ranges[0]);
				int endNum = Integer.parseInt(ranges[1]);
				for (int k = startNum; k <= endNum; k++) {
					result.add(suffix + k + perfix);
				}
			} else {
				result.add(config);
			}
		}
		return result;
	}

	/**
	 * modify for new table naming rule rule 1: alldb and everydb rule 2: jdbcRef[xx,xx,xx] (list all suffix, jdbcRef cannot start
	 * with "alldb" or "everydb") rule 3: rule1&rule2 (&: separator)
	 */
	private void setTables2DataSource(List<TableSets> dataSourceBOs) {
		if (tbSuffix != null) {
			String[] tableRules = tbSuffix.split("&");
			if (tableRules.length > 0) {
				for (String rule : tableRules) {
					rule = rule.trim();
					if (rule.length() > 0) {
						if (rule.startsWith(TB_SUFFIX_STYLE_ALL) || rule.startsWith(TB_SUFFIX_STYLE_EVERY)) {
							parseTableNamingRule1(dataSourceBOs, rule);
						} else {
							parseTableNamingRule2(dataSourceBOs, rule);
						}
					}
				}
			}
		}
	}

	/**
	 * original alldb and everydb rule alldb: every db contains 1/n tables everydb: every db contains the same tables
	 */
	private void parseTableNamingRule1(List<TableSets> dataSourceBOs, String tbRule) {
		String tbSuffixStyle = StringUtils.substringBefore(tbRule, ":");
		String tbSuffixRange = StringUtils.substringAfter(tbRule, ":");
		tbSuffixRange = StringUtils.substringBetween(tbSuffixRange, "[", "]");
		String[] ranges = tbSuffixRange.split(",");
		if (ranges.length == 2) {
			int numPartLen = getNumberPartLength(ranges[1]);
			int startNumPartLen = getNumberPartLength(ranges[0]);
			String suffix = getTableSuffix(ranges[0]);
			int startNum = Integer.parseInt(StringUtils.substring(ranges[0], -1 * startNumPartLen));
			int endNum = Integer.parseInt(StringUtils.substring(ranges[1], -1 * numPartLen));
			int dsSize = dataSourceBOs.size();
			if (TB_SUFFIX_STYLE_ALL.equals(tbSuffixStyle)) {
				int tableNum = endNum - startNum + 1;
				int tablesEachDB = (tableNum % dsSize == 0) ? tableNum / dsSize : tableNum / dsSize + 1;
				for (int i = 0; i < dataSourceBOs.size(); i++) {
					for (int j = 0; j < tablesEachDB; j++) {
						dataSourceBOs.get(i).addIntoTableSets(tableName + suffix + zeroPadding(startNum++, numPartLen));
					}
				}
			} else if (TB_SUFFIX_STYLE_EVERY.equals(tbSuffixStyle)) {
				for (int i = 0; i < dataSourceBOs.size(); i++) {
					for (int j = startNum; j <= endNum; j++) {
						dataSourceBOs.get(i).addIntoTableSets(tableName + suffix + zeroPadding(j, numPartLen));
					}
				}
			} else {
				throw new ZebraConfigException("TbSuffix property can only be 'alldb' or 'everydb'.");
			}
		} else {
			if (TB_SUFFIX_STYLE_ALL.equals(tbSuffixStyle)) {
				if (dataSourceBOs.size() > 1) {
					throw new ZebraConfigException("'alldb' cannot support only one table for multiple database");
				}
				dataSourceBOs.get(0).addIntoTableSets(tableName);
			} else if (TB_SUFFIX_STYLE_EVERY.equals(tbSuffixStyle)) {
				for (int i = 0; i < dataSourceBOs.size(); i++) {
					dataSourceBOs.get(i).addIntoTableSets(tableName);
				}
			}
		}
	}

	/**
	 * new table naming rule pattern: ref(\d+):[p1] | {p2} p1: suffix0, suffix1, ... (suffix can be a special symbol $ which means
	 * the original table name) p2: suffixm, suffixn (generate tables: table+suffix+m --- table+suffix+n)
	 *
	 * eg. (db: dbTest, tb: tbTest) 1. dbTest0:[$,_0,_1] ==> dbTest0[tbTest, tbTest_0, tbTest_1] 2. dbTest0:[_a,_b] ==>
	 * dbTest0[tbTest_a, tbTest_b] 3. dbTest1:[_2017] ==> dbTest1[tbTest_2017] 4. dbTest0:{0,2} ==> dbTest0[tbTest0, tbTest1,
	 * tbTest2] 5. dbTest0:{_bak0,_bak2} ==> dbTest0[tbTest_bak0, tbTest_bak1, tbTest_bak2]
	 */
	private void parseTableNamingRule2(List<TableSets> dataSourceBOs, String tbRule) {
		String jdbcRef = StringUtils.substringBefore(tbRule, ":");
		TableSets tableSet = null;
		for (TableSets ts : dataSourceBOs) {
			if (ts.getDbIndex().endsWith(jdbcRef)) {
				tableSet = ts;
				break;
			}
		}
		if (tableSet == null) {
			throw new ZebraConfigException("Cannot find corresponding jdbcRef from db list!");
		}

		String tbSuffixRange = StringUtils.substringAfter(tbRule, ":");

		if (tbSuffixRange.contains("{") && tbSuffixRange.contains("}")) {
			tbSuffixRange = StringUtils.substringBetween(tbSuffixRange, "{", "}");
			String[] ranges = tbSuffixRange.split(",");
			if (ranges.length == 2) {
				int startPartLen = getNumberPartLength(ranges[0]);
				int endPartLen = getNumberPartLength(ranges[1]);
				String prefix = getTableSuffix(ranges[0]);
				int startNum = Integer.parseInt(StringUtils.substring(ranges[0], -1 * startPartLen));
				int endNum = Integer.parseInt(StringUtils.substring(ranges[1], -1 * endPartLen));

				int tableCount = endNum - startNum + 1;
				for (int j = 0; j < tableCount; j++) {
					tableSet.addIntoTableSets(tableName + prefix + zeroPadding(startNum++, endPartLen));
				}
			} else {
				throw new ZebraConfigException("TbSuffix config error, there should be two parts in suffix: "
				      + tbSuffixRange);
			}

		} else {
			tbSuffixRange = StringUtils.substringBetween(tbSuffixRange, "[", "]");
			String[] ranges = tbSuffixRange.split(",");
			if (ranges.length > 0) {
				for (String suffix : ranges) {
					suffix = suffix.trim();
					if (suffix.length() > 0) {
						if ("$".equals(suffix)) {
							tableSet.addIntoTableSets(tableName);
						} else {
							tableSet.addIntoTableSets(tableName + suffix);
						}
					}
				}
			}
		}

	}

	private String zeroPadding(int num, int numPartLen) {
		if (tbSuffixZeraPadding) {
			return StringUtils.leftPad(String.valueOf(num), numPartLen, '0');
		} else {
			return String.valueOf(num);
		}
	}

	private String getTableSuffix(String string) {
		int i = string.length() - 1;
		for (; i >= 0; i--) {
			if (!Character.isDigit(string.charAt(i))) {
				break;
			}
		}
		return string.substring(0, i + 1);
	}

	private int getNumberPartLength(String string) {
		int len = 0;
		char[] charArray = string.toCharArray();
		for (int i = charArray.length - 1; i >= 0; i--) {
			if (Character.isDigit(charArray[i])) {
				len++;
			} else {
				break;
			}
		}
		return len;
	}
}
