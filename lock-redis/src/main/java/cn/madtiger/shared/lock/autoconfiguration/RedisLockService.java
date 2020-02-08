package cn.madtiger.shared.lock.autoconfiguration;

import cn.madtiger.shared.lock.AbsSpinLock;
import cn.madtiger.shared.lock.LockResultHolder;
import cn.madtiger.shared.lock.SetLockArgs;
import cn.madtiger.shared.lock.SpinSetLockArgs;
import java.util.Objects;
import java.util.UUID;
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


  private LockRedisClient lockRedisClient;

  /**
   * 构造函数，必须指定一个 redis client
   * @param lockRedisClient
   */
  @Autowired
  public RedisLockService(LockRedisClient lockRedisClient){
    Objects.requireNonNull(lockRedisClient);
    this.lockRedisClient = lockRedisClient;
  }

  @Override
  protected boolean setNX(String key, String value, SpinSetLockArgs args) {
    return lockRedisClient.setNX(key, value,args.getGetTimeoutMills(), args.getLockedSeconds());
  }

  @Override
  protected boolean release(String key, String value, SpinSetLockArgs args) {
    args = args == null ?  SpinSetLockArgs.builder().build() : args;
    Assert.isTrue(args.getGetTimeoutMills() >= SetLockArgs.NET_TIMEOUT, String.format("Redis 超时时间不能小于 %s 毫秒", SetLockArgs.NET_TIMEOUT));
    // 如果释放失败
    if (!lockRedisClient.releaseByLua(RELEASE_LUA, key, value, args.getGetTimeoutMills())){
      log.info("释放锁{}失败{}，开始降级释放...", key, lockRedisClient.get(key, args.getGetTimeoutMills()));
      // 降级释放
      if (value.equalsIgnoreCase(lockRedisClient.get(key, args.getGetTimeoutMills()))){
        lockRedisClient.delete(key, args.getGetTimeoutMills());
        log.info("释放锁{}, 降级释放成功", key);
      }else {
        log.error("释放锁{}, 降级释放失败", key);
        return false;
      }
    }
    return true;
  }

  @Override
  public LockResultHolder<Void> tryLock(String key, SpinSetLockArgs args) {
    LockResultHolder.LockResultHolderBuilder resultBuilder = LockResultHolder.builder().args(args).sharedLock(this).key(key);
    // 创建一个 uuid
    final String value = UUID.randomUUID().toString();
    resultBuilder.value(value);
    if (tryAcquire(key, value, args)){
      resultBuilder.status(LockResultHolder.LOCKING);
      return resultBuilder.build();
    }
    return resultBuilder.build();
  }
}
