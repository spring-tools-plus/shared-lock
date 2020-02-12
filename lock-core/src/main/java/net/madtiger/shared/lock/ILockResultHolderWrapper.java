package net.madtiger.shared.lock;

/**
 * 包装器对象
 * @param <T> 返回值类型
 * @author Fenghu.Shi
 * @version 1.0
 */
public interface ILockResultHolderWrapper<T> {

  /**
   * 获取实际被委托对象
   * @return
   */
  LockResultHolder<T> getDelegate();

}
