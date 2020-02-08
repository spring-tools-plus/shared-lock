package cn.madtiger.shared.lock.redis;

import java.util.concurrent.TimeoutException;
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
   * 强制执行返回直接
   * @param key 临界资源标示
   * @param callback 成功回调
   * @param failed 失败回调
   * @param <T> 返回值类型
   * @return 返回值
   * @throws TimeoutException
   */
  default <T> T executeGet(final String key, final Supplier<T> callback, final Supplier<T> failed)  throws TimeoutException {
    return executeGet(key, callback, failed, null, null);
  }

  /**
   * 强制执行返回结果
   * @param key 临界资源标示
   * @param callback 成功回调
   * @param failed 失败回调
   * @param rollback 回滚回调
   * @param args 参数
   * @param <T>
   * @return
   * @throws java.util.concurrent.TimeoutException 如果获取失败，有没有传递 failed 函数，则抛出此异常
   */
  default <T> T executeGet(final String key, final Supplier<T> callback, final Supplier<T> failed, final Supplier<T> rollback, Args args)
      throws TimeoutException {
    LockResultHolder<T> resultHolder = execute(key, callback, args);
    // 成功
    if (resultHolder.isDone()){
      return resultHolder.returnData();
    }
    // 回滚状态
    if (resultHolder.isRollback()){
      return rollback == null ? resultHolder.returnData() : callback.get();
    }
    // 其他均为失败
    if (failed != null){
      return failed.get();
    }
    throw new TimeoutException(String.format("获取 %s 共享锁失败", key));
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
  default <T> LockResultHolder<T> tryLock(final String key){
    return tryLock(key);
  }

  /**
   * 获取所并返回获取结果，需要传递一个 holder
   * @param key 锁唯一标示
   * @param holder 锁持有者 {@link LockResultHolder#newInstance()} 通过这个方法创建
   * @return
   */
 default <T> boolean tryLockGet(final String key, final LockResultHolder<T> holder) {
   // 检查 holder 是否未初始化
   if (LockResultHolder.INIT != holder.status){
     throw new IllegalArgumentException("当前的 holder 已被其他锁使用，不能重复使用。");
   }
   LockResultHolder<T> sourceHolder = tryLock(key, (Args) holder.args);
   // 设置值
   holder.args = sourceHolder.args;
   holder.key = sourceHolder.key;
   holder.sharedLock = sourceHolder.sharedLock;
   holder.value = sourceHolder.value;
   holder.status = sourceHolder.status;
   return sourceHolder.isLocking();
 }

  /**
   * 手动尝试获取锁，使用此方式获取的锁，需要自行释放
   * @param key 锁唯一标示
   * @param args 设置参数
   * @return 结果持有对象
   * @see LockResultHolder
   */
  <T> LockResultHolder<T> tryLock(final String key, final Args args);

  /**
   * 释放锁 根据 {@link #tryLock(String, SetLockArgs)} 获取的结果释放
   * @param resultHolder
   * @return
   */
  boolean unlock(LockResultHolder resultHolder);

}
