package com.dianping.zebra.filter;

import java.util.List;

/**
 * Created by Dozer on 9/2/14.
 */
public interface FilterManager {
	void addFilter(String name, JdbcFilter filter);

	void init();

	List<JdbcFilter> loadFilters(String strConfig);
}
