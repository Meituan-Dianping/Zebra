package com.dianping.zebra.shard.router.rule;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.dianping.zebra.shard.router.rule.engine.GroovyRuleEngine;
import com.dianping.zebra.shard.router.rule.engine.RuleEngine;

public class GroovyRuleEngineTest {

	@Test
	public void testRule() {
		RuleEngine ruleEngine = new GroovyRuleEngine("(#NoteID#.longValue() % 32).intdiv(8)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("NoteID", 9);
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
	}

	@Test
	public void testRule1() {
		RuleEngine ruleEngine = new GroovyRuleEngine("#NoteID#.longValue() % 8");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("NoteID", 25);
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
	}

	@Test
	public void testCRC32() {
		RuleEngine ruleEngine = new GroovyRuleEngine("(crc32(#bid#)/10).toLong()%10");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("bid", "2127114697");
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
	}

	@Test
	public void testMD5() {
		RuleEngine ruleEngine = new GroovyRuleEngine("(crc32(md5(#bid# + \"_\" +#uid#))).toLong()%10");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("bid", "1849791204");
		valMap.put("uid", "200867475132416");
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
	}

	@Test
	public void testMD5_2() {
		RuleEngine ruleEngine = new GroovyRuleEngine("(crc32(md5(#id# + #name#))/10).toLong()%4");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("id", 1849791);
		valMap.put("name", "hao.zhu");
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
	}

	@Test
	public void testData() {
		RuleEngine ruleEngine = new GroovyRuleEngine("#AddTime#");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Calendar cal = Calendar.getInstance();
		cal.set(2015, 4, 1);
		valMap.put("AddTime", cal.getTime());
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
	}

	@Test
	public void testData1() {
		RuleEngine ruleEngine = new GroovyRuleEngine("#AddTime# == null ? SKIP : 0");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Calendar cal = Calendar.getInstance();
		cal.set(2015, 4, 1);
		valMap.put("AddTime", cal.getTime());
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
	}

	@Test
	public void testData2() {
		RuleEngine ruleEngine = new GroovyRuleEngine("#id# == 0 ? 0 : 0");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("id", 2);
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
	}
	
	@Test
	public void testStringInt() {
		RuleEngine ruleEngine = new GroovyRuleEngine("#id#.toInteger()");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("id", "1");
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
		System.out.println(retVal.getClass());
	}
	
	@Test
	public void testStringLong() {
		RuleEngine ruleEngine = new GroovyRuleEngine("#id#.toLong()");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("id", "1");
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
		System.out.println(retVal.getClass());
	}
	
	@Test
	public void testIntAbs() {
		RuleEngine ruleEngine = new GroovyRuleEngine("Math.abs(#UserID#).intValue()");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("UserID", -1);
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
		System.out.println(retVal.getClass());
	}
}