package com.dianping.zebra.shard.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Test;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;

import junit.framework.Assert;

public class MultiDBPreparedStatementLifeCycleJoinTest extends MultiDBBaseTestCase {

    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "db-datafiles/createtable-multidb-join-lifecycle.xml";
    }

    @Override
    protected String getDataFile() {
        return "db-datafiles/data-join-lifecycle.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] { "ctx-multidb-join-lifecycle.xml" };
    }

    @Test
    public void testGlobalJoin() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select test.* from test,age where test.id = age.id");
            ResultSet rs = stmt.executeQuery();

            Set<String> results = new HashSet<String>();
            while (rs.next()) {
                results.add(rs.getString(2) + "-" + rs.getString(3) + "-" + rs.getString(4)
                        + "-" + rs.getString(5) + "-" + rs.getString(6));
            }

            Assert.assertEquals(8, results.size());
            Assert.assertEquals(true, results.contains("conan1-1-3-a-4"));
            Assert.assertEquals(true, results.contains("conan1-1-7-b-5"));
            Assert.assertEquals(true, results.contains("conan1-1-3-a-2"));
            Assert.assertEquals(true, results.contains("conan1-1-5-a-3"));
            Assert.assertEquals(true, results.contains("conan1-1-3-a-1"));
            Assert.assertEquals(true, results.contains("conan1-1-2-a-0"));
            Assert.assertEquals(true, results.contains("conan1-1-8-b-6"));
            Assert.assertEquals(true, results.contains("conan1-1-9-b-7"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testGlobalSelect() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select * from age");
            ResultSet rs = stmt.executeQuery();

            Set<String> results = new HashSet<String>();

            while (rs.next()) {
                results.add(rs.getString(2));
            }

            Assert.assertEquals(1, results.size());
            Assert.assertEquals(true, results.contains("conan1"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testBindingTableJoin() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select test.* from test,people where test.id = people.id ");
            ResultSet rs = stmt.executeQuery();

            Set<String> results = new HashSet<String>();

            while (rs.next()) {
                results.add(rs.getString(2) + "-" + rs.getString(3) + "-" + rs.getString(4)
                        + "-" + rs.getString(5) + "-" + rs.getString(6));
            }

            Assert.assertEquals(36, results.size());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testBindingTableJoin2() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select test.* from test join people on test.id = people.id where test.id = 1");
            ResultSet rs = stmt.executeQuery();

            Set<String> results = new HashSet<String>();

            while (rs.next()) {
                results.add(rs.getString(2) + "-" + rs.getString(3) + "-" + rs.getString(4)
                        + "-" + rs.getString(5) + "-" + rs.getString(6));
            }

            Assert.assertEquals(1, results.size());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testBindingTableJoin3() throws SQLException {
        DataSource ds = (DataSource) context.getBean("zebraDS");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select test.* from test left join people on test.id = people.id where test.id = 1");
            ResultSet rs = stmt.executeQuery();

            Set<String> results = new HashSet<String>();

            while (rs.next()) {
                results.add(rs.getString(2) + "-" + rs.getString(3) + "-" + rs.getString(4)
                        + "-" + rs.getString(5) + "-" + rs.getString(6));
            }

            Assert.assertEquals(1, results.size());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

//	@Test
//	public void testBindingTableJoin3() throws SQLException {
//		DataSource ds = (DataSource) context.getBean("zebraDS");
//		Connection conn = null;
//		try {
//			conn = ds.getConnection();
//			PreparedStatement stmt = conn.prepareStatement("update test t join people p on t.id = p.id set t.name = 'zhuhao'");
//			int executeUpdate = stmt.executeUpdate();
//
//			Assert.assertEquals(36, executeUpdate);
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail();
//		} finally {
//			if (conn != null) {
//				conn.close();
//			}
//		}
//	}
}
