package com.dianping.zebra.administrator.config;

import com.dianping.zebra.util.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by taochen on 2018/10/28.
 */
public class ZookeeperService {

    private static Logger LOGGER = LoggerFactory.getLogger(ZookeeperService.class);

    private static ZooKeeper zooKeeper;

    private static ZooKeeperConnection zooKeeperConn = new ZooKeeperConnection();

    public static boolean createKey(String host, String key) {
        String path = parsePath(key);
        String pathTemp = "";
        byte[] b = pathTemp.getBytes();
        boolean result = false;
        try {
            zooKeeper = zooKeeperConn.connect(host);

            for (int i = 0; i < path.length(); ++i) {
                if (path.charAt(i) == '/' && StringUtils.isNotBlank(pathTemp) &&
                        configExists(zooKeeper, pathTemp) == null) {
                    zooKeeper.create(pathTemp, b, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
                pathTemp += path.charAt(i);
            }
            zooKeeper.create(path, b, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            result =  true;
        } catch (Exception e) {
            LOGGER.error("create key [" + key + "] fail, host:" + host, e);
            result = false;
        } finally {
            zooKeeperConn.close();
        }
        return result;
    }

    private static Stat configExists(ZooKeeper zooKeeperExecute, String path) throws KeeperException, InterruptedException {
        Stat result =  zooKeeperExecute.exists(path, true);
        return result;
    }

    public static byte[] getConfig(String host, String key) {
        String path = parsePath(key);
        try {
            zooKeeper = zooKeeperConn.connect(host);
            Stat stat = configExists(zooKeeper, path);
            if (stat == null) {
                return null;
            }
            byte[] data = zooKeeper.getData(path, false, null);
            return data;
        } catch (Exception e) {
            LOGGER.error("get value fail, key [" + key + "],host:" + host, e);
        } finally {
            zooKeeperConn.close();
        }
        return null;
    }

    public static void setConfig(String host, String key, byte[] data) {
        String path = parsePath(key);
        try {
            zooKeeper = zooKeeperConn.connect(host);
            zooKeeper.setData(path, data, configExists(zooKeeper, path).getVersion());
        } catch (Exception e) {
            LOGGER.error("set key [" + key + "] fail!", e);
        } finally {
            zooKeeperConn.close();
        }
    }

    public static boolean deleteConfig(String host, String key) {
        String path = parsePath(key);
        boolean result = false;
        try {
            zooKeeper = zooKeeperConn.connect(host);
            zooKeeper.delete(path, configExists(zooKeeper, path).getVersion());
            result = true;
        } catch (Exception e) {
            LOGGER.error("delete key [" + key + "] fail!", e);
            result = false;
        } finally {
            zooKeeperConn.close();
        }
        return result;
    }

    private static String parsePath(String key) {
        String path = key.replace('.', '/');
        path = "/" +path;
        return path;
    }
}
