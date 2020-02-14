package net.madtiger.lock;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.madtiger.lock.provider.ISharedLockProvider;
import org.springframework.util.StringUtils;

/**
 * 共享锁 上下文持有者
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public class SharedLockContextHolder {


  /**
   * 当前支持的 锁 服务
   */
  private static final CopyOnWriteArrayList<SharedLockProviderObject> LOCK_PROVIDERS = new CopyOnWriteArrayList<SharedLockProviderObject>();

  /**
   * 默认的 服务提供者
   */
  private static ISharedLockProvider DEFAULT_PROVIDER = null;



  /**
   * 注册一个锁服务
   * @param name 服务名称
   * @param lockProvider 锁实现
   */
  public static synchronized void register(String name, ISharedLockProvider lockProvider){
    Objects.requireNonNull(name);
    Objects.requireNonNull(lockProvider);
    ISharedLockProvider prevLockProvider = get(name, null);
    // 如果前一个服务存在， 则抛出异常
    if(prevLockProvider != null){
      throw new IllegalArgumentException(String.format("Shared Lock name %s 已经存在, {}", name, prevLockProvider.getClass().getName()));
    }
    // 设置
    LOCK_PROVIDERS.add(new SharedLockProviderObject(name, lockProvider));
  }

  /**
   * 设置 一个默认的服务提供者
   * @param lockProvider
   */
  public static synchronized void setDefault(ISharedLockProvider lockProvider){
    LOCK_PROVIDERS.add(new SharedLockProviderObject(DEFAULT_PROVIDER_NAME, lockProvider));
    DEFAULT_PROVIDER = lockProvider;
  }

  /**
   * 获取默认服务提供者
   * @return
   */
  public static ISharedLockProvider getDefault(){
    return DEFAULT_PROVIDER;
  }

  /**
   * 根据名称获取锁服务对象
   * @param name 名称
   * @param defaultProvider 默认的服务提供者
   * @return 服务对象
   */
  static ISharedLockProvider get(String name, ISharedLockProvider defaultProvider){
    if (StringUtils.isEmpty(name)) {
      return defaultProvider;
    }
    for (SharedLockProviderObject obj : LOCK_PROVIDERS) {
      if (obj.name.equals(name)) {
        return obj.sharedLockProvider;
      }
    }
    return defaultProvider;
  }


  static ISharedLockProvider get(String name){
    return get(name, DEFAULT_PROVIDER);
  }


  /**
   * 默认的服务提供者名称
   */
  private static final String DEFAULT_PROVIDER_NAME = "____default_provider";

  /**
   * 共享锁和名称组合类
   */
  static class SharedLockProviderObject {

    /**
     * 名称
     */
    String name;

    /**
     * 共享锁
     */
    ISharedLockProvider sharedLockProvider;

    /**
     * 初始化一个 共享锁服务对象
     * @param name 共享锁服务名称
     * @param sharedLockProvider 具体服务实例
     */
    SharedLockProviderObject(String name, ISharedLockProvider sharedLockProvider){
      this.name = name;
      this.sharedLockProvider = sharedLockProvider;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SharedLockProviderObject that = (SharedLockProviderObject) o;
      return name.equals(that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }

}
