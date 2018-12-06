package com.dianping.zebra.log;

public interface Logger {
	boolean isDebugEnabled();

	void error(String msg, Throwable e);

	void error(String msg);

	void info(String msg);

	void debug(String msg);

	void debug(String msg, Throwable e);

	void warn(String msg);

	void warn(String msg, Throwable e);
}
