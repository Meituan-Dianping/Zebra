package com.dianping.zebra.log;

import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;

public class Log4jLogger implements Logger {
	private org.apache.log4j.Logger logger;

	static {
		init();
	}

	private static synchronized void init() {
		new DOMConfigurator().doConfigure(Log4jLogger.class.getResource("zebra_log4j.xml"),
		      LogManager.getLoggerRepository());
	}

	public Log4jLogger(String loggerName) {
		this.logger = org.apache.log4j.Logger.getLogger(loggerName);
	}

	@Override
	public void debug(String message) {
		logger.debug(message);
	}

	@Override
	public void debug(String msg, Throwable e) {

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
	public void error(String message, Throwable t) {
		logger.error(message, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}
}
