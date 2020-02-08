package net.madtiger.shared.lock.redis;

/**
 * callback  接口，用于一些回调执行场景
 */
public interface DoCallback {

  /**
   * 执行方法体
   */
  void callback();

  /**
   * 空的方法体
   */
  static final DoCallback NOOP = () -> {};
}
