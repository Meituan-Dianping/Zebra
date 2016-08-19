package com.dianping.zebra.shard.jdbc.parallel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dianping.zebra.shard.jdbc.parallel.MockCallable.MockType;

import junit.framework.Assert;

public class SQLThreadPoolExecutorTest {

	@BeforeClass
	public static void prepare() {
		SQLThreadPoolExecutor.corePoolSize = 3;
		SQLThreadPoolExecutor.maxPoolSize = 3;
		SQLThreadPoolExecutor.executeTimeOut = 1000L;
	}

	@Test
	public void testNormal() {
		SQLThreadPoolExecutor instance = SQLThreadPoolExecutor.getInstance();

		List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();

		tasks.add(new MockCallable(MockType.NORMAL, 100L, 1));
		tasks.add(new MockCallable(MockType.NORMAL, 300L, 2));
		tasks.add(new MockCallable(MockType.NORMAL, 400L, 3));
		tasks.add(new MockCallable(MockType.NORMAL, 400L, 4));

		try {
			List<Future<Integer>> invokeSQLs = instance.invokeSQLs(tasks);

			int row = 0;
			for (Future<Integer> f : invokeSQLs) {
				try {
					row += f.get();
				} catch (Exception ignore) {
				}
			}

			Assert.assertEquals(10, row);
		} catch (SQLException e) {
			Assert.assertEquals("One of your sql's execution time is beyond 1000 milliseconds.", e.getMessage());
		}
	}

	@Test
	public void testTimeOut() {
		SQLThreadPoolExecutor instance = SQLThreadPoolExecutor.getInstance();

		List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();

		tasks.add(new MockCallable(MockType.NORMAL, 100L, 1));
		tasks.add(new MockCallable(MockType.NORMAL, 300L, 1));
		tasks.add(new MockCallable(MockType.TIMEOUT, 400L, 1));
		tasks.add(new MockCallable(MockType.NORMAL, 400L, 1));

		try {
			instance.invokeSQLs(tasks);
		} catch (SQLException e) {
			Assert.assertEquals("One of your sql's execution time is beyond 1000 milliseconds.", e.getMessage());
		}
	}

	@Test
	public void testExecuteError() {
		SQLThreadPoolExecutor instance = SQLThreadPoolExecutor.getInstance();

		List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();

		tasks.add(new MockCallable(MockType.NORMAL, 100L, 1));
		tasks.add(new MockCallable(MockType.NORMAL, 300L, 1));
		tasks.add(new MockCallable(MockType.SQLEXCEPTION, 400L, 1));
		tasks.add(new MockCallable(MockType.NORMAL, 400L, 1));

		try {
			instance.invokeSQLs(tasks);
		} catch (SQLException e) {
			Assert.assertEquals("SQLERROR", e.getCause().getMessage());
		}
	}

	@Test
	public void testIntruppter() {
		SQLThreadPoolExecutor instance = SQLThreadPoolExecutor.getInstance();

		List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();

		tasks.add(new MockCallable(MockType.NORMAL, 100L, 1));
		tasks.add(new MockCallable(MockType.NORMAL, 300L, 1));
		tasks.add(new MockCallable(MockType.INTERRUPT, 400L, 1));
		tasks.add(new MockCallable(MockType.NORMAL, 400L, 1));

		try {
			instance.invokeSQLs(tasks);
		} catch (SQLException e) {
			Assert.assertEquals(InterruptedException.class, e.getCause().getClass());
		}
	}
	
	@Test
	public void testSubmitTimeout() {
		SQLThreadPoolExecutor instance = SQLThreadPoolExecutor.getInstance();

		List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();

		tasks.add(new MockCallable(MockType.NORMAL, 900L, 1));
		tasks.add(new MockCallable(MockType.NORMAL, 900L, 1));
		tasks.add(new MockCallable(MockType.NORMAL, 900L, 1));

		try {
			instance.invokeSQLs(tasks);
		} catch (SQLException e1) {
			Assert.fail();
		}

		tasks.add(new MockCallable(MockType.NORMAL, 900L, 1));
		try {
			instance.invokeSQLs(tasks);
		} catch (SQLException e) {
			Assert.assertEquals(TimeoutException.class, e.getCause().getClass());
		}
	}
}
