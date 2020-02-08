package org.madtiger.shared.lock;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * 自旋锁基类
 * @author Fenghu.Shi
 * @version 1.0
 */
public abstract class AbsSpinLock implements ISharedLock<SpinSetLockArgs>{

  /**
   * 自旋锁实现
   * @param key 锁唯一标示
   * @param callback 获取锁以后的执行函数，在当前线程执行
   * @param args 设置参数
   * @param <T>
   * @return
   */
  @Override
  public <T> LockResultHolder<T> execute(String key, Supplier<T> callback,
      SpinSetLockArgs args) {
    Objects.requireNonNull(callback);
    args = args == null ? SpinSetLockArgs.builder().build(): args;
    LockResultHolder.LockResultHolderBuilder resultBuilder = LockResultHolder.builder().args(args).key(key);
    // 创建一个 uuid
    final String value = UUID.randomUUID().toString();
    resultBuilder.value(value);
    // 尝试获取锁
    if (tryAcquire(key, value,  args)){
      try {
        resultBuilder.status(LockResultHolder.DONE);
        resultBuilder.returnData(callback.get());
        return resultBuilder.build();
      } finally{
        // 释放锁
        if (!release(key, value, args)){
          resultBuilder.status(LockResultHolder.ROLLBACK);
          resultBuilder.build().rollback(args.rollbackCallback);
        }
      }
    }else {
      // 如果有失败的则执行
      resultBuilder.status(LockResultHolder.TIMEOUT);
      args.timeoutCallback.callback();
      return resultBuilder.build().timeoutAfter(args.timeoutCallback);
    }
  }

  /**
   * 实现 锁
   * @param resultHolder
   * @return
   */
  @Override
  public boolean release(LockResultHolder resultHolder) {
    // 检查是否锁定,如果不锁定，则直接释放失败
    if (resultHolder == null || !resultHolder.isLocking()){
      return false;
    }
    // 释放锁
    if (!release(resultHolder.key, resultHolder.value, (SpinSetLockArgs) resultHolder.args)){
      return false;
    }else {
      return true;
    }
  }

  /**
   * 通过自旋锁形式获取锁
   * @param key 锁唯一标示
   * @param value 锁值
   * @param args 参数
   * @return 获取结果
   */
  protected boolean tryAcquire(String key, String value, SpinSetLockArgs args){
    args = args == null ? SpinSetLockArgs.builder().build(): args;
    long timeout = System.currentTimeMillis() + args.waitTimeoutSeconds * 1000;
    int times = args.spinTimes;
    int timesCount = 0;
    try {
      do {
        times = args.spinTimes;
        // 自旋 times 次
        for (; ; ) {
          // 如果最大尝试次数大于1，则
          if (args.maxRetryTimes > 1 && timesCount > args.maxRetryTimes){
            System.out.println("总获取次数超时");
            return false;
          }
          checkTimeout(key, timeout);
          // 如果获取成功则返回成功
          if (setNX(key, value, args)){
            return true;
          }
          if (--times <= 0){
            break;
          }
          timesCount ++;
        }
        // 随机休眠
        try {
          Thread.sleep(args.sleepMinMills + (long) ((args.sleepMaxMills - args.sleepMinMills) * Math.random()));
        } catch (Exception e) {
          return  false;
        }
      } while (true);
    }catch(TimeoutException ex){
      return false;
    }
  }

  /**
   * 从数据中心获取临界资源
   * <p>
   *   当该标示未被别人设置时才可以设置并返回 true，否则返回 false，一般支持 链接超时时间，并需要设置锁最长持有时间，超过时数据中心可以自行释放
   * </p>
   * @param key 锁标示
   * @param value
   * @param args
   * @return 设置结果
   */
  protected abstract boolean setNX(String key, String value, final SpinSetLockArgs args);


  /**
   * 释放锁
   * @param key 锁标示
   * @param value 锁的值
   * @param args 参数
   * @return 释放结果
   */
  protected abstract  boolean release(String key, String value, final SpinSetLockArgs args);

  /**
   * 检查 超时
   * @param key 检查的锁 key
   * @param timeout 检查的时间
   * @throws TimeoutException
   */
  private void checkTimeout(String key, long timeout) throws TimeoutException {
    if (System.currentTimeMillis() >= timeout){
      throw new TimeoutException(String.format("获取%s锁超时", key));
    }
  }
}
