package net.madtiger.shared.lock;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * 包含基础实现的 抽象类，子类可以直接实现此类
 * @author Fenghu.Shi
 * @version 1.0
 */
public abstract class AbsSharedLock implements ISharedLock{


  /**
   * 锁实现
   * @param key 锁唯一标示
   * @param callback 获取锁以后的执行函数，在当前线程执行
   * @param args 设置参数
   * @param <T>
   * @return
   */
  @Override
  public <T> LockResultHolder<T> execute(String key, Supplier<T> callback,
      SetLockArgs args) {
    Objects.requireNonNull(callback);
    args = args == null ? SetLockArgs.builder().build() : args;
    LockResultHolder<T> resultHolder = (LockResultHolder<T>) LockResultHolder
        .builder().sharedLock(this).args(args).key(key).value(UUID.randomUUID().toString()).build();
    // 尝试获取锁
    try{
      if (tryAcquire(resultHolder)){
        try {
          resultHolder.status = LockResultHolder.DONE;
          resultHolder.returnData = callback.get();
        } finally{
          // 释放锁
          if (!release(resultHolder)){
            // 来一个回滚状态
            resultHolder.status = LockResultHolder.ROLLBACK;
          }
        }
        // 如果是 rollback
        if (resultHolder.isRollback()) {
          // 检查 回退
          resultHolder.rollback();
        }
        return resultHolder;
      }else {
        // 如果有失败的则执行
        resultHolder.status = LockResultHolder.TIMEOUT;
        return resultHolder;
      }
    }catch (Throwable ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  @Override
  public <T> LockResultHolder<T> tryLock(String key, SetLockArgs args) {
    args = args == null ? SetLockArgs.builder().build() : args;
    LockResultHolder resultBuilder = LockResultHolder
        .builder().args(args).sharedLock(this).value(UUID.randomUUID().toString()).key(key).build();
    try{
      if (tryAcquire(resultBuilder)){
        resultBuilder.status = LockResultHolder.LOCKING;
        return resultBuilder;
      }else {
        resultBuilder.status = LockResultHolder.TIMEOUT;
      }
    }catch (Throwable ex) {
      throw new IllegalArgumentException(ex);
    }
    return resultBuilder;
  }

  /**
   * 实现 锁
   * @param resultHolder
   * @return
   */
  @Override
  public boolean unlock(LockResultHolder resultHolder) {
    // 检查是否锁定,如果不锁定，则直接释放失败
    if (resultHolder == null || !resultHolder.mybeRelease() || resultHolder.args == null){
      return false;
    }
    try{
      // 释放锁
      if (!release(resultHolder)){
        resultHolder.status = LockResultHolder.ROLLBACK;
        resultHolder.rollback();
        return false;
      }else {
        resultHolder.status = LockResultHolder.DONE;
        return true;
      }
    }catch (Throwable ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * 通过自旋锁形式获取锁
   * @param resultHolder 结果持有者，里面包含所有参数
   * @return 获取结果
   */
  protected abstract  boolean tryAcquire(LockResultHolder resultHolder) throws Exception;


  /**
   * 释放锁
   * @param resultHolder result holder
   * @return 释放结果
   */
  protected abstract boolean release(LockResultHolder resultHolder) throws Exception;

}
