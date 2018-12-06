package com.dianping.zebra.filter.mock;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Test;

import com.dianping.zebra.Constants;
import com.dianping.zebra.filter.DefaultJdbcFilter;
import com.dianping.zebra.filter.FilterManager;
import com.dianping.zebra.filter.FilterManagerFactory;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.filter.SQLProcessContext;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.jdbc.DataSourceEntry;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.group.jdbc.MultiDatabaseTestCase;
import com.dianping.zebra.group.util.DaoContextHolder;

import junit.framework.Assert;

public class FinalSqlTest extends MultiDatabaseTestCase {

    public class FinalSqlFilter extends DefaultJdbcFilter {

        private String expectedSQL;

        public void setExpectedSQL(String expectedSQL) {
            this.expectedSQL = expectedSQL;
        }

        @Override
        public String processSQL(DataSourceConfig dsConfig, SQLProcessContext ctx, JdbcFilter chain)
                throws SQLException {
            String sql = chain.processSQL(dsConfig, ctx, chain);
            Assert.assertEquals(expectedSQL, sql);
            System.out.println(sql);
            return sql;
        }
    }

    private GroupDataSource ds = null;

    private FinalSqlFilter finalSql = new FinalSqlFilter();

    @AfterClass
    public static void clear() {
        FilterManager filterManager = FilterManagerFactory.getFilterManager();
        filterManager.clear();
    }

    @Test
    public void test() throws SQLException{
        test_select_prepareStatement_zebra_hint();
    }

    public void test_select_prepareStatement_zebra_hint() throws SQLException {
        GroupDataSource ds = getDataSource();

        finalSql.setExpectedSQL("/*id:e00767dd*//*+zebra:w*/select * from PERSON");

        Connection conn = ds.getConnection();
        DaoContextHolder.setSqlName("test1");
        PreparedStatement ptmt = conn.prepareStatement("/*+zebra:w*/select * from PERSON");
        ptmt.executeQuery();

        ptmt.close();
        conn.close();
    }

    protected GroupDataSource getDataSource() {
        if (this.ds == null) {
            ds = new GroupDataSource(getResourceId());
            FilterManager filterManager = FilterManagerFactory.getFilterManager();
            filterManager.addFilter("finalsql", finalSql);

            ds.setConfigManagerType(getConfigManagerType());
            ds.init();
        }

        return ds;
    }

    @Override
    protected String getConfigManagerType() {
        return Constants.CONFIG_MANAGER_TYPE_LOCAL;
    }

    @Override
    protected String getResourceId() {
        return "sample.ds.v3";
    }

    @Override
    protected String getSchema() {
        return getClass().getResource("/schema.sql").getPath();
    }

    @Override
    protected DataSourceEntry[] getDataSourceEntryArray() {
        DataSourceEntry[] entries = new DataSourceEntry[3];

        DataSourceEntry entry1 = new DataSourceEntry("jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets.xml",
                true);
        DataSourceEntry entry2 = new DataSourceEntry("jdbc:h2:mem:test1;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets1.xml",
                false);
        DataSourceEntry entry3 = new DataSourceEntry("jdbc:h2:mem:test2;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets2.xml",
                false);

        entries[0] = entry1;
        entries[1] = entry2;
        entries[2] = entry3;

        return entries;
    }
}
