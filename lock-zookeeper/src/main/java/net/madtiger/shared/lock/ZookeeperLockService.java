package net.madtiger.shared.lock;

import lombok.extern.slf4j.Slf4j;

/**
 * zookeeper 锁 服务类
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class ZookeeperLockService extends AbsSharedLock{

  private CuratorLockClient lockClient;

  public ZookeeperLockService(CuratorLockClient lockClient) {
    this.lockClient = lockClient;
  }

  @Override
  protected boolean tryAcquire(LockResultHolder resultHolder) {
    try {
      return lockClient.tryAcquire(resultHolder);
    } catch (Exception e) {
      log.error("zk --> 获取 {} 锁失败: {}", resultHolder.getKey(), e);
      return false;
    }
  }

  @Override
  protected boolean release(LockResultHolder resultHolder) {
    try {
      return lockClient.release(resultHolder);
    } catch (Exception e) {
      log.error("zk --> 释放 {} 锁失败: {}", resultHolder.getKey(), e);
      return false;
    }
  }
}
