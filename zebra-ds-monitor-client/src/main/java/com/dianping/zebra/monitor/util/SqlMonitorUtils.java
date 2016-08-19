package com.dianping.zebra.monitor.util;

public class SqlMonitorUtils {
	
//	private final static String BIG_SQL_KEY = "zebra.monitor.client.sqllength";
//
//	private final static String BIG_RESPONSE_KEY = "zebra.monitor.client.bigresponse";
	
	public final static int BIG_SQL = 102400;

	public final static int BIG_RESPONSE = 1000 * 100;// 100k rows

	public static String getSqlLengthName(int length) {
		if (length <= 1024) {
			return "<= 1K";
		} else if (length <= 10240) {
			return "<= 10K";
		} else if (length <= 102400) {
			return "<= 100K";
		} else if (length <= 1024 * 1024) {
			return "<= 1M";
		} else if (length <= 1024 * 10240) {
			return "<= 10M";
		} else if (length <= 1024 * 102400) {
			return "<= 100M";
		} else {
			return "> 100M";
		}
	}

	public static String getSqlRowsName(int rows) {
		if (rows <= 10) {
			return "<= 10";
		} else if (rows <= 100) {
			return "<= 100";
		} else if (rows <= 1000) {
			return "<= 1000";
		} else if (rows <= 1000 * 10) {
			return "<= 10k";
		} else if (rows <= 1000 * 100) {
			return "<= 100k";
		} else {
			return "> 100k";
		}
	}
}
