package net.madtiger.shared.lock;


import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * 共享锁接口，此接口的所有方法均为同步方法
 * @author Fenghu.Shi
 * @version 1.0
 * @see LockResultHolder
 */
public interface ISharedLock {

  /**
   * 使用默认参数初始化共享锁，同时锁的申请和释放由系统全程托管
   *
   * @param key 锁唯一标示
   * @param callback 获取锁以后的执行函数，在当前线程执行
   * @param <T> callback 函数执行的返回结果
   * @return 结果持有对象
   */
  default <T> LockResultHolder<T> execute(final String key, final Supplier<T> callback) {
    return execute(key, callback, SetLockArgs.buildDefaultArgs());
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
  default <T> T executeGet(final String key, final Supplier<T> callback, @Nullable final Supplier<T> failed)  throws TimeoutException {
    return executeGet(key, callback, failed, null, SetLockArgs.buildDefaultArgs());
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
   * @throws TimeoutException 如果获取失败，有没有传递 failed 函数，则抛出此异常
   */
  default <T> T executeGet(final String key, final Supplier<T> callback, @Nullable final Supplier<T> failed, @Nullable final Supplier<T> rollback, @Nullable SetLockArgs args)
      throws TimeoutException {
    args = args == null ? SetLockArgs.buildDefaultArgs() : args;
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
  <T> LockResultHolder<T> execute(final String key, final Supplier<T> callback, final SetLockArgs args);

  /**
   * 在特定等待时间内尝试获取锁
   * @param key 锁的唯一表示
   * @param waitTimeout 等待超时时间 <=0 表示单次获取
   * @param unit 时间单位
   * @param holder 结果持有对象，用于后面释放锁
   * @param <T> 返回值类型
   * @return 获取结果
   */
  default <T> boolean tryLockGet(String key, int waitTimeout, TimeUnit unit, LockResultHolder<T> holder) {
    Objects.requireNonNull(holder);
    if (TimeUnit.MILLISECONDS != unit && TimeUnit.SECONDS != unit) {
      throw new IllegalArgumentException("waitTimeout 仅支持 MILLISECONDS 和 SECONDS 单位");
    }
    if (!(holder instanceof LockResultHolderWrapper)){
      throw new IllegalArgumentException("holder 请使用正确方式(net.madtiger.shared.lock.LockResultHolder.newInstance())方法创建！");
    }
    LockResultHolderWrapper holderWrapper = (LockResultHolderWrapper) holder;
    if (holderWrapper.getDelegate() != null){
      throw new IllegalArgumentException("holder 请使用正确方式(net.madtiger.shared.lock.LockResultHolder.newInstance())方法创建！");
    }
    SetLockArgs args = holder.args == null ? SetLockArgs.buildDefaultArgs() : holder.args;
    // 如果小于等于0则认为是单次获取
    if (waitTimeout <= 0 ) {
      //设置最大尝试次数为1
      args.maxRetryTimes = 1;
    }else {
      args.waitTimeoutMills = (int) unit.toMillis(Long.valueOf(waitTimeout));
    }
    holder.args = args;
    LockResultHolder<T> sourceHolder = tryLock(key, holder.args);
    holderWrapper.setDelegate(sourceHolder);
    return sourceHolder.isLocking();
  }


  /**
   * 获取所并返回获取结果，需要传递一个 holder
   * @param key 锁唯一标示
   * @param holder 锁持有者 {@link LockResultHolder#newInstance()} 通过这个方法创建
   * @return
   */
 default <T> boolean tryLockGet(final String key, final LockResultHolder<T> holder) {
    return tryLockGet(key, -1, TimeUnit.MILLISECONDS, null);
 }


  /**
   * 使用默认参数手动尝试获取锁，使用此方式获取的锁，需要自行释放，通过 ，只获取一次，如果失败则不等待
   * @param key 锁唯一标示
   * @return 结果持有对象
   * @see LockResultHolder
   */
  default <T> LockResultHolder<T> tryLock(final String key){
    return tryLock(key, -1, TimeUnit.MILLISECONDS);
  }

  /**
   * 根据一个给定的锁最长等待时间尝试获取锁
   * @param key 锁的key
   * @param waitTimeout 获取锁最长等待时间
   * @param unit 单位
   * @param <T> 返回值类型
   * @return 结果持有对象
   */
  default <T> LockResultHolder<T> tryLock(String key, int waitTimeout, TimeUnit unit) {
    if (TimeUnit.MILLISECONDS != unit && TimeUnit.SECONDS != unit) {
      throw new IllegalArgumentException("waitTimeout 仅支持 MILLISECONDS 和 SECONDS 单位");
    }
    SetLockArgs args = null;
    // 如果小于等于0则认为是单次获取
    if (waitTimeout <= 0 ) {
      //设置最大尝试次数为1
      args = SetLockArgs.builder().maxRetryTimes(1).build();
    }else {
      args = SetLockArgs.builder().waitTimeoutMills((int) unit.toMillis(Long.valueOf(waitTimeout))).build();
    }
    return tryLock(key, args);
  }

  /**
   * 手动尝试获取锁，使用此方式获取的锁，需要自行释放，只获取一次，如果失败则不等待
   * @param key 锁唯一标示
   * @param args 设置参数
   * @return 结果持有对象
   * @see LockResultHolder
   */
  <T> LockResultHolder<T> tryLock(final String key, @Nullable final SetLockArgs args);

  /**
   * 释放锁 根据 {@link #tryLock(String, SetLockArgs)} 获取的结果释放
   * @param resultHolder 锁持有者
   * @return  解锁结果
   * @throws LockReleaseException 持有锁时，解锁失败抛出磁异常，使用此异常进行 rollback 操作
   */
  boolean unlock(LockResultHolder resultHolder) throws LockReleaseException;

}
