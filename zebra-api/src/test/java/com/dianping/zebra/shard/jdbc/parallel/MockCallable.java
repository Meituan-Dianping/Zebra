package com.dianping.zebra.shard.jdbc.parallel;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

public class MockCallable implements Callable<Integer> {

	private MockType type;

	private long executeTime;

	private int result;

	public MockCallable(MockType type, long executeTime, int result) {
		this.type = type;
		this.executeTime = executeTime;
		this.result = result;
	}

	@Override
	public Integer call() throws Exception {
		switch (type) {
		case TIMEOUT:
			TimeUnit.MILLISECONDS.sleep(1010);
			break;
		case INTERRUPT:
			throw new InterruptedException();
		case CANCEL:
			throw new CancellationException("Cancel");
		case SQLEXCEPTION:
			throw new SQLException("SQLERROR");
		case NORMAL:
			TimeUnit.MILLISECONDS.sleep(executeTime);
			return result;
		}

		return 0;
	}

	public static enum MockType {
		NORMAL, TIMEOUT, INTERRUPT, SQLEXCEPTION, CANCEL
	}
}
