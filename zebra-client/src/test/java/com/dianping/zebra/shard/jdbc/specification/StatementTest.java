/**
 * Project: zebra-client
 *
 * File Created at 2011-6-28
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

import com.dianping.zebra.Constants;
import com.dianping.zebra.filter.FilterManagerFactory;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.shard.jdbc.ShardConnection;
import com.dianping.zebra.shard.jdbc.ShardStatement;
import com.dianping.zebra.shard.jdbc.base.BaseTestCase;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO Comment of StatementTest
 *
 * @author Leo Liang
 *
 */
public class StatementTest extends BaseTestCase {

	private List<JdbcFilter> filters = FilterManagerFactory.getFilterManager().loadFilters("cat", Constants.CONFIG_MANAGER_TYPE_REMOTE, null);

	private Mockery	context	= new Mockery();

	protected String[] getSupportedOps() {
		return new String[] { "setDataSourceRepository", "getEventNotifier", "setSyncEventNotifier", "getAttachedResultSets",
				"setAttachedResultSets", "setResultSetType", "setResultSetConcurrency", "setResultSetHoldability",
				"getConnectionWrapper", "setConnection", "isReadOnly", "setReadOnly", "isAutoCommit",
				"setAutoCommit", "getRouter", "setRouter", "checkClosed", "addBatch", "clearBatch", "close", "execute",
				"executeQuery", "executeUpdate", "getConnection", "getMoreResults", "getResultSet",
				"getResultSetConcurrency", "getResultSetHoldability", "getResultSetType", "getUpdateCount", "isClosed","" +
				"getGeneratedKeys","setConcurrencyLevel"};
	}

	protected Object getTestObj() {
		return new ShardStatement(filters);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testClose() throws Exception {
		ShardStatement stmt = new ShardStatement(filters);

		final ResultSet rs1 = context.mock(ResultSet.class, "rs1");
		final ResultSet rs2 = context.mock(ResultSet.class, "rs2");
		Set<ResultSet> attachedRS = new HashSet<ResultSet>();
		attachedRS.add(rs1);
		attachedRS.add(rs2);
		stmt.setAttachedResultSets(attachedRS);

		final Statement stmt1 = context.mock(Statement.class, "stmt1");
		final Statement stmt2 = context.mock(Statement.class, "stmt2");
		List<Statement> actualStmts = new ArrayList<Statement>();
		actualStmts.add(stmt1);
		actualStmts.add(stmt2);
		Field field = ShardStatement.class.getDeclaredField("actualStatements");
		field.setAccessible(true);
		field.set(stmt, actualStmts);

		ShardConnection conn = new ShardConnection(filters);
		stmt.setConnection(conn);
		Set<Statement> attachedStatements = new HashSet<Statement>();
		attachedStatements.add(stmt);
		conn.setAttachedStatements(attachedStatements);

		context.checking(new Expectations() {
			{
				try {
					oneOf(stmt1).close();
					oneOf(stmt2).close();
					oneOf(rs1).close();
					oneOf(rs2).close();
				} catch (SQLException e) {
				}
			}
		});

		stmt.close();

		context.assertIsSatisfied();
		Assert.assertEquals(0, stmt.getAttachedResultSets().size());
		Assert.assertEquals(0, ((List<Statement>) field.get(stmt)).size());
		Assert.assertTrue(stmt.isClosed());
	}

	@Test
	public void testCloseThrowException() throws Exception {
		ShardStatement stmt = new ShardStatement(filters);

		final ResultSet rs1 = context.mock(ResultSet.class, "rs1");
		final ResultSet rs2 = context.mock(ResultSet.class, "rs2");
		Set<ResultSet> attachedRS = new HashSet<ResultSet>();
		attachedRS.add(rs1);
		attachedRS.add(rs2);
		stmt.setAttachedResultSets(attachedRS);

		final Statement stmt1 = context.mock(Statement.class, "stmt1");
		final Statement stmt2 = context.mock(Statement.class, "stmt2");
		List<Statement> actualStmts = new ArrayList<Statement>();
		actualStmts.add(stmt1);
		actualStmts.add(stmt2);
		Field field = ShardStatement.class.getDeclaredField("actualStatements");
		field.setAccessible(true);
		field.set(stmt, actualStmts);

		ShardConnection conn = new ShardConnection(filters);
		stmt.setConnection(conn);
		Set<Statement> attachedStatements = new HashSet<Statement>();
		attachedStatements.add(stmt);
		conn.setAttachedStatements(attachedStatements);

		context.checking(new Expectations() {
			{
				try {
					oneOf(stmt1).close();
					will(throwException(new SQLException()));
					oneOf(stmt2).close();
					oneOf(rs1).close();
					oneOf(rs2).close();
				} catch (SQLException e) {
				}
			}
		});

		try {
			stmt.close();
			Assert.fail();
		} catch (SQLException e) {
			Assert.assertTrue(true);
		}

		context.assertIsSatisfied();
	}

	@Test
	public void testCloseThrowException2() throws Exception {
		ShardStatement stmt = new ShardStatement(filters);

		final ResultSet rs1 = context.mock(ResultSet.class, "rs1");
		final ResultSet rs2 = context.mock(ResultSet.class, "rs2");
		Set<ResultSet> attachedRS = new HashSet<ResultSet>();
		attachedRS.add(rs1);
		attachedRS.add(rs2);
		stmt.setAttachedResultSets(attachedRS);

		final Statement stmt1 = context.mock(Statement.class, "stmt1");
		final Statement stmt2 = context.mock(Statement.class, "stmt2");
		List<Statement> actualStmts = new ArrayList<Statement>();
		actualStmts.add(stmt1);
		actualStmts.add(stmt2);
		Field field = ShardStatement.class.getDeclaredField("actualStatements");
		field.setAccessible(true);
		field.set(stmt, actualStmts);

		ShardConnection conn = new ShardConnection(filters);
		stmt.setConnection(conn);
		Set<Statement> attachedStatements = new HashSet<Statement>();
		attachedStatements.add(stmt);
		conn.setAttachedStatements(attachedStatements);

		context.checking(new Expectations() {
			{
				try {
					oneOf(stmt1).close();
					oneOf(stmt2).close();
					oneOf(rs1).close();
					will(throwException(new SQLException()));
					oneOf(rs2).close();
				} catch (SQLException e) {
				}
			}
		});

		try {
			stmt.close();
			Assert.fail();
		} catch (SQLException e) {
			Assert.assertTrue(true);
		}

		context.assertIsSatisfied();
	}
}
