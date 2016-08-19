package com.dianping.zebra.group.util;

public class DaoContextHolder {

	private static final ThreadLocal<String> SQL_NAME_THREAD_LOCAL = new ThreadLocal<String>();

	public static String getSqlName() {
		return SQL_NAME_THREAD_LOCAL.get();
	}

	public static void setSqlName(String statementName) {
		SQL_NAME_THREAD_LOCAL.set(statementName);
	}

	public static void clearSqlName() {
		SQL_NAME_THREAD_LOCAL.remove();
	}
}
