package net.madtiger.lock;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.madtiger.lock.decorator.ReentrantLockDecorator;
import net.madtiger.lock.provider.IProviderConfigurer;
import net.madtiger.lock.provider.ISharedLockProvider;

/**
 * 共享锁环境变量
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public class SharedLockEnvironment {


  /**
   * 来个单例对象
   */
  private static final SharedLockEnvironment INSTANCE = new SharedLockEnvironment();

  /**
   * 默认 服务提供者名称
   */
  private String defaultProviderName;

  /**
   * 锁定时间
   */
  private int lockSeconds = SharedLockEnvironment.DEFAULT_LOCK_SECONDS;

  /**
   * 全局 装饰者 服务
   */
  private Set<Class<AbsSharedLockDecorator>> decoratorClasses = new HashSet<>(8);

  /**
   * 服务提供者配置
   */
  private Map<Class<ISharedLockProvider>, IProviderConfigurer> configurerMap = new HashMap<>(16);

  private  SharedLockEnvironment(){
    // 可重入
    addDecoratorClasses(ReentrantLockDecorator.class);
  }


  /**
   * 获取 实例
   * @return 环境实例
   */
  public static SharedLockEnvironment getInstance(){
    return INSTANCE;
  }

  /**
   * 设置 默认的提供者
   * @param defaultProviderName 要设置的 提供者名称
   * @return self
   */
  public SharedLockEnvironment defaultProviderName(String defaultProviderName){
    this.defaultProviderName = defaultProviderName;
    return this;
  }

  /**
   * 获取当前的服务者名称
   * @return 名称
   */
  public String getDefaultProviderName(){
    return defaultProviderName;
  }

  /**
   * 设置 锁定时长
   * @param lockSeconds 时长
   * @return self
   */
  public SharedLockEnvironment lockSeconds(int lockSeconds){
    this.lockSeconds = lockSeconds;
    return this;
  }

  /**
   * 获取当前设置的锁定时长
   * @return
   */
  public int lockSeconds(){
    return lockSeconds;
  }

  /**
   * 清空原始的 装饰者
   * @return self
   */
  public SharedLockEnvironment clearDecoratorClasses(){
    decoratorClasses.clear();
    return this;
  }

  /**
   * 获取当前的
   * @return 当前拥有的装饰者类
   */
  public Set<Class<AbsSharedLockDecorator>> getDecoratorClasses(){
    return Collections.unmodifiableSet(decoratorClasses);
  }

  /**
   * 设置 特定的 服务提供者配置
   * @param providerClass 服务提供者类
   * @param configurer 配置
   * @param <T> 提供者类型
   * @param <K> 配置类型
   * @return  self
   */
  public <T extends ISharedLockProvider, K extends IProviderConfigurer> SharedLockEnvironment setConfigurer(Class<T> providerClass, K configurer){
    Objects.requireNonNull(providerClass);
    Objects.requireNonNull(configurer);
    configurerMap.put((Class<ISharedLockProvider>) providerClass, configurer);
    return this;
  }

  /**
   * 获取 特定提供者的配置实例
   * @param providerClass 服务提供类
   * @param <T> 提供者类
   * @param <K> 配置类
   * @return 配置实例
   */
  public <T extends ISharedLockProvider, K extends IProviderConfigurer> K getConfigurer(Class<T> providerClass){
    Objects.requireNonNull(providerClass);
    return (K) configurerMap.get(providerClass);
  }

  /**
   * 添加 装饰者服务
   * @param classes 需要添加的装饰者
   * @return self
   */
  public SharedLockEnvironment addDecoratorClasses(Class<? extends AbsSharedLockDecorator>... classes) {
    if (classes == null){
      return this;
    }
    // 逐个加进去
    for (Class<? extends AbsSharedLockDecorator> clazz : classes) {
      decoratorClasses.add((Class<AbsSharedLockDecorator>) clazz);
    }
    return this;
  }

  /**
   * 设置默认提供者
   * @param provder 默认提供者
   * @param <T> 提供者类
   * @return self
   */
  public <T extends ISharedLockProvider> SharedLockEnvironment setDefaultProvder(T provder){
    SharedLockContextHolder.setDefault(provder);
    return this;
  }

  /**
   * 添加 列表装饰者服务
   * @param classes 装饰者服务
   * @return self
   */
  public SharedLockEnvironment addDecoratorClasses(List<Class<? extends AbsSharedLockDecorator>> classes) {
    Objects.requireNonNull(classes);
    // 逐个加进去
    for (Class<? extends AbsSharedLockDecorator> clazz : classes) {
      decoratorClasses.add((Class<AbsSharedLockDecorator>) clazz);
    }
    return this;
  }

  /**
   * 默认 锁定时长
   */
  public static final int DEFAULT_LOCK_SECONDS = 20;


}
