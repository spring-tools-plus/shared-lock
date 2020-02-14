package net.madtiger.lock.exception;

/**
 * 释放资源失败异常
 * @author Fenghu.Shi
 * @version 1.0
 */
public class UnLockFailSharedLockException extends AbsSharedLockException {

  /**
   * 构造函数
   * @param key 释放的key
   */
  public UnLockFailSharedLockException(String key) {
    super(key);
  }
}
