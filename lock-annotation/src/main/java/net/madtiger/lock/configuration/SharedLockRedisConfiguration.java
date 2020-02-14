package net.madtiger.lock.configuration;

import java.util.Arrays;
import java.util.List;
import net.madtiger.lock.AbsSharedLockDecorator;
import net.madtiger.lock.decorator.SpinLockDecorator;
import net.madtiger.lock.provider.ISharedLockProvider;
import net.madtiger.lock.redis.RedisLockClient;
import net.madtiger.lock.redis.RedisLockProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 自动装载
 * @author Fenghu.Shi
 * @version 1.0
 */
@Configuration
public class SharedLockRedisConfiguration extends AbsSharedLockConfiguration<RedisTemplate> {

  @Override
  protected List<Class<? extends AbsSharedLockDecorator>> defaultDecorators() {
    return Arrays.asList(SpinLockDecorator.class);
  }

  /**
   * 配置 共享锁
   * @param redisTemplate
   * @return
   */
  @Override
  protected ISharedLockProvider newSharedLockProvider(RedisTemplate redisTemplate){
    return new RedisLockProvider(new RedisLockClient(redisTemplate));
  }
}
