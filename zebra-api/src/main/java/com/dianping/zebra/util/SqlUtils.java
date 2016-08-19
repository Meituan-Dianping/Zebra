package com.dianping.zebra.util;

import com.dianping.zebra.Constants;

import java.sql.SQLException;
import java.util.regex.Pattern;

public final class SqlUtils {

	/**
	 * 用于判断是否是一个select ... for update的sql
	 */
	private static final Pattern SELECT_FOR_UPDATE_PATTERN = Pattern.compile("^select\\s+.*\\s+for\\s+update.*$",
	      Pattern.CASE_INSENSITIVE);

	public static SqlType getSqlType(String sql) throws SQLException {
		SqlType sqlType = null;
		String noCommentsSql = sql;
		if (sql.contains("/*")) {
			noCommentsSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true).trim();
		}

		if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "select")) {
			String lowerCaseSql = noCommentsSql.toLowerCase();

			if (lowerCaseSql.contains(" for ") && SELECT_FOR_UPDATE_PATTERN.matcher(noCommentsSql).matches()) {
				sqlType = SqlType.SELECT_FOR_UPDATE;
			} else if (lowerCaseSql.contains("@@identity") || lowerCaseSql.contains("last_insert_id()")) {
				sqlType = SqlType.SELECT_FOR_IDENTITY;
			} else {
				sqlType = SqlType.SELECT;
			}
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "insert")) {
			sqlType = SqlType.INSERT;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "update")) {
			sqlType = SqlType.UPDATE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "delete")) {
			sqlType = SqlType.DELETE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "show")) {
			sqlType = SqlType.SHOW;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "replace")) {
			sqlType = SqlType.REPLACE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "truncate")) {
			sqlType = SqlType.TRUNCATE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "create")) {
			sqlType = SqlType.CREATE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "drop")) {
			sqlType = SqlType.DROP;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "load")) {
			sqlType = SqlType.LOAD;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "merge")) {
			sqlType = SqlType.MERGE;
		} else if (noCommentsSql.toLowerCase().contains("call")) {
			sqlType = SqlType.EXECUTE;
		} else {
			sqlType = SqlType.DEFAULT_SQL_TYPE;
		}

		return sqlType;
	}

	public static String buildSqlType(String sql) {
		try {
			char c = sql.trim().charAt(0);
			if (c == 's' || c == 'S') {
				return "Select";
			} else if (c == '/') {
				if (sql.contains(Constants.SQL_FORCE_WRITE_HINT)) {
					return "Select";
				}
			} else if (c == 'u' || c == 'U') {
				return "Update";
			} else if (c == 'i' || c == 'I') {
				return "Insert";
			} else if (c == 'd' || c == 'D') {
				return "Delete";
			} else if (c == 'c' || c == 'C') {
				return "Call";
			} else if (c == 'b' || c == 'B') {
				return "Batch";
			}
		} catch (Exception e) {
		}
		return "Execute";
	}

	public static String parseSqlComment(String sql) {
		String trimSql = sql.trim();

		int start = trimSql.indexOf("/*");

		if (start >= 0) {
			int end = trimSql.indexOf("*/");

			if (end > 0 && end > start) {
				return trimSql.substring(start, end + 2);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
