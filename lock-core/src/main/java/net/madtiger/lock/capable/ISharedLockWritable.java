package net.madtiger.lock.capable;

import net.madtiger.lock.SharedLockStatus;

/**
 * 共享锁 状态 可修改能力
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public interface ISharedLockWritable {


  /**
   * 设置 锁定时间，单位秒
   * @param lockSeconds 要设置的锁定时长
   */
  void setLockSeconds(int lockSeconds);

  /**
   * 设置 状态
   * @param status 需要更新的状态
   */
  void setStatus(SharedLockStatus status);

  /**
   * 获取当前状态
   * @return
   */
  SharedLockStatus getStatus();

  /**
   * 当前状态在给定的状态列表中
   * @param statuses 检查的状态列表
   * @return 结果
   */
  boolean inStates(SharedLockStatus...statuses);

  /**
   * 状态
   * @param status
   * @return
   */
  boolean isStatus(SharedLockStatus status);

  /**
   * 是否需要解锁
   * @return
   */
  boolean needUnlock();

  /**
   * 成功并设置状态
   * @param release 是否释放成功
   */
  default void unlocked(boolean release) {
    // 如果之前是 cancel ，则改成 cancel unlock
    if (isStatus(SharedLockStatus.CANCEL)) {
      setStatus(SharedLockStatus.CANCEL_UNLOCK);
    }
    // TIMEOUT -> TIMEOUT_UNLOCK
    else if (isStatus(SharedLockStatus.TIMEOUT)) {
      setStatus(SharedLockStatus.TIMEOUT_UNLOCK);
    }
    // LOCKED -> DONE|UNLOCK_FAIL
    else if (isStatus(SharedLockStatus.LOCKED)) {
      setStatus(release ? SharedLockStatus.DONE : SharedLockStatus.UNLOCK_FAIL);
    }
  }
}
