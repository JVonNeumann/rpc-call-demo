package com.github.rpccall.registry;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * 本类用于client发现server节点的变化，实现负载均衡与zookeeper的分布式应用系统服务器上下线动态感知demo类似
 **/
public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<String>();

    private String registryAddress;

    /**
     * zk连接
     * 
     */
    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;

        ZooKeeper zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }

    /**
     * 发现新节点
     */
    public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                LOGGER.info("using only data: {}", data);
            } else {
                data = dataList.get(new Random().nextInt(size));
                LOGGER.info("using random data: {}", data);
            }
        }
        return data;
    }

    /**
     * 连接zk
     * 
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return zk;
    }

    /**
     * 监听节点，采用递归，实现不断监听节点变化，每一次变化则重新获取所有节点（即所有服务器地址）
     */
    private void watchNode(final ZooKeeper zooKeeper) {
        try {
            List<String> nodeList = zooKeeper.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    // 节点改变
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zooKeeper);
                    }
                }
            });
            List<String> dataList = new ArrayList<String>();
            // 循环子节点
            for (String node : nodeList) {
                // 获取节点中的服务器地址
                byte[] bytes = zooKeeper.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                // 存储到list中
                dataList.add(new String(bytes));
            }
            LOGGER.info("node data: {}", dataList);
            this.dataList = dataList;
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
}
