package net.madtiger.shared.lock;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 包含基础实现的 抽象类，子类可以直接实现此类
 * @author Fenghu.Shi
 * @version 1.0
 */
public abstract class AbsSharedLock implements ISharedLock {

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
    // 获取一个锁
    try(LockResultHolder<T> resultHolder = tryLock(key, args)){
      // 如果获取成功，则执行 回调
      if (resultHolder.isLocking()){
          resultHolder.returnData = callback.get();
      }
      return resultHolder;
    } catch (IOException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  @Override
  public <T> LockResultHolder<T> tryLock(String key, SetLockArgs args) {
    args = args == null ? SetLockArgs.buildDefaultArgs() : args;
    LockResultHolder resultHolder = LockResultHolder
        .builder().args(args).sharedLock(this).value(UUID.randomUUID().toString()).key(key).build();
    // 添加到本地线程中
    SharedLockContextHolder.add(resultHolder);
    // 开始尝试获取锁
    if (tryAcquire(resultHolder)){
      resultHolder.status(LockResultHolder.LOCKING);
      return resultHolder;
    }else {
      resultHolder.status(LockResultHolder.TIMEOUT);
    }
    return resultHolder;
  }

  /**
   * 实现 锁
   * @param resultHolder
   */
  @Override
  public boolean unlock(LockResultHolder resultHolder) throws LockReleaseException{
    // 释放本地线程的数据
    if (resultHolder != null){
      SharedLockContextHolder.remove(resultHolder.getKey());
    }
    // 检查是否锁定,如果不锁定，则直接释放失败
    if (resultHolder == null || !resultHolder.mybeRelease() || resultHolder.args == null){
      return false;
    }
    // 释放锁
    if (!release(resultHolder)){
      // 如果没有超时，则设置 rollback
      if (!resultHolder.isTimeout()){
        resultHolder.status(LockResultHolder.ROLLBACK);
        resultHolder.rollback();
      }
      throw new LockReleaseException();
    }else {
      resultHolder.status(LockResultHolder.DONE);
      return true;
    }
  }

  /**
   * 获取锁实现
   * @param resultHolder 结果持有者，里面包含所有参数
   * @return 获取结果
   */
  protected abstract boolean tryAcquire(LockResultHolder resultHolder);


  /**
   * 释放锁实现
   * @param resultHolder result holder
   * @return 释放结果
   */
  protected abstract boolean release(LockResultHolder resultHolder);

}
