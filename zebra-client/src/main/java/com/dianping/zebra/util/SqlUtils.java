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
package com.dianping.zebra.util;

public final class SqlUtils {

	private static final String SELECT_KEY = "select";

	private static final String INSERT_KEY = "insert";

	private static final String UPDATE_KEY = "update";

	private static final String DELETE_KEY = "delete";

	private static final String SHOW_KEY = "show";

	private static final String REPLACE_KEY = "replace";

	private static final String TRUNCATE_KEY = "truncate";

	private static final String CREATE_KEY = "create";

	private static final String DROP_KEY = "drop";

	private static final String LOAD_KEY = "load";

	private static final String MERGE_KEY = "merge";

	private static final String EXPLAIN_KEY = "explain";

	private static final String EXECUTE_KEY = "call";

	private static final String IDENTITY_PATTERN = "@@identity";

	private static final String LAST_INSERT_PATTERN = "last_insert_id(";

	private static final String FOR_PATTERN = "for";

	private static final String UPDATE_PATTERN = "update";

	private static final String ALTER_PATTERN = "alter";

	public static SqlType getSqlType(String sql) {
		if (StringUtils.isBlank(sql)) {
			return SqlType.UNKNOWN_SQL_TYPE;
		}

		SqlType sqlType = null;
		String lowerSql = sql.toLowerCase();
		boolean inComment = false;
		int begin = 0;

		// skip spaces and hint
		for (; begin < lowerSql.length(); ++begin) {
			char currentChar = lowerSql.charAt(begin);
			if (!inComment) {
				if (currentChar == '/') {
					inComment = true;
				} else if (currentChar > ' ') {
					break;
				}
			} else if (currentChar == '/') {
				inComment = !inComment;
			}
		}

		if (lowerSql.length() - begin < EXECUTE_KEY.length()) {
			return SqlType.UNKNOWN_SQL_TYPE;
		}

		// capture the first word
		switch (lowerSql.charAt(begin)) {
		case 's':
			if (lowerSql.charAt(begin + 1) == 'e') {
				sqlType = selectHandler(lowerSql, begin);
			} else {
				sqlType = showHandler(lowerSql, begin);
			}
			break;
		case 'i':
			sqlType = insertHandler(lowerSql, begin);
			break;
		case 'u':
			sqlType = updateHandler(lowerSql, begin);
			break;
		case 'd':
			if (lowerSql.charAt(begin + 1) == 'e') {
				sqlType = deleteHandler(lowerSql, begin);
			} else {
				sqlType = dropHandler(lowerSql, begin);
			}
			break;
		case 'r':
			sqlType = replaceHandler(lowerSql, begin);
			break;
		case 't':
			sqlType = truncateHandler(lowerSql, begin);
			break;
		case 'c':
			sqlType = createHandler(lowerSql, begin);
			break;
		case 'l':
			sqlType = loadHandler(lowerSql, begin);
			break;
		case 'm':
			sqlType = mergeHandler(lowerSql, begin);
			break;
		case 'e':
			sqlType = explainHandler(lowerSql, begin);
			break;
		case 'a':
			sqlType = alterHandler(lowerSql, begin);
			break;
		default:
			sqlType = executeHandler(lowerSql, begin);
			break;
		}

		return sqlType;
	}

