package com.zz.zookeeperui;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    private Logger logger = LoggerFactory.getLogger(Application.class);

    @Value("${zookeeper.url}")
    private String zkUrl;

    @Bean
    public CuratorFramework client() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkUrl, retryPolicy);
        client.start();
        /*
            Path Cache：监视一个路径下1）孩子结点的创建、2）删除，3）以及结点数据的更新。产生的事件会传递给注册的PathChildrenCacheListener。
            Node Cache：监视一个结点的创建、更新、删除，并将结点的数据缓存在本地。
            Tree Cache：Path Cache和Node Cache的“合体”，监视路径下的创建、更新、删除事件，并缓存路径下所有孩子结点的数据
        */
        PathChildrenCache watcher = new PathChildrenCache(client, "/zookeeper", true);
        watcher.getListenable().addListener((CuratorFramework client1, PathChildrenCacheEvent event) -> {
            ChildData data = event.getData();
            if (data == null) {
                logger.info("No data in event[" + event + "]");
            } else {
                logger.info("Receive event: "
                        + "type=[" + event.getType() + "]"
                        + ", path=[" + data.getPath() + "]"
                        + ", data=[" + new String(data.getData()) + "]"
                        + ", stat=[" + data.getStat() + "]");
            }
        });
        watcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        return client;
    }

    @Bean
    public CuratorWatcher defaultCuratorWatcher() {
        return (WatchedEvent event) -> logger.info("type:" + event.getType() + ",path:" + event.getPath());
    }

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
    }

}
