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
import com.dianping.zebra.shard.exception.ShardRouterException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ShardDateParseUtil {
	private static final String DATE_FORMAT_1 = "yyyy-MM-dd";

	private static final String DATE_FORMAT_2 = "yyyy/MM/dd";

	public static ShardDate parseToYMD(String dateFormat, Object input) {
		if (input instanceof Date) {
			return innerParseToYMD((Date) input);
		} else if (input instanceof String) {
			return innerParseToYMD(dateFormat, ((String) input).trim());
		} else {
			throw new ZebraConfigException("Error when parse input date");
		}
	}

	private static ShardDate innerParseToYMD(Date inputDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(inputDate);
		return new ShardDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
		      calendar.get(Calendar.DAY_OF_MONTH) - 1);
	}

	// TODO exception deal
	private static ShardDate innerParseToYMD(String dateFormat, String dateStr) {
		if (DATE_FORMAT_1.equals(dateFormat)) {
			String[] ymdStr = dateStr.split("-");
			int year = Integer.parseInt(ymdStr[0]);
			int month = Integer.parseInt(ymdStr[1]) - 1;
			int spIdx = ymdStr[2].indexOf(' ');
			if (spIdx > 0) {
				ymdStr[2] = ymdStr[2].substring(0, spIdx);
			}
			int day = Integer.parseInt(ymdStr[2]) - 1;

			return new ShardDate(year, month, day);
		} else if (DATE_FORMAT_2.equals(dateFormat)) {
			String[] ymdStr = dateStr.split("/");
			int year = Integer.parseInt(ymdStr[0]);
			int month = Integer.parseInt(ymdStr[1]) - 1;
			int spIdx = ymdStr[2].indexOf(' ');
			if (spIdx > 0) {
				ymdStr[2] = ymdStr[2].substring(0, spIdx);
			}
			int day = Integer.parseInt(ymdStr[2]) - 1;

			return new ShardDate(year, month, day);
		} else {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

				return innerParseToYMD(formatter.parse(dateStr));
			} catch (Exception e) {
				throw new ShardRouterException(String.format(
				      "Parse the date error, date format: %s, " + "data string: %s.", dateFormat, dateStr), e);
			}
		}
	}

	public static ShardDate addDay(String dateFormat, Object input, int delta) {
		if (input instanceof Date) {
			return innerAddDay((Date) input, delta);
		} else if (input instanceof String) {
			return innerAddDay(dateFormat, (String) input, delta);
		} else {
			throw new ShardRouterException(String.format("Add day error, no support input date type ") + input);
		}
	}

	public static ShardDate addMonth(String dateFormat, Object input, int delta) {
		if (input instanceof Date) {
			return innerAddMonth((Date) input, delta);
		} else if (input instanceof String) {
			return innerAddMonth(dateFormat, (String) input, delta);
		} else {
			throw new ShardRouterException(String.format("Add month error, no support input date type ") + input);
		}
	}

	private static ShardDate innerAddDay(String dateFormat, String dateStr, int delta) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
			Date date = formatter.parse(dateStr);

			return innerAddDay(date, delta);
		} catch (Exception e) {
			throw new ShardRouterException("ShardByMonth add day error! " + dateStr);
		}
	}

	private static ShardDate innerAddDay(Date inputDate, int delta) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(inputDate);
		calendar.add(Calendar.DAY_OF_MONTH, delta);

		return new ShardDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
		      calendar.get(Calendar.DAY_OF_MONTH) - 1);

	}

	private static ShardDate innerAddMonth(String dateFormat, String dateStr, int delta) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
			Date date = formatter.parse(dateStr);

			return innerAddMonth(date, delta);
		} catch (Exception e) {
			throw new ShardRouterException("ShardByMonth add month error! " + dateStr);
		}

	}

	private static ShardDate innerAddMonth(Date inputDate, int delta) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(inputDate);
		calendar.add(Calendar.MONTH, delta);

		return new ShardDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
		      calendar.get(Calendar.DAY_OF_MONTH) - 1);

	}

	public static class ShardDate {
		public int year;

		public int month; // start from zero, 0 present the first moth

		public int day; // start from zero

		public ShardDate() {
		}

		public ShardDate(int year, int month, int day) {
			this.year = year;
			this.month = month;
			this.day = day;
		}

		public boolean greaterThan(ShardDate sd) {
			if (this.year > sd.year) {
				return true;
			} else if (this.year == sd.year) {
				if (this.month > sd.month) {
					return true;
				} else if (this.month == sd.month && this.day > sd.day) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof ShardDate) {
				ShardDate sd = (ShardDate) obj;
				return (this.year == sd.year && this.month == sd.month && this.day == sd.day);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return (this.year * 31 + this.month) * 31 + this.day;
		}

		@Override
		public String toString() {
			return "ShardDate[" + year + "-" + month + "-" + day + "]";
		}

		public int getYear() {
			return year;
		}

		public void setYear(int year) {
			this.year = year;
		}

		public int getMonth() {
			return month;
		}

		public void setMonth(int month) {
			this.month = month;
		}

		public int getDay() {
			return day;
		}

		public void setDay(int day) {
			this.day = day;
		}
	}
}
