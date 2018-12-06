package com.dianping.zebra.group.performance;


public class C3p0Case extends ZebraBaseCase {

	public C3p0Case() throws Exception {
	   super("performance/appcontext-aop-core-c3p0.xml");
   }

	public static void main(String args[]) throws Exception {
		C3p0Case zebraCase = new C3p0Case();
		Thread.sleep(10000);// 构建链接池的时间

		// c3p0: 单线程，库1个，25连接池，10000次，399194ms
		// c3p0: 单线程，库1个，25连接池，10000次，481309ms
		// 无干扰：单线程，库1个，300连接池，1000次，36451ms
		// 无干扰：单线程，库1个，300连接池，1000次，35681ms
		// 无干扰：单线程，库1个，300连接池，1000次，36530ms
		// 无干扰：单线程，库1个，300连接池，1000次，35741ms
		TestRunner runner = new TestRunner(1000, 1, zebraCase);

		// 无干扰： 单线程，库1个，300连接池，2000次，70983ms
		//71192
		//TestRunner runner = new TestRunner(2000, 1, zebraCase);

		// 10线程，写库1个，50连接池，读库4个，每个5连接池，10000次，84933ms，117qps
		// 无干扰：10线程，库1个，300连接池，10000次，74962ms
		// 无干扰：10线程，库1个，300连接池，10000次，76077ms
		// TestRunner runner = new TestRunner(10000, 10, zebraCase);

		// 无干扰： 50线程，库1个，300连接池，50000次，83010ms
		// 无干扰： 50线程，库1个，300连接池，50000次，83382ms
		//TestRunner runner = new TestRunner(50000, 50, zebraCase);

		// 无干扰： 100线程，库1个，300连接池，100000次，92624ms
		// 无干扰： 100线程，库1个，300连接池，100000次，93881ms
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
