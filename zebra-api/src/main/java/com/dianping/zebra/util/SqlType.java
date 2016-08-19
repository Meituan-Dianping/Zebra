package com.dianping.zebra.util;

/**
 * @author kezhu.wu
 */
public enum SqlType {
	SELECT(true, true, 0), //
	INSERT(false, false, 1), //
	UPDATE(false, false, 2), //
	DELETE(false, false, 3), //
	SELECT_FOR_UPDATE(false, true, 4), //
	REPLACE(false, false, 5), //
	TRUNCATE(false, false, 6), //
	CREATE(false, false, 7), //
	DROP(false, false, 8), //
	LOAD(false, false, 9), //
	MERGE(false, false, 10), //
	SHOW(true, true, 11), //
	EXECUTE(false, false, 12), //
	SELECT_FOR_IDENTITY(false, true, 13), //
	DEFAULT_SQL_TYPE(false, true, -100), //
	;

	private boolean isRead;

	private boolean isQuery;

	private int i;

	private SqlType(boolean isRead, boolean isQuery, int i) {
		this.isRead = isRead;
		this.isQuery = isQuery;
		this.i = i;
	}

	public int value() {
		return this.i;
	}

	public boolean isRead() {
		return isRead;
	}

	public boolean isQuery() {
		return isQuery;
	}

	public static SqlType valueOf(int i) {
		for (SqlType t : values()) {
			if (t.value() == i) {
				return t;
			}
		}
		throw new IllegalArgumentException("Invalid SqlType:" + i);
	}

}
