package com.dianping.zebra.log;

public class EmptyLogger implements Logger {
	public EmptyLogger(String loggerName) {

	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public void error(String msg, Throwable e) {

	}

	@Override
	public void error(String msg) {

	}

	@Override
	public void info(String msg) {

	}

	@Override
	public void debug(String msg) {

	}

	@Override
	public void debug(String msg, Throwable e) {

	}

	@Override
	public void warn(String msg) {

	}

	@Override
	public void warn(String msg, Throwable e) {

	}
}
