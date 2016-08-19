/**
 * Project: zebra-client
 * 
 * File Created at 2011-6-27
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.jdbc.specification;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import com.dianping.zebra.shard.jdbc.ShardConnection;
import com.dianping.zebra.shard.jdbc.ShardDatabaseMetaData;
import com.dianping.zebra.shard.jdbc.ShardPreparedStatement;
import com.dianping.zebra.shard.jdbc.ShardStatement;
import com.dianping.zebra.shard.jdbc.base.BaseTestCase;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Leo Liang
 * 
 */
@SuppressWarnings("resource")
public class ConnectionTest extends BaseTestCase {

	private Mockery context = new Mockery();

	protected String[] getSupportedOps() {
		return new String[] { "setDataSourceRepository", "getAttachedStatements", "setAttachedStatements", "setClosed",
				"getRealConnection", "setRealConnection", "setActualConnections", "getUsername", "setUsername",
				"getPassword", "setPassword", "getRouter", "setRouter", "close", "commit", "createStatement",
				"getAutoCommit", "getMetaData", "getTransactionIsolation", "isClosed", "isReadOnly", "prepareStatement",
				"rollback", "setAutoCommit", "setReadOnly", "setTransactionIsolation" };
	}

	protected Object getTestObj() {
		return new ShardConnection();
	}

	@Test
	public void testClose() throws Exception {
		ShardConnection conn = new ShardConnection();

		Map<String, Connection> actualConnections = new HashMap<String, Connection>();
		final Connection conn1 = context.mock(Connection.class, "conn1");
		final Connection conn2 = context.mock(Connection.class, "conn2");
		actualConnections.put("test-conn1", conn1);
		actualConnections.put("test-conn2", conn2);
		conn.setActualConnections(actualConnections);

		Set<Statement> attachedStatements = new HashSet<Statement>();
		final Statement stmt1 = context.mock(Statement.class, "stmt1");
		final Statement stmt2 = context.mock(Statement.class, "stmt2");
		attachedStatements.add(stmt1);
		attachedStatements.add(stmt2);
		conn.setAttachedStatements(attachedStatements);

		context.checking(new Expectations() {
			{
				try {
					oneOf(conn1).close();
					oneOf(conn2).close();
					oneOf(stmt1).close();
					oneOf(stmt2).close();
				} catch (SQLException e) {
				}
			}
		});

		conn.close();
		context.assertIsSatisfied();

		Assert.assertEquals(0, conn.getAttachedStatements().size());
		Assert.assertEquals(0, conn.getAttachedStatements().size());
		Assert.assertTrue(conn.isClosed());
	}

	// @Test
	public void testCloseThrowException() throws Exception {
		ShardConnection conn = new ShardConnection();

		Map<String, Connection> actualConnections = new HashMap<String, Connection>();
		final Connection conn1 = context.mock(Connection.class, "conn1");
		final Connection conn2 = context.mock(Connection.class, "conn2");
		actualConnections.put("test-conn1", conn1);
		actualConnections.put("test-conn2", conn2);
		conn.setActualConnections(actualConnections);

		Set<Statement> attachedStatements = new HashSet<Statement>();
		final Statement stmt1 = context.mock(Statement.class, "stmt1");
		final Statement stmt2 = context.mock(Statement.class, "stmt2");
		attachedStatements.add(stmt1);
		attachedStatements.add(stmt2);
		conn.setAttachedStatements(attachedStatements);

		context.checking(new Expectations() {
			{
				try {
					oneOf(conn1).close();
					will(throwException(new SQLException()));
					oneOf(conn2).close();
					oneOf(stmt1).close();
					oneOf(stmt2).close();
				} catch (SQLException e) {
				}
			}
		});

		try {
			conn.close();
			Assert.fail();
		} catch (SQLException e) {
			Assert.assertTrue(true);
		}
		context.assertIsSatisfied();

	}

	// @Test
	public void testCloseThrowException2() throws Exception {
		ShardConnection conn = new ShardConnection();

		Map<String, Connection> actualConnections = new HashMap<String, Connection>();
		final Connection conn1 = context.mock(Connection.class, "conn1");
		final Connection conn2 = context.mock(Connection.class, "conn2");
		actualConnections.put("test-conn1", conn1);
		actualConnections.put("test-conn2", conn2);
		conn.setActualConnections(actualConnections);

		Set<Statement> attachedStatements = new HashSet<Statement>();
		final Statement stmt1 = context.mock(Statement.class, "stmt1");
		final Statement stmt2 = context.mock(Statement.class, "stmt2");
		attachedStatements.add(stmt1);
		attachedStatements.add(stmt2);
		conn.setAttachedStatements(attachedStatements);

		context.checking(new Expectations() {
			{
				try {
					oneOf(conn1).close();
					oneOf(conn2).close();
					oneOf(stmt1).close();
					will(throwException(new SQLException()));
					oneOf(stmt2).close();
				} catch (SQLException e) {
				}
			}
		});

		try {
			conn.close();
			Assert.fail();
		} catch (SQLException e) {
			Assert.assertTrue(true);
		}
		context.assertIsSatisfied();

	}