	private static SqlType alterHandler(String lowerSql, int begin) {
		if (isStartWithKeyWord(lowerSql, begin, ALTER_PATTERN)) {
			return SqlType.ALTER;
		}
		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType selectHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, SELECT_KEY)) {
			if (endsWithForUpdate(sql)) {
				return SqlType.SELECT_FOR_UPDATE;
			}

			boolean inWord = false;
			boolean inCommon = false;
			begin = begin + SELECT_KEY.length();
			for (; begin < sql.length(); ++begin) {
				char ch = sql.charAt(begin);
				switch (ch) {
				case 'l':
					if (!inCommon && !inWord && isKeyWord(sql, begin, LAST_INSERT_PATTERN)) {
						return SqlType.SELECT_FOR_IDENTITY;
					}
					begin += LAST_INSERT_PATTERN.length();
					break;
				case '@':
					if (!inCommon && !inWord && isKeyWord(sql, begin, IDENTITY_PATTERN)) {
						return SqlType.SELECT_FOR_IDENTITY;
					}
					begin += IDENTITY_PATTERN.length();
					break;
				case '/':
					inCommon = !inCommon;
					if (inWord) {
						inWord = false;
					}
					break;
				case ' ':
				case '\r':
				case '\n':
				case '\t':
				case ',':
					if (inWord) {
						inWord = false;
					}
					break;
				default:
					if (!inCommon && !inWord) {
						inWord = true;
					}
					break;
				}
			}

			return SqlType.SELECT;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType insertHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, INSERT_KEY)) {
			return SqlType.INSERT;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType updateHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, UPDATE_KEY)) {
			return SqlType.UPDATE;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType deleteHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, DELETE_KEY)) {
			return SqlType.DELETE;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType showHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, SHOW_KEY)) {
			return SqlType.SHOW;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType replaceHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, REPLACE_KEY)) {
			return SqlType.REPLACE;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType truncateHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, TRUNCATE_KEY)) {
			return SqlType.TRUNCATE;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType createHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, CREATE_KEY)) {
			return SqlType.CREATE;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType dropHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, DROP_KEY)) {
			return SqlType.DROP;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType loadHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, LOAD_KEY)) {
			return SqlType.LOAD;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType mergeHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, MERGE_KEY)) {
			return SqlType.MERGE;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType explainHandler(String sql, int begin) {
		if (isStartWithKeyWord(sql, begin, EXPLAIN_KEY)) {
			return SqlType.EXPLAIN;
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	private static SqlType executeHandler(String sql, int begin) {
		boolean inWord = false;
		boolean inCommon = false;
		for (; begin < sql.length(); ++begin) {
			char ch = sql.charAt(begin);
			switch (ch) {
			case 'c':
				if (!inCommon && !inWord && isKeyWord(sql, begin, EXECUTE_KEY)) {
					char nextCh = sql.charAt(begin + EXECUTE_KEY.length());
					if (isControlChar(nextCh)) {
						return SqlType.EXECUTE;
					}
					begin = begin + EXECUTE_KEY.length();
				}
				break;
			case '/':
				inCommon = !inCommon;
				if (inWord) {
					inWord = false;
				}
				break;
			case ' ':
			case '\r':
			case '\n':
			case '\t':
			case ',':
				if (inWord) {
					inWord = false;
				}
				break;
			default:
				if (!inCommon) {
					if (Character.isLetter(ch)) {
						inWord = true;
					}
				}
				break;
			}
		}

		return SqlType.UNKNOWN_SQL_TYPE;
	}

	public static boolean isStartWithKeyWord(String sql, int begin, String word) {
		if (isKeyWord(sql, begin, word)) {
			if (sql.length() > begin + word.length()) {
				char nextCh = sql.charAt(begin + word.length());
				if (!isControlChar(nextCh) && nextCh != '/') {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	private static boolean isKeyWord(String sql, int begin, String word) {
		int end = begin;
		for (int i = 0; i < word.length(); ++i, ++end) {
			if (end >= sql.length() || word.charAt(i) != sql.charAt(end)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isControlChar(char ch) {
		if (' ' == ch || '\r' == ch || '\n' == ch || '\t' == ch) {
			return true;
		}

		return false;
	}

	private static boolean endsWithForUpdate(String sql) {
		int sqlPos = sql.length() - 1;
		for (; sqlPos > 0; --sqlPos) {
			char ch = sql.charAt(sqlPos);
			if (!isControlChar(ch) && ch != ';') {
				break;
			}
		}

		for (int i = UPDATE_PATTERN.length() - 1; i >= 0; --i, --sqlPos) {
			if (sqlPos <= 0 || sql.charAt(sqlPos) != UPDATE_PATTERN.charAt(i)) {
				return false;
			}
		}

		for (; sqlPos > 0; --sqlPos) {
			if (!isControlChar(sql.charAt(sqlPos))) {
				break;
			}
		}

		if (sqlPos > 0) {
			for (int i = FOR_PATTERN.length() - 1; i >= 0; --i, --sqlPos) {
				if (sqlPos <= 0 || sql.charAt(sqlPos) != FOR_PATTERN.charAt(i)) {
					return false;
				}
			}
		}

		if (sqlPos <= 0 || !isControlChar(sql.charAt(sqlPos))) {
			return false;
		}

		return true;
	}

	public static String buildSqlType(String sql) {
		return getSqlType(sql).name();
	}
}
