package net.madtiger.lock;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.madtiger.lock.decorator.ReentrantLockDecorator;
import net.madtiger.lock.provider.IProviderConfigurer;

/**
 * 共享锁构造器
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public class SharedLockBuilder {

  /**
   * 服务提供者名称
   */
  private String providerName;

  /**
   * 资源锁定时长, 单位秒
   */
  private int lockSeconds;

  /**
   * 锁的资源
   */
  private String key;

  /**
   * 装饰者
   */
  private Set<Class<? extends AbsSharedLockDecorator>> decorators = new HashSet<>(8);

  /**
   * 服务提供商配置
   */
  private IProviderConfigurer providerConfigurer;

  /**
   * 设置 服务提供者名称，默认是默认资源
   * @param providerName 服务提供者名称
   * @return chain 对象
   */
  public SharedLockBuilder providerName(String providerName){
    Objects.requireNonNull(providerName);
    this.providerName = providerName;
    return this;
  }


  /**
   * 设置 共享锁 key
   * @param key key
   * @return chain 对象
   */
  public SharedLockBuilder key(String key){
    Objects.requireNonNull(key);
    this.key = key;
    return this;
  }

  /**
   * 设置 服务提供者配置
   * @param providerConfigurer 配置，可以通过 各自的 builder
   * @return chain
   */
  public SharedLockBuilder providerConfigurer(IProviderConfigurer providerConfigurer){
    this.providerConfigurer = providerConfigurer;
    return this;
  }


  /**
   * 设置资源锁定时间，单位秒
   * @param lockSeconds
   * @return
   */
  public SharedLockBuilder lockSeconds(int lockSeconds){
    this.lockSeconds = lockSeconds;
    return this;
  }

  /**
   * 添加多个装饰者
   * @param classes 装饰者类
   * @return
   */
  public SharedLockBuilder addDecorators(Class<? extends AbsSharedLockDecorator>... classes){
    if (classes != null){
      for (Class<? extends AbsSharedLockDecorator> clazz : classes) {
        decorators.add(clazz);
      }
    }
    return this;
  }

  /**
   * 开始生成
   * @return
   */
  public ISharedLock build(){
    DefaultSharedLock lock = new DefaultSharedLock(key, providerName, lockSeconds);
    lock.setProviderConfigurer(providerConfigurer);
    // 来一个 装饰者
    return SharedlockUtils.mergeEnv(lock, decorators);
  }


  /**
   * 生成 一个 builder
   * @return builder 对象
   */
  public static SharedLockBuilder builder(){
    return builder(null);
  }

  /**
   * 生成
   * @param key key
   * @return 生成一个 builder
   */
  public static SharedLockBuilder builder(String key){
    return new SharedLockBuilder().key(key);
  }
}