	@Test
	public void testCommit() throws Exception {
		ShardConnection conn = new ShardConnection();

		Map<String, Connection> actualConnections = new HashMap<String, Connection>();
		final Connection conn1 = context.mock(Connection.class, "conn1");
		final Connection conn2 = context.mock(Connection.class, "conn2");
		actualConnections.put("test-conn1", conn1);
		actualConnections.put("test-conn2", conn2);
		conn.setActualConnections(actualConnections);
		conn.setAutoCommit(false);

		context.checking(new Expectations() {
			{
				try {
					oneOf(conn1).commit();
					oneOf(conn2).commit();
				} catch (SQLException e) {
				}
			}
		});

		conn.commit();
		context.assertIsSatisfied();

	}

	// @Test
	public void testCommitThrowException() throws Exception {
		ShardConnection conn = new ShardConnection();

		Map<String, Connection> actualConnections = new HashMap<String, Connection>();
		final Connection conn1 = context.mock(Connection.class, "conn1");
		final Connection conn2 = context.mock(Connection.class, "conn2");
		actualConnections.put("test-conn1", conn1);
		actualConnections.put("test-conn2", conn2);
		conn.setActualConnections(actualConnections);
		conn.setAutoCommit(false);

		context.checking(new Expectations() {
			{
				try {
					oneOf(conn1).commit();
					will(throwException(new SQLException()));
					oneOf(conn2).commit();
				} catch (SQLException e) {
				}
			}
		});

		try {
			conn.commit();
			Assert.fail();
		} catch (SQLException e) {
			Assert.assertTrue(true);
		}
		context.assertIsSatisfied();

	}

