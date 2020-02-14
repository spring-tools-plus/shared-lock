package net.madtiger.lock;

/**
 * 共享锁状态
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public enum SharedLockStatus {

  /**
   * 新建
   */
  NEW,

  /**
   * 已锁定
   */
  LOCKED,

  /**
   * 获取所超时
   */
  TIMEOUT,

  /**
   * 取消
   */
  CANCEL,

  /**
   * 获取锁成功后，解锁失败，此阶段正常应该回滚
   */
  UNLOCK_FAIL,

  /**
   * 超时后，已解锁
   */
  TIMEOUT_UNLOCK,

  /**
   * 取消后，已解锁
   */
  CANCEL_UNLOCK,

  /**
   * 释放锁成功
   */
  DONE;

}
