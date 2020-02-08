package org.shared.lock.demo;

import cn.madtiger.shared.lock.redis.ISharedLock;
import cn.madtiger.shared.lock.redis.LockRedisClient;
import cn.madtiger.shared.lock.redis.RedisLockService;
import cn.madtiger.shared.lock.redis.SpinSetLockArgs;
import cn.madtiger.shared.lock.redis.SpringRedisLockClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

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
  @Bean
  public ISharedLock<SpinSetLockArgs> defaultSharedLock(ObjectProvider<RedisTemplate> redisTemplate){
    return new RedisLockService(new SpringRedisLockClient(redisTemplate.getIfAvailable()));
  }

}
