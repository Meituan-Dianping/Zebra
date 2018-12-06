package com.dianping.zebra.shard.router.rule.engine;

import org.junit.Test;

import java.util.Date;

/**
 * Dozer @ 6/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class RuleEngineBaseTest {

	@Test
	public void testDate() throws Exception {
		RuleEngineBase target = new RuleEngineBase();
		Date result = target.date("2015-01-01 19:02:02.0001");
		System.out.println(result.toString());
	}
}