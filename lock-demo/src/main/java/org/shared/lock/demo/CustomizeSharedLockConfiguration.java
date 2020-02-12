package org.shared.lock.demo;

import net.madtiger.shared.lock.CuratorLockClient;
import net.madtiger.shared.lock.ISharedLock;
import net.madtiger.shared.lock.ZookeeperLockService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;

/**
 * 手动 配置 分布式锁
 * @author Fenghu.Shi
 * @version 1.0
 */
public class CustomizeSharedLockConfiguration {

  /**
   * 配置 共享锁
   * @param redisTemplate
   * @return
   */
//  @Bean
//  public ISharedLock redisSharedLock(RedisTemplate redisTemplate){
//    return new RedisLockService(new RedisLockClient(redisTemplate));
//  }

  /**
   * 配置 共享锁
   * @param curatorFramework
   * @return
   */
  @Bean
  public ISharedLock zkSharedLock(CuratorFramework curatorFramework){
    return new ZookeeperLockService(new CuratorLockClient(curatorFramework));
  }


}
