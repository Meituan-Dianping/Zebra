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
package com.dianping.zebra.shard.router.rule.engine;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.shard.exception.ShardRouterException;
import com.dianping.zebra.shard.router.rule.ShardRange;
import com.dianping.zebra.shard.util.ShardDateParseUtil;
import com.dianping.zebra.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.CRC32;

public class RuleEngineBase {
	public static final int SKIP = -1;

	public static final String DATE_FORMAT_1 = "yyyy-MM-dd";

	public static final String DATE_FORMAT_2 = "yyyy/MM/dd";

	protected volatile boolean initShardByMonth = false;

	private volatile ShardDateParseUtil.ShardDate beginShardDate;

	private volatile ShardDateParseUtil.ShardDate endShardDate;

	private volatile int beginDayNumber = 0;

	private volatile int endDayNumber = 0;

	private volatile int beginMonthNumber = 0;

	private volatile int endMonthNumber = 0;

	public long crc32(Object str) throws UnsupportedEncodingException {
		return crc32(str, "utf-8");
	}

	public Date date(Object value) throws ParseException {
		if (value instanceof Date) {
			return (Date) value;
		}
		if (value instanceof String) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.SIMPLIFIED_CHINESE);
			return format.parse((String) value);
		}

		throw new IllegalArgumentException();
	}

	public String month(Date value) throws ParseException {
		DateFormat format = new SimpleDateFormat("yyyyMM", Locale.SIMPLIFIED_CHINESE);
		return format.format((Date) value);
	}

	public String md5(String input) throws NoSuchAlgorithmException {
		return StringUtils.md5(input);
	}

	public long crc32(Object str, String encode) throws UnsupportedEncodingException {
		CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(str).getBytes(encode));
		return crc32.getValue();
	}

	// ****************************************************************************************************
	public Map<Integer, List<Integer>> shardByHash(long shardValue, int dbNumber, int tableNumberPerDb) {
		return shardByHash(shardValue, dbNumber, tableNumberPerDb, true);
	}

	public Map<Integer, List<Integer>> shardByHash(long shardValue, int dbNumber, int tableNumberPerDb,
	      boolean useDefaultOrder) {
		Map<Integer, List<Integer>> resultMap = new HashMap<Integer, List<Integer>>();
		long dbIndex, tbIndex;
		if (useDefaultOrder) {
			dbIndex = shardValue / tableNumberPerDb % dbNumber;
			tbIndex = shardValue % tableNumberPerDb;
		} else {
			dbIndex = shardValue % dbNumber;
			tbIndex = shardValue / dbNumber % tableNumberPerDb;
		}
		List<Integer> tbList = new ArrayList<Integer>();
		tbList.add((int) tbIndex);
		resultMap.put((int) dbIndex, tbList);
		return resultMap;
	}

	public Map<Integer, List<Integer>> shardByLong(Object shardValues, long start, long end, int countPerTable,
	      int tableCountPerDb, int databaseCount, int defaultDbIndex, int defaultTableIndex) {
		return shardByLong(shardValues, start, end, countPerTable, tableCountPerDb, databaseCount, defaultDbIndex,
		      defaultTableIndex, true);
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, List<Integer>> shardByLong(Object shardValues, long start, long end, int countPerTable,
	      int tableCountPerDb, int databaseCount, int defaultDbIndex, int defaultTableIndex, boolean useDefaultOrder) {
		Map<Integer, List<Integer>> resultMap = new HashMap<Integer, List<Integer>>();
		if (shardValues instanceof Set) {
			Set<ShardRange> conditions = (Set<ShardRange>) shardValues;
			Long min = null, max = null;
			for (ShardRange range : conditions) {
				if (range.isEqual()) {
					long input = innerParseLong(range.getFirstParameter());
					if (input > end || input < start) {
						putIntoMap(resultMap, defaultDbIndex, defaultTableIndex);
					} else {
						long tbIdx = (input - start) / countPerTable;
						calculateDbTbIndex(resultMap, tbIdx, databaseCount, tableCountPerDb, useDefaultOrder);
					}
					return resultMap;
				} else if (range.isIn()) {
					Set<Object> ranges = (Set<Object>) range.getFirstParameter();
					Map<Integer, Set<Integer>> tempMap = new HashMap<Integer, Set<Integer>>();
					for (Object obj : ranges) {
						long input = innerParseLong(obj);
						if (input < start || input > end) {
							putIntoMapForIn(tempMap, defaultDbIndex, defaultTableIndex);
						} else {
							long tbIdx = (input - start) / countPerTable;
							int di, ti;
							if (useDefaultOrder) {
								di = (int) (tbIdx / tableCountPerDb % databaseCount);
								ti = (int) (tbIdx % tableCountPerDb);
							} else {
								di = (int) (tbIdx % databaseCount);
								ti = (int) (tbIdx / databaseCount % tableCountPerDb);
							}
							putIntoMapForIn(tempMap, di, ti);
						}
					}
					for (Map.Entry<Integer, Set<Integer>> entry : tempMap.entrySet()) {
						int di = entry.getKey();
						for (int ti : entry.getValue()) {
							putIntoMap(resultMap, di, ti);
						}
					}
					return resultMap;
				} else {
					if (range.isGreater()) {
						long input = innerParseLong(range.getFirstParameter()) + 1;
						if (min == null || input > min) {
							min = input + 1;
						}
					} else if (range.isGreaterOrEqual()) {
						long input = innerParseLong(range.getFirstParameter());
						if (min == null || input > min) {
							min = input;
						}
					} else if (range.isLess()) {
						long input = innerParseLong(range.getFirstParameter()) - 1;
						if (max == null || input < max) {
							max = input - 1;
						}
					} else if (range.isLessOrEqual()) {
						long input = innerParseLong(range.getFirstParameter());
						if (max == null || input < max) {
							max = input;
						}
					} else if (range.isBetweenAnd()) {
						long sl = innerParseLong(range.getFirstParameter());
						if (min == null || sl > min) {
							min = sl;
						}
						long el = innerParseLong(range.getSecondParameter());
						if (max == null || el < max) {
							min = el;
						}
					}
				}
			}
			return calculateAllTbIndexByLong(start, end, min, max, countPerTable, tableCountPerDb, databaseCount,
			      defaultDbIndex, defaultTableIndex, useDefaultOrder);
		} else if (shardValues instanceof Number || shardValues instanceof String) {
			// 正常zebra-client不会使用
			long input = innerParseLong(shardValues);
			if (input < start || input > end) {
				putIntoMap(resultMap, defaultDbIndex, defaultTableIndex);
			} else {
				long tbIdx = (input - start) / countPerTable;
				calculateDbTbIndex(resultMap, tbIdx, databaseCount, tableCountPerDb, useDefaultOrder);
			}
		} else {
			throw new ShardRouterException("Cannot recognize the parameters type" + shardValues);
		}

		return resultMap;
	}

	private Map<Integer, List<Integer>> calculateAllTbIndexByLong(long start, long end, Long min, Long max,
	      int countPerTable, int tableCountPerDb, int databaseCount, int defaultDbIndex, int defaultTableIndex,
	      boolean useDefaultOrder) {
		Map<Integer, List<Integer>> resultMap = new HashMap<Integer, List<Integer>>();

		boolean useDefault = false;
		long s = start - 1, e = end + 1;
		if (min != null) {
			s = min;
		}
		if (max != null) {
			e = max;
		}
		if (s < start) {
			s = start;
			useDefault = true;
		} else if (s > end) {
			putIntoMap(resultMap, defaultDbIndex, defaultTableIndex);
			return resultMap;
		}
		if (e > end) {
			e = end;
			useDefault = true;
		} else if (e < start) {
			putIntoMap(resultMap, defaultDbIndex, defaultTableIndex);
			return resultMap;
		}
		if (useDefault) {
			putIntoMap(resultMap, defaultDbIndex, defaultTableIndex);
		}

		return calculateTbIndexByCondition(resultMap, start, end, s, e, countPerTable, tableCountPerDb, databaseCount,
		      useDefaultOrder);
	}

	// --------------------------------------------------

	public Map<Integer, List<Integer>> shardByMonth(Object shardValues, String dateFormat, String inclusiveStartTime,
	      String inclusiveEndTime, int monthCountPerTable, int databaseCount, int tableCountPerDb, int defaultDbIndex,
	      int defaultTableIndex) {
		return shardByMonth(shardValues, dateFormat, inclusiveStartTime, inclusiveEndTime, monthCountPerTable,
		      databaseCount, tableCountPerDb, defaultDbIndex, defaultTableIndex, true);
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, List<Integer>> shardByMonth(Object shardValues, String dateFormat, String inclusiveStartTime,
	      String inclusiveEndTime, int monthCountPerTable, int databaseCount, int tableCountPerDb, int defaultDbIndex,
	      int defaultTableIndex, boolean useDefaultOrder) {
		if (initShardByMonth == false) {
			synchronized (RuleEngineBase.class) {
				if (initShardByMonth == false) {
					innerParseShardByMonth(dateFormat, inclusiveStartTime, inclusiveEndTime);
					initShardByMonth = true;
				}
			}
		}

		Map<Integer, List<Integer>> resultMap = new HashMap<Integer, List<Integer>>(databaseCount * 2);

		if (shardValues instanceof Set) {
			Set<ShardRange> conditions = (Set<ShardRange>) shardValues;
			ShardDateParseUtil.ShardDate minTime = null;
			ShardDateParseUtil.ShardDate maxTime = null;

			for (ShardRange range : conditions) {
				if (range.isEqual()) {
					ShardDateParseUtil.ShardDate sd = ShardDateParseUtil.parseToYMD(dateFormat, range.getFirstParameter());
					innerShardByMonth(resultMap, sd, monthCountPerTable, tableCountPerDb, databaseCount, defaultDbIndex,
					      defaultTableIndex, useDefaultOrder);
					return resultMap;
				} else if (range.isIn()) {
					// TODO optimize
					Set<Object> dates = (Set<Object>) range.getFirstParameter();
					Map<Integer, Set<Integer>> tempMap = new HashMap<Integer, Set<Integer>>();
					for (Object obj : dates) {
						ShardDateParseUtil.ShardDate sd = ShardDateParseUtil.parseToYMD(dateFormat, obj);
						innerShardByMonthForIn(tempMap, sd, monthCountPerTable, tableCountPerDb, databaseCount,
						      defaultDbIndex, defaultTableIndex, useDefaultOrder);
					}
					for (Map.Entry<Integer, Set<Integer>> entry : tempMap.entrySet()) {
						int di = entry.getKey();
						for (int ti : entry.getValue()) {
							putIntoMap(resultMap, di, ti);
						}
					}
					return resultMap;
				} else {
					if (range.isGreater()) {
						ShardDateParseUtil.ShardDate sd = ShardDateParseUtil.addDay(dateFormat, range.getFirstParameter(), 1);
						if (minTime == null || sd.greaterThan(minTime)) {
							minTime = sd;
						}
					} else if (range.isGreaterOrEqual()) {
						ShardDateParseUtil.ShardDate sd = ShardDateParseUtil
						      .parseToYMD(dateFormat, range.getFirstParameter());
						if (minTime == null || sd.greaterThan(minTime)) {
							minTime = sd;
						}
					} else if (range.isLess()) {
						ShardDateParseUtil.ShardDate sd = ShardDateParseUtil
						      .addDay(dateFormat, range.getFirstParameter(), -1);
						if (maxTime == null || maxTime.greaterThan(sd)) {
							maxTime = sd;
						}
					} else if (range.isLessOrEqual()) {
						ShardDateParseUtil.ShardDate sd = ShardDateParseUtil
						      .parseToYMD(dateFormat, range.getFirstParameter());
						if (maxTime == null || maxTime.greaterThan(sd)) {
							maxTime = sd;
						}
					} else if (range.isBetweenAnd()) {
						ShardDateParseUtil.ShardDate sd = ShardDateParseUtil
						      .parseToYMD(dateFormat, range.getFirstParameter());
						if (minTime == null || sd.greaterThan(minTime)) {
							minTime = sd;
						}
						sd = ShardDateParseUtil.parseToYMD(dateFormat, range.getSecondParameter());
						if (maxTime == null || maxTime.greaterThan(sd)) {
							maxTime = sd;
						}
					}
				}
			}

			// TODO optimize: deal the range
			return calculateAllTbIndexByMonth(minTime, maxTime, monthCountPerTable, databaseCount, tableCountPerDb,
			      defaultDbIndex, defaultTableIndex, useDefaultOrder);
		} else if (shardValues instanceof Date || shardValues instanceof String) {
			// 正常业务服务不会使用, 仅供zebra-sync-service内部使用
			ShardDateParseUtil.ShardDate sd = ShardDateParseUtil.parseToYMD(dateFormat, shardValues);
			innerShardByMonth(resultMap, sd, monthCountPerTable, tableCountPerDb, databaseCount, defaultDbIndex,
			      defaultTableIndex, useDefaultOrder);
		} else {
			throw new ShardRouterException("Cannot recognize the parameters type" + shardValues);
		}

		return resultMap;
	}

	private void innerShardByMonth(Map<Integer, List<Integer>> resultMap, ShardDateParseUtil.ShardDate ymd, int mpt,
	      int tpd, int dc, int ddi, int dti, boolean defaultOrder) {
		int currentMonthNumber = ymd.getYear() * 12 + ymd.getMonth();
		int currentDayNumber = currentMonthNumber * 31 + ymd.getDay();
		if (currentDayNumber < beginDayNumber || currentDayNumber > endDayNumber) {
			putIntoMap(resultMap, ddi, dti);
		} else {
			int tbIdx = (currentMonthNumber - beginMonthNumber) / mpt;
			calculateDbTbIndex(resultMap, tbIdx, dc, tpd, defaultOrder);
		}
	}

	private void calculateDbTbIndex(Map<Integer, List<Integer>> resultMap, long index, long dc, long tpd,
	      boolean defaultOrder) {
		int di, ti;
		if (defaultOrder) {
			di = (int) (index / tpd % dc);
			ti = (int) (index % tpd);
		} else {
			di = (int) (index % dc);
			ti = (int) (index / dc % tpd);
		}
		putIntoMap(resultMap, di, ti);
	}

	private Map<Integer, List<Integer>> calculateAllTbIndexByMonth(ShardDateParseUtil.ShardDate min,
	      ShardDateParseUtil.ShardDate max, int monthPerTable, int databaseCount, int tableCountPerDb,
	      int defaultDbIndex, int defaultTableIndex, boolean useDefaultOrder) {
		Map<Integer, List<Integer>> resultMap = new LinkedHashMap<Integer, List<Integer>>(databaseCount * 2);

		int sd = beginDayNumber - 1, sm = beginMonthNumber - 1, ed = endDayNumber + 1, em = endMonthNumber + 1;
		if (min != null) {
			sm = min.getYear() * 12 + min.getMonth();
			sd = sm * 31 + min.getDay();
		}
		if (max != null) {
			em = max.getYear() * 12 + max.getMonth();
			ed = em * 31 + max.getDay();
		}

		boolean useDefault = false;
		if (sd < beginDayNumber) {
			sm = beginMonthNumber;
			useDefault = true;
		} else if (sd > endDayNumber) {
			putIntoMap(resultMap, defaultDbIndex, defaultTableIndex);
			return resultMap;
		}
		if (ed > endDayNumber) {
			em = endMonthNumber;
			useDefault = true;
		} else if (ed < beginDayNumber) {
			putIntoMap(resultMap, defaultDbIndex, defaultTableIndex);
			return resultMap;
		}
		if (useDefault) {
			putIntoMap(resultMap, defaultDbIndex, defaultTableIndex);
		}

		return calculateTbIndexByCondition(resultMap, beginMonthNumber, endMonthNumber, sm, em, monthPerTable,
		      tableCountPerDb, databaseCount, useDefaultOrder);
	}

	private Map<Integer, List<Integer>> calculateTbIndexByCondition(Map<Integer, List<Integer>> resultMap, long start,
	      long end, long si, long ei, int cpt, int tpd, int dc, boolean defaultOrder) {
		long lastStart = si;
		long lastIdx = -1;
		for (long i = si; i <= ei; i += cpt) {
			long idx = (i - start) / cpt;
			calculateDbTbIndex(resultMap, idx, dc, tpd, defaultOrder);
			lastStart = i;
			lastIdx = idx;
		}

		if (lastStart < ei) {
			long idx = (ei - start) / cpt;
			if (idx != lastIdx) {
				calculateDbTbIndex(resultMap, idx, dc, tpd, defaultOrder);
			}
		}

		return resultMap;
	}

	private void innerShardByMonthForIn(Map<Integer, Set<Integer>> resultMap, ShardDateParseUtil.ShardDate ymd, int mpt,
	      int tpd, int dc, int ddi, int dti, boolean useDefaultOrder) {
		int currentMonthNumber = ymd.getYear() * 12 + ymd.getMonth();
		int currentDayNumber = currentMonthNumber * 31 + ymd.getDay();
		if (currentDayNumber < beginDayNumber || currentDayNumber > endDayNumber) {
			putIntoMapForIn(resultMap, ddi, dti);
		} else {
			int tbIdx = (currentMonthNumber - beginMonthNumber) / mpt;
			int di, ti;

			if (useDefaultOrder) {
				di = tbIdx / tpd % dc;
				ti = tbIdx % tpd;
			} else {
				di = tbIdx % dc;
				ti = tbIdx / dc % tpd;
			}
			putIntoMapForIn(resultMap, di, ti);
		}
	}

	private void innerParseShardByMonth(String format, String inclusiveBegin, String inclusiveEnd) {
		if (format == null || inclusiveBegin == null || inclusiveEnd == null) {
			throw new ZebraConfigException("ShardByMonth config error, format, "
			      + "inclusiveBegin or inclusiveEnd can't be null! Format: " + format + ", inclusiveBegin: "
			      + inclusiveBegin + ", inclusiveEnd: " + inclusiveEnd);
		}
		this.beginShardDate = ShardDateParseUtil.parseToYMD(format, inclusiveBegin);
		this.beginMonthNumber = this.beginShardDate.getYear() * 12 + this.beginShardDate.getMonth();
		this.beginDayNumber = this.beginMonthNumber * 31 + this.beginShardDate.getDay();

		this.endShardDate = ShardDateParseUtil.parseToYMD(format, inclusiveEnd);
		this.endMonthNumber = this.endShardDate.getYear() * 12 + this.endShardDate.getMonth();
		this.endDayNumber = this.endMonthNumber * 31 + this.endShardDate.getDay();
	}

	private long innerParseLong(Object input) {
		if (input instanceof String) {
			return Long.parseLong((String) input);
		} else if (input instanceof Number) {
			return ((Number) input).longValue();
		} else {
			throw new ZebraConfigException("No support parameter type int shardByLong! " + input);
		}
	}

	private void putIntoMap(Map<Integer, List<Integer>> resultMap, int dbIndex, int tbIndex) {
		List<Integer> tbList = resultMap.get(new Integer(dbIndex));
		if (tbList == null) {
			tbList = new ArrayList<Integer>(8);
			resultMap.put(new Integer(dbIndex), tbList);
		}
		tbList.add(new Integer(tbIndex));
	}

	private void putIntoMapForIn(Map<Integer, Set<Integer>> resultMap, int dbIndex, int tbIndex) {
		Set<Integer> tbSet = resultMap.get(dbIndex);
		if (tbSet == null) {
			tbSet = new LinkedHashSet<Integer>();
			resultMap.put(dbIndex, tbSet);
		}
		tbSet.add(tbIndex);
	}
}
