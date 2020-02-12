package net.madtiger.shared.lock;

import java.io.IOException;
import java.util.Objects;

/**
 * lock holder 包装器
 * @author Fenghu.Shi
 * @version 1.0
 */
public class LockResultHolderWrapper<T> extends LockResultHolder<T> implements ILockResultHolderWrapper<T> {

  private LockResultHolder<T> delegate;

  LockResultHolderWrapper(SetLockArgs args){
    this.args = args;
  }

  @Override
  public LockResultHolder<T> getDelegate() {
    return delegate;
  }

  /**
   * 设置委托源
   * @param delegate
   */
  public void setDelegate(LockResultHolder<T> delegate) {
    Objects.requireNonNull(delegate);
    if (args != null){
      delegate.args = args;
    }
    this.delegate = delegate;
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
  public IDoCallback<T> getRollback() {
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
  public T getReturnData() {
    return delegate.getReturnData();
  }

  @Override
  public void rollback() {
    delegate.rollback();
  }

  @Override
  public boolean isDone() {
    return delegate.isDone();
  }

  @Override
  public boolean mybeRelease() {
    return delegate.mybeRelease();
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
    return delegate.isLocking();
  }

  @Override
  public boolean isFail() {
    return delegate.isFail();
  }

  @Override
  public LockResultHolder<T> status(int status) {
    return delegate.status(status);
  }

  @Override
  public boolean isAvaliable() {
    return delegate.isAvaliable();
  }

  @Override
  public T returnData() {
    return delegate.returnData();
  }

  @Override
  public boolean unlock() {
    return delegate.unlock();
  }

  @Override
  public void copyTo(LockResultHolder<T> target) {
    delegate.copyTo(target);
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }
}
