package com.zz.zookeeperui;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    private String zkUrl = "127.0.0.1:2181";

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
        watcher.getListenable().addListener((client1, event) -> {
            ChildData data = event.getData();
            if (data == null) {
                System.out.println("No data in event[" + event + "]");
            } else {
                System.out.println("Receive event: "
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
        return event -> System.out.println("type:" + event.getType() + ",path:" + event.getPath());
    }

}
