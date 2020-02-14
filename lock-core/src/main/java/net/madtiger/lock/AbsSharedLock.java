package net.madtiger.lock;

import static net.madtiger.lock.SharedLockStatus.CANCEL;
import static net.madtiger.lock.SharedLockStatus.LOCKED;
import static net.madtiger.lock.SharedLockStatus.NEW;
import static net.madtiger.lock.SharedLockStatus.TIMEOUT;

import java.util.Objects;
import net.madtiger.lock.provider.IProviderConfigurer;
import net.madtiger.lock.provider.ISharedLockProvider;

/**
 * shared lock 对象
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public abstract class AbsSharedLock implements CompositeSharedLock {

  /**
   * 当前状态
   */
  protected SharedLockStatus status = NEW;

  /**
   * 获取 provider
   */
  protected ISharedLockProvider provider;

  /**
   * 服务提供者名称
   */
  protected String providerName;

  /**
   * 资源锁定时长, 单位秒
   */
  protected int lockSeconds;

  /**
   * 锁的资源
   */
  protected String key;

  /**
   * 服务提供商需要的数据
   */
  protected Object providerData;

  /**
   * 服务提供者配置信息
   */
  protected IProviderConfigurer providerConfigurer;



  /**
   * 构造函数
   * @param key 锁定的 key
   * @param providerName 服务提供者名称
   * @param lockSeconds 锁定时长，单位秒
   */
  public AbsSharedLock(String key, String providerName, int lockSeconds) {
    this.providerName = providerName;
    this.lockSeconds = lockSeconds;
    this.key = key;
    provider = SharedLockContextHolder.get(providerName, SharedLockContextHolder.getDefault());
    Objects.requireNonNull(key);
    Objects.requireNonNull(provider);
  }


  @Override
  public String getProviderName() {
    return providerName;
  }

  /**
   * 获取所的状态
   * @return
   */
  @Override
  public SharedLockStatus getStatus() {
    return status;
  }

  @Override
  public ISharedLockProvider getProvider() {
    return provider;
  }

  @Override
  public void setProvider(ISharedLockProvider provider) {
    this.provider = provider;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public <T extends ISharedLockProvider, K> K getProviderData() {
    return (K) providerData;
  }

  @Override
  public <K> void setProviderData(K data) {
      this.providerData = data;
  }

  @Override
  public <T extends IProviderConfigurer> T getProviderConfigurer() {
    return (T) providerConfigurer;
  }

  @Override
  public <K extends IProviderConfigurer> void setProviderConfigurer(K configurer) {
    this.providerConfigurer = configurer;
  }

  /**
   * 是否需要解锁
   * @return
   */
  @Override
  public boolean needUnlock(){
    return status == LOCKED || status == TIMEOUT || status == CANCEL;
  }


  @Override
  public int getLockSeconds() {
    return lockSeconds;
  }

  @Override
  public void setLockSeconds(int lockSeconds) {
    this.lockSeconds = lockSeconds;
  }

  @Override
  public void setStatus(SharedLockStatus status) {
    this.status = status;
  }

  /**
   * 取消当前锁，只对 NEW 状态可用
   * @return 是否取消成功，当
   */
  @Override
  public boolean interrupted(){
    if (!isFinished() && status != LOCKED){
      status = CANCEL;
      return true;
    }
    return false;
  }
}
