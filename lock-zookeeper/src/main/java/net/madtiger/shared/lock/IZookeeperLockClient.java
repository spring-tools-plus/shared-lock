package net.madtiger.shared.lock;

/**
 * zk 客户端接口
 */
public interface IZookeeperLockClient {

  /**
   * 尝试获取锁并
   * @param resultHolder  结果持有者
   * @return 获取结果
   */
  boolean tryAcquire(LockResultHolder resultHolder)  throws Exception ;

  /**
   * 释放资源
   * @param resultHolder
   * @return
   * @throws Exception
   */
  boolean release(LockResultHolder resultHolder) throws Exception;

}
