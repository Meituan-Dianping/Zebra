package com.dianping.zebra.single.jdbc;

import java.sql.SQLException;

import com.dianping.zebra.group.util.DataSourceState;

public interface DataSourceLifeCycle {

	DataSourceState getState();

	void init();

	void markClosed();

	void markDown();

	void markUp();

	void close() throws SQLException;

}
