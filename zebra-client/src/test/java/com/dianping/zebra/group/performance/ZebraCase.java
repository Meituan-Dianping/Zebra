package com.dianping.zebra.group.performance;


public class ZebraCase extends ZebraBaseCase {

	public ZebraCase() throws Exception {
		super("performance/appcontext-aop-core.xml");
	}

	public static void main(String args[]) throws Exception {
		ZebraCase zebraCase = new ZebraCase();
		Thread.sleep(20000);// 构建链接池的时间

		// 单线程，写库1个，5连接池，读库4个，每个5连接池，10000次，1108670ms，每次110ms，9qps
		// 单线程，写库1个，45连接池，读库4个，每个50连接池，10000次，400655ms
		// 无干扰： 单线程，写库1个，100连接池，读库4个，每个50连接池，1000次，36526ms,
		// 无干扰： 单线程，写库1个，100连接池，读库4个，每个50连接池，1000次，36595ms
		// 无干扰： 单线程，写库1个，100连接池，读库4个，每个50连接池，1000次，36992ms
		TestRunner runner = new TestRunner(1000, 1, zebraCase);
		 
		// 无干扰： 单线程，写库1个，100连接池，读库4个，每个50连接池，2000次，70833ms
		//72273
		// TestRunner runner = new TestRunner(2000, 1, zebraCase);

		// 10线程，写库1个，50连接池，读库4个，每个5连接池，10000次，84933ms，117qps
		// 无干扰： 10线程，写库1个，100连接池，读库4个，每个50连接池，10000次，75083ms
		// 无干扰： 10线程，写库1个，100连接池，读库4个，每个50连接池，10000次，75451ms
		// TestRunner runner = new TestRunner(10000, 10, zebraCase);

		// 无干扰： 50线程，写库1个，100连接池，读库4个，每个50连接池，50000次，82996ms
		// 无干扰： 50线程，写库1个，100连接池，读库4个，每个50连接池，50000次，82996ms
		// TestRunner runner = new TestRunner(50000, 50, zebraCase);

		// 无干扰： 100线程，写库1个，100连接池，读库4个，每个50连接池，100000次，93517ms
		// 无干扰： 100线程，写库1个，100连接池，读库4个，每个50连接池，100000次，92196ms
      // TestRunner runner = new TestRunner(100000, 100, zebraCase);

		// 10线程，写库1个，50连接池，读库4个，每个5连接池，100000次，851037ms，117qps
		// TestRunner runner = new TestRunner(100000, 10, zebraCase);

		// 50线程，写库1个，45连接池，读库4个，每个50连接池，100000次，199323ms，502qps
		// TestRunner runner = new TestRunner(100000, 50, zebraCase);

		// 50线程，写库1个，100连接池，读库4个，每个50连接池，100000次，186769ms，537qps
		// TestRunner runner = new TestRunner(100000, 50, zebraCase);

		// 100线程，写库1个，100连接池，读库4个，每个50连接池，100000次，103783ms，970qps
		// TestRunner runner = new TestRunner(100000, 100, zebraCase);

		runner.start();
	}

}
