package net.madtiger.lock.exception;

/**
 * 共享锁异常基类
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public class AbsSharedLockException extends Exception{


  /**
   * key
   */
  protected String key;

  /**
   * 构造函数
   * @param key 释放的key
   */
  public AbsSharedLockException(String key) {
    this.key = key;
  }

}
