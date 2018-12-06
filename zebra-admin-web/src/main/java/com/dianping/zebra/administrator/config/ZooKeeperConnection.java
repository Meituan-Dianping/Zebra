package com.dianping.zebra.administrator.config;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by taochen on 2018/10/28.
 */
public class ZooKeeperConnection {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private ZooKeeper zooKeeper;

    private int sessionTimeout = 5000;

    private final CountDownLatch connectedSignal = new CountDownLatch(1);

    public ZooKeeper connect(String host) throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(host, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
               if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                   connectedSignal.countDown();
               }
            }
        });
        connectedSignal.await();
        return zooKeeper;
    }

    public void close() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            logger.error("zookeeper connection close fail!", e);
        }
    }
}
