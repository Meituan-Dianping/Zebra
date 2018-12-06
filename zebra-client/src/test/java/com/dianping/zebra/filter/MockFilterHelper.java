package com.dianping.zebra.filter;

/**
 * Created by Dozer on 9/10/14.
 */

import org.mockito.Mockito;

import com.dianping.zebra.filter.DefaultJdbcFilter;
import com.dianping.zebra.filter.FilterManager;
import com.dianping.zebra.filter.FilterManagerFactory;
import com.dianping.zebra.filter.JdbcFilter;

public class MockFilterHelper {
    private static JdbcFilter mockedFilter = Mockito.spy(new DefaultJdbcFilter());

    public static JdbcFilter getMockedFilter() {
        return mockedFilter;
    }

    public static void injectMockFilter() {
        FilterManager manager = FilterManagerFactory.getFilterManager();
        manager.addFilter("mock", mockedFilter);
    }
}