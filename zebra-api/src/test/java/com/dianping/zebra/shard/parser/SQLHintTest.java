package com.dianping.zebra.shard.parser;

import org.junit.Test;

import junit.framework.Assert;

public class SQLHintTest {

	@Test
	public void test0() {
		String line = "";

		SQLHint hint = SQLHint.parseHint(line);

		Assert.assertNotNull(hint);
	}

	@Test
	public void test1() {
		String line = "/*+zebra:w*/";

		SQLHint hint = SQLHint.parseHint(line);

		Assert.assertEquals(true, hint.isForceMaster());
	}

	@Test
	public void test2() {
		String line = "/*+zebra:sk=UserId|w*/";

		SQLHint hint = SQLHint.parseHint(line);

		Assert.assertEquals(true, hint.isForceMaster());
		Assert.assertEquals("UserId", hint.getShardColumn());
	}

	@Test
	public void test3() {
		String line = "/*+zebra:sk=UserId*/";

		SQLHint hint = SQLHint.parseHint(line);

		Assert.assertEquals(false, hint.isForceMaster());
		Assert.assertEquals("UserId", hint.getShardColumn());
	}

}
