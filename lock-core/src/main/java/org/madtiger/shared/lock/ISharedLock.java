package org.madtiger.shared.lock;

import java.util.function.Supplier;

/**
 * 共享锁接口，此接口的所有方法均为同步方法
 * @see LockResultHolder
 */
public interface ISharedLock<Args extends  SetLockArgs> {

  /**
   * 使用默认参数初始化共享锁，同时锁的申请和释放由系统全程托管
   *
   * @param key 锁唯一标示
   * @param callback 获取锁以后的执行函数，在当前线程执行
   * @param <T> callback 函数执行的返回结果
   * @return 结果持有对象
   */
  default <T> LockResultHolder<T> execute(final String key, final Supplier<T> callback) {
    return execute(key, callback, null);
  }

  /**
   * 执行回调，锁的申请释放都由系统管理
   * @param key 锁唯一标示
   * @param callback 获取锁以后的执行函数，在当前线程执行
   * @param args 设置参数
   * @param <T> callback 函数执行的返回结果
   * @return 结果持有对象
   */
  <T> LockResultHolder<T> execute(final String key, final Supplier<T> callback, final Args args);

  /**
   * 使用默认参数手动尝试获取锁，使用此方式获取的锁，需要自行释放，通过
   * @param key 锁唯一标示
   * @return 结果持有对象
   * @see LockResultHolder
   */
  default LockResultHolder<Void> tryLock(final String key){
    return tryLock(key, null);
  }

  /**
   * 手动尝试获取锁，使用此方式获取的锁，需要自行释放，通过
   * @param key 锁唯一标示
   * @param args 设置参数
   * @return 结果持有对象
   * @see LockResultHolder
   */
  LockResultHolder<Void> tryLock(final String key, final Args args);

  /**
   * 释放锁 根据 {@link #tryLock(String, SetLockArgs)} 获取的结果释放
   * @param resultHolder
   * @return
   */
  boolean release(LockResultHolder resultHolder);

}
