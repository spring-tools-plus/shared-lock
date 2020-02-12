package net.madtiger.shared.lock.decorator.reentrant;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import net.madtiger.shared.lock.IDoCallback;
import net.madtiger.shared.lock.ILockResultHolderWrapper;
import net.madtiger.shared.lock.ISharedLock;
import net.madtiger.shared.lock.LockResultHolder;
import net.madtiger.shared.lock.SetLockArgs;

/**
 * 来个 wrapper
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class ReentrantLockHolderWrapper extends LockResultHolder<Object> implements
    ILockResultHolderWrapper<Object> {

  /**
   * 委托
   */
  LockResultHolder<Object> delegate;

  /**
   * 当前是否结束
   */
  boolean currentDone = false;

  /**
   * 构造一个 实例
   * @param delegate
   */
  protected  ReentrantLockHolderWrapper(LockResultHolder<?> delegate) {
    this.delegate = (LockResultHolder<Object>) delegate;
  }

  /**
   * 构造一个 实例
   * @param delegate
   * @param returnData
   */
  protected  ReentrantLockHolderWrapper(LockResultHolder<?> delegate, Object returnData) {
    this.delegate = (LockResultHolder<Object>) delegate;
    this.returnData = returnData;
  }

  @Override
  public LockResultHolder<Object> getDelegate() {
    return delegate;
  }

  @Override
  public String getKey() {
    return delegate.getKey();
  }

  @Override
  public String getValue() {
    return delegate.getValue();
  }

  @Override
  public int getStatus() {
    return delegate.getStatus();
  }

  @Override
  public SetLockArgs getArgs() {
    return delegate.getArgs();
  }

  @Override
  public ISharedLock getSharedLock() {
    return delegate.getSharedLock();
  }

  @Override
  public Object getParam1() {
    return delegate.getParam1();
  }

  @Override
  public Object getParam2() {
    return delegate.getParam2();
  }

  @Override
  public IDoCallback<Object> getRollback() {
    return delegate.getRollback();
  }

  @Override
  public long getLockedTime() {
    return delegate.getLockedTime();
  }

  @Override
  public long getFinishedTime() {
    return delegate.getFinishedTime();
  }

  @Override
  public Object getReturnData() {
    return delegate.getReturnData();
  }

  @Override
  public boolean isDone() {
    return currentDone;
  }

  @Override
  public boolean mybeRelease() {
    return !currentDone;
  }

  @Override
  public boolean isTimeout() {
    return delegate.isTimeout();
  }

  @Override
  public boolean isRollback() {
    return delegate.isRollback();
  }

  @Override
  public boolean isLocking() {
    return !currentDone && delegate.isLocking();
  }

  @Override
  public boolean isFail() {
    return delegate.isFail();
  }

  @Override
  public boolean isAvaliable() {
    return !currentDone && delegate.isAvaliable();
  }

  @Override
  public Object returnData() {
    return returnData;
  }

  @Override
  public boolean unlock() {
    try {
      this.close();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public void close() throws IOException {
    if (!mybeRelease()){
      throwIOException();
    }
    log.debug("{} --> {} 锁释放锁成功",ReentrantLockDecorator.class.getName(), delegate.getKey());
    currentDone = true;
  }

  /**
   * 检查 锁持有对象是否是可重入锁，如果是包装器，则递归向上查询
   * @param holder 检查的 对象
   * @return 结果
   */
  public static boolean isReentrantWrapper(LockResultHolder<?> holder){
      // 逐层检查
      if (holder instanceof ReentrantLockHolderWrapper) {
        return true;
      }
      // 当前
      if (holder instanceof ILockResultHolderWrapper) {
        return isReentrantWrapper(((ILockResultHolderWrapper) holder).getDelegate());
      }
      return false;
  }
}
