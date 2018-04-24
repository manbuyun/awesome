package com.manbuyun.awesome.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.KillSession;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: cs
 * Date: 2017-09-13
 * <p>
 * wiki: http://www.cnblogs.com/LiZhiW/tag/ZooKeeper/
 */
@Slf4j
public class Zookeeper {

    private CuratorFramework client;
    private byte[] data = "init".getBytes(StandardCharsets.UTF_8);

    /**
     * 参考spark的curator写法：SparkCuratorUtil.scala
     * <p>
     * 操作zk，可能会抛出KeeperException异常
     *
     * @throws Exception
     */
    @BeforeTest
    public void init() throws Exception {
        // 默认时，会话超时60s，连接创建超时15s，尝试连接的间隔是指数算法
        // 该客户端对zookeeper的操作都是基于/test相对目录进行，不需要create这个节点。这里不加反斜线！
        // ExponentialBackoffRetry: zk连接异常时，自动重连策略。curator: 所有的操作forPath，都是执行RetryLoop.callWithRetry，封装StandardConnectionHandlingPolicy.callWithRetry
        client = CuratorFrameworkFactory.builder().connectString("10.104.111.35:2181").sessionTimeoutMs(60000).connectionTimeoutMs(15000).retryPolicy(new ExponentialBackoffRetry(5000, 3)).namespace("test").build();

        // 连接状态监听事件。先1.新建连接，连接不上时，先2.暂停操作，后3.连接丢失，恢复连接后4.重新连接
        // 官方wiki：http://curator.apache.org/errors.html
        client.getConnectionStateListenable().addListener((client, state) -> {
            switch (state) {
                case CONNECTED:
                    log.info("新建连接成功");
                    break;
                case SUSPENDED:
                    log.warn("暂停操作zk");
                    break;
                case RECONNECTED:
                    log.warn("重新连接成功");
                    // 这里重连成功只表示网络心跳，当使用临时节点时，重连并不会重新注册临时节点，所以这里必须加上临时节点的重新注册机制
                    try {
                        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/path4", data);
                    } catch (Exception e) {
                        log.error("重连成功，注册节点失败", e);
                    }
                    break;
                case LOST:
                    // SessionConnectionStateErrorPolicy类：当Curator认为ZooKeeper会话已经过期SessionTimeout，则进入此状态
                    // 当GC时，zk无法获得客户端的心跳，超过60s也会SessionTimeout
                    log.error("连接丢失");
                    break;
            }
        });

        // 节点操作监听事件，用来监听get方法。详看: this.getWatch()。不推荐使用
        client.getCuratorListenable().addListener((client, event) -> {
            if (event.getType().equals(CuratorEventType.WATCHED)) {
                WatchedEvent e = event.getWatchedEvent();
                log.info("state: {}, type: {}, path: {}", e.getState(), e.getType(), e.getPath());
            }
        });

        // 出现zookeeper的exception时，会触发
        client.getUnhandledErrorListenable().addListener((message, e) -> {
            log.error("不可恢复的zookeeper错误，关闭进程");
            Runtime.getRuntime().halt(1);
        });

        if (client.getState() != CuratorFrameworkState.STARTED) {
            client.start();
            // 连接zk，等待连接上或超过client设置的连接超时时间(connectionTimeoutMs)：client.getZookeeperClient().blockUntilConnectedOrTimedOut();
            // 此方法可以自主设置等待连接超时时间
            client.blockUntilConnected(10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void createNode() throws Exception {
        // 默认创建持久节点，内容默认为空
        client.create().withMode(CreateMode.EPHEMERAL).forPath("/path1", data);

        client.create().withMode(CreateMode.EPHEMERAL).forPath("/path2", data);

        // create node时可以设置权限
        // OPEN_ACL_UNSAFE: 完全开放; CREATOR_ALL_ACL: 只有创建该Node的客户端有操作权限; READ_ACL_UNSAFE: 所有客户端都可读
        client.create().withMode(CreateMode.EPHEMERAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath("/path3", data);

        // 避免NoNodeException异常，自动创建节点需要的父节点。非叶子节点必须是持久节点!
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/path4", data);
    }

    @Test
    public void deleteNode() throws Exception {
        client.create().forPath("/path1");
        client.create().forPath("/path2");
        client.create().forPath("/path3");
        client.create().forPath("/path4");

        // 只能删除叶子节点
        client.delete().forPath("/path1");

        // 强制保证删除节点。在Master选举等必须要删除彻底的场景下使用
        client.delete().guaranteed().forPath("/path2");

        // 删除节点，并递归删除其子节点
        client.delete().deletingChildrenIfNeeded().forPath("/path3");

        Stat stat = new Stat();
        // 存储节点属性信息
        client.getData().storingStatIn(stat).forPath("/path4");
        // 删除节点的指定version
        client.delete().withVersion(stat.getVersion()).forPath("/path4");
    }

    @Test
    public void getData() throws Exception {
        client.create().forPath("/path1", data);

        byte[] byte1 = client.getData().forPath("/path1");

        Stat stat = new Stat();
        // 获取节点数据，同时获取节点属性信息stat
        byte[] bytes2 = client.getData().storingStatIn(stat).forPath("/path1");
    }

    @Test
    public void setData() throws Exception {
        client.create().forPath("/path1");

        // 更新节点的数据内容，返回stat信息
        Stat stat = client.setData().forPath("/path1", data);

        // 指定version，进行数据更新，并返回stat信息
        Stat stat1 = client.setData().withVersion(stat.getVersion()).forPath("/path1");
    }

    @Test
    public void inBackground() throws Exception {
        // 添加自定义executor，处理耗时事件
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).inBackground((client, event) -> {

            log.info("event code: [{}], type: [{}].", event.getResultCode(), event.getType());
            log.info("thread of process: [{}]", Thread.currentThread().getName());

        }, Executors.newFixedThreadPool(2)).forPath("/path1", data);
    }

    /**
     * Zookeeper的CREATE、SET、DELETE会触发watch
     *
     * @throws Exception
     */
    @Test
    public void nodeWatch() throws Exception {
        String path = "/nodeWatch";

        NodeCache nodeWatch = new NodeCache(client, path, false);
        // 默认为false。若是true，第一次启动时会从zk读取内容，并保存在cache中
        nodeWatch.start(true);
        nodeWatch.getListenable().addListener(() -> {
            log.info("node data update. new data: {}", new String(nodeWatch.getCurrentData().getData()));
        });

        // 触发listener
        client.create().forPath(path, data);

        // 不触发listener
        client.getData().forPath(path);

        // 触发listener
        client.setData().forPath(path);

        // 触发listener
        client.delete().forPath(path);
    }

    @Test
    public void childrenWatch() throws Exception {
        String path = "/childrenWatch";

        PathChildrenCache childrenWatch = new PathChildrenCache(client, path, true);
        // NORMAL: 异步初始化; BUILD_INITIAL_CACHE: 同步初始化; POST_INITIALIZED_EVENT: 异步初始化
        childrenWatch.start(StartMode.NORMAL);
        childrenWatch.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    log.info("child added: {}", event.getData().getPath());
                    break;
                case CHILD_UPDATED:
                    log.info("child updated: {}", event.getData().getPath());
                    break;
                case CHILD_REMOVED:
                    log.info("child removed: {}", event.getData().getPath());
                    break;
                default:
                    break;
            }
        });

        // 不触发
        client.create().creatingParentsIfNeeded().forPath(path);

        // 触发child_added
        client.create().forPath(path + "/child1", "hello".getBytes());

        // 触发child_updated
        client.setData().forPath(path + "/child1", data);

        // 不触发
        client.getData().forPath(path + "/child1");

        // 触发 child_removed
        client.delete().forPath(path + "/child1");

        // 不触发
        client.delete().forPath(path);
    }

