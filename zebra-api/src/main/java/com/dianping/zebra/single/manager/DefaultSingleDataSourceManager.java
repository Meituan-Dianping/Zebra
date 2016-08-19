package com.dianping.zebra.single.manager;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.single.jdbc.SingleDataSource;

public class DefaultSingleDataSourceManager implements SingleDataSourceManager {

	private Thread dataSourceMonitor;

	private BlockingQueue<SingleDataSource> toBeClosedDataSource = new LinkedBlockingQueue<SingleDataSource>();

	@Override
	public synchronized SingleDataSource createDataSource(DataSourceConfig config, List<JdbcFilter> filters) {
		return new SingleDataSource(config, filters);
	}

	@Override
	public synchronized void destoryDataSource(SingleDataSource dataSource) {
		if (dataSource != null) {
			this.toBeClosedDataSource.offer(dataSource);
		}
	}

	@Override
	public synchronized void init() {
		if (dataSourceMonitor == null) {
			dataSourceMonitor = new Thread(new CloseDataSourceTask());

			dataSourceMonitor.setDaemon(true);
			dataSourceMonitor.setName("Dal-" + CloseDataSourceTask.class.getSimpleName());
			dataSourceMonitor.start();
		}
	}

	@Override
	public synchronized void stop() {
		if (dataSourceMonitor != null) {
			dataSourceMonitor.interrupt();
		}
	}

	class CloseDataSourceTask implements Runnable {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				SingleDataSource dataSource = null;
				try {
					dataSource = toBeClosedDataSource.take();
					dataSource.close();
				} catch (ZebraException e) {
					if (dataSource != null) {
						try {
							TimeUnit.MILLISECONDS.sleep(100);
						} catch (InterruptedException e1) {
						}

						toBeClosedDataSource.offer(dataSource);
					}
				} catch (Exception ignore) {
				}
			}
		}
	}
}
