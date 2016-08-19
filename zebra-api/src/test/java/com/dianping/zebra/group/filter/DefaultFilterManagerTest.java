package com.dianping.zebra.group.filter;

/**
 * Created by Dozer on 9/2/14.
 */

import junit.framework.Assert;
import org.junit.Test;

import com.dianping.zebra.filter.FilterManagerFactory;
import com.dianping.zebra.filter.JdbcFilter;

import java.util.List;

public class DefaultFilterManagerTest {

	@Test
	public void test_load_by_empty_name() {
		List<JdbcFilter> filters = FilterManagerFactory.getFilterManager().loadFilters("");
		Assert.assertEquals(filters.size(), 0);
	}

	@Test
	public void test_load_by_name() {
		List<JdbcFilter> filters  = FilterManagerFactory.getFilterManager().loadFilters("wall");
		Assert.assertEquals(filters.size(), 1);
	}

	@Test
	public void test_load_by_two_name() {
		List<JdbcFilter> filters = FilterManagerFactory.getFilterManager().loadFilters("wall,no_exist");
		Assert.assertEquals(filters.size(), 1);
	}

	@Test
	public void test_load_null() {
		List<JdbcFilter> filters = FilterManagerFactory.getFilterManager().loadFilters(null);
		Assert.assertEquals(filters.size(), 0);
	}
}
