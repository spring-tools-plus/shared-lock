package net.madtiger.lock.provider;

/**
 * 锁服务端的提供者接口
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */

import java.util.concurrent.TimeUnit;
import net.madtiger.lock.CompositeSharedLock;
import net.madtiger.lock.ISharedLock;
import net.madtiger.lock.exception.UnLockFailSharedLockException;

/**
 * 共享锁接口，此接口的所有方法均为同步方法
 * @author Fenghu.Shi
 * @version 1.0
 */
public interface ISharedLockProvider {


  /**
   * 立即获取锁
   * @param lock 需要获取所的对象
   * @return 获取所结果
   */
  boolean doAcquire(CompositeSharedLock lock);

  /**
   * 特定时间内获取锁
   * @param lock 需要获取所的对象
   * @param time 时间
   * @param unit 单位
   * @return 获取所结果
   */
  boolean doAcquire(CompositeSharedLock lock, long time, TimeUnit unit);

  /**
   * 释放锁
   * @param lock 需要释放锁的对象
   * @return 获取所结果
   * @throws UnLockFailSharedLockException done 状态，释放失败，则抛出磁异常
   */
  boolean doRelease(CompositeSharedLock lock) throws UnLockFailSharedLockException;

}
