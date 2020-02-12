package net.madtiger.shared.lock.decorator;

import java.util.Objects;
import net.madtiger.shared.lock.ISharedLock;

/**
 * SharedLock 装饰着 接口
 *
 * @author Fenghu.Shi
 * @version 1.0
 */
public abstract class AbsSharedLockDecorator implements ISharedLock {

  /**
   * 委托对象
   */
  protected ISharedLock delegate;

  public AbsSharedLockDecorator(ISharedLock delegate){
    Objects.requireNonNull(delegate);
    this.delegate = delegate;
  }

  /**
   * 获取包装器上一级
   * @return
   */
  public ISharedLock getDelegate() {
    return delegate;
  }

  /**
   * 获取委托顶级对象
   * @return
   */
  public ISharedLock getRootDelegate(){
    return delegate instanceof AbsSharedLockDecorator ? getDelegate() : delegate;
  }
}
