package com.dianping.zebra.shard.router.rule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import junit.framework.Assert;
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

	@Test
	public void testIfElse() throws ParseException {
		RuleEngine ruleEngine = new GroovyRuleEngine("#CreateTime# > date('2017-03-25 00:00:00') ? 0 : ( #UserID# % 10 + 1)");

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("CreateTime", format.parse("2017-03-26 00:00:00"));
		valMap.put("UserID", 1);
		Object retVal = ruleEngine.eval(valMap);
		Assert.assertEquals(new Integer(0), retVal);
	}

	@Test
	public void testIfElse2() {
		RuleEngine ruleEngine = new GroovyRuleEngine("#CreateTime# < date('2011-03-23 00:00:00') ? 0 : ( #UserID# % 10 + 1)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("CreateTime", new Date());
		valMap.put("UserID", 1);
		Object retVal = ruleEngine.eval(valMap);
		Assert.assertEquals(new Integer(2), retVal);
	}

	@Test
	public void testDate() {
		RuleEngine ruleEngine = new GroovyRuleEngine("date('2017-03-23 00:00:00')");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
	}

	@Test
	public void testDate1() {
		long now = System.currentTimeMillis();
		System.out.println(now);
		RuleEngine ruleEngine = new GroovyRuleEngine("date(new Date(1490237457285))");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Object retVal = ruleEngine.eval(valMap);
		System.out.println(((Date)retVal).getTime());
	}

	@Test
	public void testTableNameRule() {
		RuleEngine ruleEngine = new GroovyRuleEngine("#UserID#+'_1'");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("UserID", 1);

		Object retVal = ruleEngine.eval(valMap);
		Assert.assertEquals("1_1", retVal);
	}

	@Test
	public void testTableNameRule2() {
		RuleEngine ruleEngine = new GroovyRuleEngine("'Table' + '_'+ #UserID#+'_' + #GroupID#%10");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("UserID", 1);
		valMap.put("GroupID", 7);

		Object retVal = ruleEngine.eval(valMap);
		Assert.assertEquals("Table_1_7", retVal);
	}

	@Test
	public void testTableNameRule3() {
		RuleEngine ruleEngine = new GroovyRuleEngine("#CreateTime# < date('2017-03-23 00:00:00') ? 'Table' : ('Table' + '_'+ #UserID#+'_' + #GroupID#%10)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("CreateTime", new Date());
		valMap.put("UserID", 1);
		valMap.put("GroupID", 7);

		Object retVal = ruleEngine.eval(valMap);
		Assert.assertEquals("Table_1_7", retVal);
	}

	@Test
	public void testTableNameRule4() {
		RuleEngine ruleEngine = new GroovyRuleEngine("#CreateTime# < date('2017-03-23 00:00:00') ? 'Table' : ('Table' + '_'+ #UserID#+'_' + #GroupID#%10)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Calendar instance = Calendar.getInstance();
		instance.set(Calendar.YEAR, 2016);
		valMap.put("CreateTime", instance.getTime());
		valMap.put("UserID", 1);
		valMap.put("GroupID", 7);

		Object retVal = ruleEngine.eval(valMap);
		Assert.assertEquals("Table", retVal);
	}

	@Test
	public void testTableNameRule5() {
		RuleEngine ruleEngine = new GroovyRuleEngine("'Table' + '_'+ #GroupID#%16");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("GroupID", 15);

		Object retVal = ruleEngine.eval(valMap);
		Assert.assertEquals("Table_15", retVal);
	}

	@Test
	public void testTableNameRule6() {
		RuleEngine ruleEngine = new GroovyRuleEngine("'Table' + '_'+ (#GroupID#.intValue() / 4).intValue() %4");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("GroupID", 15);

		Object retVal = ruleEngine.eval(valMap);
		Assert.assertEquals("Table_3", retVal);
	}

	@Test
	public void testTableNameRule7() {
		RuleEngine ruleEngine = new GroovyRuleEngine("month(#Time#)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("Time", new Date());

		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
	}

	@Test
	public void testZero() {
		RuleEngine ruleEngine = new GroovyRuleEngine("0");
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put("UserID", 1);
		valMap.put("GroupID", 7);

		Object retVal = ruleEngine.eval(valMap);
		System.out.println(retVal);
		Assert.assertEquals(new Integer(0), retVal);
	}

	private void putIntoMap(Map<Integer, List<Integer>> map, int di, int[] arr) {
		List<Integer> tbList = map.get(di);
		if(tbList == null) {
			tbList = new ArrayList<Integer>();
			map.put(di, tbList);
		}
		for(int i : arr) {
			tbList.add(i);
		}
	}

	/**
	 *           DATE          | DB | TB
	 * ------------------------+----+------
	 *            - 2015-01-14 |  2 |  0
	 * ------------------------+----+------
	 * 2015-01-15 — 2015-03-31 |  0 |  0
	 * ------------------------+----+------
	 * 2015-04-01 — 2015-06-30 |  0 |  1
	 * ------------------------+----+------
	 * 2015-07-01 — 2015-09-30 |  0 |  2
	 * ------------------------+----+------
	 * 2015-10-01 — 2015-12-31 |  0 |  3
	 * ------------------------+----+------
	 * 2016-01-01 — 2016-03-31 |  1 |  0
	 * ------------------------+----+------
	 * 2016-04-01 — 2016-06-30 |  1 |  1
	 * ------------------------+----+------
	 * 2016-07-01 — 2016-09-30 |  1 |  2
	 * ------------------------+----+------
	 * 2016-10-15 — 2016-12-20 |  1 |  3
	 * ------------------------+----+------
	 * 2016-12-21 -            |  2 |  0
	 *
	 */
	@Test
	public void testShardByMonthDefaultOrder() {
		RuleEngine ruleEngine = new GroovyRuleEngine("shardByMonth(#time#, 'yyyy-MM-dd', '2015-01-15', '2016-12-20', 3, 2, 4, 2, 0)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Set<ShardRange> ranges = new HashSet<ShardRange>();
		valMap.put("time", ranges);
		Map<Integer, List<Integer>> real = new HashMap<Integer, List<Integer>>();

		// < 2015-01-14
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Less, "2015-01-14"));
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// <= 2015-01-15
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2015-01-15"));
		putIntoMap(real, 0, new int[]{0});
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// = 2015-01-15
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Equal, "2015-01-15"));
		putIntoMap(real, 0, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// > 2016-12-20
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, "2016-12-20"));
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// >= 2016-12-20
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2016-12-20"));
		putIntoMap(real, 2, new int[]{0});
		putIntoMap(real, 1, new int[]{3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// = 2016-12-20
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Equal, "2016-12-20"));
		putIntoMap(real, 1, new int[]{3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// 2015-01-15 <= date <= 2015-12-31
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2015-01-15"));
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2015-12-31"));
		putIntoMap(real, 0, new int[]{0,1,2,3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// 2016-01-15 <= date <= 2016-12-20
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2016-01-15"));
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2016-12-20"));
		putIntoMap(real, 1, new int[]{0,1,2,3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// 2015-08-09 <= date <= 2016-08-20
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2015-08-09"));
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2016-08-20"));
		putIntoMap(real, 0, new int[]{2,3});
		putIntoMap(real, 1, new int[]{0,1,2});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// = 2015-06-30
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Equal, "2015-06-30"));
		putIntoMap(real, 0, new int[]{1});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// = 2015-07-01
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Equal, "2015-07-01"));
		putIntoMap(real, 0, new int[]{2});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// 2015-06-09 < date < 2015-11-20
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, "2015-06-09"));
		ranges.add(new ShardRange(ShardRange.OP_Less, "2015-11-20"));
		putIntoMap(real, 0, new int[]{1,2,3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// 2015-06-09 < date < 2015-11-20
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, "2015-06-09"));
		ranges.add(new ShardRange(ShardRange.OP_Less, "2015-11-20"));
		putIntoMap(real, 0, new int[]{1,2,3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// 2016-03-31 < date < 2016-10-01
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, "2016-03-31"));
		ranges.add(new ShardRange(ShardRange.OP_Less, "2016-10-01"));
		putIntoMap(real, 1, new int[]{1,2});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// 2016-03-31 <= date < 2016-10-01
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2016-03-31"));
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2016-10-01"));
		putIntoMap(real, 1, new int[]{0,1,2,3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2016-03-31"));
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2016-10-01"));
		ranges.add(new ShardRange(ShardRange.OP_Equal, "2016-02-21"));
		putIntoMap(real, 1, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		// in 2015-03-31 2015-04-01 2015-05-02 2016-11-23 2016-12-23
		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2016-03-31"));
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2016-10-01"));
		Set<String> s = new HashSet<String>();
		s.add("2015-03-31"); s.add("2015-04-01"); s.add("2015-05-02"); s.add("2016-11-23"); s.add("2016-12-23");
		ranges.add(new ShardRange(ShardRange.OP_InList, s));
		putIntoMap(real, 0, new int[]{1,0});
		putIntoMap(real, 1, new int[]{3});
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));


		//---------------
		real.clear(); valMap.clear();
		valMap.put("time", "2015-01-14");
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();
		valMap.put("time", "2016-06-30");
		putIntoMap(real, 1, new int[]{1});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();
		valMap.put("time", "2016-07-01");
		putIntoMap(real, 1, new int[]{2});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();
		valMap.put("time", "2015-07-01");
		putIntoMap(real, 0, new int[]{2});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

	}

	/**
	 *           DATE          | DB | TB
	 * ------------------------+----+------
	 *            - 2015-01-14 |  2 |  0
	 * ------------------------+----+------
	 * 2015-01-15 — 2015-03-31 |  0 |  0
	 * ------------------------+----+------
	 * 2015-04-01 — 2015-06-30 |  1 |  0
	 * ------------------------+----+------
	 * 2015-07-01 — 2015-09-30 |  0 |  1
	 * ------------------------+----+------
	 * 2015-10-01 — 2015-12-31 |  1 |  1
	 * ------------------------+----+------
	 * 2016-01-01 — 2016-03-31 |  0 |  2
	 * ------------------------+----+------
	 * 2016-04-01 — 2016-06-30 |  1 |  2
	 * ------------------------+----+------
	 * 2016-07-01 — 2016-09-30 |  0 |  3
	 * ------------------------+----+------
	 * 2016-10-15 — 2016-12-20 |  1 |  3
	 * ------------------------+----+------
	 * 2016-12-21 -            |  2 |  0
	 *
	 */
	@Test
	public void testShardByMonthCross() {
		RuleEngine ruleEngine = new GroovyRuleEngine("shardByMonth(#time#, 'yyyy-MM-dd', '2015-01-15', '2016-12-20', 3, 2, 4, 2, 0, false)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Set<ShardRange> ranges = new HashSet<ShardRange>();
		valMap.put("time", ranges);
		Map<Integer, List<Integer>> real = new HashMap<Integer, List<Integer>>();

		real.clear();
		ranges.add(new ShardRange(ShardRange.OP_Less, "2015-01-14"));
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2015-01-15"));
		putIntoMap(real, 2, new int[]{0});	putIntoMap(real, 0, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, "2016-12-20"));
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2016-12-20"));
		putIntoMap(real, 2, new int[]{0});	putIntoMap(real, 1, new int[]{3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, "2015-1-15"));
		putIntoMap(real, 0, new int[]{0, 1, 2, 3});		putIntoMap(real, 1, new int[]{0, 1, 2, 3});
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Less, "2016-12-15"));
		putIntoMap(real, 0, new int[]{0, 1, 2, 3});		putIntoMap(real, 1, new int[]{0, 1, 2, 3});
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, "2015-12-31"));
		ranges.add(new ShardRange(ShardRange.OP_Less, "2016-12-15"));
		putIntoMap(real, 0, new int[]{2,3});		putIntoMap(real, 1, new int[]{2,3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2015-12-31"));
		ranges.add(new ShardRange(ShardRange.OP_Less, "2016-12-15"));
		putIntoMap(real, 0, new int[]{2,3});		putIntoMap(real, 1, new int[]{1,2,3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2016-07-11"));
		ranges.add(new ShardRange(ShardRange.OP_Less, "2017-01-01"));
		putIntoMap(real, 0, new int[]{3}); putIntoMap(real, 1, new int[]{3}); putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2015-07-11"));
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2016-01-01"));
		putIntoMap(real, 0, new int[]{1, 2});	putIntoMap(real, 1, new int[]{1});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2016-07-11"));
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2017-01-01"));
		ranges.add(new ShardRange(ShardRange.OP_Equal, "2018-05-13"));
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();	ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, "2015-07-11"));
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, "2017-01-01"));
		Set<String> s = new HashSet<String>();
		s.add("2015-08-23"); s.add("2016-05-23"); s.add("2016-04-07"); s.add("2016-06-07"); s.add("2017-11-11");
		ranges.add(new ShardRange(ShardRange.OP_InList, s));
		putIntoMap(real, 0, new int[]{1});	putIntoMap(real, 1, new int[]{2});
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));


		//---------------
		real.clear(); valMap.clear();
		valMap.put("time", "2015-01-14");
		putIntoMap(real, 2, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();
		valMap.put("time", "2016-06-30");
		putIntoMap(real, 1, new int[]{2});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();
		valMap.put("time", "2016-07-01");
		putIntoMap(real, 0, new int[]{3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();
		valMap.put("time", "2015-07-01");
		putIntoMap(real, 0, new int[]{1});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear();
		valMap.put("time", "2015-06-01");
		putIntoMap(real, 1, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

	}


	@Test
	public void testShardByLongDefault() {
		RuleEngine ruleEngine = new GroovyRuleEngine("shardByLong(#uid#, 10001, 90000, 10000, 4, 2, 1, 4)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Set<ShardRange> ranges = new HashSet<ShardRange>();
		valMap.put("uid", ranges);
		Map<Integer, List<Integer>> real = new HashMap<Integer, List<Integer>>();

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Less, 10001));
		putIntoMap(real, 1, new int[]{4});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, 10001));
		putIntoMap(real, 1, new int[]{4}); 	putIntoMap(real, 0, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, 90000));
		putIntoMap(real, 1, new int[]{4});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, 90000));
		putIntoMap(real, 1, new int[]{4}); 	putIntoMap(real, 1, new int[]{3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, 30000));
		ranges.add(new ShardRange(ShardRange.OP_Less, 40000));
		putIntoMap(real, 0, new int[]{1,2});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, 30000));
		ranges.add(new ShardRange(ShardRange.OP_Less, 40000));
		putIntoMap(real, 0, new int[]{2});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, 50000));
		ranges.add(new ShardRange(ShardRange.OP_Less, 70000));
		putIntoMap(real, 0, new int[]{3}); putIntoMap(real, 1, new int[]{0, 1});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

	}

	@Test
	public void testShardByLongCross() {
		RuleEngine ruleEngine = new GroovyRuleEngine("shardByLong(#uid#, 10001, 90000, 10000, 4, 2, 1, 4, false)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Set<ShardRange> ranges = new HashSet<ShardRange>();
		valMap.put("uid", ranges);
		Map<Integer, List<Integer>> real = new HashMap<Integer, List<Integer>>();

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Less, 10001));
		putIntoMap(real, 1, new int[]{4});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_LessOrEqual, 10001));
		putIntoMap(real, 1, new int[]{4}); 	putIntoMap(real, 0, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, 90000));
		putIntoMap(real, 1, new int[]{4});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, 90000));
		putIntoMap(real, 1, new int[]{4}); 	putIntoMap(real, 1, new int[]{3});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, 30000));
		ranges.add(new ShardRange(ShardRange.OP_Less, 40000));
		putIntoMap(real, 0, new int[]{1});	putIntoMap(real, 1, new int[]{0});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_Greater, 30000));
		ranges.add(new ShardRange(ShardRange.OP_Less, 40000));
		putIntoMap(real, 0, new int[]{1});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

		real.clear(); ranges.clear();
		ranges.add(new ShardRange(ShardRange.OP_GreaterOrEqual, 50000));
		ranges.add(new ShardRange(ShardRange.OP_Less, 70000));
		putIntoMap(real, 1, new int[]{1, 2}); putIntoMap(real, 0, new int[]{2});
		Assert.assertEquals(real, ruleEngine.eval(valMap));

	}


	@Test
	@SuppressWarnings("unchecked")
	public void testShardByHashDefaultOrder() {
		Map<Integer, List<Integer>> real = new HashMap<Integer, List<Integer>>();
		RuleEngine ruleEngine = new GroovyRuleEngine("shardByHash(#id#, 2, 4)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Map<Integer, List<Integer>> res;

		for(int i = 0; i <= 16; ++i) {
			valMap.put("id", i);
			res = (Map<Integer, List<Integer>>)ruleEngine.eval(valMap);
			real.clear();
			int db = i / 4 % 2;
			int tb = i % 4;
			putIntoMap(real, db, new int[]{tb});
			Assert.assertEquals(real, res);
		}

		valMap.put("id", 1);
		res = (Map<Integer, List<Integer>>)ruleEngine.eval(valMap);
		real.clear();	putIntoMap(real, 0, new int[]{1});
		Assert.assertEquals(real, res);

		valMap.put("id", 2);
		res = (Map<Integer, List<Integer>>)ruleEngine.eval(valMap);
		real.clear();	putIntoMap(real, 0, new int[]{2});
		Assert.assertEquals(real, res);

		valMap.put("id", 3);
		res = (Map<Integer, List<Integer>>)ruleEngine.eval(valMap);
		real.clear();	putIntoMap(real, 0, new int[]{3});
		Assert.assertEquals(real, res);

		valMap.put("id", 4);
		res = (Map<Integer, List<Integer>>)ruleEngine.eval(valMap);
		real.clear();	putIntoMap(real, 1, new int[]{0});
		Assert.assertEquals(real, res);

		valMap.put("id", 6);
		res = (Map<Integer, List<Integer>>)ruleEngine.eval(valMap);
		real.clear();	putIntoMap(real, 1, new int[]{2});
		Assert.assertEquals(real, res);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testShardByHashCross() {

		Map<Integer, List<Integer>> real = new HashMap<Integer, List<Integer>>();
		RuleEngine ruleEngine = new GroovyRuleEngine("shardByHash(#id#, 2, 4, false)");
		Map<String, Object> valMap = new HashMap<String, Object>();
		Map<Integer, List<Integer>> res;

		for(int i = 0; i <= 16; ++i) {
			valMap.put("id", i);
			res = (Map<Integer, List<Integer>>)ruleEngine.eval(valMap);
			real.clear();
			int db = i % 2;
			int tb = i / 2 % 4;
			putIntoMap(real, db, new int[]{tb});
			Assert.assertEquals(real, res);
		}

		valMap.put("id", 1);
		res = (Map<Integer, List<Integer>>)ruleEngine.eval(valMap);
		real.clear();	putIntoMap(real, 1, new int[]{0});
		Assert.assertEquals(real, res);

		valMap.put("id", 2);
		res = (Map<Integer, List<Integer>>)ruleEngine.eval(valMap);
		real.clear();	putIntoMap(real, 0, new int[]{1});
		Assert.assertEquals(real, res);

		valMap.put("id", 3);
		res = (Map<Integer, List<Integer>>)ruleEngine.eval(valMap);
		real.clear();	putIntoMap(real, 1, new int[]{1});
		Assert.assertEquals(real, res);
	}
}