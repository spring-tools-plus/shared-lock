package net.madtiger.shared.lock;

import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

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
   * zk 的 根目录节点，获取锁时，lock key 对应的路径 = root path + key
   */
  private String namespace;


  /**
   * 初始化一个 zk 实例，使用默认空间
   * @param lockClient zk 的客户端
   */
  public CuratorLockClient(CuratorFramework lockClient){
    this(DEFAULT_NAMESPACE, lockClient);
  }

  /**
   * 初始化一个 zk 实例
   * @param namespace 命名空间， 分享锁的公共节点
   * @param lockClient zk 客户端
   */
  public CuratorLockClient(String namespace, CuratorFramework lockClient){
    this.namespace = namespace;
    this.lockClient = lockClient;
  }

  /**
   * 尝试获取锁并
   * @param resultHolder  结果持有者
   * @return 获取结果
   */
  public boolean tryAcquire(LockResultHolder resultHolder) throws Exception {
    InterProcessMutex lock = new InterProcessMutex(lockClient, nodePath(resultHolder.key));
    // 将 lock holder 放到结果集中
    resultHolder.param1 = lock;
    return lock.acquire(resultHolder.args.waitTimeoutMills, TimeUnit.MILLISECONDS);
  }

  /**
   * 释放资源
   * @param resultHolder
   * @return
   * @throws Exception
   */
  public boolean release(LockResultHolder resultHolder) throws Exception {
    if (resultHolder.param1 == null || !(resultHolder.param1 instanceof  InterProcessMutex)) {
      throw new IllegalArgumentException("当前的 holder 数据异常，请传入 tryLock 返回的 LockResultHolder ");
    }
    InterProcessMutex lock = (InterProcessMutex) resultHolder.param1;
    try{
      lock.release();
      return true;
    }catch (Throwable ex) {
      return false;
    }
  }


  /**
   * 生成 parent path
   * @param key
   * @return
   */
  private String nodePath(String key){
    return namespace + (key.startsWith("/") ? key : "/" + key);
  }

  /**
   * 默认的 命名空间
   */
  private static final String DEFAULT_NAMESPACE = "/__SHARED_LOCK_NODE";
}
