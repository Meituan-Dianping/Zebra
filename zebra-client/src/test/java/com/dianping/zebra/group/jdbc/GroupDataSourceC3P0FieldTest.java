package com.dianping.zebra.group.jdbc;

import java.util.List;

import org.junit.Test;

import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.single.jdbc.AbstractSingleDataSourceTest;
import com.google.common.collect.Lists;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class GroupDataSourceC3P0FieldTest extends AbstractSingleDataSourceTest {

    @Test
    public void test() {
        assertField(GroupDataSource.class, ComboPooledDataSource.class,
                Lists.newArrayList("setJdbcUrl", "setPassword", "setUser","setIdentityToken"));
    }

    @Override
    protected List<String> getNotSupportedMethod() {
        return Lists.newArrayList();
    }
}
