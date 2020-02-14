package net.madtiger.lock;

import static net.madtiger.lock.SharedLockStatus.CANCEL;
import static net.madtiger.lock.SharedLockStatus.CANCEL_UNLOCK;
import static net.madtiger.lock.SharedLockStatus.DONE;
import static net.madtiger.lock.SharedLockStatus.LOCKED;
import static net.madtiger.lock.SharedLockStatus.TIMEOUT;
import static net.madtiger.lock.SharedLockStatus.TIMEOUT_UNLOCK;
import static net.madtiger.lock.SharedLockStatus.UNLOCK_FAIL;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.madtiger.lock.capable.IProviderWritable;
import net.madtiger.lock.capable.ISharedLockWritable;
import net.madtiger.lock.exception.TimeoutSharedLockException;

/**
 * 集合了所有接口能力的集合接口，内部流转都使用此接口
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public interface CompositeSharedLock extends ISharedLock, IProviderWritable, ISharedLockWritable {

  /**
   * 是否需要解锁
   * @return 是否需要
   */
  @Override
  default boolean needUnlock(){
    return inStates(LOCKED, TIMEOUT, CANCEL);
  }


  @Override
  default boolean isFinished(){
    return inStates(TIMEOUT_UNLOCK, CANCEL_UNLOCK, DONE, UNLOCK_FAIL);
  }

  /**
   * 尝试获取一次
   * @return
   */
  @Override
  default boolean tryLock() {
    try {
      return tryLock(-1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      return false;
    }
  }

  @Override
  default void close() throws Exception {
    // 是否结束，结束的话，直接忽略
    if (isFinished()) {
      return;
    }
    // 是否 需要 解锁
    if (needUnlock()) {
      unlock();
      return;
    }
    // 如果未持有锁，则抛出异常
    throw new InterruptedException();
  }

  @Override
  default <T> T execute(IDoCallback<T> callback, IDoCallback<T> faultCallback,
      IDoCallback<T> rollback, int time, TimeUnit unit)
      throws Throwable {
    Objects.requireNonNull(unit);
    T result;
    try{
      // 获取锁
      if (time <= 0 ? tryLock() : tryLock(time, unit)) {
        // 成功，执行业务
        result = callback.callback();
      }else {
        // 失败的话，如果没有设置降级函数，则抛出 异常
        if (faultCallback == null) {
          throw new TimeoutSharedLockException(getKey());
        }
        // 降级
        result = faultCallback.callback();
      }
    }finally {
      unlock();
      // 检查是否需要回滚
      if (isStatus(UNLOCK_FAIL) && rollback != null) {
        result = rollback.callback();
      }
    }
    return result;
  }

  /**
   * 比较状态
   * @param status 比较的状态
   * @return 比较结果
   */
  @Override
  default boolean isStatus(SharedLockStatus status){
    return getStatus() == status;
  }

  /**
   * 当前状态是否在 此状态中
   * @param statuses 状态列表
   * @return 结果
   */
  @Override
  default boolean inStates(SharedLockStatus...statuses){
    if (statuses == null) {
      return false;
    }
    for (SharedLockStatus status : statuses){
      if (status == getStatus()){
        return true;
      }
    }
    return false;
  }
}
