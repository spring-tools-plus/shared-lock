package net.madtiger.lock;

import static net.madtiger.lock.SharedLockStatus.LOCKED;
import static net.madtiger.lock.SharedLockStatus.NEW;
import static net.madtiger.lock.SharedLockStatus.TIMEOUT;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.madtiger.lock.exception.TimeoutSharedLockException;
import net.madtiger.lock.exception.UnLockFailSharedLockException;

/**
 * 简单的互斥锁
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
@Slf4j
public class DefaultSharedLock extends AbsSharedLock{

  /**
   * 构造函数
   *
   * @param providerName 服务提供者名称
   * @param lockSenconds 锁
   * @param key          锁定的 key
   */
  public DefaultSharedLock(String key, String providerName, int lockSenconds) {
    super(key, providerName, lockSenconds);
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(unit);
    // 如果当前已经获取锁，则检查是否超时
    // 检查状态
    if (status != NEW) {
      throw new InterruptedException(String.format("%s锁的当前状态是 %s 不能再次获取锁", key, status));
    }
    // 获取成功
    try{
      // 如果小于0 则降级成 try lock
      if (time <= 0){
        if (provider.doAcquire(this, time, unit) ){
          status = LOCKED;
          return true;
        } else {
          status = TIMEOUT;
          return false;
        }
      }
      if (doAcquire(time, unit)) {
        status = LOCKED;
        return true;
      }else {
        // 如果 是 新的，则设置为 长时间
        if (status == NEW) {
          status = TIMEOUT;
        }
        return false;
      }
    }catch (TimeoutSharedLockException timeout){
      status = TIMEOUT;
      return false;
    }
  }

  /**
   * 支持自旋的 获取
   * @param time
   * @param unit
   * @return
   * @throws InterruptedException
   */
  protected boolean doAcquire(long time, TimeUnit unit) throws InterruptedException, TimeoutSharedLockException{
    return provider.doAcquire(this, time, unit);
  }

  @Override
  public void unlock() {
    try{
      unlocked(provider.doRelease(this));
    }catch (UnLockFailSharedLockException ex) {
      // 释放失败
      unlocked(false);
    }
  }
}
