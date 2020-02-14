package net.madtiger.lock.exception;

/**
 * 获取锁超时异常
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public class TimeoutSharedLockException extends AbsSharedLockException{

  /**
   * 构造函数
   *
   * @param key 释放的key
   */
  public TimeoutSharedLockException(String key) {
    super(key);
  }
}
