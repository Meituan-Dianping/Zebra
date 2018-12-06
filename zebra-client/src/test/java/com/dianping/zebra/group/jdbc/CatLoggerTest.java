package com.dianping.zebra.group.jdbc;

import com.dianping.zebra.Constants;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class CatLoggerTest extends MultiDatabaseTestCase {

    private static final String SQL_STATEMENT_NAME = "sql_statement_name";

    @Test
    public void testStatementQuery() throws Exception {
        execute(new ConnectionCallback() {

            @Override
            public Object doInConnection(Connection conn) throws Exception {
                Statement cstmt = conn.createStatement();
                cstmt.execute("SELECT ID,NAME,AGE from PERSON");
                return null;
            }
        });
    }

    @Test
    public void testStatementUpdate() throws Exception {
        execute(new ConnectionCallback() {

            @Override
            public Object doInConnection(Connection conn) throws Exception {
                Statement cstmt = conn.createStatement();
                cstmt.execute("insert into PERSON(NAME,LAST_NAME,AGE) values ('zhuhao','damon',12)");
                return null;
            }
        });
    }

    @Test
    public void testStatementBatch() throws Exception {
        execute(new ConnectionCallback() {

            @Override
            public Object doInConnection(Connection conn) throws Exception {
                Statement cstmt = conn.createStatement();
                cstmt.addBatch("insert into PERSON(NAME,LAST_NAME,AGE) values ('zhuhao','damon',12)");

                cstmt.executeBatch();
                return null;
            }
        });
    }

    @Test
    public void testPreparedStatementQuery() throws Exception {
        execute(new ConnectionCallback() {

            @Override
            public Object doInConnection(Connection conn) throws Exception {
                PreparedStatement stmt = conn.prepareStatement("SELECT ID,NAME,AGE from PERSON where AGE = ?");
                stmt.setInt(1, 18);
                stmt.execute();
                return null;
            }
        });
    }

    @Test
    public void testPreparedStatementUpdate() throws Exception {
        execute(new ConnectionCallback() {

            @Override
            public Object doInConnection(Connection conn) throws Exception {
                PreparedStatement stmt = conn.prepareStatement("update PERSON p set p.Name = ? where p.NAME = ?");
                stmt.setString(1, "writer-new");
                stmt.setString(2, "writer");
                stmt.execute();
                return null;
            }
        });
    }

    @Test
    public void testPreparedStatementBatch() throws Exception {
        execute(new ConnectionCallback() {

            @Override
            public Object doInConnection(Connection conn) throws Exception {
                PreparedStatement stmt = conn.prepareStatement("update PERSON p set p.Name = ? where p.NAME = ?");
                stmt.setString(1, "writer-new0");
                stmt.setString(2, "writer0");

                stmt.addBatch();

                stmt.setString(1, "writer-new1");
                stmt.setString(2, "writer1");

                stmt.addBatch();

                stmt.executeBatch();
                return null;
            }
        });
    }

    @Override
    protected String getConfigManagerType() {
        return Constants.CONFIG_MANAGER_TYPE_LOCAL;
    }

    @Override
    protected String getResourceId() {
        return "sample.ds.v2";
    }

    @Override
    protected String getSchema() {
        return getClass().getResource("/schema.sql").getPath();
    }

    @Override
    protected DataSourceEntry[] getDataSourceEntryArray() {
        DataSourceEntry[] entries = new DataSourceEntry[3];

        DataSourceEntry entry1 = new DataSourceEntry("jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1", "datasets.xml", true);
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
