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
package com.dianping.zebra.shard.parser;

import com.dianping.zebra.Constants;
import com.dianping.zebra.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class SQLHint {
	private boolean forceMaster = false;

	private boolean batchInsert = false;

	private Set<String> shardColumns = null;

	private String extreHint = null;

	private int concurrencyLevel;

	private Boolean optimizeIn = null;

	private boolean multiQueries = false;

	public String getForceMasterComment() {
		if (forceMaster) {
			return Constants.SQL_FORCE_WRITE_HINT;
		} else {
			return null;
		}
	}

	public static SQLHint parseHint(SQLParser.HintCommentHandler hintComment) {
		SQLHint sqlHint = parseZebraHint(hintComment.getZebraHintComment());

		if (!hintComment.getOtherHintComments().isEmpty()) {
			sqlHint.setExtreHint(joinHintString(hintComment.getOtherHintComments()));
		}

		return sqlHint;
	}

	public static SQLHint parseZebraHint(String hint) {
		SQLHint sqlHint = new SQLHint();

		if (StringUtils.isNotBlank(hint)) {
			int pos = hint.trim().lastIndexOf("*/");
			hint = hint.substring(2, pos);
			StringBuilder sb = new StringBuilder(64);
			String key = null;
			boolean startHint = false;
			for (int i = 0; i < hint.length(); i++) {
				char c = hint.charAt(i);

				if (c == ':') {
					startHint = true;
				} else if (c == '|') {
					if (key == null) {
						key = sb.toString();
						if (key.equalsIgnoreCase("w")) {
							sqlHint.setForceMaster(true);
							sb.setLength(0);
							key = null;
						} else if (key.equalsIgnoreCase("bi")) {
							sqlHint.setBatchInsert(true);
							sb.setLength(0);
							key = null;
						} else if (key.equalsIgnoreCase("mq")) {
							sqlHint.setMultiQueries(true);
							sb.setLength(0);
							key = null;
						}
					} else {
						if (key.equalsIgnoreCase("sk")) {
							sqlHint.setShardColumns(parseShardColumns(sb.toString()));
							sb.setLength(0);
							key = null;
						} else if (key.equalsIgnoreCase("cl")){
							sqlHint.setConcurrencyLevel(Integer.valueOf(sb.toString()));
							sb.setLength(0);
							key = null;
						} else if (key.equalsIgnoreCase("oi")){
							sqlHint.setOptimizeIn(Boolean.valueOf(sb.toString()));
							sb.setLength(0);
							key = null;
						}
					}
				} else if (c == '=') {
					key = sb.toString();
					sb.setLength(0);
				} else if (startHint) {
					sb.append(c);
				} else {
					// skip prefix : +zebra
				}
			}

			if (sb.toString().equalsIgnoreCase("w")) {
				sqlHint.setForceMaster(true);
			} else if (sb.toString().equalsIgnoreCase("bi")) {
				sqlHint.setBatchInsert(true);
			} else if (sb.toString().equalsIgnoreCase("mq")) {
				sqlHint.setMultiQueries(true);
			}


			if (key != null) {
				if (key.equalsIgnoreCase("sk")) {
					sqlHint.setShardColumns(parseShardColumns(sb.toString()));
					sb.setLength(0);
				} else if (key.equalsIgnoreCase("cl")){
					sqlHint.setConcurrencyLevel(Integer.valueOf(sb.toString()));
					sb.setLength(0);
				} else if (key.equalsIgnoreCase("oi")){
					sqlHint.setOptimizeIn(Boolean.valueOf(sb.toString()));
					sb.setLength(0);
				}
			}
		}

		return sqlHint;
	}

	public static Set<String> parseShardColumns(String columnStr) {
		if(columnStr != null) {
			Set<String> colSet = new HashSet<String>();
			int idx = -1;
			int start = 0;
			do {
				idx = columnStr.indexOf('+', start);
				if (idx >= 0) {
					String col = columnStr.substring(start, idx).trim();
					if(col.length() > 0) {
						colSet.add(col);
					}
					start = idx + 1;
				} else {
					String col = columnStr.substring(start).trim();
					if(col.length() > 0) {
						colSet.add(col);
					}
					break;
				}
			} while (start < columnStr.length());
			return colSet;
		}
		return null;
	}


	public void setForceMaster(boolean forceMaster) {
		this.forceMaster = forceMaster;
	}

	public boolean isForceMaster() {
		return forceMaster;
	}

	public boolean isBatchInsert() {
		return batchInsert;
	}

	public void setBatchInsert(boolean batchInsert) {
		this.batchInsert = batchInsert;
	}

	public Set<String> getShardColumns() {
		return shardColumns;
	}

	public void setShardColumns(Set<String> shardColumns) {
		this.shardColumns = shardColumns;
	}

	public String getExtreHint() {
		return extreHint;
	}

	public void setExtreHint(String extreHint) {
		this.extreHint = extreHint;
	}

	public int getConcurrencyLevel() {
		return concurrencyLevel;
	}

	public void setConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
	}

	public Boolean getOptimizeIn() {
		return optimizeIn;
	}

	public void setOptimizeIn(Boolean optimizeIn) {
		this.optimizeIn = optimizeIn;
	}

	public String getHintComments() {
		if(forceMaster && extreHint != null) {
			return Constants.SQL_FORCE_WRITE_HINT + extreHint;
		} else {
			return forceMaster ? Constants.SQL_FORCE_WRITE_HINT : extreHint;
		}
	}

	public boolean isMultiQueries() {
		return multiQueries;
	}

	public void setMultiQueries(boolean multiQueries) {
		this.multiQueries = multiQueries;
	}

	public static String joinHintString(Set<String> set) {
		String result = "";
		for (String hint : set) {
			result += hint;
		}
		return result;
	}
}
