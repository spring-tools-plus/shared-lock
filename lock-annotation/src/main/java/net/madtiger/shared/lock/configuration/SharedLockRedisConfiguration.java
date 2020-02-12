package net.madtiger.shared.lock.configuration;

import net.madtiger.shared.lock.ISharedLock;
import net.madtiger.shared.lock.RedisLockClient;
import net.madtiger.shared.lock.RedisLockService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 自动装载
 * @author Fenghu.Shi
 * @version 1.0
 */
@Configuration
public class SharedLockRedisConfiguration extends AbsSharedLockConfiguration<RedisTemplate> {

  /**
   * 配置 共享锁
   * @param redisTemplate
   * @return
   */
  @Override
  protected ISharedLock newSharedLock(RedisTemplate redisTemplate){
    return new RedisLockService(new RedisLockClient(redisTemplate));
  }
}
