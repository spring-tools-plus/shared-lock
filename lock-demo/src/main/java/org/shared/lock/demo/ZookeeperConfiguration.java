package org.shared.lock.demo;

import java.io.IOException;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.DefaultACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * zk 的配置
 * @author Fenghu.Shi
 * @version 1.0
 */
@Configuration
public class ZookeeperConfiguration {


  @Bean
  public ZooKeeper newZookeeper() throws IOException{
    return new ZooKeeper(
        "127.0.0.1:2181",
        600000,
        event -> {
          if (Event.KeeperState.SyncConnected == event.getState()) {
            System.out.println("初始化成功");
          }
        });
  }

  @Bean
  public CuratorFramework newCurator() throws Exception{
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client =
        CuratorFrameworkFactory.builder()
            .connectString("127.0.0.1:2181")
            .retryPolicy(retryPolicy)
            .connectionTimeoutMs(500)
            .sessionTimeoutMs(60000)
            .build();
    client.start();
    return client;
  }

}
