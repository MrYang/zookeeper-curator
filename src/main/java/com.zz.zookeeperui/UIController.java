package com.zz.zookeeperui;

import com.codahale.metrics.annotation.Timed;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.Yang
 * @since 2017-05-23
 */

@Controller
public class UIController {

    @Resource
    private CuratorFramework client;

    @Resource
    private CuratorWatcher curatorWatcher;

    private Logger logger = LoggerFactory.getLogger(Application.class);

    @RequestMapping("/")
    public String ui() {
        return "index.html";
    }

    @Timed
    @ResponseBody
    @RequestMapping("/list")
    public List<String> list(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }
        List<String> paths = client.getChildren().forPath(path);
        return paths;
    }

    private List<String> recursion(String path, List<String> paths) throws Exception {
        List<String> addPaths = client.getChildren().forPath(path);

        addPaths.forEach(p -> paths.add(path.equals("/") ? "/" + p : path + "/" + p));
        for (String p : addPaths) {
            String ap = path.equals("/") ? "/" + p : path + "/" + p;
            recursion(ap, paths);
        }
        return paths;
    }

    @ResponseBody
    @RequestMapping("/get")
    public Stat get(String path) throws Exception {
        NodeCache nodeCache = new NodeCache(client, path);
        nodeCache.start(true);
        return nodeCache.getCurrentData().getStat();
    }

    public String create(String path, String data) throws Exception {
        return client.create().forPath(path, data.getBytes());
    }

    public String createEphemeral(String path, String data) throws Exception {
        return client.create().withMode(CreateMode.EPHEMERAL).forPath(path, data.getBytes());
    }

    public String createEphemeralSequential(String path, String data) throws Exception {
        return client.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, data.getBytes());
    }

    /*
        Stat数据结构为:
        ZooKeeper状态的每一次改变, 都对应着一个递增的Transaction id, 该id称为zxid. 由于zxid的递增性质,
        如果zxid1小于zxid2, 那么zxid1肯定先于zxid2发生. 创建任意节点, 或者更新任意节点的数据, 或者删除任意节点,
        都会导致Zookeeper状态发生改变, 从而导致zxid的值增加
        节点类型:persistent,ephemeral,sequence
        persistent节点不和特定的session绑定, 不会随着创建该节点的session的结束而消失, 而是一直存在, 除非该节点被显式删除
        ephemeral节点是临时性的, 如果创建该节点的session结束了, 该节点就会被自动删除. ephemeral节点不能拥有子节点.
        sequence节点既可以是ephemeral的, 也可以是persistent的. 创建sequence节点时, ZooKeeper server会在指定的节点名称后加上一个数字序列,
        该数字序列是递增的. 因此可以多次创建相同的sequence节点, 而得到不同的节点

        czxid. 节点创建时的zxid；
        mzxid. 节点最新一次更新发生时的zxid；
        ctime. 节点创建时的时间戳；
        mtime. 节点最新一次更新发生时的时间戳；
        dataVersion. 节点数据的更新次数；
        cversion. 其子节点的更新次数；
        aclVersion. 节点ACL(授权信息)的更新次数；
        ephemeralOwner. 如果该节点为ephemeral节点, ephemeralOwner值表示与该节点绑定的session id. 如果该节点不是ephemeral节点, ephemeralOwner值为0.
        dataLength. 节点数据的字节数；
        numChildren. 子节点个数；
     */
    public Stat set(String path, String newData) throws Exception {
        return client.setData().forPath(path, newData.getBytes());
    }

    public Stat setListener(String path, String newData) throws Exception {
        CuratorListener listener = (client, event) -> {
            logger.info("set data event:{}", event.getType().name());
        };
        client.getCuratorListenable().addListener(listener);
        //listener 只能监听相同thread的client事件, 跨thread或者跨process则不行,
        // 操作必须使用inbackground()模式才能触发listener
        return client.setData().inBackground().forPath(path, newData.getBytes());
    }

    @ResponseBody
    @RequestMapping("/del")
    public Void delete(String path) throws Exception {
        return client.delete().forPath(path);
    }

    public void tran() throws Exception {
        client.inTransaction()
                .delete().forPath("/abc")
                .and().create().forPath("/def", "def".getBytes())
                .and().commit();
    }

    public List<String> watchedGetChildren(String path) throws Exception {
        return client.getChildren().usingWatcher(curatorWatcher).forPath(path);
    }

    public void lock(String lockPath) throws Exception {
        InterProcessMutex lock = new InterProcessMutex(client, lockPath);
        if (lock.acquire(10, TimeUnit.SECONDS)) {
            try {
                logger.info("i get a lock");
            } finally {
                lock.release();
            }
        }
    }

    public void leader(String path) {
        LeaderSelectorListener listener = new LeaderSelectorListenerAdapter() {
            public void takeLeadership(CuratorFramework client) throws Exception {
                logger.info("i'm a leader");
            }
        };

        LeaderSelector selector = new LeaderSelector(client, path, listener);
        selector.autoRequeue();  // not required, but this is behavior that you will probably expect
        selector.start();
    }
}
