package com.dianping.zebra.group.router;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

public class LocalContextReadWriteStrategyTest {

	@Test
	public void test_force_master() {
		LocalContextReadWriteStrategy.setReadFromMaster();
		LocalContextReadWriteStrategy strategy = new LocalContextReadWriteStrategy();

		Assert.assertTrue(strategy.shouldReadFromMaster());
	}

	@After
	public void clearContext(){
		LocalContextReadWriteStrategy.clearContext();
	}

	@Test
	public void test_clear_context() {
		LocalContextReadWriteStrategy strategy = new LocalContextReadWriteStrategy();
		Assert.assertFalse(strategy.shouldReadFromMaster());
		
		LocalContextReadWriteStrategy.setReadFromMaster();
		Assert.assertTrue(strategy.shouldReadFromMaster());
		
		LocalContextReadWriteStrategy.clearContext();
		Assert.assertFalse(strategy.shouldReadFromMaster());
	}
}
