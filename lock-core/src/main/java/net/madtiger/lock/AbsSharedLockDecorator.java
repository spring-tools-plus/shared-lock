package net.madtiger.lock;

import net.madtiger.lock.provider.IProviderConfigurer;
import net.madtiger.lock.provider.ISharedLockProvider;
import org.slf4j.Logger;
import org.springframework.lang.NonNull;

/**
 * 共享锁 包装器
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public abstract class AbsSharedLockDecorator implements CompositeSharedLock {

  /**
   * 处理实例
   */
  protected CompositeSharedLock delegate;


  /**
   * 设置 状态
   * @param status
   */
  @Override
  public void setStatus (SharedLockStatus status) {
    // 如果是 装饰者，则交由子类委托
    if (delegate instanceof AbsSharedLockDecorator) {
      ((AbsSharedLockDecorator) delegate).setStatus(status);
    }
    // 如果 是 abs shared lock 则设置
    else if (this.delegate instanceof AbsSharedLock) {
      ((AbsSharedLock) delegate).status = status;
    }
    // 如果啥也不是则抛出异常
    else {
      throw new UnsupportedOperationException("当前类型不支持 setStatus 方法");
    }
  }

  /**
   * 构造一个 包装器实例
   * @param delegate 实际执行者
   */
  public AbsSharedLockDecorator(CompositeSharedLock delegate){
    this.delegate = delegate;
  }

  @Override
  public SharedLockStatus getStatus() {
    return delegate.getStatus();
  }

  @Override
  public ISharedLockProvider getProvider() {
    return delegate.getProvider();
  }

  @Override
  public String getKey() {
    return delegate.getKey();
  }

  @Override
  public int getLockSeconds() {
    return delegate.getLockSeconds();
  }

  @Override
  public boolean needUnlock() {
    return delegate.needUnlock();
  }

  @Override
  public boolean isFinished() {
    return delegate.isFinished();
  }

  @Override
  public boolean interrupted() {
    return delegate.interrupted();
  }

  @Override
  public void setProvider(ISharedLockProvider provider) {
    delegate.setProvider(provider);
  }

  @Override
  @NonNull
  public <T extends ISharedLockProvider, K> K getProviderData() {
    return delegate.getProviderData();
  }

  @Override
  public <K> void setProviderData(K data) {
    delegate.setProviderData(data);
  }

  @Override
  @NonNull
  public <T extends IProviderConfigurer> T getProviderConfigurer() {
    return delegate.getProviderConfigurer();
  }

  @Override
  @NonNull
  public <K extends IProviderConfigurer> void setProviderConfigurer(K configurer) {
    delegate.setProviderConfigurer(configurer);
  }

  /**
   * 获取特定 class 类型的装饰者实例
   * @param clazz 类名
   * @param <T> 类型
   * @return 结果
   */
  public final <T extends AbsSharedLockDecorator> T getDecoratorByClass(Class<T> clazz){
    // 当前对象是否是 此装饰者实例
    if (clazz.isAssignableFrom(this.getClass())){
      return (T) this;
    }
    // 检查上一级是否是
    if (clazz.isAssignableFrom(this.delegate.getClass())) {
      return (T) this.delegate;
    }
    // 上一级是否是非装饰者
    if (!(this.delegate instanceof AbsSharedLockDecorator)) {
      return null;
    }
    return ((AbsSharedLockDecorator) this.delegate).getDecoratorByClass(clazz);
  }


  /**
   * 获取 logger 对象
   * @return 对象
   */
  protected abstract Logger getLogger();


  /**
   * 来一个 debug 消息
   * @param message 消息
   */
  protected void debugMessage(String message){
    getLogger().debug(String.format("%s:SharedLockDecorator --> %s 锁 %s", getClass().getSimpleName(), getKey(), message));
  }

  /**
   * 来一个 info 消息
   * @param message 消息
   */
  protected void infoMessage(String message){
    getLogger().info(String.format("%s:SharedLockDecorator --> %s 锁 %s", getClass().getSimpleName(), getKey(), message));
  }

  /**
   * 来一个 error 消息
   * @param message 消息
   */
  protected void errorMessage(String message){
    getLogger().error(String.format("%s:SharedLockDecorator --> %s 锁 %s", getClass().getSimpleName(), getKey(), message));
  }

  @Override
  public void setLockSeconds(int lockSeconds) {
    delegate.setLockSeconds(lockSeconds);
  }

  @Override
  public void unlocked(boolean release) {
    delegate.unlocked(release);
  }

  @Override
  public String getProviderName() {
    return delegate.getProviderName();
  }

  @Override
  public void unlock() {
    delegate.unlock();
    debugMessage("释放锁成功");
  }
  /**
   * 默认的序号
   */
  public static final int ORDER_DEFAULT = 1000;
}
