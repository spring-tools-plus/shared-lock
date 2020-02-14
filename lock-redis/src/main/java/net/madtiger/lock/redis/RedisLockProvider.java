package net.madtiger.lock.redis;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.madtiger.lock.CompositeSharedLock;
import net.madtiger.lock.exception.UnLockFailSharedLockException;
import net.madtiger.lock.provider.ISharedLockProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * redis 共享锁服务
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class RedisLockProvider implements ISharedLockProvider {

  /**
   * 解锁的lua脚本
   */
  private static final String RELEASE_LUA;

  static {
    StringBuilder sb = new StringBuilder();
    sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
    sb.append("then ");
    sb.append("    return redis.call(\"del\",KEYS[1]) ");
    sb.append("else ");
    sb.append("    return 0 ");
    sb.append("end ");
    RELEASE_LUA = sb.toString();
  }

  private RedisLockClient lockRedisClient;

  /**
   * 构造函数，必须指定一个 redis client
   * @param lockRedisClient
   */
  @Autowired
  public RedisLockProvider(RedisLockClient lockRedisClient){
    Objects.requireNonNull(lockRedisClient);
    this.lockRedisClient = lockRedisClient;
  }

  @Override
  public boolean doAcquire(CompositeSharedLock lock) {
    String uuid = UUID.randomUUID().toString();
    // 设置 数据
    lock.setProviderData(uuid);
    return lockRedisClient.setNX(lock.getKey(), uuid, lock.getLockSeconds());
  }

  @Override
  public boolean doAcquire(CompositeSharedLock lock, long time, TimeUnit unit) {
    String uuid = UUID.randomUUID().toString();
    // 设置 数据
    lock.setProviderData(uuid);
    long stopTime = unit.toMillis(time) + System.currentTimeMillis();
    do{
      // 如果设置成功
      if (lockRedisClient.setNX(lock.getKey(), uuid, lock.getLockSeconds())) {
        return true;
      }
      try {
        // 随机休眠
        Thread.sleep((long) (200 + (int) 500 * Math.random()));
      } catch (InterruptedException e) {
        return false;
      }
    }while(System.currentTimeMillis() < stopTime);
    return false;
  }

  @Override
  public boolean doRelease(CompositeSharedLock lock) throws UnLockFailSharedLockException {
    String uuid =  lock.getProviderData();
    if (StringUtils.isEmpty(uuid)) {
      throw new IllegalArgumentException(String.format("redis 共享锁 %s provider data不存在", lock.getKey()));
    }
    // 先来 lua 释放
    if (lockRedisClient.releaseByLua(RELEASE_LUA, lock.getKey(), uuid)) {
      return true;
    }
    // 降级释放
    // 如果存在，key ，切value == uuid
    if (uuid.equals(lockRedisClient.get(lock.getKey()))) {
      return lockRedisClient.delete(lock.getKey());
    }
    return false;
  }
}
