package com.dianping.zebra.log;

public class Slf4jLogger implements Logger {
	private org.slf4j.Logger logger;

	public Slf4jLogger(String loggerName) {
		this.logger = org.slf4j.LoggerFactory.getLogger(loggerName);
	}

	@Override
	public void debug(String message) {
		logger.debug(message);
	}

	@Override
	public void debug(String msg, Throwable e) {
		logger.debug(msg, e);
	}

	@Override
	public void info(String message) {
		logger.info(message);
	}

	@Override
	public void warn(String message) {
		logger.warn(message);
	}

	@Override
	public void warn(String message, Throwable t) {
		logger.warn(message, t);
	}

	@Override
	public void error(String message) {
		logger.error(message);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public void error(String message, Throwable t) {
		logger.error(message, t);
	}
}
