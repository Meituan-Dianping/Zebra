package com.dianping.zebra.group.performance;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class TestRunner {

	private long timeConsumed;

	private CyclicBarrier barrier;

	private CountDownLatch countdownLatch;

	protected long startTime;

	private volatile AtomicInteger count = new AtomicInteger(1);

	// *** 以下是参数 ***

	private int repeatCount;

	private int threadCount = 0;

	private Runnable runnableCase;

	public TestRunner(int repeatCount, int threadCount, Runnable runnableCase) {
		super();
		this.repeatCount = repeatCount;
		this.threadCount = threadCount;
		this.runnableCase = runnableCase;
		this.barrier = new CyclicBarrier(threadCount, new Runnable() {
			@Override
			public void run() {
				startTime = System.currentTimeMillis();
				System.out.printf("start to run case for %d times, with %d threads. \n", TestRunner.this.repeatCount,
				      TestRunner.this.threadCount);
			}
		});
		this.countdownLatch = new CountDownLatch(threadCount);

	}

	public void start() throws InterruptedException {
		for (int i = 0; i < threadCount; i++) {
			Thread caseTask = new CaseTask();
			caseTask.start();
		}

		countdownLatch.await();

		timeConsumed = System.currentTimeMillis() - startTime;
		System.out.printf("done. timeConsumed is %dms.\n", timeConsumed);
	}

	public long getTimeConsuming() {
		return timeConsumed;
	}

	@SuppressWarnings("unused")
	private class CaseTask extends Thread {
		@Override
		public void run() {
			try {
				barrier.await();
				int c;
				while ((c = count.getAndIncrement()) <= repeatCount) {
					runnableCase.run();

					// displayProgress(c);
				}

				countdownLatch.countDown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			} finally {
			}
		}
	}

	private AtomicInteger percentProgress = new AtomicInteger(0);

	public void displayProgress(int c) {
		int _progress = (c * 100) / repeatCount;
		// System.out.println(c);
		// System.out.println(_progress);
		int i = percentProgress.get();
		//
		if (_progress > i && percentProgress.compareAndSet(i, _progress)) {
			System.out.println("Progress:" + _progress + "%, timeConsumed: " + (System.currentTimeMillis() - startTime)
			      + "ms");
		}
	}
}
