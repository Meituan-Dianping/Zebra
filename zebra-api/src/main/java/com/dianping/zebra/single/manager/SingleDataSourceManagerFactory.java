package com.dianping.zebra.single.manager;

public final class SingleDataSourceManagerFactory {
	private volatile static SingleDataSourceManager dataSourceManager;

	private SingleDataSourceManagerFactory() {
	}

	public static SingleDataSourceManager getDataSourceManager() {
		if (dataSourceManager == null) {
			synchronized (SingleDataSourceManagerFactory.class) {
				if (dataSourceManager == null) {
					dataSourceManager = new DefaultSingleDataSourceManager();
					dataSourceManager.init();
				}
			}
		}

		return dataSourceManager;
	}
}
