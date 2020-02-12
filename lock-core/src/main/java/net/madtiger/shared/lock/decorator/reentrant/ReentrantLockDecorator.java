package net.madtiger.shared.lock.decorator.reentrant;

import java.util.Objects;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import net.madtiger.shared.lock.ISharedLock;
import net.madtiger.shared.lock.LockReleaseException;
import net.madtiger.shared.lock.LockResultHolder;
import net.madtiger.shared.lock.SetLockArgs;
import net.madtiger.shared.lock.SharedLockContextHolder;
import net.madtiger.shared.lock.decorator.AbsSharedLockDecorator;
import org.springframework.lang.Nullable;

/**
 * 支持可重入的分布式锁
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class ReentrantLockDecorator extends AbsSharedLockDecorator {

  /**
   * 初始化一个支持
   * @param sharedLock
   */
  public ReentrantLockDecorator(ISharedLock sharedLock){
    super(sharedLock);
    log.info("SharedLock 初始化 {} 装饰者成功", getClass().getName());
  }

  @Override
  public <T> LockResultHolder<T> execute(final String key, final Supplier<T> callback, @Nullable final SetLockArgs args) {
    LockResultHolder<T> basicHolder = getByThread(key);
    // 如果不存在或者已经失效，则新建
    if (basicHolder == null){
      // 重新获取
      return delegate.execute(key, callback, args);
    }else {
      // 执行回调
      return (LockResultHolder<T>) new ReentrantLockHolderWrapper(basicHolder, (Object) callback.get());
    }
  }

  @Override
  public <T> LockResultHolder<T> tryLock(String key, @Nullable SetLockArgs args) {
    LockResultHolder<T> basicHolder = getByThread(key);
    // 如果不存在，则新建
    if (basicHolder == null){
      return delegate.tryLock(key, args);
    }
    return (LockResultHolder<T>) new ReentrantLockHolderWrapper(basicHolder);
  }

  /**
   * 从当前线程中获取
   * @param key
   * @param <T>
   * @return
   */
  protected <T> LockResultHolder<T> getByThread(final String key){
    LockResultHolder<T> basicHolder = SharedLockContextHolder.get(key);
    // 如果不存在或者已经失效，则新建
    if (basicHolder == null || !basicHolder.isAvaliable()){
      if (basicHolder != null){
        log.error(String.format("%s锁已超时失效"), LOG_TAG, key);
      }
      return null;
    }
    return basicHolder;
  }

  @Override
  public boolean unlock(final LockResultHolder resultHolder) throws LockReleaseException {
    Objects.requireNonNull(resultHolder);
    // 解锁吧，直接设置自己的值
    return resultHolder.unlock();
  }

  private static final String LOG_TAG = "可重入锁 --> ";

}
