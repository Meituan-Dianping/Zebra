package com.dianping.zebra.filter;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcOperationCallback<T> {
	T doAction(Connection conn) throws SQLException;
}