package net.madtiger.shared.lock;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * redis 共享锁服务
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class RedisLockService extends AbsSpinLock {

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
  public RedisLockService(RedisLockClient lockRedisClient){
    Objects.requireNonNull(lockRedisClient);
    this.lockRedisClient = lockRedisClient;
  }

  @Override
  protected boolean setNX(LockResultHolder resultHolder) {
    Objects.requireNonNull(resultHolder.args);
    return lockRedisClient.setNX(resultHolder);
  }

  @Override
  protected boolean release(LockResultHolder resultHolder) {
    Objects.requireNonNull(resultHolder.args);
    Assert.isTrue(resultHolder.args.getGetTimeoutMills() >= SetLockArgs.NET_TIMEOUT, String.format("Redis 超时时间不能小于 %s 毫秒", SetLockArgs.NET_TIMEOUT));
    // 如果释放失败
    if (!lockRedisClient.releaseByLua(RELEASE_LUA, resultHolder)){
      log.info("释放锁{}失败{}，开始降级释放...", resultHolder.getKey(), lockRedisClient.get(resultHolder));
      // 降级释放
      if (resultHolder.getValue().equalsIgnoreCase(lockRedisClient.get(resultHolder))){
        lockRedisClient.delete(resultHolder);
        log.info("释放锁{}, 降级释放成功", resultHolder.getKey());
      }else {
        log.error("释放锁{}, 降级释放失败", resultHolder.getKey());
        return false;
      }
    }
    return true;
  }
}