	@Test
	public void testCloseAutoCommitTrue() throws Exception {
		ShardConnection conn = new ShardConnection();

		Map<String, Connection> actualConnections = new HashMap<String, Connection>();
		final Connection conn1 = context.mock(Connection.class, "conn1");
		final Connection conn2 = context.mock(Connection.class, "conn2");
		actualConnections.put("test-conn1", conn1);
		actualConnections.put("test-conn2", conn2);
		conn.setActualConnections(actualConnections);

		context.checking(new Expectations() {
			{
				try {
					never(conn1).commit();
					never(conn2).commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		conn.commit();
		context.assertIsSatisfied();

	}

	@Test
	public void testCreateStatement() throws Exception {
		ShardConnection conn = new ShardConnection();
		Statement stmt = conn.createStatement();
		Assert.assertNotNull(stmt);
		Assert.assertTrue((stmt instanceof ShardStatement));
		Assert.assertEquals(1, conn.getAttachedStatements().size());
		Assert.assertTrue(conn.getAttachedStatements().contains(stmt));
	}

	@Test
	public void testCreateStatement2() throws Exception {
		ShardConnection conn = new ShardConnection();
		Statement stmt = conn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
		Assert.assertNotNull(stmt);
		Assert.assertTrue((stmt instanceof ShardStatement));
		Assert.assertEquals(1, conn.getAttachedStatements().size());
		Assert.assertTrue(conn.getAttachedStatements().contains(stmt));
	}

	@Test
	public void testCreateStatement3() throws Exception {
		ShardConnection conn = new ShardConnection();
		Statement stmt = conn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY,
				ResultSet.HOLD_CURSORS_OVER_COMMIT);
		Assert.assertNotNull(stmt);
		Assert.assertTrue((stmt instanceof ShardStatement));
		Assert.assertEquals(1, conn.getAttachedStatements().size());
		Assert.assertTrue(conn.getAttachedStatements().contains(stmt));
	}

	@Test
	public void testCreateStatement4() throws Exception {
		ShardConnection conn = new ShardConnection();
		conn.close();
		try {
			conn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY,
					ResultSet.HOLD_CURSORS_OVER_COMMIT);
			Assert.fail();
		} catch (SQLException e) {
			Assert.assertTrue(true);
		}

	}

	@Test
	public void testGetMetaData() throws Exception {
		ShardConnection conn = new ShardConnection();
		DatabaseMetaData meta = conn.getMetaData();
		Assert.assertNotNull(meta);
		Assert.assertTrue((meta instanceof ShardDatabaseMetaData));
	}

	@Test
	public void testPrepareStatement() throws Exception {
		ShardConnection conn = new ShardConnection();
		Statement stmt = conn.prepareStatement("SELECT * FROM A");
		Assert.assertNotNull(stmt);
		Assert.assertTrue((stmt instanceof ShardPreparedStatement));
		Assert.assertEquals(1, conn.getAttachedStatements().size());
		Assert.assertTrue(conn.getAttachedStatements().contains(stmt));
	}

	@Test
	public void testPrepareStatement2() throws Exception {
		ShardConnection conn = new ShardConnection();
		Statement stmt = conn.prepareStatement("SELECT * FROM A", Statement.NO_GENERATED_KEYS);
		Assert.assertNotNull(stmt);
		Assert.assertTrue((stmt instanceof ShardPreparedStatement));
		Assert.assertEquals(1, conn.getAttachedStatements().size());
		Assert.assertTrue(conn.getAttachedStatements().contains(stmt));
	}

	@Test
	public void testPrepareStatement3() throws Exception {
		ShardConnection conn = new ShardConnection();
		Statement stmt = conn.prepareStatement("SELECT * FROM A", new int[] {});
		Assert.assertNotNull(stmt);
		Assert.assertTrue((stmt instanceof ShardPreparedStatement));
		Assert.assertEquals(1, conn.getAttachedStatements().size());
		Assert.assertTrue(conn.getAttachedStatements().contains(stmt));
	}

	@Test
	public void testPrepareStatement4() throws Exception {
		ShardConnection conn = new ShardConnection();
		Statement stmt = conn.prepareStatement("SELECT * FROM A", new String[] {});
		Assert.assertNotNull(stmt);
		Assert.assertTrue((stmt instanceof ShardPreparedStatement));
		Assert.assertEquals(1, conn.getAttachedStatements().size());
		Assert.assertTrue(conn.getAttachedStatements().contains(stmt));
	}

	@Test
	public void testPrepareStatement5() throws Exception {
		ShardConnection conn = new ShardConnection();
		Statement stmt = conn.prepareStatement("SELECT * FROM A", ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
		Assert.assertNotNull(stmt);
		Assert.assertTrue((stmt instanceof ShardPreparedStatement));
		Assert.assertEquals(1, conn.getAttachedStatements().size());
		Assert.assertTrue(conn.getAttachedStatements().contains(stmt));
	}

	@Test
	public void testPrepareStatement6() throws Exception {
		ShardConnection conn = new ShardConnection();
		Statement stmt = conn.prepareStatement("SELECT * FROM A", ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY,
				ResultSet.HOLD_CURSORS_OVER_COMMIT);
		Assert.assertNotNull(stmt);
		Assert.assertTrue((stmt instanceof ShardPreparedStatement));
		Assert.assertEquals(1, conn.getAttachedStatements().size());
		Assert.assertTrue(conn.getAttachedStatements().contains(stmt));
	}

	@Test
	public void testRollback() throws Exception {
		ShardConnection conn = new ShardConnection();

		Map<String, Connection> actualConnections = new HashMap<String, Connection>();
		final Connection conn1 = context.mock(Connection.class, "conn1");
		final Connection conn2 = context.mock(Connection.class, "conn2");
		actualConnections.put("test-conn1", conn1);
		actualConnections.put("test-conn2", conn2);
		conn.setActualConnections(actualConnections);
		conn.setAutoCommit(false);

		context.checking(new Expectations() {
			{
				try {
					oneOf(conn1).rollback();
					oneOf(conn2).rollback();
				} catch (SQLException e) {
				}
			}
		});

		conn.rollback();
		context.assertIsSatisfied();

	}

	// @Test
	public void testRollbackThrowException() throws Exception {
		ShardConnection conn = new ShardConnection();

		Map<String, Connection> actualConnections = new HashMap<String, Connection>();
		final Connection conn1 = context.mock(Connection.class, "conn1");
		final Connection conn2 = context.mock(Connection.class, "conn2");
		actualConnections.put("test-conn1", conn1);
		actualConnections.put("test-conn2", conn2);
		conn.setActualConnections(actualConnections);
		conn.setAutoCommit(false);

		context.checking(new Expectations() {
			{
				try {
					oneOf(conn1).rollback();
					will(throwException(new SQLException()));
					oneOf(conn2).rollback();
				} catch (SQLException e) {
				}
			}
		});

		try {
			conn.rollback();
			Assert.fail();
		} catch (SQLException e) {
			Assert.assertTrue(true);
		}
		context.assertIsSatisfied();

	}

	@Test
	public void testRollbackClosed() throws Exception {
		ShardConnection conn = new ShardConnection();

		Map<String, Connection> actualConnections = new HashMap<String, Connection>();
		final Connection conn1 = context.mock(Connection.class, "conn1");
		final Connection conn2 = context.mock(Connection.class, "conn2");
		actualConnections.put("test-conn1", conn1);
		actualConnections.put("test-conn2", conn2);
		conn.setActualConnections(actualConnections);
		conn.setAutoCommit(false);

		context.checking(new Expectations() {
			{
				try {
					oneOf(conn1).close();
					oneOf(conn2).close();
					never(conn1).rollback();
					never(conn2).rollback();
				} catch (SQLException e) {
				}
			}
		});

		conn.close();

		try {
			conn.rollback();
			Assert.fail();
		} catch (SQLException e) {
			Assert.assertTrue(true);
		}
		context.assertIsSatisfied();

	}
}
