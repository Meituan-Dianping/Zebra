package com.dianping.zebra.group.router;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;

import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.log.LoggerLoader;

public class DpdlReadWriteStrategy implements ReadWriteStrategy {
	private static final Logger logger = LoggerLoader.getLogger(DpdlReadWriteStrategy.class);

	private static Method getContextMethod = null;

	private static Method setContextMethod = null;

	private static Method isAuthenticatedMethod = null;

	private static Method setAuthenticatedMethod = null;

	private GroupDataSourceConfig config;

	static {
		try {
			Class<?> contextHolderClass = Class.forName("com.dianping.avatar.tracker.ExecutionContextHolder");
			Class<?> contextClass = Class.forName("com.dianping.avatar.tracker.TrackerContext");

			getContextMethod = contextHolderClass.getDeclaredMethod("getTrackerContext", new Class[] {});
			setContextMethod = contextHolderClass.getDeclaredMethod("setTrackerContext", new Class[] { contextClass });
			isAuthenticatedMethod = contextClass.getDeclaredMethod("isAuthenticated", new Class[] {});
			setAuthenticatedMethod = contextClass.getDeclaredMethod("setAuthenticated", new Class[] { boolean.class });

			getContextMethod.setAccessible(true);
			isAuthenticatedMethod.setAccessible(true);
			setAuthenticatedMethod.setAccessible(true);
		} catch (Throwable ignore) {
		}
	}

	@Override
	public boolean shouldReadFromMaster() {
		if (config != null && config.getForceWriteOnLogin()) {
			try {
				Object context = getContextMethod.invoke(null);

				if (context != null) {
					return (Boolean) isAuthenticatedMethod.invoke(context);
				}
			} catch (Exception error) {
				logger.error(error.getMessage(), error);
			}
		}

		return false;
	}

	protected static void setReadFromMaster() {
		try {
			Object context = getContextMethod.invoke(null);

			if (context == null) {
				Class<?> contextClass = Class.forName("com.dianping.avatar.tracker.TrackerContext");
				context = contextClass.newInstance();
				setContextMethod.invoke(null, context);
			}

			setAuthenticatedMethod.invoke(context, true);
		} catch (Exception error) {
			logger.error(error.getMessage(), error);
		}
	}

	@Override
	public void setGroupDataSourceConfig(GroupDataSourceConfig config) {
		this.config = config;
	}
}
