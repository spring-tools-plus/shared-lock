package net.madtiger.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import net.madtiger.lock.capable.ISharedLockExecute;

/**
 * 共享锁接口
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public interface ISharedLock extends Lock, AutoCloseable, ISharedLockExecute {


  /**
   * 获取所的状态
   * @return 当前状态
   */
  SharedLockStatus getStatus();

  /**
   * 获取锁的key
   * @return 当前 key
   */
  String getKey();

  /**
   * 需要锁定的时间
   * @return 当前设置的 时间
   */
  int getLockSeconds();


  /**
   * 流程结束
   * @return 是否结束
   */
  boolean isFinished();

  /**
   * 是否 以获取锁
   * @return 是否锁定
   */
  default boolean isLocked(){
    return getStatus() == SharedLockStatus.LOCKED;
  }

  /**
   * 是否需要 回滚, status={@link SharedLockStatus#UNLOCK_FAIL} 时返回true
   * @return 结果
   */
  default boolean isRollback(){
    return getStatus() == SharedLockStatus.UNLOCK_FAIL;
  }

  /**
   * 取消当前锁，只对 NEW 状态可用
   * @return 是否取消成功
   */
  boolean interrupted();


  /**
   * 此方法不支持
   * @return 条件
   */
  @Override
  default Condition newCondition() {
    throw new UnsupportedOperationException("newCondition 不支持");
  }

  @Override
  default void lock() {
    throw new UnsupportedOperationException("lock  方法不支持");
  }

  @Override
  default void lockInterruptibly() throws InterruptedException {
    throw new UnsupportedOperationException("lockInterruptibly  方法不支持");
  }

}
