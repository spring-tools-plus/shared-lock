package net.madtiger.lock.zk;

import java.util.concurrent.TimeUnit;
import net.madtiger.lock.capable.IProviderWritable;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.util.StringUtils;

/**
 * CuratorFramework 实现
 * @author Fenghu.Shi
 * @version 1.0
 */
public class CuratorLockClient{

  /**
   * 客户端
   */
  private CuratorFramework lockClient;

  /**
   * 初始化一个 zk 实例
   * @param lockClient zk 客户端
   */
  public CuratorLockClient(CuratorFramework lockClient){
    this.lockClient = lockClient;
  }


  /**
   * 尝试获取锁并
   * @param writable  结果持有者
   * @param key 锁的key
   * @param time 时间
   * @param unit 单位
   * @return 获取结果
   */
  public boolean tryAcquire(IProviderWritable writable, String key, long time, TimeUnit unit) throws Exception {
    // 获取 configurer
    ZookeeperConfigurer configurer = writable.getProviderConfigurer() == null ? ZookeeperConfigurer.builder().build() : writable.getProviderConfigurer();
    InterProcessMutex lock = new InterProcessMutex(lockClient, nodePath(configurer.getNamespace(), key));
    // 将 lock holder 放到结果集中
    writable.setProviderData(lock);
    return lock.acquire(time, unit);
  }

  /**
   * 释放资源
   * @param writable 配置项
   * @return 释放结果
   */
  public boolean release(IProviderWritable writable) {
    InterProcessMutex lock = (InterProcessMutex) writable.getProviderData();
    if (lock == null || !(lock instanceof  InterProcessMutex)) {
      throw new IllegalArgumentException("当前的 holder 数据异常，请传入 tryLock 返回的 SharedLock ");
    }
    try{
      lock.release();
      return true;
    }catch (Throwable ex) {
      return false;
    }
  }


  /**
   * 生成 parent path
   * @param namespace 命名空间
   * @param key 锁定的key
   * @return
   */
  private String nodePath(String namespace, String key){
    return (StringUtils.isEmpty(namespace) ? DEFAULT_NAMESPACE : namespace) + (key.startsWith("/") ? key : "/" + key);
  }

  /**
   * 默认的 命名空间
   */
  private static final String DEFAULT_NAMESPACE = "/__SHARED_LOCK_NODE";
}
