package com.dianping.zebra.log;

import java.util.zip.Deflater;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class LoggerLoader {

	private static String LOG_ROOT = System.getProperty("zebra.log.dir", "/data/applogs/zebra");

	private static LoggerContext context = null;

	private static volatile boolean init = false;

	static {
		if (!init) {
			init();
			init = true;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static synchronized void init() {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		Layout layout = PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss}:%p %t %c - %m%n")
				.withConfiguration(config).withRegexReplacement(null).withCharset(null).withAlwaysWriteExceptions(true)
				.withNoConsoleNoAnsi(false).withHeader(null).withFooter(null).build();

		// file info
		Filter fileInfoFilter = ThresholdFilter.createFilter(Level.ERROR, Result.DENY, Result.ACCEPT);
		Appender fileInfoAppender = RollingFileAppender.createAppender(LOG_ROOT + "/zebra.log",
				LOG_ROOT + "/zebra.log.%d{yyyy-MM-dd}.gz", "true", "FileInfo", "true", "4000", "true",
				TimeBasedTriggeringPolicy.createPolicy("1", "true"),
				ZebraRolloverStrategy.createStrategy("30", "1", null, Deflater.DEFAULT_COMPRESSION + "", config),
				layout, fileInfoFilter, "false", null, null, config);
		config.addAppender(fileInfoAppender);
		fileInfoAppender.start();
		AppenderRef fileInfoRef = AppenderRef.createAppenderRef("FileInfo", null, fileInfoFilter);

		// console error
		Appender consoleErrorAppender = ConsoleAppender.createAppender(layout, null, "SYSTEM_ERR", "ConsoleError",
				"false", "false");
		config.addAppender(consoleErrorAppender);
		consoleErrorAppender.start();

		// console info
		Filter consoleWarnFilter = ThresholdFilter.createFilter(Level.ERROR, Result.DENY, Result.NEUTRAL);
		Appender consoleWarnAppender = ConsoleAppender.createAppender(layout, consoleWarnFilter, "SYSTEM_OUT",
				"ConsoleWarn", "false", "false");
		config.addAppender(consoleWarnAppender);
		consoleWarnAppender.start();
		AppenderRef consoleWarnAppenderRef = AppenderRef.createAppenderRef("ConsoleWarn", Level.WARN,
				consoleWarnFilter);
		AppenderRef consoleErrorAppenderRef = AppenderRef.createAppenderRef("ConsoleError", Level.WARN, null);

		AppenderRef[] refs = new AppenderRef[] { consoleErrorAppenderRef, consoleWarnAppenderRef, fileInfoRef };

		LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.INFO, "com.dianping.zebra", "true", refs,
				null, config, null);
		loggerConfig.addAppender(consoleErrorAppender, Level.ERROR, null);
		loggerConfig.addAppender(consoleWarnAppender, Level.INFO, null);
		loggerConfig.addAppender(fileInfoAppender, Level.INFO, null);

		config.addLogger("com.dianping.zebra", loggerConfig);

		ctx.updateLoggers();

		context = ctx;
	}

	public static Logger getLogger(Class<?> className) {
		return getLogger(className.getName());
	}

	public static Logger getLogger(String name) {
		if (context == null) {
			init();
		}
		return context.getLogger(name);
	}

	public static LoggerContext getLoggerContext() {
		return context;
	}
}