    /**
     * nodecache、pathchildrencache只能监听create、setdate、delete方法
     * <p>
     * 监听get方法
     * <p>
     * http://blog.csdn.net/wo541075754/article/details/70167841
     * http://blog.csdn.net/wo541075754/article/details/70207722
     * <p>
     * 不推荐使用
     */
    @Test
    public void getWatch() throws Exception {
        client.getData().watched().forPath("/path1");
        client.getChildren().watched().forPath("/path2");

        client.getCuratorListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case WATCHED:
                    // 事件处理
                    break;
                default:
                    break;
            }
        });

        client.getData().usingWatcher((CuratorWatcher) event -> {
            log.info("事件处理: {}", event);
        }).forPath("/path1");

        client.getChildren().usingWatcher((CuratorWatcher) event -> {
            log.info("事件处理: {}", event);
        }).forPath("/path2");
    }

    /**
     * 当takeLeadership方法执行完，zk集群会发起新一轮leader选举。不适合HA方案
     */
    @Test
    public void leaderSelector() {
        // 不需要预先创建/leader_selector目录
        // stateChanged接口，继承LeaderSelectorListenerAdapter的默认实现
        // 如果发生SUSPENDED或者LOST连接问题，最好直接抛CancelLeadershipException，此时，leaderSelector实例会尝试中断并且取消正在执行takeLeadership()方法的线程
        // 参考WrappedListener里stateChanged方法的leaderSelector.interruptLeadership()
        LeaderSelector selector = new LeaderSelector(client, "/leader_selector", new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                log.info("成为leader");
            }
        });

        // 保证在此实例释放领导权之后还可能获得领导权
        selector.autoRequeue();
        selector.start();
    }

    /**
     * 推荐的HA写法
     * <p>
     * 参考spark的ha实现：ZooKeeperLeaderElectionAgent.scala
     *
     * @throws Exception
     */
    @Test
    public void leaderLatch() throws Exception {
        // 最后一个参数是id，也就是leader的info信息：host、port等序列化。/test/leader_latch
        LeaderLatch leader = new LeaderLatch(client, "/leader_election", "localhost");

        // 参考LeaderLatchListener的说明: 由于通知是异步的，因此有可能在接口被调用的时候，这个状态是准确的，需要确认一下LeaderLatch的hasLeadership()是否的确是true/false
        // LeaderLatch类有默认的listener，其中的handleStateChange()方法，针对SUSPENDED、LOST作了notLeader处理
        leader.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                if (!leader.hasLeadership()) {
                    return;
                }
                // 当LeaderLatch的hasLeaderShip()从false到true后，就会调用isLeader()，表明这个LeaderLatch成为leader了
                log.info("i am leader");
            }

            @Override
            public void notLeader() {
                if (leader.hasLeadership()) {
                    return;
                }
                // 当LeaderLatch的hahLeaderShip从true到false后，就会调用notLeader(),表明这个LeaderLatch不再是leader了
                log.info("i am not leader");
            }
        });

        leader.start();

        // 阻塞，直到被close()或自身当选为master
        leader.await();
    }

    @AfterTest
    public void close() throws InterruptedException {
        CloseableUtils.closeQuietly(client);
    }

    @Test
    public void kill() throws Exception {
        KillSession.kill(client.getZookeeperClient().getZooKeeper());
    }
}
