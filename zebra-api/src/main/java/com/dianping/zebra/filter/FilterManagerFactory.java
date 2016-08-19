package com.dianping.zebra.filter;

/**
 * Created by Dozer on 9/2/14.
 */
public class FilterManagerFactory {
	private volatile static FilterManager filterManager;

	public static FilterManager getFilterManager() {
		if (filterManager == null) {
			synchronized (FilterManagerFactory.class) {
				if (filterManager == null) {
					filterManager = new DefaultFilterManager();
					filterManager.init();
				}
			}
		}

		return filterManager;
	}
}
