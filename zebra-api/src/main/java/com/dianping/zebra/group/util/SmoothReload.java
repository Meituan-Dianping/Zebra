package com.dianping.zebra.group.util;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Dozer <br/>
 *         重新加载 DataSource 的时候设定一个延时，防止同一时间大量的 Connection 对数据库服务器造成冲击。
 */

public class SmoothReload {
	public static final int SLEEP_TIME = 100;

	private static final long DEFAULT_MAX_MILLISECOND_INTERVAL = 1000;

	private static Random rnd = new Random();

	private long maxMillisecondInterval = 0;

	private long randomInterval = 0;

	private long startMillisecond = 0;

	public SmoothReload() {
		this(DEFAULT_MAX_MILLISECOND_INTERVAL);
	}

	public SmoothReload(long maxMillisecondInterval) {
		this.maxMillisecondInterval = maxMillisecondInterval;
		this.startMillisecond = System.currentTimeMillis();
		initRandomInterval();
	}

	public void initRandomInterval() {
		this.randomInterval = (long) (rnd.nextDouble() * maxMillisecondInterval);
	}

	public void waitForReload() {
		if (maxMillisecondInterval <= 0) {
			return;
		}

		while (startMillisecond + randomInterval > System.currentTimeMillis()) {
			try {
				TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}
}