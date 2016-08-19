package com.dianping.zebra.group.util;

import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

public class SmoothReloadTest {

	@Test
	public void test_smooth_reload() {
		Random rnd = new Random();
		long maxTime = rnd.nextInt(1000);

		SmoothReload sr = new SmoothReload(maxTime);
		long now = System.currentTimeMillis();
		sr.waitForReload();
		long interval = System.currentTimeMillis() - now;

		System.out.println(String.format("SmoothReload Actual Time: %d", interval));
		System.out.println(String.format("SmoothReload Max Time: %d", maxTime));

		Assert.assertTrue(interval >= 0);
		Assert.assertTrue(interval <= maxTime + SmoothReload.SLEEP_TIME);
	}

}
