package com.dianping.zebra.filter;

/**
 * Created by Dozer on 9/2/14.
 */

import com.dianping.zebra.Constants;
import junit.framework.Assert;
import org.junit.Test;

import com.dianping.zebra.filter.FilterManagerFactory;
import com.dianping.zebra.filter.JdbcFilter;

import java.util.List;

public class DefaultFilterManagerTest {

	@Test
	public void test_load_by_empty_name() {
		List<JdbcFilter> filters = FilterManagerFactory.getFilterManager().loadFilters("",  Constants.CONFIG_MANAGER_TYPE_REMOTE, null);
		Assert.assertEquals(filters.size(), 0);
	}

	@Test
	public void test_load_by_name() {
		List<JdbcFilter> filters  = FilterManagerFactory.getFilterManager().loadFilters("wall",  Constants.CONFIG_MANAGER_TYPE_REMOTE, null);
		Assert.assertEquals(filters.size(), 1);
	}

	@Test
	public void test_load_by_two_name() {
		List<JdbcFilter> filters = FilterManagerFactory.getFilterManager().loadFilters("wall,no_exist",  Constants.CONFIG_MANAGER_TYPE_REMOTE, null);
		Assert.assertEquals(filters.size(), 1);
	}

	@Test
	public void test_load_null() {
		List<JdbcFilter> filters = FilterManagerFactory.getFilterManager().loadFilters(null,  Constants.CONFIG_MANAGER_TYPE_REMOTE, null);
		Assert.assertEquals(filters.size(), 0);
	}
}
